#!/bin/bash

echo "Fixing Gradle Wrapper for AutoAgents project..."

# Create wrapper directory if it doesn't exist
mkdir -p gradle/wrapper

# Check if wrapper jar exists
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "gradle-wrapper.jar not found. Attempting to download..."
    
    # Try to download from GitHub
    WRAPPER_URL="https://github.com/gradle/gradle/raw/v8.12.0/gradle/wrapper/gradle-wrapper.jar"
    
    echo "Downloading from: $WRAPPER_URL"
    curl -L -o gradle/wrapper/gradle-wrapper.jar "$WRAPPER_URL"
    
    if [ $? -eq 0 ] && [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
        echo "Successfully downloaded gradle-wrapper.jar"
    else
        echo "Failed to download gradle-wrapper.jar"
        echo "Please manually download it from another Gradle project or from:"
        echo "https://github.com/gradle/gradle/tree/master/gradle/wrapper"
        exit 1
    fi
fi

# Make gradlew executable
chmod +x gradlew

echo "Gradle wrapper setup complete!"
echo ""
echo "Next steps:"
echo "1. Ensure you're using Java 17 or 21 (not Java 23)"
echo "2. Run: ./gradlew clean buildPlugin"