#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generate per-file token-count curves from simulation JSON logs (timeline-based),
with title showing only the initial composition text (e.g., "all devA", "1 wild 10 devA").
Explicit colors: Wilds=green, Wastes=orange, DevA=pink, DevB=dark blue.

Usage:
  python make_token_plots_colored.py --zip round2.zip --outdir ./out_plots
"""
import argparse
import json
import zipfile
from pathlib import Path
import numpy as np
import matplotlib.pyplot as plt

COLOR_MAP = {
    "WILDS": "green",       # Wilds -> green
    "WASTES": "orange",     # Wastes -> orange
    "DEVA": "#ff66b2",      # DevA -> pink
    "DEVB": "#003366",      # DevB -> dark blue
}

def parse_args():
    p = argparse.ArgumentParser(description="Plot token counts vs rounds for each simulation JSON (colored).")
    p.add_argument("--zip", required=True, help="Path to ZIP containing JSON files (e.g., round2.zip)")
    p.add_argument("--outdir", required=True, help="Directory to write PNGs and the final ZIP")
    return p.parse_args()

def counts_from_timeline(json_path: Path):
    with open(json_path, "r", encoding="utf-8") as f:
        obj = json.load(f)
    legend = obj.get("legend", {})
    tl = obj.get("timeline", [])
    rounds = [e.get("round") for e in tl]
    matrices = [e.get("states", []) for e in tl]
    series = {name: [] for name in legend.keys()}
    for row in matrices:
        for token_name, code in legend.items():
            series[token_name].append(sum(1 for x in row if x == code))
    init_counts = {}
    if matrices:
        first_row = matrices[0]
        for token_name, code in legend.items():
            init_counts[token_name] = sum(1 for x in first_row if x == code)
    return np.array(rounds), series, init_counts

def composition_to_text(init_counts: dict) -> str:
    total = sum(init_counts.values())
    nonzero = [(k, v) for k, v in init_counts.items() if v > 0]
    order = ["WILDS","WASTES","DEVA","DEVB"]
    if len(nonzero) == 1 and nonzero[0][1] == total:
        return {"WILDS":"all wilds","WASTES":"all wastes","DEVA":"all devA","DEVB":"all devB"}[nonzero[0][0]]
    parts = []
    for k in order:
        v = init_counts.get(k, 0)
        if v > 0:
            base = {"WILDS":"wild","WASTES":"waste","DEVA":"devA","DEVB":"devB"}[k]
            word = base if (k in ("DEVA","DEVB") or v==1) else base + "s"
            parts.append(f"{v} {word}")
    return " ".join(parts)

def main():
    import argparse
    args = parse_args()
    outdir = Path(args.outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    tmpdir = outdir / "_unzipped"
    tmpdir.mkdir(parents=True, exist_ok=True)

    # Unzip data
    with zipfile.ZipFile(args.zip, "r") as zf:
        zf.extractall(tmpdir)
    json_files = sorted(tmpdir.rglob("*.json"))
    if not json_files:
        print("No JSON files found.")
        return

    label_map = {"WILDS":"Wilds","WASTES":"Wastes","DEVA":"DevA","DEVB":"DevB"}
    out_images = []
    for p in json_files:
        rounds, series, init_counts = counts_from_timeline(p)
        if rounds.size == 0:
            continue
        title = composition_to_text(init_counts)

        plt.figure(figsize=(7,4.5))
        for key in ["WILDS","WASTES","DEVA","DEVB"]:
            if key in series:
                y = np.array(series[key])
                plt.plot(rounds, y, marker="o", label=label_map[key], color=COLOR_MAP[key])
        plt.title(title)
        plt.xlabel("Round")
        plt.ylabel("token count")
        plt.legend()
        plt.tight_layout()

        out_png = outdir / f"board_token_curves_{p.stem}.png"
        plt.savefig(out_png, dpi=144)
        plt.close()
        out_images.append(out_png)

    # zip
    zip_out = outdir / "board_token_curves_plots_colored.zip"
    with zipfile.ZipFile(zip_out, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        for img in out_images:
            zf.write(img, arcname=img.name)

    print(f"Created {len(out_images)} figures")
    print(f"Bundle: {zip_out}")

if __name__ == "__main__":
    main()
