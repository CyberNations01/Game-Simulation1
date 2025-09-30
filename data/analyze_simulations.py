
from pathlib import Path
import json
import pandas as pd
import matplotlib.pyplot as plt
from typing import List, Dict, Any, Tuple

def load_simulations(input_dir: str) -> Tuple[pd.DataFrame, Dict[int, str]]:
    """
    Scan a directory for *.json simulation files and build a tidy per-round dataframe.
    Returns (df, legend_map). df has columns:
      file, round, bag_total, max_rounds, seed, pos_1..pos_K
    If there are no files, returns (empty_df, {}).
    """
    folder = Path(input_dir)
    files = sorted(folder.rglob("*.json"))
    records = []
    legend_map = {}  # int -> name, e.g., 1->WILDS

    for fp in files:
        try:
            with open(fp, "r", encoding="utf-8") as f:
                data = json.load(f)
        except Exception as e:
            # Skip bad file
            print(f"[WARN] Could not read {fp}: {e}")
            continue

        # Try to read legend (optional but helpful for labels)
        try:
            legend = data.get("legend", {})
            for k, v in legend.items():
                # legend is name->int; invert to int->name
                legend_map[int(v)] = str(k)
        except Exception:
            pass

        game_state = data.get("game_state", {})
        bag_total = game_state.get("bag_total")
        max_rounds = game_state.get("max_rounds")
        seed = game_state.get("seed")

        timeline = data.get("timeline", [])
        for entry in timeline:
            round_idx = entry.get("round")
            states = entry.get("states")
            if not isinstance(states, list):
                continue
            rec = {
                "file": fp.name,
                "round": round_idx,
                "bag_total": bag_total,
                "max_rounds": max_rounds,
                "seed": seed,
                "states": states
            }
            records.append(rec)

    if not records:
        return pd.DataFrame(), legend_map

    df = pd.DataFrame(records)

    # Expand states to pos_1..pos_K
    max_len = df["states"].map(lambda x: len(x) if isinstance(x, list) else 0).max()
    pos_cols = [f"pos_{i+1}" for i in range(max_len)]
    df[pos_cols] = df["states"].apply(lambda arr: pd.Series(arr) if isinstance(arr, list) else pd.Series([None]*max_len))

    # Ensure integer if possible
    for c in pos_cols:
        df[c] = pd.to_numeric(df[c], errors="coerce").astype("Int64")

    df.drop(columns=["states"], inplace=True)
    return df, legend_map


def save_csv(df: pd.DataFrame, out_path: str) -> None:
    out_fp = Path(out_path)
    out_fp.parent.mkdir(parents=True, exist_ok=True)
    df.to_csv(out_fp, index=False)
    print(f"[OK] Saved CSV -> {out_fp}")


def plot_token_frequency_overall(df: pd.DataFrame, legend_map: Dict[int, str], out_path: str) -> None:
    """
    Count frequency of all token IDs across all positions and rounds.
    Saves a simple bar chart.
    """
    pos_cols = [c for c in df.columns if c.startswith("pos_")]
    if not pos_cols:
        print("[INFO] No position columns found; skip plot_token_frequency_overall.")
        return

    long_vals = pd.concat([df[c] for c in pos_cols], axis=0).dropna().astype(int)
    counts = long_vals.value_counts().sort_index()

    # Map integer IDs to legend names if we have them
    labels = [legend_map.get(int(i), str(int(i))) for i in counts.index]

    plt.figure()
    plt.bar(labels, counts.values)
    plt.title("Overall token frequency (all positions, all rounds)")
    plt.xlabel("Token")
    plt.ylabel("Count")
    plt.xticks(rotation=45, ha="right")
    plt.tight_layout()
    out_fp = Path(out_path)
    out_fp.parent.mkdir(parents=True, exist_ok=True)
    plt.savefig(out_fp)
    plt.close()
    print(f"[OK] Saved plot -> {out_fp}")


def plot_avg_bag_total_over_rounds(df: pd.DataFrame, out_path: str) -> None:
    """
    Average bag_total by round (if bag_total varies over time in your data).
    If bag_total is constant, the line will be flat.
    """
    if "bag_total" not in df.columns or "round" not in df.columns:
        print("[INFO] Missing bag_total or round; skip plot_avg_bag_total_over_rounds.")
        return

    # If bag_total is recorded per round-row, we can groupby round:
    grp = df.groupby("round", dropna=True)["bag_total"].mean(numeric_only=True)
    if grp.empty:
        print("[INFO] bag_total by round is empty; skip plot.")
        return

    plt.figure()
    plt.plot(grp.index.values, grp.values, marker="o")
    plt.title("Average bag_total over rounds")
    plt.xlabel("Round")
    plt.ylabel("Average bag_total")
    plt.tight_layout()
    out_fp = Path(out_path)
    out_fp.parent.mkdir(parents=True, exist_ok=True)
    plt.savefig(out_fp)
    plt.close()
    print(f"[OK] Saved plot -> {out_fp}")


def plot_final_state_distribution(df: pd.DataFrame, legend_map: Dict[int, str], out_path: str) -> None:
    """
    Look only at the final round of each file, count token distribution at pos_1 (as an example).
    Adjust pos_* to target different positions.
    """
    if "file" not in df.columns or "round" not in df.columns:
        print("[INFO] Missing file/round columns; skip final state plot.")
        return

    pos_col = "pos_1"
    if pos_col not in df.columns:
        print(f"[INFO] Missing {pos_col}; skip final state plot.")
        return

    # Final round per file:
    last_rounds = df.groupby("file", dropna=True)["round"].max()
    final_df = df.merge(last_rounds.rename("last_round"), on="file", how="inner")
    final_df = final_df[final_df["round"] == final_df["last_round"]]

    vals = final_df[pos_col].dropna().astype(int)
    if vals.empty:
        print("[INFO] No values in final state for plotting; skip.")
        return

    counts = vals.value_counts().sort_index()
    labels = [legend_map.get(int(i), str(int(i))) for i in counts.index]

    plt.figure()
    plt.bar(labels, counts.values)
    plt.title(f"Final-round distribution at {pos_col}")
    plt.xlabel("Token")
    plt.ylabel("Count (files)")
    plt.xticks(rotation=45, ha="right")
    plt.tight_layout()
    out_fp = Path(out_path)
    out_fp.parent.mkdir(parents=True, exist_ok=True)
    plt.savefig(out_fp)
    plt.close()
    print(f"[OK] Saved plot -> {out_fp}")


def main(input_dir: str, out_dir: str) -> None:
    out_dir_p = Path(out_dir)
    out_dir_p.mkdir(parents=True, exist_ok=True)

    df, legend_map = load_simulations(input_dir)
    if df.empty:
        print("[INFO] No data loaded. Check your input directory.")
        return

    # Save combined CSV
    save_csv(df, str(out_dir_p / "sim_all_rounds.csv"))

    # Basic plots
    plot_token_frequency_overall(df, legend_map, str(out_dir_p / "fig_token_frequency_overall.png"))
    plot_avg_bag_total_over_rounds(df, str(out_dir_p / "fig_avg_bag_total_over_rounds.png"))
    plot_final_state_distribution(df, legend_map, str(out_dir_p / "fig_final_pos1_distribution.png"))


if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description="Analyze game simulation JSON files.")
    parser.add_argument("--input_dir", type=str, required=True, help="Directory containing *.json files")
    parser.add_argument("--out_dir", type=str, default="sim_outputs", help="Directory to write CSV and plots")
    args = parser.parse_args()
    main(args.input_dir, args.out_dir)
