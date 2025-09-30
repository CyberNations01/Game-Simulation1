from pathlib import Path
import argparse
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from collections import defaultdict

# === 如你的legend不一致，请修改这里 ===
LEGEND_INT2NAME = {1: "WILDS", 2: "WASTES", 3: "DEVA", 4: "DEVB"}
WILDS_CODE = 1

def load_df(csv_path: Path):
    df = pd.read_csv(csv_path)
    if "file" not in df.columns or "round" not in df.columns:
        raise RuntimeError("CSV 缺少 'file' 或 'round' 列。请确认是合并脚本导出的表。")
    pos_cols = [c for c in df.columns if c.startswith("pos_")]
    if not pos_cols:
        raise RuntimeError("CSV 中没有 pos_* 列。")
    for c in pos_cols:
        df[c] = pd.to_numeric(df[c], errors="coerce")
    return df, pos_cols

def define_regions(pos_cols):
    """返回区域映射：region -> [pos_col,...]。尽量容错，按索引计算。"""
    # 提取位置号
    idxs = []
    for c in pos_cols:
        try:
            idxs.append(int(c.split("_")[1]))
        except Exception:
            idxs.append(None)
    # 位置号 -> 列名
    id2col = {i: c for i, c in zip(idxs, pos_cols) if i is not None}

    inner = [id2col[i] for i in [1] if i in id2col]
    middle = [id2col[i] for i in range(2, 8) if i in id2col]
    outer = [id2col[i] for i in range(8, 12) if i in id2col]

    regions = {"inner": inner, "middle": middle, "outer": outer}
    # 去掉空区域（以防有缺列的情况）
    regions = {k: v for k, v in regions.items() if v}
    return regions

def mark_absorption(df, pos_cols, wilds_code=WILDS_CODE):
    """给每个文件判定是否吸收：存在某回合全部pos都==wilds_code。返回 per-file Series。"""
    absorbed = {}
    for fname, g in df.groupby("file", dropna=True):
        g2 = g.sort_values("round")
        mask_all = (g2[pos_cols] == wilds_code).all(axis=1)
        absorbed[fname] = bool(mask_all.any())
    return pd.Series(absorbed, name="absorbed")

def region_counts_per_round(df_grp_round, region_cols):
    """针对单个 (file, round) 分组，统计该区域内四类token的数量。"""
    row = df_grp_round.iloc[0]  # 同一(file, round)只有一行
    vals = row[region_cols].dropna().astype(int).values
    vc = pd.Series(vals).value_counts()
    rec = {}
    for code, name in LEGEND_INT2NAME.items():
        rec[name] = int(vc.get(code, 0))
    return rec

def build_region_evolution(df, regions):
    """
    生成长表：columns = [file, round, region, WILDS, WASTES, DEVA, DEVB]
    """
    out_rows = []
    for (fname, rnd), g in df.groupby(["file", "round"], dropna=True):
        for region_name, cols in regions.items():
            rec = {"file": fname, "round": rnd, "region": region_name}
            rec.update(region_counts_per_round(g, cols))
            out_rows.append(rec)
    evo = pd.DataFrame(out_rows).sort_values(["file", "region", "round"])
    return evo

def avg_curves(evo_df, subset_mask=None):
    """
    对给定子集（可选）按 (region, round) 取均值，得到每回合每区域平均占格数曲线。
    返回 dict: region -> DataFrame(index=round, cols=[WILDS,WASTES,DEVA,DEVB])
    """
    df = evo_df if subset_mask is None else evo_df[subset_mask]
    results = {}
    for region, g in df.groupby("region", dropna=True):
        avg = g.groupby("round", dropna=True)[list(LEGEND_INT2NAME.values())].mean()
        results[region] = avg
    return results

def save_avg_curves_to_csv(avg_dict, out_dir: Path, tag: str):
    for region, avg in avg_dict.items():
        path = out_dir / f"avg_evolution_{tag}_{region}.csv"
        avg.to_csv(path)

def plot_avg_curves(avg_dict, out_dir: Path, tag: str):
    """
    绘制每个区域一张图（四条线），不设颜色/风格。
    """
    for region, avg in avg_dict.items():
        if avg.empty:
            continue
        plt.figure()
        for name in LEGEND_INT2NAME.values():
            if name in avg.columns:
                plt.plot(avg.index.values, avg[name].values, label=name)
        plt.title(f"Average evolution ({tag}) – {region}")
        plt.xlabel("Round"); plt.ylabel("Average count within region")
        plt.legend()
        plt.tight_layout()
        plt.savefig(out_dir / f"fig_avg_evolution_{tag}_{region}.png")
        plt.close()

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--csv", required=True, help="Path to sim_all_rounds.csv")
    parser.add_argument("--out", required=True, help="Directory to write CSV/figures")
    args = parser.parse_args()

    csv_path = Path(args.csv)
    out_dir = Path(args.out); out_dir.mkdir(parents=True, exist_ok=True)

    df, pos_cols = load_df(csv_path)
    regions = define_regions(pos_cols)
    if not regions:
        raise RuntimeError("未能识别任何区域，请检查 pos_* 列。")

    # 标注每个文件是否吸收
    absorbed_map = mark_absorption(df, pos_cols, wilds_code=WILDS_CODE)
    df = df.copy()
    df["absorbed"] = df["file"].map(absorbed_map)

    # 构造区域演化长表
    evo = build_region_evolution(df, regions)
    evo.to_csv(out_dir / "region_evolution_long.csv", index=False)

    # All
    avg_all = avg_curves(evo)
    save_avg_curves_to_csv(avg_all, out_dir, tag="all")
    plot_avg_curves(avg_all, out_dir, tag="all")

    # Absorbed
    mask_abs = evo["file"].map(absorbed_map).astype(bool)
    avg_abs = avg_curves(evo, subset_mask=mask_abs)
    save_avg_curves_to_csv(avg_abs, out_dir, tag="absorbed")
    plot_avg_curves(avg_abs, out_dir, tag="absorbed")

    # Unabsorbed
    mask_un = ~mask_abs
    avg_un = avg_curves(evo, subset_mask=mask_un)
    save_avg_curves_to_csv(avg_un, out_dir, tag="unabsorbed")
    plot_avg_curves(avg_un, out_dir, tag="unabsorbed")

    print("[OK] Written to:", out_dir)
    print("Generated files include:")
    print(" - region_evolution_long.csv")
    for tag in ["all","absorbed","unabsorbed"]:
        for region in regions.keys():
            print(f" - avg_evolution_{tag}_{region}.csv")
            print(f" - fig_avg_evolution_{tag}_{region}.png")

if __name__ == "__main__":
    main()