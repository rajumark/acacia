package com.acacia.cache

import com.acacia.model.ModifierFunction
import org.gradle.api.Project
import java.io.File
import java.util.Properties

/**
 * Manages caching of parsed Modifier functions for performance.
 */
class CacheManager(private val project: Project) {
    
    private val cacheDir = project.layout.buildDirectory.dir("shortify/cache").get().asFile
    private val cacheFile = File(cacheDir, "parsed_functions.properties")
    
    /**
     * Gets cached modifier functions if available and valid.
     */
    fun getCachedFunctions(): List<ModifierFunction>? {
        if (!cacheFile.exists()) {
            return null
        }
        
        try {
            val properties = Properties()
            cacheFile.inputStream().use { properties.load(it) }
            
            val cacheVersion = properties.getProperty("cache_version")
            val currentVersion = getCurrentCacheVersion()
            
            if (cacheVersion != currentVersion) {
                project.logger.debug("Shortify: Cache version mismatch, invalidating cache")
                return null
            }
            
            val functionsData = properties.getProperty("functions")
            if (functionsData.isNullOrEmpty()) {
                return null
            }
            
            return deserializeFunctions(functionsData)
            
        } catch (e: Exception) {
            project.logger.warn("Shortify: Failed to read cache: ${e.message}")
            return null
        }
    }
    
    /**
     * Caches modifier functions for future use.
     */
    fun cacheFunctions(functions: List<ModifierFunction>) {
        try {
            cacheDir.mkdirs()
            
            val properties = Properties()
            properties.setProperty("cache_version", getCurrentCacheVersion())
            properties.setProperty("timestamp", System.currentTimeMillis().toString())
            properties.setProperty("functions", serializeFunctions(functions))
            
            cacheFile.outputStream().use { properties.store(it, "Shortify Plugin Cache") }
            
            project.logger.debug("Shortify: Cached ${functions.size} functions")
            
        } catch (e: Exception) {
            project.logger.warn("Shortify: Failed to write cache: ${e.message}")
        }
    }
    
    /**
     * Clears the cache.
     */
    fun clearCache() {
        if (cacheFile.exists()) {
            cacheFile.delete()
            project.logger.lifecycle("Shortify: Cleared function cache")
        }
    }
    
    /**
     * Gets the current cache version based on plugin version and dependencies.
     */
    private fun getCurrentCacheVersion(): String {
        // Simple version based on plugin version and a hash of dependencies
        val pluginVersion = project.version.toString()
        val dependencyHash = try {
            val resolver = com.acacia.resolver.DependencyResolver(project)
            val artifacts = resolver.getComposeDependencies()
            artifacts.joinToString(",") { "${it.group}:${it.name}:${it.version}" }.hashCode().toString()
        } catch (e: Exception) {
            "unknown"
        }
        
        return "${pluginVersion}_${dependencyHash}"
    }
    
    /**
     * Serializes functions to a string for caching.
     */
    private fun serializeFunctions(functions: List<ModifierFunction>): String {
        return functions.joinToString("|") { function ->
            val params = function.parameters.joinToString(";") { param ->
                "${param.name}:${param.type}:${param.hasDefault}:${param.defaultValue ?: ""}"
            }
            "${function.name}:${function.returnType}:${function.isDeprecated}[$params]"
        }
    }
    
    /**
     * Deserializes functions from a cached string.
     */
    private fun deserializeFunctions(data: String): List<ModifierFunction> {
        return data.split("|").mapNotNull { functionData ->
            try {
                val parts = functionData.split("[")
                val functionInfo = parts[0].split(":")
                val paramsData = parts.getOrNull(1)?.removeSuffix("]") ?: ""
                
                val name = functionInfo[0]
                val returnType = functionInfo[1]
                val isDeprecated = functionInfo[2].toBoolean()
                
                val parameters = if (paramsData.isNotEmpty()) {
                    paramsData.split(";").mapNotNull { paramData ->
                        val paramParts = paramData.split(":")
                        if (paramParts.size >= 3) {
                            com.acacia.model.ModifierFunction.Parameter(
                                name = paramParts[0],
                                type = paramParts[1],
                                hasDefault = paramParts[2].toBoolean(),
                                defaultValue = paramParts.getOrNull(3).takeIf { it?.isNotEmpty() == true }
                            )
                        } else null
                    }
                } else {
                    emptyList()
                }
                
                ModifierFunction(name, parameters, returnType, isDeprecated)
                
            } catch (e: Exception) {
                project.logger.debug("Shortify: Failed to deserialize function: $functionData")
                null
            }
        }
    }
}
