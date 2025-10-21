from pathlib import Path
import argparse
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

LEGEND_INT2NAME = {1: "WILDS", 2: "WASTES", 3: "DEVA", 4: "DEVB"}

def load_df(csv_path: Path):
    df = pd.read_csv(csv_path)
    if "file" not in df.columns or "round" not in df.columns:
        raise RuntimeError("wrong csv files!")
    pos_cols = [c for c in df.columns if c.startswith("pos_")]
    if not pos_cols:
        raise RuntimeError("cannot find position columns.")

    for c in pos_cols:
        df[c] = pd.to_numeric(df[c], errors="coerce")
    return df, pos_cols

def compute_absorption_time(df: pd.DataFrame, pos_cols, wilds_code=1) -> pd.DataFrame:
    """Absorption time: The round in which all positions for each file are WILDS for the first time; NaN if not occurred."""
    res = []
    for fname, g in df.groupby("file", dropna=True):
        g2 = g.sort_values("round")
        mask_all_wilds = (g2[pos_cols] == wilds_code).all(axis=1)
        idxs = mask_all_wilds[mask_all_wilds].index.tolist()
        first_round = int(g2.loc[idxs[0], "round"]) if idxs else np.nan
        res.append({"file": fname, "absorption_round": first_round})
    return pd.DataFrame(res)

def compute_final_state_counts(df: pd.DataFrame, pos_cols) -> pd.DataFrame:
    """Final State Distribution: Number of WILDS/WASTES/DEVA/DEVB tiles in the last turn of each file, covering 11 tiles."""
    final_idx = df.groupby("file", dropna=True)["round"].idxmax()
    final_rows = df.loc[final_idx.values]
    rows = []
    for _, row in final_rows.iterrows():
        vals = row[pos_cols].dropna().astype(int).values
        vc = pd.Series(vals).value_counts()
        rec = {"file": row["file"]}
        for code, name in LEGEND_INT2NAME.items():
            rec[f"count_{name}"] = int(vc.get(code, 0))
        rows.append(rec)
    return pd.DataFrame(rows).sort_values("file")

def build_evolution_curves(df: pd.DataFrame, pos_cols) -> pd.DataFrame:
    """Final State Distribution: Number of WILDS/WASTES/DEVA/DEVB tiles in the last turn of each file, covering 11 tiles."""
    out = []
    for (fname, rnd), g in df.groupby(["file", "round"], dropna=True):
        vals = g[pos_cols].iloc[0].dropna().astype(int).values
        vc = pd.Series(vals).value_counts()
        rec = {"file": fname, "round": rnd}
        for code, name in LEGEND_INT2NAME.items():
            rec[name] = int(vc.get(code, 0))
        out.append(rec)
    return pd.DataFrame(out).sort_values(["file", "round"])

def save_summaries(absorb_df, final_counts_df, evo_df, out_dir: Path):
    out_dir.mkdir(parents=True, exist_ok=True)
    absorb_df.to_csv(out_dir / "summary_absorption.csv", index=False)
    final_counts_df.to_csv(out_dir / "summary_final_counts.csv", index=False)
    evo_df.to_csv(out_dir / "evolution_per_round.csv", index=False)

def plot_figures(absorb_df, final_counts_df, evo_df, out_dir: Path):
    out_dir.mkdir(parents=True, exist_ok=True)

    # 1) Absorption Time Histogram (only counts games with absorption).
    nn = absorb_df["absorption_round"].dropna()
    if not nn.empty:
        plt.figure()
        plt.hist(nn.values, bins=20)
        plt.title("Absorption time (first all-WILDS round)")
        plt.xlabel("Round"); plt.ylabel("Frequency")
        plt.tight_layout()
        plt.savefig(out_dir / "fig_absorption_hist.png")
        plt.close()

    # 2) Average Evolution Curve (calculated by averaging the counts for each file within the same round)
    if not evo_df.empty:
        avg = evo_df.groupby("round", dropna=True)[list(LEGEND_INT2NAME.values())].mean()
        plt.figure()
        for name in LEGEND_INT2NAME.values():
            if name in avg.columns:
                plt.plot(avg.index.values, avg[name].values, label=name)
        plt.title("Average evolution curves (mean counts per round)")
        plt.xlabel("Round"); plt.ylabel("Average count across files")
        plt.legend()
        plt.tight_layout()
        plt.savefig(out_dir / "fig_evolution_avg.png")
        plt.close()

    # 3) Final State Total Bar Chart (summing the final state counts of all files)
    if not final_counts_df.empty:
        labels = list(LEGEND_INT2NAME.values())
        totals = [final_counts_df[f"count_{lbl}"].sum() if f"count_{lbl}" in final_counts_df.columns else 0
                  for lbl in labels]
        plt.figure()
        plt.bar(labels, totals)
        plt.title("Final-state composition (sum over files)")
        plt.xlabel("Token"); plt.ylabel("Total count across files")
        plt.tight_layout()
        plt.savefig(out_dir / "fig_final_state_totals.png")
        plt.close()

def main():
    parser = argparse.ArgumentParser(description="Compute metrics from sim_all_rounds.csv")
    parser.add_argument("--csv", required=True, help="Path to sim_all_rounds.csv")
    parser.add_argument("--out", required=True, help="Output directory for summaries and figures")
    args = parser.parse_args()

    csv_path = Path(args.csv)
    out_dir = Path(args.out)

    df, pos_cols = load_df(csv_path)
    absorb_df = compute_absorption_time(df, pos_cols, wilds_code=1)
    final_counts_df = compute_final_state_counts(df, pos_cols)
    evo_df = build_evolution_curves(df, pos_cols)

    save_summaries(absorb_df, final_counts_df, evo_df, out_dir)
    plot_figures(absorb_df, final_counts_df, evo_df, out_dir)

    print("[OK] Done.")
    print("Summaries:", out_dir / "summary_absorption.csv",
          out_dir / "summary_final_counts.csv",
          out_dir / "evolution_per_round.csv")
    print("Figures:", out_dir / "fig_absorption_hist.png",
          out_dir / "fig_evolution_avg.png",
          out_dir / "fig_final_state_totals.png")

if __name__ == "__main__":
    main()