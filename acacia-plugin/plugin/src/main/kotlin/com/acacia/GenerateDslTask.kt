package com.acacia

import com.acacia.generator.KotlinGenerator
import com.acacia.model.ModifierFunction
import com.acacia.resolver.DependencyResolver
import com.acacia.resolver.AarExtractor
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
        println("=== SHORTIFY TASK STARTED ===")
        
        if (!enabled.getOrElse(true)) {
            println("Shortify plugin is disabled")
            return
        }
        
        val isDebug = debug.getOrElse(false)
        println("Shortify: Generating DSL in debug mode: $isDebug")
        println("Shortify: Output directory: ${outputDir.absolutePath}")
        
        try {
            // Part 1: Dependency Resolution
            println("Shortify: Resolving Compose dependencies...")
            val resolver = DependencyResolver(project)
            val composeArtifacts = resolver.getComposeDependencies()
            println("Shortify: Found ${composeArtifacts.size} Compose artifacts")
            
            if (isDebug) {
                composeArtifacts.forEach { artifact ->
                    println("Shortify: - ${artifact.group}:${artifact.name}:${artifact.version}")
                }
            }
            
            // Extract jar files from artifacts
            val extractor = AarExtractor(project)
            val jarFiles = extractor.extractJars(composeArtifacts)
            println("Shortify: Extracted ${jarFiles.size} jar files")
            
            if (isDebug) {
                jarFiles.forEach { jar ->
                    println("Shortify: - ${jar.name} (${jar.length()} bytes)")
                }
            }
            
            // For now, still use hardcoded functions until we implement parsing
            val modifierFunctions = getHardcodedModifierFunctions()
            println("Shortify: Using ${modifierFunctions.size} hardcoded functions (parsing coming in Part 2)")
            
            // Generate the ShortModifiers.kt file
            val generator = KotlinGenerator()
            val generatedFile = generator.generateShortModifiers(modifierFunctions, outputDir)
            
            println("Shortify: Generated DSL functions in ${generatedFile.absolutePath}")
            println("Shortify: File exists: ${generatedFile.exists()}")
            
        } catch (e: Exception) {
            println("Shortify: Failed to generate DSL functions: ${e.message}")
            if (isDebug) {
                e.printStackTrace()
            }
        }
        
        println("=== SHORTIFY TASK COMPLETED ===")
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
