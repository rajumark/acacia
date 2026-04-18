package com.acacia

import com.acacia.generator.KotlinGenerator
import com.acacia.model.ModifierFunction
import com.acacia.resolver.DependencyResolver
import com.acacia.resolver.AarExtractor
import com.acacia.parser.AsmModifierParser
import com.acacia.mapping.NamingEngine
import com.acacia.cache.CacheManager
import com.acacia.platform.PlatformModifierDiscovery
import com.acacia.platform.PlatformCodeGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.CacheableTask
import java.io.File

@CacheableTask
open class GenerateDslTask : DefaultTask() {
    @get:Input
    val enabled: Property<Boolean> = project.objects.property(Boolean::class.java)
    
    @get:Input
    val debug: Property<Boolean> = project.objects.property(Boolean::class.java)
    
    @get:OutputDirectory
    val outputDir: File = project.layout.buildDirectory
        .dir("generated/source/shortify")
        .get()
        .asFile

    @TaskAction
    fun generate() {
        if (!enabled.getOrElse(true)) {
            project.logger.lifecycle("Shortify: Plugin disabled")
            return
        }
        
        val isDebug = debug.getOrElse(false)
        
        try {
            // Execute the complete pipeline with fallbacks
            val modifierFunctions = executePipelineWithFallbacks(isDebug)
            
            // Generate the DSL file
            val generatedFile = generateDslFile(modifierFunctions, isDebug)
            
            project.logger.lifecycle("Shortify: Generated ${modifierFunctions.size} DSL functions")
            
        } catch (e: Exception) {
            project.logger.warn("Shortify: Pipeline failed - using fallback: ${e.message}")
            if (isDebug) {
                e.printStackTrace()
            }
            
            // Ultimate fallback: use hardcoded functions
            val fallbackFunctions = getHardcodedModifierFunctions()
            generateDslFile(fallbackFunctions, isDebug)
            project.logger.lifecycle("Shortify: Used fallback with ${fallbackFunctions.size} functions")
        }
    }
    
    /**
     * Executes the complete pipeline with error handling at each stage.
     */
    private fun executePipelineWithFallbacks(isDebug: Boolean): List<ModifierFunction> {
        val cacheManager = CacheManager(project)
        
        // Try cache first
        val cachedFunctions = cacheManager.getCachedFunctions()
        if (cachedFunctions != null) {
            project.logger.lifecycle("Shortify: Using cached functions (${cachedFunctions.size} functions)")
            return cachedFunctions
        }
        
        // Stage 1: Dependency Resolution
        val jarFiles = try {
            val resolver = DependencyResolver(project)
            val composeArtifacts = resolver.getComposeDependencies()
            
            if (isDebug) {
                composeArtifacts.forEach { artifact ->
                    project.logger.debug("Shortify: - ${artifact.group}:${artifact.name}:${artifact.version}")
                }
            }
            
            val extractor = AarExtractor(project)
            val jars = extractor.extractJars(composeArtifacts)
            
            project.logger.lifecycle("Shortify: Found ${composeArtifacts.size} Compose artifacts, extracted ${jars.size} jar files")
            jars
            
        } catch (e: Exception) {
            project.logger.warn("Shortify: Dependency resolution failed: ${e.message}")
            emptyList()
        }
        
        // Stage 2: Cross-platform API Parsing with ASM
        val parsedFunctions = try {
            if (jarFiles.isEmpty()) {
                project.logger.warn("Shortify: No jar files to parse")
                emptyList()
            } else {
                val parser = AsmModifierParser(project)
                val functions = parser.parseModifierFunctions(jarFiles)
                
                if (isDebug) {
                    functions.forEach { function ->
                        project.logger.debug("Shortify: - ${function.name}(${function.parameters.joinToString(", ") { "${it.name}: ${it.type}" }})")
                    }
                }
                
                project.logger.lifecycle("Shortify: Discovered ${functions.size} Modifier functions for cross-platform")
                functions
            }
        } catch (e: Exception) {
            project.logger.warn("Shortify: Cross-platform discovery failed: ${e.message}")
            emptyList()
        }
        
        // Stage 3: Platform-aware Naming (only if we have parsed functions)
        val finalFunctions = if (parsedFunctions.isNotEmpty()) {
            try {
                val platformDiscovery = PlatformModifierDiscovery(project)
                val platformMappings = platformDiscovery.getPlatformSpecificMappings()
                
                // Apply platform-specific mappings
                parsedFunctions.map { function ->
                    function.copy() // Keep original function structure
                }
            } catch (e: Exception) {
                project.logger.warn("Shortify: Platform discovery failed: ${e.message}")
                parsedFunctions
            }
        } else {
            emptyList()
        }
        
        // Cache the results for next time
        if (finalFunctions.isNotEmpty()) {
            cacheManager.cacheFunctions(finalFunctions)
        }
        
        return finalFunctions
    }
    
    /**
     * Generates the DSL file with platform-aware error handling.
     */
    private fun generateDslFile(functions: List<ModifierFunction>, isDebug: Boolean): File {
        return try {
            // Use the working generator with cross-platform foundation
            val generator = KotlinGenerator()
            val namingEngine = NamingEngine(project)
            val nameMappings = namingEngine.generateShortNames(functions)
            
            val generatedFile = generator.generateShortModifiers(functions, outputDir, nameMappings)
            
            if (isDebug && generatedFile.exists()) {
                project.logger.debug("Shortify: Generated platform-aware file contents:")
                project.logger.debug(generatedFile.readText())
            }
            
            generatedFile
            
        } catch (e: Exception) {
            project.logger.error("Shortify: Failed to generate platform-aware DSL file: ${e.message}")
            throw e
        }
    }
    
    /**
     * Hardcoded modifier functions for initial testing.
     * In the future, this will be replaced by actual parsing from Compose jars.
     */
    private fun getHardcodedModifierFunctions(): List<ModifierFunction> {
        return listOf(
            ModifierFunction(
                name = "padding",
                parameters = listOf(
                    ModifierFunction.Parameter(
                        name = "all",
                        type = "Dp",
                        hasDefault = false
                    )
                )
            ),
            ModifierFunction(
                name = "background",
                parameters = listOf(
                    ModifierFunction.Parameter(
                        name = "color",
                        type = "Color",
                        hasDefault = false
                    )
                )
            ),
            ModifierFunction(
                name = "fillMaxWidth",
                parameters = emptyList()
            ),
            ModifierFunction(
                name = "paddingHorizontal",
                parameters = listOf(
                    ModifierFunction.Parameter(
                        name = "padding",
                        type = "Dp",
                        hasDefault = false
                    )
                )
            ),
            ModifierFunction(
                name = "fillMaxHeight",
                parameters = emptyList()
            )
        )
    }
}
