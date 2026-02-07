@echo off
echo Updating Tutorial Island Bot...
call gradlew.bat clean shadowJar copyToPlugins
echo.
echo Update complete! Restart RuneLite.
pause
