@echo off
echo ========================================
echo Tutorial Island Bot - Installation
echo ========================================
echo.

java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed
    pause
    exit /b 1
)

echo Building plugin...
call gradlew.bat clean shadowJar
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo Installing to RuneLite...
call gradlew.bat copyToPlugins

echo.
echo Installation Complete!
echo Restart RuneLite and enable the plugin.
pause
