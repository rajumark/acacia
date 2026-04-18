package com.acacia

import com.acacia.generator.KotlinGenerator
import com.acacia.model.ModifierFunction
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
            // Create hardcoded modifier functions for testing
            val modifierFunctions = getHardcodedModifierFunctions()
            println("Shortify: Found ${modifierFunctions.size} functions to generate")
            
            // Generate the ShortModifiers.kt file
            val generator = KotlinGenerator()
            val generatedFile = generator.generateShortModifiers(modifierFunctions, outputDir)
            
            println("Shortify: Generated DSL functions in ${generatedFile.absolutePath}")
            println("Shortify: File exists: ${generatedFile.exists()}")
            
            if (generatedFile.exists()) {
                println("Shortify: File contents:")
                println(generatedFile.readText())
            }
            
        } catch (e: Exception) {
            println("Shortify: Failed to generate DSL functions: ${e.message}")
            e.printStackTrace()
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
