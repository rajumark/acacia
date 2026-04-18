#!/bin/bash

# Setup script for Acacia Plugin Publishing Environment Variables
# This script sets up the environment variables needed for Gradle Plugin Portal publishing

echo "Setting up Acacia Plugin publishing environment variables..."

# Set environment variables for current session
export GRADLE_PUBLISH_KEY=T6oAWi1a7KCTUiZS0acHyQxXkunOdCmp
export GRADLE_PUBLISH_SECRET=Wj9Grs8JpdwKANvlJvSC3wi3LAvAwkV2

# Add to ~/.bashrc for persistence
if ! grep -q "GRADLE_PUBLISH_KEY" ~/.bashrc; then
    echo 'export GRADLE_PUBLISH_KEY=T6oAWi1a7KCTUiZS0acHyQxXkunOdCmp' >> ~/.bashrc
    echo 'export GRADLE_PUBLISH_SECRET=Wj9Grs8JpdwKANvlJvSC3wi3LAvAwkV2' >> ~/.bashrc
    echo "Environment variables added to ~/.bashrc"
else
    echo "Environment variables already exist in ~/.bashrc"
fi

# Verify setup
echo "Environment variables set:"
echo "GRADLE_PUBLISH_KEY: ${GRADLE_PUBLISH_KEY:0:10}..."
echo "GRADLE_PUBLISH_SECRET: ${GRADLE_PUBLISH_SECRET:0:10}..."

echo ""
echo "Setup complete! You can now publish the plugin with:"
echo "./gradlew :plugin:publishPlugins"
