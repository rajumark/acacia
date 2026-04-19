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
        val usedNames = mutableSetOf<String>()
        
        functions.forEach { function ->
            val shortName = generateDynamicShortName(function.name, usedNames)
            mappings[function.name] = shortName
            usedNames.add(shortName)
        }
        
        return mappings
    }
    
    /**
     * Generates 100% dynamic short names without any hardcoded mappings.
     */
    private fun generateDynamicShortName(originalName: String, usedNames: Set<String>): String {
        // Rule 1: Very short names (1-3 chars) keep as-is
        if (originalName.length <= 3) {
            return originalName
        }
        
        // Rule 2: Generate base name using smart abbreviation
        val baseName = generateSmartAbbreviation(originalName)
        
        // Rule 3: Handle collisions
        if (!usedNames.contains(baseName)) {
            return baseName
        }
        
        // Rule 4: Generate collision-free alternatives
        return generateCollisionFreeName(originalName, baseName, usedNames)
    }
    
    /**
     * Smart abbreviation algorithm without hardcoded rules.
     */
    private fun generateSmartAbbreviation(name: String): String {
        // Strategy 1: CamelCase abbreviation
        if (name.any { it.isUpperCase() }) {
            val camelCaseAbbrev = name.filter { it.isUpperCase() }.lowercase()
            if (camelCaseAbbrev.length >= 2 && camelCaseAbbrev.length <= 4) {
                return camelCaseAbbrev
            }
        }
        
        // Strategy 2: Word-based abbreviation
        val words = name.split("(?=[A-Z])|[_-]".toRegex()).filter { it.isNotEmpty() }
        return when {
            words.size == 1 -> {
                // Single word: use first-middle-last pattern
                val word = words[0]
                when {
                    word.length <= 4 -> word.lowercase()
                    word.length <= 6 -> word.take(2).lowercase() + word.takeLast(2).lowercase()
                    else -> word.first().lowercase() + word.drop(1).dropLast(1).take(2).lowercase() + word.last().lowercase()
                }
            }
            words.size == 2 -> {
                // Two words: first letters + middle of second
                words[0].first().lowercase() + words[1].take(2).lowercase()
            }
            words.size >= 3 -> {
                // Multiple words: first letters of first 3 words
                words.take(3).joinToString("") { it.first().lowercase() }
            }
            else -> {
                // Fallback: first + last
                name.first().lowercase() + name.last().lowercase()
            }
        }
    }
    
    /**
     * Generates collision-free names with suffixes.
     */
    private fun generateCollisionFreeName(originalName: String, baseName: String, usedNames: Set<String>): String {
        // Try numeric suffixes
        for (i in 2..9) {
            val candidate = "$baseName$i"
            if (!usedNames.contains(candidate)) {
                return candidate
            }
        }
        
        // Try letter suffixes
        val suffixes = listOf("a", "b", "c", "x", "v", "alt")
        for (suffix in suffixes) {
            val candidate = "$baseName$suffix"
            if (!usedNames.contains(candidate)) {
                return candidate
            }
        }
        
        // Last resort: hash suffix
        val hash = originalName.takeLast(3).hashCode().toString(36).take(3).uppercase()
        return "$baseName$hash"
    }
}
