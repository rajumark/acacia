package com.acacia.platform

import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.Configuration

/**
 * Detects the target platform for Compose projects.
 */
class PlatformDetector(private val project: Project) {
    
    enum class Platform {
        ANDROID,
        DESKTOP,
        WEB,
        IOS,
        UNKNOWN
    }
    
    /**
     * Detects the primary Compose platform based on project dependencies.
     */
    fun detectPlatform(): Platform {
        val configurations = listOf(
            "implementation", "compileOnly", "api", "compileClasspath"
        )
        
        for (configName in configurations) {
            val config = project.configurations.findByName(configName)
            if (config != null) {
                val platform = detectPlatformFromConfiguration(config)
                if (platform != Platform.UNKNOWN) {
                    return platform
                }
            }
        }
        
        // Fallback: check for known plugins
        return detectPlatformFromPlugins()
    }
    
    /**
     * Detects platform from configuration dependencies.
     */
    private fun detectPlatformFromConfiguration(configuration: Configuration): Platform {
        return try {
            val resolvedArtifacts = configuration.resolvedConfiguration.resolvedArtifacts
            
            for (artifact in resolvedArtifacts) {
                val moduleVersion = artifact.moduleVersion.id
                val group = moduleVersion.group
                val name = moduleVersion.name
                
                when {
                    // Android Compose
                    group.startsWith("androidx.compose") && !name.contains("desktop") && !name.contains("web") -> {
                        return Platform.ANDROID
                    }
                    // Desktop Compose
                    group.startsWith("org.jetbrains.compose.desktop") || name.contains("desktop") -> {
                        return Platform.DESKTOP
                    }
                    // Web Compose
                    group.startsWith("org.jetbrains.compose.web") || name.contains("web") -> {
                        return Platform.WEB
                    }
                    // iOS Compose (future)
                    group.startsWith("org.jetbrains.compose.ios") || name.contains("ios") -> {
                        return Platform.IOS
                    }
                }
            }
            
            Platform.UNKNOWN
        } catch (e: Exception) {
            Platform.UNKNOWN
        }
    }
    
    /**
     * Detects platform from applied plugins.
     */
    private fun detectPlatformFromPlugins(): Platform {
        return when {
            project.pluginManager.hasPlugin("com.android.application") ||
            project.pluginManager.hasPlugin("com.android.library") ||
            project.pluginManager.hasPlugin("org.jetbrains.kotlin.android") -> {
                Platform.ANDROID
            }
            project.pluginManager.hasPlugin("org.jetbrains.kotlin.jvm") &&
            hasComposeDesktopDependency() -> {
                Platform.DESKTOP
            }
            project.pluginManager.hasPlugin("org.jetbrains.kotlin.js") &&
            hasComposeWebDependency() -> {
                Platform.WEB
            }
            else -> Platform.UNKNOWN
        }
    }
    
    /**
     * Checks if project has Compose Desktop dependencies.
     */
    private fun hasComposeDesktopDependency(): Boolean {
        return try {
            val config = project.configurations.findByName("implementation")
            config?.resolvedConfiguration?.resolvedArtifacts?.any { artifact ->
                artifact.moduleVersion.id.group.startsWith("org.jetbrains.compose.desktop")
            } == true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Checks if project has Compose Web dependencies.
     */
    private fun hasComposeWebDependency(): Boolean {
        return try {
            val config = project.configurations.findByName("implementation")
            config?.resolvedConfiguration?.resolvedArtifacts?.any { artifact ->
                artifact.moduleVersion.id.group.startsWith("org.jetbrains.compose.web")
            } == true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Gets platform-specific package names.
     */
    fun getPlatformPackage(platform: Platform): String {
        return when (platform) {
            Platform.ANDROID -> "androidx.compose.ui"
            Platform.DESKTOP -> "androidx.compose.ui"
            Platform.WEB -> "androidx.compose.ui"
            Platform.IOS -> "androidx.compose.ui"
            Platform.UNKNOWN -> "androidx.compose.ui"
        }
    }
    
    /**
     * Gets platform-specific Modifier class name.
     */
    fun getModifierClassName(platform: Platform): String {
        return when (platform) {
            Platform.ANDROID -> "androidx.compose.ui.Modifier"
            Platform.DESKTOP -> "androidx.compose.ui.Modifier"
            Platform.WEB -> "androidx.compose.ui.Modifier"
            Platform.IOS -> "androidx.compose.ui.Modifier"
            Platform.UNKNOWN -> "androidx.compose.ui.Modifier"
        }
    }
}
