#!/bin/bash
echo "Updating Tutorial Island Bot..."
./gradlew clean shadowJar copyToPlugins
echo ""
echo "Update complete! Restart RuneLite."
