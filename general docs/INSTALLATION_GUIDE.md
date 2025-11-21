# AutoAgents Plugin Installation Guide

## Prerequisites

1. **Java 17 or 21** (for building - Java 23 is NOT compatible with Gradle 8.12)
2. **IntelliJ IDEA** 2023.3 or later
3. **Internet connection** for downloading dependencies

## Step 1: Configure Java for Gradle

Since Java 23 is incompatible with Gradle 8.12, you must use Java 17 or 21 for building.

### Method A: Configure in IntelliJ (Recommended)

1. Open IntelliJ IDEA
2. Open the AutoAgents project
3. Go to: **File → Settings** (Windows/Linux) or **IntelliJ IDEA → Preferences** (Mac)
4. Navigate to: **Build, Execution, Deployment → Build Tools → Gradle**
5. In "Gradle JVM" dropdown:
   - Select **Java 17** or **Java 21**
   - If not available, click **"Download JDK"** and download Java 17 or 21
6. Click **OK**

### Method B: Use Command Line

```bash
# Windows - Set JAVA_HOME temporarily
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

# Linux/Mac - Set JAVA_HOME temporarily
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

## Step 2: Get the Gradle Wrapper JAR

The project needs `gradle-wrapper.jar`. Get it by:

### Option 1: Copy from another project
```bash
# Find any Gradle project on your system and copy:
copy "C:\OtherProject\gradle\wrapper\gradle-wrapper.jar" "gradle\wrapper\gradle-wrapper.jar"
```

### Option 2: Download directly
1. Download from: https://github.com/gradle/gradle/raw/v8.12.0/gradle/wrapper/gradle-wrapper.jar
2. Save to: `AutoAgents\gradle\wrapper\gradle-wrapper.jar`

## Step 3: Build the Plugin

Open a terminal in the AutoAgents directory:

```bash
# Windows
gradlew.bat clean buildPlugin

# Linux/Mac
./gradlew clean buildPlugin
```

Wait for the build to complete. You should see "BUILD SUCCESSFUL".

## Step 4: Locate the Built Plugin

After successful build, the plugin ZIP file will be at:
```
AutoAgents\build\distributions\AutoAgents-1.0.0.zip
```

## Step 5: Install the Plugin in IntelliJ

1. **Open IntelliJ IDEA**
2. **Go to Settings**:
   - Windows/Linux: **File → Settings**
   - Mac: **IntelliJ IDEA → Preferences**
3. **Navigate to**: **Plugins**
4. **Click the gear icon** (⚙️) next to "Installed"
5. **Select**: **Install Plugin from Disk...**
6. **Browse to**: `AutoAgents\build\distributions\AutoAgents-1.0.0.zip`
7. **Click OK**
8. **Restart IntelliJ IDEA** when prompted

## Step 6: Verify Installation

After restart:

1. Check if plugin is active:
   - **File → Settings → Plugins**
   - Search for "AutoAgents"
   - Should show as installed and enabled

2. Look for the tool window:
   - **View → Tool Windows → AutoAgents**
   - Or check the right sidebar for AutoAgents icon

3. Check the Tools menu:
   - **Tools → AutoAgents** should be visible

## Troubleshooting

### Build Fails with Java Error

**Error**: "Unsupported class file major version 67"
**Solution**: You're still using Java 23. Make sure Gradle is using Java 17/21:
```bash
# Check which Java Gradle is using
gradlew.bat --version

# Should show Java 17 or 21, not 23
```

### Gradle Wrapper Not Found

**Error**: "Could not find gradle-wrapper.jar"
**Solution**: Follow Step 2 to get the wrapper JAR file

### Plugin Not Visible After Installation

**Solution**:
1. Make sure you restarted IntelliJ
2. Check if plugin is enabled in Settings → Plugins
3. Try invalidating caches: File → Invalidate Caches and Restart

### Out of Memory During Build

**Solution**: Increase Gradle memory in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m
```

## Quick Installation Script

Save this as `install.bat` for Windows:

```batch
@echo off
echo Building AutoAgents Plugin...
echo.
echo Make sure you're using Java 17 or 21 for Gradle!
echo.

REM Build the plugin
call gradlew.bat clean buildPlugin

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Build failed! Check that Gradle is using Java 17 or 21
    pause
    exit /b 1
)

echo.
echo Build successful!
echo Plugin is at: build\distributions\AutoAgents-1.0.0.zip
echo.
echo To install:
echo 1. Open IntelliJ IDEA
echo 2. Go to File - Settings - Plugins
echo 3. Click gear icon - Install Plugin from Disk
echo 4. Select: %cd%\build\distributions\AutoAgents-1.0.0.zip
echo 5. Restart IntelliJ
echo.
pause
```

## Next Steps

After installation:

1. Open the AutoAgents tool window (View → Tool Windows → AutoAgents)
2. Create your first station (Ctrl+Alt+S)
3. Deploy agents
4. Start using AI features!

See the [Station Management Guide](docs/STATION_MANAGEMENT_GUIDE.md) for detailed usage instructions.