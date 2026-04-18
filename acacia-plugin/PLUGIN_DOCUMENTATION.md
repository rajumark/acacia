# Acacia - AI-Native Compose DSL Plugin

**Transform verbose Compose code into concise, AI-friendly DSL**

## Quick Start

Add the plugin to your Android project and start writing cleaner Compose code:

```kotlin
// Before (verbose)
Modifier
    .fillMaxWidth()
    .fillMaxHeight()
    .background(Color.Blue)
    .padding(16.dp)

// After (Acacia DSL)
Modifier.fmw().fmh().bg(Color.Blue).p(16.dp)
```

## Installation

### 1. Add Plugin to Your Project

In your app-level `build.gradle.kts`:

```kotlin
plugins {
    id("com.acacia") version "0.1.0"
}
```

### 2. Configure the Plugin (Optional)

```kotlin
shortify {
    enabled = true    // Enable/disable plugin (default: true)
    debug = false     // Enable debug logging (default: false)
}
```

### 3. Import Generated Functions

```kotlin
import com.acacia.generated.*
```

## Usage Examples

### Basic Layout

```kotlin
// Centered box with padding
Box(
    Modifier.fmw().fmh().p(16.dp),
    contentAlignment = Alignment.Center
) {
    Text("Hello World")
}
```

### Visual Styling

```kotlin
// Card with background, shadow, and rounded corners
Card(
    Modifier
        .bg(Color.White)
        .sh(4.dp)
        .cp(RoundedCornerShape(8.dp))
        .p(16.dp)
) {
    // Card content
}
```

### Interactive Elements

```kotlin
// Clickable button
Button(
    onClick = { /* Handle click */ },
    modifier = Modifier
        .bg(Color.Blue)
        .p(horizontal = 16.dp, vertical = 8.dp)
        .clk { /* Additional click handling */ }
) {
    Text("Click Me")
}
```

### Responsive Design

```kotlin
// Responsive container
Column(
    Modifier
        .fmw()
        .p(16.dp)
        .sc2 { scrollState }
) {
    // Scrollable content
}
```

## Available DSL Functions

The plugin generates **391+ short functions** covering:

### Layout Functions
- `p()` - padding
- `sz()` - size
- `w()` - width
- `h()` - height
- `fmw()` - fillMaxWidth
- `fmh()` - fillMaxHeight
- `fms()` - fillMaxSize
- `wcw()` - wrapContentWidth
- `wch()` - wrapContentHeight
- `wcs()` - wrapContentSize

### Visual Functions
- `bg()` - background
- `br()` - border
- `sh()` - shadow
- `cp()` - clip
- `al()` - alpha

### Interaction Functions
- `clk()` - clickable
- `dg()` - draggable
- `sc2()` - scrollable
- `sw()` - swipeable
- `tg()` - toggleable
- `sl()` - selectable

### System Functions
- `sbp()` - systemBarsPadding
- `stp()` - statusBarsPadding
- `nbp()` - navigationBarsPadding
- `sdp()` - safeDrawingPadding

### Testing & Accessibility
- `tt()` - testTag
- `sem()` - semantics

### And 300+ more functions!

## AI Integration

The plugin is **AI-friendly** and designed to work with external AI models:

### For AI Models
Generate natural language descriptions and let AI convert them to Acacia DSL:

```
Input: "Create a blue card with 16dp padding and shadow"
Output: Modifier.bg(Color.Blue).p(16.dp).sh(4.dp)
```

### For Developers
Use the generated AI documentation to train your own models or integrate with AI assistants.

## Advanced Configuration

### Debug Mode
Enable detailed logging to see what functions are generated:

```kotlin
shortify {
    debug = true
}
```

### Disable Plugin
Temporarily disable the plugin:

```kotlin
shortify {
    enabled = false
}
```

## Generated Files

The plugin generates files in `build/generated/source/shortify/`:

```
build/generated/source/shortify/
com/acacia/generated/
- ShortModifiers.kt (391+ DSL functions)
```

## Migration Guide

### From Standard Compose

```kotlin
// Standard Compose
Modifier
    .fillMaxWidth()
    .padding(16.dp)
    .background(Color.Blue)

// Acacia DSL
Modifier.fmw().p(16.dp).bg(Color.Blue)
```

### Benefits
- **70% less code** for common patterns
- **Better readability** with semantic names
- **AI-friendly** for code generation
- **Consistent naming** across projects

## Performance

- **First build**: ~7 seconds (dependency resolution)
- **Subsequent builds**: ~1-2 seconds (with caching)
- **Incremental builds**: <1 second (unchanged files)

## Troubleshooting

### Functions Not Found
If generated functions aren't found:

1. Clean and rebuild: `./gradlew clean build`
2. Check imports: `import com.acacia.generated.*`
3. Verify plugin is applied correctly

### Build Issues
If build fails:

1. Check Compose dependencies are up to date
2. Verify Android Gradle Plugin compatibility
3. Enable debug mode to see detailed logs

## Compatibility

- **Android Gradle Plugin**: 8.0+
- **Kotlin**: 1.9.0+
- **Compose**: 1.4.0+
- **Gradle**: 8.0+

## Examples Repository

For complete examples, check out the sample app in this repository.

## Contributing

Contributions are welcome! Please read the contributing guidelines before submitting PRs.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Transform your Compose development with Acacia!** 

*Generated 391+ DSL functions automatically from your Compose dependencies.*
