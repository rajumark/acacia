package com.acacia.resolver

import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact
import java.io.File

/**
 * Resolves Compose dependencies from the project's classpath.
 */
class DependencyResolver(private val project: Project) {
    
    /**
     * Gets all Compose dependencies from the project's compile classpath.
     */
    fun getComposeDependencies(): List<Artifact> {
        val artifacts = mutableListOf<Artifact>()
        
        try {
            // Try to get compile classpath configuration
            val configuration = when {
                project.configurations.findByName("compileClasspath") != null -> {
                    project.configurations.getByName("compileClasspath")
                }
                project.configurations.findByName("debugCompileClasspath") != null -> {
                    project.configurations.getByName("debugCompileClasspath")
                }
                else -> {
                    project.logger.warn("No suitable compile classpath configuration found")
                    return emptyList()
                }
            }
            
            configuration.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                if (isComposeDependency(artifact)) {
                    artifacts.add(artifact.toArtifact())
                }
            }
            
        } catch (e: Exception) {
            project.logger.warn("Failed to resolve Compose dependencies: ${e.message}")
        }
        
        return artifacts
    }
    
    /**
     * Checks if an artifact is a Compose dependency.
     */
    private fun isComposeDependency(artifact: ResolvedArtifact): Boolean {
        val moduleVersion = artifact.moduleVersion
        val group = moduleVersion.id.group
        
        return group.startsWith("androidx.compose")
    }
    
    /**
     * Converts ResolvedArtifact to our simplified Artifact model.
     */
    private fun ResolvedArtifact.toArtifact(): Artifact {
        val moduleVersion = this.moduleVersion
        return Artifact(
            group = moduleVersion.id.group,
            name = moduleVersion.id.name,
            version = moduleVersion.id.version,
            file = this.file
        )
    }
    
    /**
     * Simple artifact data model.
     */
    data class Artifact(
        val group: String,
        val name: String,
        val version: String,
        val file: File
    )
}
