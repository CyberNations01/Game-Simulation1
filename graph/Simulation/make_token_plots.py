#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generate per-file token-count curves from simulation JSON logs (timeline-based),
with title showing only the initial composition text (e.g., "all devA", "1 wild 10 devA").
Outputs one PNG per JSON plus a ZIP bundle of all figures.

Usage:
  python make_token_plots.py --zip round2.zip --outdir ./out_plots

Notes:
  - Uses only matplotlib (no seaborn) and creates one chart per file.
  - X axis: Round; Y axis: token count; Legend: Wilds/Wastes/DevA/DevB.
  - Counts are derived from `timeline.states` using `legend` mapping, so
    counts per round across types sum to the board size (e.g., 11).
"""
import argparse
import json
import zipfile
from pathlib import Path
import numpy as np
import matplotlib.pyplot as plt

def parse_args():
    p = argparse.ArgumentParser(description="Plot token counts vs rounds for each simulation JSON.")
    p.add_argument("--zip", required=True, help="Path to ZIP containing JSON files (e.g., round2.zip)")
    p.add_argument("--outdir", required=True, help="Directory to write PNGs and the final ZIP")
    return p.parse_args()

def counts_from_timeline(json_path: Path):
    """Return (rounds, series, init_counts) from a JSON file.
    - rounds: np.array of round numbers
    - series: dict[str, list[int]] token counts per round from timeline.states
    - init_counts: dict[str, int] composition at the first round
    """
    with open(json_path, "r", encoding="utf-8") as f:
        obj = json.load(f)
    legend = obj.get("legend", {})
    tl = obj.get("timeline", [])
    rounds = [e.get("round") for e in tl]
    matrices = [e.get("states", []) for e in tl]  # list[list[int]]
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
    """Format initial composition as title-only text.
    Examples: 'all devA', '1 wild 10 devA', '5 wastes 6 devA'.
    """
    total = sum(init_counts.values())
    nonzero = [(k, v) for k, v in init_counts.items() if v > 0]
    order = ["WILDS","WASTES","DEVA","DEVB"]
    # all of a single type
    if len(nonzero) == 1 and nonzero[0][1] == total:
        name = nonzero[0][0]
        return {"WILDS":"all wilds","WASTES":"all wastes","DEVA":"all devA","DEVB":"all devB"}[name]
    # mixed composition
    parts = []
    for k in order:
        v = init_counts.get(k, 0)
        if v > 0:
            base = {"WILDS":"wild","WASTES":"waste","DEVA":"devA","DEVB":"devB"}[k]
            word = base if (k in ("DEVA","DEVB") or v==1) else base + "s"
            parts.append(f"{v} {word}")
    return " ".join(parts)

def main():
    args = parse_args()
    outdir = Path(args.outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    tmpdir = outdir / "_unzipped"
    tmpdir.mkdir(parents=True, exist_ok=True)

    # 1) Unzip input
    with zipfile.ZipFile(args.zip, "r") as zf:
        zf.extractall(tmpdir)

    json_files = sorted(tmpdir.rglob("*.json"))
    if not json_files:
        print("No JSON files found in the ZIP. Nothing to do.")
        return

    # 2) Generate plots
    label_map = {"WILDS":"Wilds","WASTES":"Wastes","DEVA":"DevA","DEVB":"DevB"}
    out_images = []
    for p in json_files:
        rounds, series, init_counts = counts_from_timeline(p)
        if rounds.size == 0:
            continue
        title = composition_to_text(init_counts)  # title-only as requested

        plt.figure(figsize=(7, 4.5))
        for key in ["WILDS","WASTES","DEVA","DEVB"]:
            if key in series:
                y = np.array(series[key])
                plt.plot(rounds, y, marker="o", label=label_map[key])
        plt.title(title)
        plt.xlabel("Round")
        plt.ylabel("token count")
        plt.legend()
        plt.tight_layout()

        out_png = outdir / f"board_token_curves_{p.stem}.png"
        plt.savefig(out_png, dpi=144)
        plt.close()
        out_images.append(out_png)

    # 3) Zip all figures
    zip_out = outdir / "board_token_curves_plots.zip"
    with zipfile.ZipFile(zip_out, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        for img in out_images:
            zf.write(img, arcname=img.name)

    print(f"Created {len(out_images)} figures")
    print(f"Figures directory: {outdir}")
    print(f"Bundle: {zip_out}")

if __name__ == "__main__":
    main()
