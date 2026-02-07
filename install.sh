#!/bin/bash

echo "========================================"
echo "Tutorial Island Bot - Installation"
echo "========================================"

if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed"
    exit 1
fi

echo "Building plugin..."
chmod +x gradlew
./gradlew clean shadowJar

echo "Installing to RuneLite..."
./gradlew copyToPlugins

echo ""
echo "Installation Complete!"
echo "Restart RuneLite and enable the plugin."
