package com.acacia.platform

import com.acacia.model.ModifierFunction
import com.acacia.parser.AsmModifierParser
import com.acacia.resolver.DependencyResolver
import com.acacia.resolver.AarExtractor
import org.gradle.api.Project
import java.io.File

/**
 * Platform-specific Modifier function discovery.
 */
class PlatformModifierDiscovery(private val project: Project) {
    
    private val platformDetector = PlatformDetector(project)
    private val asmParser = AsmModifierParser(project)
    
    /**
     * Discovers Modifier functions for the detected platform.
     */
    fun discoverModifierFunctions(): List<ModifierFunction> {
        val platform = platformDetector.detectPlatform()
        
        project.logger.lifecycle("Shortify: Detected platform: ${platform.name}")
        
        return when (platform) {
            PlatformDetector.Platform.ANDROID -> discoverAndroidModifiers()
            PlatformDetector.Platform.DESKTOP -> discoverDesktopModifiers()
            PlatformDetector.Platform.WEB -> discoverWebModifiers()
            PlatformDetector.Platform.IOS -> discoverIosModifiers()
            PlatformDetector.Platform.UNKNOWN -> discoverGenericModifiers()
        }
    }
    
    /**
     * Discovers Android Compose Modifier functions.
     */
    private fun discoverAndroidModifiers(): List<ModifierFunction> {
        project.logger.lifecycle("Shortify: Discovering Android Compose modifiers...")
        
        val resolver = DependencyResolver(project)
        val artifacts = resolver.getComposeDependencies()
        
        val extractor = AarExtractor(project)
        val jarFiles = extractor.extractJars(artifacts)
        
        return asmParser.parseModifierFunctions(jarFiles)
    }
    
    /**
     * Discovers Desktop Compose Modifier functions.
     */
    private fun discoverDesktopModifiers(): List<ModifierFunction> {
        project.logger.lifecycle("Shortify: Discovering Desktop Compose modifiers...")
        
        // For Desktop, we need to look for desktop-specific dependencies
        val resolver = DependencyResolver(project)
        val artifacts = resolver.getComposeDependencies()
        
        // Filter for desktop-specific artifacts
        val desktopArtifacts = artifacts.filter { artifact ->
            artifact.name.contains("desktop") || 
            artifact.group.startsWith("org.jetbrains.compose.desktop")
        }
        
        val extractor = AarExtractor(project)
        val jarFiles = extractor.extractJars(desktopArtifacts)
        
        // Also include common Compose artifacts
        val commonArtifacts = artifacts.filter { artifact ->
            !artifact.name.contains("desktop") && 
            !artifact.group.startsWith("org.jetbrains.compose.desktop")
        }
        val commonJarFiles = extractor.extractJars(commonArtifacts)
        
        val allJarFiles = jarFiles + commonJarFiles
        return asmParser.parseModifierFunctions(allJarFiles)
    }
    
    /**
     * Discovers Web Compose Modifier functions.
     */
    private fun discoverWebModifiers(): List<ModifierFunction> {
        project.logger.lifecycle("Shortify: Discovering Web Compose modifiers...")
        
        // For Web, we need to look for web-specific dependencies
        val resolver = DependencyResolver(project)
        val artifacts = resolver.getComposeDependencies()
        
        // Filter for web-specific artifacts
        val webArtifacts = artifacts.filter { artifact ->
            artifact.name.contains("web") || 
            artifact.group.startsWith("org.jetbrains.compose.web")
        }
        
        val extractor = AarExtractor(project)
        val jarFiles = extractor.extractJars(webArtifacts)
        
        // Also include common Compose artifacts
        val commonArtifacts = artifacts.filter { artifact ->
            !artifact.name.contains("web") && 
            !artifact.group.startsWith("org.jetbrains.compose.web")
        }
        val commonJarFiles = extractor.extractJars(commonArtifacts)
        
        val allJarFiles = jarFiles + commonJarFiles
        return asmParser.parseModifierFunctions(allJarFiles)
    }
    
    /**
     * Discovers iOS Compose Modifier functions (future).
     */
    private fun discoverIosModifiers(): List<ModifierFunction> {
        project.logger.lifecycle("Shortify: Discovering iOS Compose modifiers...")
        
        // For iOS, we need to look for iOS-specific dependencies
        val resolver = DependencyResolver(project)
        val artifacts = resolver.getComposeDependencies()
        
        // Filter for iOS-specific artifacts
        val iosArtifacts = artifacts.filter { artifact ->
            artifact.name.contains("ios") || 
            artifact.group.startsWith("org.jetbrains.compose.ios")
        }
        
        val extractor = AarExtractor(project)
        val jarFiles = extractor.extractJars(iosArtifacts)
        
        // Also include common Compose artifacts
        val commonArtifacts = artifacts.filter { artifact ->
            !artifact.name.contains("ios") && 
            !artifact.group.startsWith("org.jetbrains.compose.ios")
        }
        val commonJarFiles = extractor.extractJars(commonArtifacts)
        
        val allJarFiles = jarFiles + commonJarFiles
        return asmParser.parseModifierFunctions(allJarFiles)
    }
    
    /**
     * Discovers generic Modifier functions for unknown platforms.
     */
    private fun discoverGenericModifiers(): List<ModifierFunction> {
        project.logger.lifecycle("Shortify: Discovering generic Compose modifiers...")
        
        // Fallback to standard discovery
        val resolver = DependencyResolver(project)
        val artifacts = resolver.getComposeDependencies()
        
        val extractor = AarExtractor(project)
        val jarFiles = extractor.extractJars(artifacts)
        
        return asmParser.parseModifierFunctions(jarFiles)
    }
    
    /**
     * Gets platform-specific function mappings for functions that differ across platforms.
     */
    fun getPlatformSpecificMappings(): Map<String, String> {
        val platform = platformDetector.detectPlatform()
        
        return when (platform) {
            PlatformDetector.Platform.ANDROID -> getAndroidMappings()
            PlatformDetector.Platform.DESKTOP -> getDesktopMappings()
            PlatformDetector.Platform.WEB -> getWebMappings()
            PlatformDetector.Platform.IOS -> getIosMappings()
            PlatformDetector.Platform.UNKNOWN -> emptyMap()
        }
    }
    
    private fun getAndroidMappings(): Map<String, String> {
        return mapOf(
            "systemBarsPadding" to "sbp",
            "statusBarsPadding" to "stp",
            "navigationBarsPadding" to "nbp",
            "imePadding" to "imepg"
        )
    }
    
    private fun getDesktopMappings(): Map<String, String> {
        return mapOf(
            "onKeyEvent" to "okey",
            "onPreviewKeyEvent" to "opkey"
        )
    }
    
    private fun getWebMappings(): Map<String, String> {
        return mapOf(
            "onClick" to "oclk",
            "onDoubleClick" to "odclk"
        )
    }
    
    private fun getIosMappings(): Map<String, String> {
        return mapOf(
            "safeAreaPadding" to "sap"
        )
    }
}
