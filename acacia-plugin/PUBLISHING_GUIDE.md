# Acacia Plugin Publishing Guide

## Step-by-Step Guide to Publish to Gradle Plugin Portal

### 1. Register for Gradle Plugin Portal Account

1. Go to: https://plugins.gradle.org/user/register
2. Click **"Register with GitHub"** (recommended)
3. Authorize GitHub access
4. Accept terms and conditions
5. You'll receive your API keys

### 2. Get Your API Keys

1. Go to your profile page on the Plugin Portal
2. Click the **"API Keys"** tab
3. Copy your API key and secret:
   - **API Key**: `gradle.publish.key=your_key_here`
   - **API Secret**: `gradle.publish.secret=your_secret_here`

### 3. Configure Local Environment

1. Copy the example file:
```bash
cp gradle.properties.example gradle.properties
```

2. Edit `gradle.properties` and add your API keys:
```properties
gradle.publish.key=your_actual_api_key_here
gradle.publish.secret=your_actual_api_secret_here
signingKey=-----BEGIN PGP PRIVATE KEY BLOCK-----
[your PGP key here]
-----END PGP PRIVATE KEY BLOCK-----
signingPassword=
```

### 4. Publish the Plugin

Run the publishing command:
```bash
./gradlew :plugin:publishPlugins
```

### 5. Approval Process

**Important:** Your plugin will go through manual review:

1. **Initial Publication**: Plugin is submitted for review
2. **Manual Review**: Gradle team reviews your plugin
3. **Approval**: You'll receive an email when approved
4. **Public Availability**: Plugin appears on the Portal

**Review Criteria:**
- Plugin must have useful functionality
- Should be useful to a wide audience
- Must be documented with proper description
- Tags should describe plugin categories
- Website and VCS URLs must point to documentation/sources
- Plugin ID should trace back to the author
- Only final versions (no SNAPSHOT)
- Should declare supported Gradle features

### 6. Verify Publication

1. Wait for approval email
2. Check: https://plugins.gradle.org/plugin/com.acacia
3. Your plugin should appear after approval

## Publishing Commands

### Standard Publishing
```bash
./gradlew :plugin:publishPlugins
```

### With API Keys as Arguments
```bash
./gradlew :plugin:publishPlugins \
  -Pgradle.publish.key=your_key \
  -Pgradle.publish.secret=your_secret
```

### Dry Run (Testing)
```bash
./gradlew :plugin:publishPlugins --dry-run
```

## Plugin Information

- **Plugin ID**: `com.acacia`
- **Display Name**: "Acacia"
- **Description**: "AI-Native Compose DSL Plugin"
- **Tags**: `compose`, `dsl`, `ai`
- **Version**: `0.1.0`

## What Gets Published

- **Plugin JAR**: The compiled plugin
- **Sources JAR**: Source code for developers
- **Javadoc JAR**: Documentation
- **PGP Signature**: Security verification

## Post-Publishing

### 1. Test Installation
Developers can now use:
```kotlin
plugins {
    id("com.acacia") version "0.1.0"
}
```

### 2. Monitor Usage
- Check plugin portal for download stats
- Monitor GitHub issues and discussions
- Update documentation as needed

### 3. Version Updates
For new versions:
1. Update version in `plugin/build.gradle.kts`
2. Run `./gradlew :plugin:publishPlugins`
3. Update documentation

## Troubleshooting

### Common Issues

**"API key not found"**
- Check `gradle.properties` exists
- Verify API keys are correct
- Ensure no extra spaces in keys

**"PGP signing failed"**
- Verify PGP key is properly formatted
- Check for line breaks in signing key
- Ensure signing password is empty (no passphrase)

**"Plugin already exists"**
- Increment version number
- Or use different plugin ID

**"Network timeout"**
- Check internet connection
- Try again later
- Use VPN if needed

### Debug Mode
Enable debug logging:
```bash
./gradlew :plugin:publishPlugins --info
```

## Security Notes

- **Never commit API keys** to version control
- **Add `gradle.properties`** to `.gitignore`
- **Use environment variables** for CI/CD
- **Rotate API keys** periodically

## CI/CD Publishing (Optional)

For automated publishing with GitHub Actions:

```yaml
name: Publish Plugin
on:
  push:
    tags:
      - 'v*'
jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Publish Plugin
        run: ./gradlew :plugin:publishPlugins
        env:
          gradle.publish.key: ${{ secrets.GRADLE_PUBLISH_KEY }}
          gradle.publish.secret: ${{ secrets.GRADLE_PUBLISH_SECRET }}
          signingKey: ${{ secrets.SIGNING_KEY }}
```

## Success Indicators

When publishing succeeds, you'll see:
```
> Task :plugin:publishPlugins
Publishing plugin com.acacia:0.1.0
Plugin com.acacia:0.1.0 has been published and is pending acceptance.
```

**Note:** "pending acceptance" means it's submitted for manual review - this is normal!

## Next Steps

After successful publishing:

1. **Announce** the plugin on social media
2. **Update** README with installation instructions
3. **Create** examples and tutorials
4. **Monitor** for issues and feedback
5. **Plan** for future versions

---

**Your Acacia plugin will be available to millions of Android developers worldwide!**
