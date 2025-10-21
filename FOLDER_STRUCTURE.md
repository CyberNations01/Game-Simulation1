# 项目文件夹结构说明

## 📁 文件夹组织

### `data-analysis/` - 数据分析文件夹
**用途**：存放用于分析模拟结果的Python脚本
- `analyze_simulations.py` - 模拟分析脚本
- `data_analyze.py` - 数据分析脚本  
- `exact_histogram.py` - 精确直方图分析
- `region_evolution_by_absorption.py` - 区域演化分析

### `game-data/` - 游戏数据文件夹
**用途**：存放游戏运行所需的数据文件
- `disruption.json` - 破坏卡数据文件（Stage 2使用）

### `assets/` - 模拟结果文件夹
**用途**：存放模拟运行生成的JSON结果文件
- 各种模拟结果文件（如 `8WILDS3DEVA_round7.json`）

### `src/src/` - Java源代码文件夹
**用途**：存放Java游戏模拟源代码
- `SimulationApp.java` - 主程序入口
- `Simulation.java` - 核心模拟逻辑
- `DisruptionCard.java` - 破坏卡系统
- `DisruptionCardManager.java` - 破坏卡管理器
- `Parameters.java` - 资源管理系统
- 其他支持类...

### `visualization/` - 可视化文件夹
**用途**：存放可视化相关的Java和Python文件

### `graph/` - 图表生成文件夹
**用途**：存放图表生成和分析脚本

## 🔄 文件夹变更历史

**之前**：
- `data/` 文件夹混合了数据分析脚本和游戏数据文件

**现在**：
- `data-analysis/` - 专门用于数据分析脚本
- `game-data/` - 专门用于游戏数据文件
- 更清晰的职责分离，便于维护和理解

## 🎯 使用说明

- **运行Stage 2模拟**：确保 `game-data/disruption.json` 文件存在
- **数据分析**：使用 `data-analysis/` 文件夹中的Python脚本
- **查看结果**：模拟结果保存在 `assets/` 文件夹中
