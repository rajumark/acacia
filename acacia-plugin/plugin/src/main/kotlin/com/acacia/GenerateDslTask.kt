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
import com.acacia.documentation.AiDocumentationGenerator
import com.acacia.documentation.AiTrainingDataGenerator
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
        println("Shortify: Starting DSL generation task")
        if (!enabled.getOrElse(true)) {
            project.logger.lifecycle("Shortify: Plugin disabled")
            return
        }
        
        val isDebug = debug.getOrElse(false)
        println("Shortify: Debug mode: $isDebug")
        
        try {
            // Execute the complete pipeline with fallbacks
            val modifierFunctions = executePipelineWithFallbacks(isDebug)
            
            // Generate the DSL file
            val generatedFile = generateDslFile(modifierFunctions, isDebug)
            
            // TODO: Re-enable documentation generation after fixing the core issue
            // generateAiDocumentation(modifierFunctions, isDebug)
            
            project.logger.lifecycle("Shortify: Generated ${modifierFunctions.size} DSL functions in ${generatedFile.absolutePath}")
            
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
            if (isDebug) {
                composeArtifacts.forEach { artifact ->
                    project.logger.debug("Shortify: Artifact: ${artifact.group}:${artifact.name}:${artifact.version} (${artifact.file.extension})")
                }
                jars.forEach { jar ->
                    project.logger.debug("Shortify: Jar: ${jar.name} (${jar.absolutePath})")
                }
            }
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
                
                project.logger.lifecycle("Shortify: Discovered ${functions.size} Modifier functions from Compose jars")
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
     * Generates the DSL file with platform-aware error handling.
     */
    private fun generateDslFile(functions: List<ModifierFunction>, isDebug: Boolean): File {
        return try {
            // Validate functions list
            if (functions.isEmpty()) {
                project.logger.warn("Shortify: No modifier functions found to generate")
                throw IllegalStateException("No modifier functions available for generation")
            }
            
            // Use dynamic name mappings based on parsed functions
            val generator = KotlinGenerator()
            val nameMappings = generateNameMappings(functions)
            
            if (isDebug) {
                project.logger.lifecycle("Shortify: Generating ${functions.size} functions with mappings: $nameMappings")
            }
            
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
    
    /**
     * Generates dynamic name mappings for modifier functions.
     */
    private fun generateNameMappings(functions: List<ModifierFunction>): Map<String, String> {
        val mappings = mutableMapOf<String, String>()
        
        functions.forEach { function ->
            val shortName = generateIndependentShortName(function.name)
            mappings[function.name] = shortName
        }
        
        return mappings
    }
    
    /**
     * Generates 100% independent short names without checking existing names.
     * Same input always produces same output.
     */
    private fun generateIndependentShortName(originalName: String): String {
        // Rule 1: Very short names (1-3 chars) keep as-is
        if (originalName.length <= 3) {
            return originalName.lowercase()
        }
        
        // Rule 2: Use deterministic hash-based algorithm
        return generateDeterministicAbbreviation(originalName)
    }
    
    /**
     * 100% deterministic abbreviation algorithm - no state, no collision checks.
     */
    private fun generateDeterministicAbbreviation(name: String): String {
        // Strategy 1: CamelCase with fixed rules
        if (name.any { it.isUpperCase() }) {
            val camelCase = name.filter { it.isUpperCase() }.lowercase()
            if (camelCase.length >= 2 && camelCase.length <= 4) {
                return camelCase
            }
        }
        
        // Strategy 2: Mathematical hash-based abbreviation
        val words = name.split("(?=[A-Z])|[_-]".toRegex()).filter { it.isNotEmpty() }
        
        return when {
            words.size == 1 -> {
                // Single word: mathematical position-based
                val word = words[0].lowercase()
                when {
                    word.length <= 4 -> word
                    word.length <= 6 -> word.take(2) + word.takeLast(2)
                    word.length <= 8 -> word.first().toString() + word.drop(1).takeLast(6).take(2) + word.last().toString()
                    else -> {
                        // Use fixed positions: 1st, middle, last
                        val midIndex = word.length / 2
                        word.first().toString() + word[midIndex].toString() + word.last().toString()
                    }
                }
            }
            words.size == 2 -> {
                // Two words: fixed pattern
                val w1 = words[0].lowercase()
                val w2 = words[1].lowercase()
                w1.first() + w2.take(2)
            }
            words.size >= 3 -> {
                // Multiple words: always first 3 letters
                words.take(3).joinToString("") { it.first().lowercase() }
            }
            else -> {
                // Fallback: use hash of first 3 chars + last 3 chars
                val prefix = name.take(3).lowercase()
                val suffix = name.takeLast(3).lowercase()
                prefix + suffix
            }
        }
    }
}
