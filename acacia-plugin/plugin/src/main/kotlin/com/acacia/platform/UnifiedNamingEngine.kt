package com.acacia.platform

import com.acacia.mapping.NamingEngine
import com.acacia.model.ModifierFunction
import org.gradle.api.Project

/**
 * Unified naming engine that works across all Compose platforms.
 */
class UnifiedNamingEngine(private val project: Project) {
    
    private val platformDetector = PlatformDetector(project)
    private val baseNamingEngine = NamingEngine(project)
    private val platformDiscovery = PlatformModifierDiscovery(project)
    
    /**
     * Generates unified short names for Modifier functions across all platforms.
     */
    fun generateUnifiedShortNames(functions: List<ModifierFunction>): Map<String, String> {
        val platform = platformDetector.detectPlatform()
        val platformMappings = platformDiscovery.getPlatformSpecificMappings()
        
        project.logger.lifecycle("Shortify: Generating unified names for ${platform.name} platform")
        
        // Start with base naming engine mappings
        val baseMappings = baseNamingEngine.generateShortNames(functions)
        
        // Apply platform-specific mappings
        val unifiedMappings = mutableMapOf<String, String>()
        unifiedMappings.putAll(baseMappings)
        
        // Override with platform-specific mappings
        platformMappings.forEach { (functionName, shortName) ->
            if (baseMappings.containsKey(functionName)) {
                unifiedMappings[functionName] = shortName
                project.logger.debug("Shortify: Applied platform-specific mapping: $functionName -> $shortName")
            }
        }
        
        // Add platform prefixes for conflicting functions
        val finalMappings = resolvePlatformConflicts(unifiedMappings, platform)
        
        return finalMappings
    }
    
    /**
     * Resolves conflicts between platform-specific and common functions.
     */
    private fun resolvePlatformConflicts(
        mappings: Map<String, String>, 
        platform: PlatformDetector.Platform
    ): Map<String, String> {
        val resolvedMappings = mutableMapOf<String, String>()
        val usedNames = mutableSetOf<String>()
        
        // First, add platform-specific functions with priority
        val platformFunctions = getPlatformSpecificFunctions(platform)
        
        platformFunctions.forEach { functionName ->
            val shortName = mappings[functionName]
            if (shortName != null && !usedNames.contains(shortName)) {
                resolvedMappings[functionName] = shortName
                usedNames.add(shortName)
            }
        }
        
        // Then add common functions, avoiding conflicts
        mappings.forEach { (functionName, shortName) ->
            if (!resolvedMappings.containsKey(functionName) && !usedNames.contains(shortName)) {
                resolvedMappings[functionName] = shortName
                usedNames.add(shortName)
            } else if (resolvedMappings.containsKey(functionName) && usedNames.contains(shortName)) {
                // Conflict detected, generate alternative
                val alternativeName = generatePlatformAlternative(shortName, platform, usedNames)
                resolvedMappings[functionName] = alternativeName
                usedNames.add(alternativeName)
            }
        }
        
        return resolvedMappings
    }
    
    /**
     * Gets platform-specific function names.
     */
    private fun getPlatformSpecificFunctions(platform: PlatformDetector.Platform): Set<String> {
        return when (platform) {
            PlatformDetector.Platform.ANDROID -> setOf(
                "systemBarsPadding", "statusBarsPadding", "navigationBarsPadding", 
                "imePadding", "mandatorySystemGesturesPadding", "displayCutoutPadding"
            )
            PlatformDetector.Platform.DESKTOP -> setOf(
                "onKeyEvent", "onPreviewKeyEvent", "onPointerEvent"
            )
            PlatformDetector.Platform.WEB -> setOf(
                "onClick", "onDoubleClick", "onContextMenu"
            )
            PlatformDetector.Platform.IOS -> setOf(
                "safeAreaPadding", "safeAreaInsets"
            )
            PlatformDetector.Platform.UNKNOWN -> emptySet()
        }
    }
    
    /**
     * Generates platform-specific alternative names for conflicts.
     */
    private fun generatePlatformAlternative(
        baseName: String, 
        platform: PlatformDetector.Platform, 
        usedNames: Set<String>
    ): String {
        val platformPrefix = when (platform) {
            PlatformDetector.Platform.ANDROID -> "a"
            PlatformDetector.Platform.DESKTOP -> "d"
            PlatformDetector.Platform.WEB -> "w"
            PlatformDetector.Platform.IOS -> "i"
            PlatformDetector.Platform.UNKNOWN -> "u"
        }
        
        val alternative = "${platformPrefix}${baseName}"
        
        return if (!usedNames.contains(alternative)) {
            alternative
        } else {
            // Try with number
            for (i in 2..9) {
                val numberedAlternative = "${alternative}$i"
                if (!usedNames.contains(numberedAlternative)) {
                    return numberedAlternative
                }
            }
            
            // Last resort
            "${alternative}_${platform.name.lowercase().take(3)}"
        }
    }
    
    /**
     * Gets platform-specific package imports for generated code.
     */
    fun getPlatformImports(platform: PlatformDetector.Platform): List<String> {
        return when (platform) {
            PlatformDetector.Platform.ANDROID -> listOf(
                "androidx.compose.ui.Modifier",
                "androidx.compose.ui.unit.Dp",
                "androidx.compose.ui.graphics.Color",
                "androidx.compose.foundation.layout.PaddingValues"
            )
            PlatformDetector.Platform.DESKTOP -> listOf(
                "androidx.compose.ui.Modifier",
                "androidx.compose.ui.unit.Dp",
                "androidx.compose.ui.graphics.Color",
                "androidx.compose.foundation.layout.PaddingValues",
                "org.jetbrains.compose.desktop.ui.keyEvent"
            )
            PlatformDetector.Platform.WEB -> listOf(
                "androidx.compose.ui.Modifier",
                "androidx.compose.ui.unit.Dp",
                "androidx.compose.ui.graphics.Color",
                "androidx.compose.foundation.layout.PaddingValues",
                "org.w3c.dom.events.MouseEvent"
            )
            PlatformDetector.Platform.IOS -> listOf(
                "androidx.compose.ui.Modifier",
                "androidx.compose.ui.unit.Dp",
                "androidx.compose.ui.graphics.Color",
                "androidx.compose.foundation.layout.PaddingValues"
            )
            PlatformDetector.Platform.UNKNOWN -> listOf(
                "androidx.compose.ui.Modifier",
                "androidx.compose.ui.unit.Dp",
                "androidx.compose.ui.graphics.Color"
            )
        }
    }
    
    /**
     * Gets platform-specific function documentation.
     */
    fun getPlatformDocumentation(functionName: String, platform: PlatformDetector.Platform): String {
        val platformNote = when (platform) {
            PlatformDetector.Platform.ANDROID -> "Android-specific"
            PlatformDetector.Platform.DESKTOP -> "Desktop-specific"
            PlatformDetector.Platform.WEB -> "Web-specific"
            PlatformDetector.Platform.IOS -> "iOS-specific"
            PlatformDetector.Platform.UNKNOWN -> "Generic"
        }
        
        return "Short DSL function for [Modifier.$functionName] ($platformNote)"
    }
}
