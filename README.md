# Game Simulation Project

This is a game simulation project containing visualization components and future simulation functionality.

## Project Structure

```
Game-Simulation1/
├── visualization/          # Visualization components
│   ├── GameVisualizer.java    # Main program entry and GUI interface
│   ├── HexagonPanel.java      # Hexagon drawing panel
│   ├── HexagonDrawer.java     # Hexagon drawing utility class
│   ├── JsonReader.java        # JSON data reader
│   ├── GameData.java          # Game data model
│   ├── HexData.java           # Hexagon data model
│   ├── GameBoard.java         # Game board data model
│   ├── GameState.java         # Game state data model
│   ├── Tokens.java            # Token data model
│   ├── game_data.json         # Sample JSON data file
│   ├── gson-2.10.1.jar        # JSON parsing library
│   └── run.sh                 # Run script
├── simulation/             # Simulation components (to be developed)
└── README.md               # Project documentation
```

## Visualization Features

### Key Features
- Read JSON format game data
- Visualize 11 hexagons with different colors and types
- Support loading JSON data from files
- Display game state information
- Modern graphical user interface
- Real-time token statistics and percentages

### Running the Visualization Program

#### Method 1: Using the provided script
```bash
./visualization/run.sh
```

#### Method 2: Manual compilation and run
```bash
cd visualization
javac -cp ".:gson-2.10.1.jar" -source 8 -target 8 *.java
java -cp ".:gson-2.10.1.jar" GameVisualizer
```

#### Method 3: Using Maven
```bash
mvn clean compile exec:java -Dexec.mainClass="visualization.GameVisualizer"
```

## JSON数据格式

程序支持以下JSON格式：

```json
{
  "version": 1,
  "board": {
    "hexes": [
      { "id": 1, "color": "green", "type": "Wilds" },
      { "id": 2, "color": "brown", "type": "Wastes" },
      { "id": 3, "color": "blue", "type": "DevA" },
      { "id": 4, "color": "pink", "type": "DevB" }
    ]
  },
  "game_state": {
    "current_round": 1,
    "max_rounds": 50,
    "bag_total": 100
  },
  "tokens": {
    "Wilds": 25,
    "Wastes": 25,
    "DevA": 25,
    "DevB": 25
  }
}
```

## 六边形颜色

- 绿色 (green) - Wilds类型
- 棕色 (brown) - Wastes类型  
- 蓝色 (blue) - DevA类型
- 粉色 (pink) - DevB类型

## 系统要求

- Java 8或更高版本
- Maven 3.6或更高版本（可选）
The first stage of simulation.
