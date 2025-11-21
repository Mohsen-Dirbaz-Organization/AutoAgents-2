@echo off
echo ============================================
echo    AutoAgents Plugin Installation Helper
echo ============================================
echo.

REM Check if gradle wrapper exists
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo ERROR: gradle-wrapper.jar not found!
    echo.
    echo Please download it from:
    echo https://github.com/gradle/gradle/raw/v8.12.0/gradle/wrapper/gradle-wrapper.jar
    echo.
    echo And save it to: gradle\wrapper\gradle-wrapper.jar
    echo.
    pause
    exit /b 1
)

echo Step 1: Cleaning previous builds...
call gradlew.bat clean

echo.
echo Step 2: Building the plugin...
echo This may take a few minutes on first run...
echo.

call gradlew.bat buildPlugin

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ====================================
    echo    BUILD FAILED!
    echo ====================================
    echo.
    echo Common issues:
    echo 1. Make sure Gradle is using Java 17 or 21 (not Java 23)
    echo 2. Configure in IntelliJ: File - Settings - Build Tools - Gradle - Gradle JVM
    echo 3. Or set JAVA_HOME to Java 17/21 before running this script
    echo.
    pause
    exit /b 1
)

echo.
echo ====================================
echo    BUILD SUCCESSFUL!
echo ====================================
echo.
echo Plugin ZIP created at:
echo %cd%\build\distributions\AutoAgents-1.0.0.zip
echo.
echo TO INSTALL IN INTELLIJ:
echo ----------------------
echo 1. Open IntelliJ IDEA
echo 2. Go to: File → Settings → Plugins
echo 3. Click the gear icon (⚙️)
echo 4. Select: "Install Plugin from Disk..."
echo 5. Browse to: %cd%\build\distributions\AutoAgents-1.0.0.zip
echo 6. Click OK
echo 7. Restart IntelliJ IDEA
echo.
echo After restart, access the plugin:
echo - View → Tool Windows → AutoAgents
echo - Or press Ctrl+Alt+S for Station Management
echo.
pause