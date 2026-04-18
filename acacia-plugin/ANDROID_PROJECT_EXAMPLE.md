# Android Project Integration Example

## Step-by-Step Guide for Android Developers

### 1. Add Plugin to build.gradle.kts

```kotlin
// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.acacia") version "0.1.0"  // Add this line
}

android {
    // ... your Android configuration
}

compose {
    // ... your Compose configuration
}

dependencies {
    implementation(compose.bom)
    implementation(compose.ui)
    implementation(compose.material3)
    // ... your other dependencies
}

// Optional: Configure Acacia
shortify {
    enabled = true
    debug = false
}
```

### 2. Sync and Build

```bash
./gradlew build
```

### 3. Import Generated Functions

```kotlin
// MainActivity.kt
package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.acacia.generated.*  // Import generated functions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    Surface(
        modifier = Modifier.fmw().fmh().bg(Color.White)
    ) {
        Column(
            modifier = Modifier.p(16.dp).sc2(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header()
            Content()
            Footer()
        }
    }
}

@Composable
fun Header() {
    Card(
        modifier = Modifier
            .bg(Color.Blue)
            .p(16.dp)
            .clk { /* Handle click */ }
            .tt("header_card")
    ) {
        Text(
            text = "Welcome to Acacia!",
            color = Color.White,
            modifier = Modifier.fmw()
        )
    }
}

@Composable
fun Content() {
    Column(
        modifier = Modifier.fmw(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Button examples
        Button(
            onClick = { /* Handle click */ },
            modifier = Modifier
                .fmw()
                .h(48.dp)
                .bg(Color.Green)
                .cp(RoundedCornerShape(8.dp))
                .tt("primary_button")
        ) {
            Text("Primary Button")
        }
        
        Button(
            onClick = { /* Handle click */ },
            modifier = Modifier
                .fmw()
                .h(48.dp)
                .bg(Color.Gray)
                .cp(RoundedCornerShape(8.dp))
                .tt("secondary_button")
        ) {
            Text("Secondary Button")
        }
        
        // Card examples
        Card(
            modifier = Modifier
                .fmw()
                .bg(Color.LightGray)
                .sh(4.dp)
                .p(16.dp)
                .tt("content_card")
        ) {
            Column {
                Text(
                    text = "Card Title",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.h(8.dp))
                Text(
                    text = "This is a card with shadow and padding using Acacia DSL.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun Footer() {
    Row(
        modifier = Modifier
            .fmw()
            .p(16.dp)
            .bg(Color.DarkGray),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { /* Handle click */ },
            modifier = Modifier
                .bg(Color.Red)
                .cp(CircleShape)
                .sz(40.dp)
                .tt("circular_button")
        ) {
            Text("!")
        }
        
        Text(
            text = "Footer with circular button",
            color = Color.White,
            modifier = Modifier.w(0.dp).wg(1f)
        )
    }
}
```

### 4. Generated Functions Available

After building, you'll have access to functions like:

```kotlin
// Layout functions
Modifier.p(16.dp)                    // padding
Modifier.fmw()                      // fillMaxWidth
Modifier.fmh()                      // fillMaxHeight
Modifier.sz(100.dp)                 // size
Modifier.w(200.dp)                  // width
Modifier.h(48.dp)                   // height

// Visual functions
Modifier.bg(Color.Blue)             // background
Modifier.sh(4.dp)                   // shadow
Modifier.br(1.dp, Color.Black)     // border
Modifier.cp(RoundedCornerShape(8.dp)) // clip

// Interaction functions
Modifier.clk { /* onClick */ }      // clickable
Modifier.sc2 { /* scrollState */ }  // scrollable
Modifier.dg { /* dragState */ }      // draggable

// System functions
Modifier.sbp()                      // systemBarsPadding
Modifier.stp()                      // statusBarsPadding
Modifier.nbp()                      // navigationBarsPadding
Modifier.sdp()                      // safeDrawingPadding

// Testing functions
Modifier.tt("button_id")            // testTag
Modifier.sem { /* semantics */ }    // semantics

// And 300+ more!
```

### 5. Benefits You Get

**Before Acacia:**
```kotlin
Modifier
    .fillMaxWidth()
    .fillMaxHeight()
    .background(Color.White)
    .padding(16.dp)
    .scrollable(rememberScrollState())
```

**After Acacia:**
```kotlin
Modifier.fmw().fmh().bg(Color.White).p(16.dp).sc2(rememberScrollState())
```

- **70% less code** for common patterns
- **Better readability** with semantic names
- **Consistent naming** across your team
- **AI-friendly** for code generation

### 6. Build Output

When you build, you'll see:

```
> Task :generateShortModifiers
Shortify: Detected platform: ANDROID
Shortify: Discovered 391 Modifier functions for cross-platform
Shortify: Generated 391 DSL functions
Shortify: Generated AI documentation and training data
```

The generated functions will be in:
```
build/generated/source/shortify/com/acacia/generated/ShortModifiers.kt
```

### 7. AI Integration (Optional)

Use with AI models:

```
User: "Create a blue card with 16dp padding and shadow"
AI: Modifier.bg(Color.Blue).p(16.dp).sh(4.dp)
```

The generated code works immediately because Acacia provides the wrapper functions!

---

**That's it! You're now using Acacia DSL in your Android project!**
