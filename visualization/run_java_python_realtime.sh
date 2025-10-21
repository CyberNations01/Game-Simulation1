#!/bin/bash
# 启动Java + Python实时图表可视化系统

echo "=== Java + Python 实时图表可视化系统 ==="
echo ""

# 检查Python依赖
echo "检查Python依赖..."
python3 -c "import websockets, matplotlib, tkinter" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "安装Python依赖..."
    pip3 install websockets matplotlib
fi

# 启动Python实时图表服务器
echo "启动Python实时图表服务器..."
cd /Users/david/IdeaProjects/Game-Simulation1/visualization
source venv/bin/activate
python real_time_chart_server.py &
PYTHON_PID=$!

# 等待Python服务器启动
sleep 3

# 启动Java可视化程序
echo "启动Java可视化程序..."
cd /Users/david/IdeaProjects/Game-Simulation1/visualization
java -cp ".:gson-2.10.1.jar:../src/src" AnimatedGameVisualizer &
JAVA_PID=$!

echo ""
echo "系统已启动！"
echo "Python服务器 PID: $PYTHON_PID"
echo "Java程序 PID: $JAVA_PID"
echo ""
echo "使用说明："
echo "1. Java程序会显示游戏可视化界面"
echo "2. Python程序会显示实时图表窗口"
echo "3. 在Java界面中点击 'Toggle Real-time Charts' 启用实时同步"
echo "4. 播放动画时，Python图表会实时更新"
echo ""
echo "按 Ctrl+C 停止所有程序"

# 等待用户中断
trap 'echo "正在停止程序..."; kill $PYTHON_PID $JAVA_PID 2>/dev/null; exit' INT
wait
