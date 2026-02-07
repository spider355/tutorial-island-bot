#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "========================================"
echo "Tutorial Island Bot - Installation"
echo "========================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ ERROR: Java is not installed${NC}"
    echo "Please install Java 11 or higher"
    exit 1
fi

echo -e "${GREEN}✅ Java found:${NC}"
java -version 2>&1 | head -n 1
echo ""

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo -e "${RED}❌ ERROR: gradlew not found${NC}"
    echo "Please run this script from the plugin root directory"
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo -e "${BLUE}🔨 Building plugin...${NC}"
echo ""

# Build the fat JAR
./gradlew clean shadowJar
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Build failed!${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}✅ Build successful!${NC}"
echo ""

# Copy to RuneLite plugins folder
echo -e "${BLUE}📦 Installing to RuneLite...${NC}"
./gradlew copyToPlugins
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Installation failed!${NC}"
    exit 1
fi

echo ""
echo "========================================"
echo -e "${GREEN}✅ Installation Complete!${NC}"
echo "========================================"
echo ""
echo -e "Plugin installed to: ${YELLOW}~/.runelite/plugins/${NC}"
echo ""
echo -e "${BLUE}📋 Next steps:${NC}"
echo "   1. RESTART RuneLite (if running)"
echo "   2. Open RuneLite Configuration (wrench icon)"
echo "   3. Search for 'Tutorial Island Bot'"
echo "   4. Toggle it ON"
echo "   5. Configure settings as desired"
echo ""
