#!/bin/bash

echo "Compiling Java visualization project..."
cd visualization
javac -cp ".:gson-2.10.1.jar" -source 8 -target 8 *.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running visualization program..."
    java -cp ".:gson-2.10.1.jar" GameVisualizer
else
    echo "Compilation failed!"
    exit 1
fi
