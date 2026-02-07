@echo off
echo ========================================
echo Tutorial Island Bot - Installation
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ ERROR: Java is not installed or not in PATH
    echo Please install Java 11 or higher
    pause
    exit /b 1
)

echo ✅ Java found
echo.

REM Check if gradlew exists
if not exist "gradlew.bat" (
    echo ❌ ERROR: gradlew.bat not found
    echo Please run this script from the plugin root directory
    pause
    exit /b 1
)

echo 🔨 Building plugin...
echo.

REM Build the fat JAR
call gradlew.bat clean shadowJar
if %errorlevel% neq 0 (
    echo ❌ Build failed!
    pause
    exit /b 1
)

echo.
echo ✅ Build successful!
echo.

REM Copy to RuneLite plugins folder
echo 📦 Installing to RuneLite...
call gradlew.bat copyToPlugins
if %errorlevel% neq 0 (
    echo ❌ Installation failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ Installation Complete!
echo ========================================
echo.
echo Plugin installed to: %USERPROFILE%\.runelite\plugins\
echo.
echo 📋 Next steps:
echo    1. RESTART RuneLite (if running)
echo    2. Open RuneLite Configuration (wrench icon)
echo    3. Search for "Tutorial Island Bot"
echo    4. Toggle it ON
echo    5. Configure settings as desired
echo.
pause
