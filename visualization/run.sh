#!/bin/bash

echo "Compiling Java visualization project..."
cd visualization
javac -cp ".:gson-2.10.1.jar:../target/dependency/jfreechart-1.5.3.jar:../src/src" -source 14 -target 14 *.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running visualization program..."
    java -cp ".:gson-2.10.1.jar:../target/dependency/jfreechart-1.5.3.jar:../src/src" AnimatedGameVisualizer
else
    echo "Compilation failed!"
    exit 1
fi
