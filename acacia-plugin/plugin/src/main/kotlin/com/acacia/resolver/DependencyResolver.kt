package com.acacia.resolver

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import java.io.File

/**
 * Resolves project dependencies and filters Compose libraries.
 */
class DependencyResolver(private val project: Project) {

    /**
     * Resolves all Compose artifacts from the compile classpath.
     */
    fun resolveComposeArtifacts(): List<ComposeArtifact> {
        val artifacts = mutableListOf<ComposeArtifact>()
        
        // Try to get the appropriate configuration
        val config = getCompileClasspathConfiguration()
        
        config?.let { configuration ->
            // Get all resolved files including transitive dependencies
            val resolvedFiles = configuration.resolve()
            
            project.logger.lifecycle("Acacia: Total resolved files: ${resolvedFiles.size}")
            
            resolvedFiles.forEach { file ->
                if (isComposeFile(file)) {
                    val artifact = createComposeArtifact(file)
                    artifacts.add(artifact)
                    project.logger.lifecycle("Acacia: Found Compose artifact: ${file.name}")
                } else if (file.name.contains("foundation")) {
                    project.logger.lifecycle("Acacia: FOUNDATION file not detected as Compose: ${file.name}")
                }
            }
            
            // Also try to get all dependency artifacts directly
            try {
                configuration.incoming.artifacts.artifacts.forEach { artifact ->
                    val file = artifact.file
                    if (isComposeFile(file) && !artifacts.any { it.file.absolutePath == file.absolutePath }) {
                        val composeArtifact = createComposeArtifact(file)
                        artifacts.add(composeArtifact)
                        project.logger.lifecycle("Acacia: Found additional Compose artifact: ${file.name}")
                    }
                }
            } catch (e: Exception) {
                project.logger.debug("Acacia: Could not access incoming artifacts: ${e.message}")
            }
        }
        
        return artifacts.sortedBy { it.name }
    }

    /**
     * Gets the compile classpath configuration, with fallbacks for different Android variants.
     */
    private fun getCompileClasspathConfiguration(): Configuration? {
        val configurations = listOf(
            "compileClasspath",
            "debugCompileClasspath", 
            "releaseCompileClasspath",
            "mainCompileClasspath"
        )
        
        for (configName in configurations) {
            try {
                val config = project.configurations.findByName(configName)
                if (config != null) {
                    project.logger.lifecycle("Acacia: Using configuration '$configName'")
                    return config
                }
            } catch (e: Exception) {
                project.logger.debug("Acacia: Configuration '$configName' not available: ${e.message}")
            }
        }
        
        project.logger.warn("Acacia: No suitable compile classpath configuration found")
        return null
    }

    /**
     * Checks if a file is likely a Compose library based on filename.
     */
    private fun isComposeFile(file: File): Boolean {
        val fileName = file.name.lowercase()
        return fileName.contains("compose") || 
               fileName.contains("androidx") ||
               fileName.contains("ui") ||
               fileName.contains("foundation")
    }

    /**
     * Creates a ComposeArtifact from a file.
     */
    private fun createComposeArtifact(file: File): ComposeArtifact {
        val fileName = file.nameWithoutExtension
        val version = extractVersionFromFileName(fileName)
        val group = inferGroupFromFileName(fileName)
        
        return ComposeArtifact(
            group = group,
            name = fileName,
            version = version,
            file = file
        )
    }

    /**
     * Attempts to extract version from filename.
     */
    private fun extractVersionFromFileName(fileName: String): String {
        // Look for version patterns like "-1.4.3" or "-1.2.0-beta01"
        val versionPattern = Regex("""-([\d.]+(?:-[a-zA-Z0-9]+)?)""")
        val match = versionPattern.find(fileName)
        return match?.groupValues?.get(1) ?: "unknown"
    }

    /**
     * Attempts to infer group from filename.
     */
    private fun inferGroupFromFileName(fileName: String): String {
        return when {
            fileName.contains("androidx") -> "androidx.compose"
            fileName.contains("compose") -> "androidx.compose"
            fileName.contains("foundation") -> "androidx.compose.foundation"
            else -> "unknown"
        }
    }
}

/**
 * Represents a Compose artifact.
 */
data class ComposeArtifact(
    val group: String,
    val name: String,
    val version: String,
    val file: File
) {
    val isAar: Boolean
        get() = file.extension.lowercase() == "aar"
    
    val isJar: Boolean
        get() = file.extension.lowercase() == "jar"
}
