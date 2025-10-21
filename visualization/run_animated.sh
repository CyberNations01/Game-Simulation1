#!/bin/bash

echo "Compiling Java animated visualization project..."
cd visualization
javac -cp ".:gson-2.10.1.jar:../src/src" -source 14 -target 14 *.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running animated visualization program..."
    java -cp ".:gson-2.10.1.jar:../src/src" AnimatedGameVisualizer
else
    echo "Compilation failed!"
    exit 1
fi


