import pandas as pd
import matplotlib.pyplot as plt

absorb = pd.read_csv("C:\\Users\\Lenovo\\IdeaProjects\\Game-Simulation1\\data\\out_metrics\\summary_absorption.csv")
vc = absorb["absorption_round"].dropna().astype(int).value_counts().sort_index()

plt.figure()
plt.bar(vc.index.astype(str), vc.values)
plt.title("Absorption time (discrete counts)")
plt.xlabel("Round")
plt.ylabel("Number of runs")
plt.tight_layout()
plt.show()

# 一些描述统计
n_total = len(absorb)
n_absorbed = absorb["absorption_round"].notna().sum()
rate = n_absorbed / n_total if n_total else float("nan")
mean = absorb["absorption_round"].mean()
median = absorb["absorption_round"].median()

print(f"Absorption ratio: {rate:.1%}")
print(f"Average absorption turn:{mean:.2f}, Median{median}")