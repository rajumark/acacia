package com.acacia.integration

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for the Acacia plugin.
 * 
 * CRITICAL: Tests the full plugin application in realistic scenarios.
 * These tests apply the plugin to actual Gradle projects and verify behavior.
 */
class PluginIntegrationTest {

    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `plugin applies successfully to Android project`() {
        setupAndroidProject()
        
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("tasks", "--all")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":tasks")?.outcome)
        assertTrue(result.output.contains("generateShortModifiers"), 
            "Plugin should register generateShortModifiers task")
    }

    @Test
    fun `generateShortModifiers task creates output file`() {
        setupAndroidProject()
        
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("generateShortModifiers")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":generateShortModifiers")?.outcome)
        
        // Verify output file was created
        val outputFile = File(testProjectDir.root, 
            "build/generated/source/shortify/ShortModifiers.kt")
        assertTrue(outputFile.exists(), "Generated file should exist")
        
        val content = outputFile.readText()
        assertTrue(content.contains("package com.acacia.generated"), 
            "Should have correct package")
    }

    @Test
    fun `generated code compiles successfully`() {
        setupAndroidProject()
        
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("compileDebugKotlin")
            .withPluginClasspath()
            .build()

        // May be SUCCESS or UP_TO_DATE depending on setup
        val outcome = result.task(":compileDebugKotlin")?.outcome
        assertTrue(
            outcome == TaskOutcome.SUCCESS || outcome == TaskOutcome.UP_TO_DATE,
            "Generated code should compile. Outcome: $outcome"
        )
    }

    @Test
    fun `plugin is incremental - skips when no changes`() {
        setupAndroidProject()
        
        // First build
        val firstResult = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("generateShortModifiers")
            .withPluginClasspath()
            .build()
        
        assertEquals(TaskOutcome.SUCCESS, firstResult.task(":generateShortModifiers")?.outcome)
        
        // Second build (no changes)
        val secondResult = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("generateShortModifiers")
            .withPluginClasspath()
            .build()
        
        // Should be UP_TO_DATE
        assertEquals(
            TaskOutcome.UP_TO_DATE,
            secondResult.task(":generateShortModifiers")?.outcome,
            "Task should be UP_TO_DATE when no changes"
        )
    }

    @Test
    fun `plugin respects enabled flag`() {
        setupAndroidProject(enabled = false)
        
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("generateShortModifiers")
            .withPluginClasspath()
            .build()

        // Task should complete but skip generation
        assertEquals(TaskOutcome.SUCCESS, result.task(":generateShortModifiers")?.outcome)
        
        val outputFile = File(testProjectDir.root, 
            "build/generated/source/shortify/ShortModifiers.kt")
        assertTrue(!outputFile.exists() || outputFile.readText().isBlank(), 
            "Should not generate when disabled")
    }

    @Test
    fun `plugin handles missing Compose dependencies gracefully`() {
        setupEmptyProject()
        
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("generateShortModifiers")
            .withPluginClasspath()
            .build()

        // Should succeed even without Compose dependencies
        assertEquals(TaskOutcome.SUCCESS, result.task(":generateShortModifiers")?.outcome)
        
        // Should log warning about missing dependencies
        assertTrue(
            result.output.contains("No Compose JAR files found") ||
            result.output.contains("No modifier functions found"),
            "Should warn about missing Compose dependencies"
        )
    }

    private fun setupAndroidProject(enabled: Boolean = true) {
        // settings.gradle
        testProjectDir.newFile("settings.gradle").writeText("""
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
            dependencyResolutionManagement {
                repositories {
                    google()
                    mavenCentral()
                }
            }
            rootProject.name = 'test-app'
        """.trimIndent())

        // build.gradle
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
                id 'com.android.application' version '8.2.0'
                id 'org.jetbrains.kotlin.android' version '1.9.22'
                id 'com.acacia' version '0.1.0'
            }
            
            android {
                namespace 'com.test.app'
                compileSdk 34
                
                defaultConfig {
                    applicationId 'com.test.app'
                    minSdk 24
                    targetSdk 34
                    versionCode 1
                    versionName '1.0'
                }
                
                compileOptions {
                    sourceCompatibility JavaVersion.VERSION_17
                    targetCompatibility JavaVersion.VERSION_17
                }
                
                kotlinOptions {
                    jvmTarget = '17'
                }
            }
            
            shortify {
                enabled = $enabled
                debug = true
            }
            
            dependencies {
                implementation 'androidx.compose.ui:ui:1.6.0'
                implementation 'androidx.compose.foundation:foundation:1.6.0'
            }
        """.trimIndent())

        // Create minimal source structure
        val srcDir = testProjectDir.newFolder("src", "main", "kotlin", "com", "test", "app")
        File(srcDir, "MainActivity.kt").writeText("""
            package com.test.app
            
            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.material3.Text
            
            class MainActivity : ComponentActivity() {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    setContent {
                        Text("Hello")
                    }
                }
            }
        """.trimIndent())
    }

    private fun setupEmptyProject() {
        // settings.gradle
        testProjectDir.newFile("settings.gradle").writeText("""
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
            rootProject.name = 'test-empty'
        """.trimIndent())

        // build.gradle
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
                id 'java'
                id 'com.acacia' version '0.1.0'
            }
            
            shortify {
                enabled = true
                debug = true
            }
        """.trimIndent())
    }
}
