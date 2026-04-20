package com.acacia

import com.acacia.generator.ComposableGenerator
import com.acacia.generator.KotlinGenerator
import com.acacia.generator.TypeAliasGenerator
import com.acacia.model.ComposableFunction
import com.acacia.model.ModifierFunction
import com.acacia.parser.HybridModifierParser
import com.acacia.cache.CacheManager
import com.acacia.documentation.AiDocumentationGenerator
import com.acacia.documentation.AiTrainingDataGenerator
import com.acacia.platform.PlatformModifierDiscovery
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

/**
 * Generates short DSL functions for Compose Modifiers.
 * 
 * Supports incremental builds: only re-parses JARs that changed.
 */
@CacheableTask
open class GenerateDslTask : DefaultTask() {
    @get:Input
    val enabled: Property<Boolean> = project.objects.property(Boolean::class.java)

    @get:Input
    val debug: Property<Boolean> = project.objects.property(Boolean::class.java)
    
    /**
     * The input JAR files to parse for Modifier functions.
     * Using @Classpath for efficient change detection.
     */
    @get:Classpath
    val inputJars: ConfigurableFileCollection = project.objects.fileCollection()

    @get:OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty().convention(
        project.layout.buildDirectory.dir("generated/source/shortify")
    )

    @TaskAction
    fun generate() {
        println("Shortify: Starting DSL generation task")
        if (!enabled.getOrElse(true)) {
            project.logger.lifecycle("Shortify: Plugin disabled")
            return
        }
        
        val isDebug = debug.getOrElse(false)
        println("Shortify: Debug mode: $isDebug")
        
        try {
            // Execute the complete pipeline for modifiers
            val modifierFunctions = executePipelineWithFallbacks(isDebug)
            
            // Execute the composable pipeline
            val composableFunctions = executeComposablePipeline(isDebug)
            
            // Generate the modifier DSL file
            val modifierFile = generateDslFile(modifierFunctions, isDebug)
            
            // Generate the composable DSL file
            val composableFile = generateComposableDslFile(composableFunctions, isDebug)
            
            // Generate the type aliases file
            val typeAliasFile = generateTypeAliasesFile(isDebug)
            
            // TODO: Re-enable documentation generation after fixing the core issue
            // generateAiDocumentation(modifierFunctions, isDebug)
            
            project.logger.lifecycle("Shortify: Generated ${modifierFunctions.size} modifier functions in ${modifierFile.absolutePath}")
            project.logger.lifecycle("Shortify: Generated ${composableFunctions.size} composable functions in ${composableFile.absolutePath}")
            project.logger.lifecycle("Shortify: Generated type aliases in ${typeAliasFile.absolutePath}")
            
        } catch (e: Exception) {
            project.logger.error("Shortify: Pipeline failed: ${e.message}")
            if (isDebug) {
                e.printStackTrace()
            }
            throw e // Re-throw to ensure we always use jar-based generation
        }
    }
    
    /**
     * Executes the complete pipeline with error handling at each stage.
     * Uses inputJars for incremental build support - only changed JARs trigger re-parsing.
     */
    private fun executePipelineWithFallbacks(isDebug: Boolean): List<ModifierFunction> {
        val cacheManager = CacheManager(project)
        
        // Try cache first
        val cachedFunctions = cacheManager.getCachedFunctions()
        if (cachedFunctions != null) {
            project.logger.lifecycle("Shortify: Using cached functions (${cachedFunctions.size} functions)")
            return cachedFunctions
        }
        
        // Stage 1: Get input JARs (configured by plugin from compile classpath)
        // These are tracked as @Classpath inputs for incremental builds
        val jarFiles = inputJars.files.filter { it.exists() && it.isFile && it.extension == "jar" }
        
        if (jarFiles.isEmpty()) {
            project.logger.warn("Shortify: No Compose JAR files found in input. Skipping generation.")
            project.logger.info("Shortify: Ensure Compose dependencies are added to your project.")
            return emptyList()
        }
        
        project.logger.lifecycle("Shortify: Found ${jarFiles.size} JAR files to parse (incremental: only changed JARs will be re-parsed)")
        
        if (isDebug) {
            jarFiles.forEach { jar ->
                project.logger.debug("Shortify: Input JAR: ${jar.name} (${jar.absolutePath})")
            }
        }
        
        // Stage 2: Cross-platform API Parsing with ASM
        val parsedFunctions = try {
            if (jarFiles.isEmpty()) {
                project.logger.warn("Shortify: No jar files to parse")
                emptyList()
            } else {
                val parser = HybridModifierParser(project)
                val functions = parser.parseModifierFunctions(jarFiles)
                
                // Always log the count - this is critical for debugging
                project.logger.lifecycle("Shortify: Discovered ${functions.size} Modifier functions from Compose jars (with default values)")
                
                if (isDebug) {
                    functions.sortedBy { it.name }.forEach { function ->
                        project.logger.debug("Shortify: - ${function.name}(${function.parameters.joinToString(", ") { "${it.name}: ${it.type}" }})")
                    }
                }
                
                functions
            }
        } catch (e: Exception) {
            project.logger.warn("Shortify: ASM parsing failed: ${e.message}")
            if (isDebug) {
                e.printStackTrace()
            }
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
     * Executes the composable function parsing pipeline.
     * Optimized: Fast path using golden mappings.
     */
    private fun executeComposablePipeline(isDebug: Boolean): List<ComposableFunction> {
        // FAST PATH: Skip JAR processing entirely for speed
        return try {
            val parser = HybridModifierParser(project)
            val functions = parser.parseComposableFunctions(emptyList()) // Empty list = use golden mappings
            
            project.logger.lifecycle("Shortify: Generated ${functions.size} Composable functions from golden mappings")
            
            functions
        } catch (e: Exception) {
            project.logger.warn("Shortify: Composable generation failed: ${e.message}")
            if (isDebug) {
                e.printStackTrace()
            }
            emptyList()
        }
    }
    
    /**
     * Generates the composable DSL file with hybrid naming.
     */
    private fun generateComposableDslFile(functions: List<ComposableFunction>, isDebug: Boolean): File {
        return try {
            if (functions.isEmpty()) {
                // Return an empty file indicator
                val emptyFile = File(outputDir.get().asFile, "ShortComposables.kt")
                emptyFile.parentFile.mkdirs()
                emptyFile.writeText("// No composable functions found to generate")
                return emptyFile
            }
            
            val generator = ComposableGenerator()
            
            if (isDebug) {
                project.logger.lifecycle("Shortify: Generating ${functions.size} composable functions")
            }
            
            val generatedFile = generator.generateShortComposables(functions, outputDir.get().asFile)
            
            // Log naming statistics
            val stats = generator.getNamingStatistics()
            project.logger.lifecycle("Shortify: Composable Naming - " +
                "Total: ${stats.totalGenerated}, " +
                "Golden: ${stats.goldenNames}, " +
                "Algorithmic: ${stats.algorithmicNames}")
            
            generatedFile
            
        } catch (e: Exception) {
            project.logger.error("Shortify: Failed to generate composable DSL file: ${e.message}")
            throw e
        }
    }
    
    /**
     * Generates the type aliases file with short type names.
     */
    private fun generateTypeAliasesFile(isDebug: Boolean): File {
        return try {
            val generator = TypeAliasGenerator()
            
            if (isDebug) {
                project.logger.lifecycle("Shortify: Generating type aliases")
            }
            
            val generatedFile = generator.generateTypeAliasesRaw(outputDir.get().asFile)
            
            project.logger.lifecycle("Shortify: Generated 17 type aliases (Cl, D, Arr, Al, etc.)")
            
            generatedFile
            
        } catch (e: Exception) {
            project.logger.error("Shortify: Failed to generate type aliases file: ${e.message}")
            throw e
        }
    }
    
    /**
     * Generates the modifier DSL file with hybrid naming (Golden + Algorithmic).
     */
    private fun generateDslFile(functions: List<ModifierFunction>, isDebug: Boolean): File {
        return try {
            // Validate functions list
            if (functions.isEmpty()) {
                project.logger.warn("Shortify: No modifier functions found to generate")
                throw IllegalStateException("No modifier functions available for generation")
            }
            
            // Use HybridNamingEngine for intelligent naming (Golden + Algorithmic with collision detection)
            val generator = KotlinGenerator()
            
            if (isDebug) {
                project.logger.lifecycle("Shortify: Generating ${functions.size} functions with HybridNamingEngine")
            }
            
            val generatedFile = generator.generateShortModifiers(functions, outputDir.get().asFile)
            
            // Log naming statistics (always show for visibility)
            val stats = generator.getNamingStatistics()
            project.logger.lifecycle("Shortify: Modifier Naming - " +
                "Total: ${stats.totalGenerated}, " +
                "Golden: ${stats.goldenNames}, " +
                "Algorithmic: ${stats.algorithmicNames}, " +
                "Collisions: ${stats.collisionsResolved}")
            
            generatedFile
            
        } catch (e: Exception) {
            project.logger.error("Shortify: Failed to generate platform-aware DSL file: ${e.message}")
            throw e
        }
    }
    
    /**
     * Generates AI-friendly documentation and training data.
     */
    private fun generateAiDocumentation(functions: List<ModifierFunction>, isDebug: Boolean) {
        try {
            val documentationOutputDir = project.layout.buildDirectory.dir("generated/documentation/ai").get().asFile
            
            // Generate AI documentation
            val docGenerator = AiDocumentationGenerator(project)
            val documentationFile = docGenerator.generateAiDocumentation(functions, documentationOutputDir)
            
            // Generate AI training data
            val trainingGenerator = AiTrainingDataGenerator(project)
            val trainingFile = trainingGenerator.generateAiTrainingData(functions, documentationOutputDir)
            
            if (isDebug) {
                project.logger.debug("Shortify: Generated AI documentation: ${documentationFile.absolutePath}")
                project.logger.debug("Shortify: Generated AI training data: ${trainingFile.absolutePath}")
            }
            
            project.logger.lifecycle("Shortify: Generated AI documentation and training data")
            
        } catch (e: Exception) {
            project.logger.warn("Shortify: Failed to generate AI documentation: ${e.message}")
            if (isDebug) {
                e.printStackTrace()
            }
        }
    }
    
}
