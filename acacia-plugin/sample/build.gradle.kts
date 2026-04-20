plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    id("io.github.rajumark.acacia")
}

android {
    namespace = "org.sample.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "org.sample.app"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
    // Note: Generated sources are automatically registered by the Acacia plugin
    // via the Android Variant API - no manual sourceSets configuration needed
}

kotlin {
    jvmToolchain(libs.versions.gradle.jvmToolchain.get().toInt())
}

shortify {
    enabled = true
    debug = true
}

dependencies {
    implementation(dependencies.platform(libs.compose.bom))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle)
    implementation(libs.bundles.compose)
}
