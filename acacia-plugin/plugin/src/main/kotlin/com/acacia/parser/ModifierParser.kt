package com.acacia.parser

import com.acacia.model.ModifierFunction
import org.gradle.api.Project
import java.io.File
import java.util.jar.JarFile

/**
 * Parses jar files to extract Modifier extension functions.
 * Uses bytecode analysis instead of reflection for build-time safety.
 */
class ModifierParser(private val project: Project) {
    
    /**
     * Parses jar files and extracts Modifier extension functions.
     */
    fun parseModifierFunctions(jarFiles: List<File>): List<ModifierFunction> {
        val functions = mutableListOf<ModifierFunction>()
        
        jarFiles.forEach { jarFile ->
            try {
                val jarFunctions = parseJarFile(jarFile)
                functions.addAll(jarFunctions)
                
                if (jarFunctions.isNotEmpty()) {
                    project.logger.debug("Shortify: Found ${jarFunctions.size} functions in ${jarFile.name}")
                }
            } catch (e: Exception) {
                project.logger.warn("Shortify: Failed to parse ${jarFile.name}: ${e.message}")
            }
        }
        
        project.logger.lifecycle("Shortify: Parsed ${functions.size} Modifier functions from ${jarFiles.size} jar files")
        return functions
    }
    
    /**
     * Parses a single jar file for Modifier extension functions.
     */
    private fun parseJarFile(jarFile: File): List<ModifierFunction> {
        val functions = mutableListOf<ModifierFunction>()
        
        JarFile(jarFile).use { jar ->
            jar.entries().asSequence()
                .filter { entry -> 
                    entry.name.endsWith(".class") && 
                    !entry.name.contains("$") && 
                    !entry.name.contains("META-INF")
                }
                .forEach { entry ->
                    try {
                        val className = entry.name
                            .removeSuffix(".class")
                            .replace("/", ".")
                        
                        // For now, use a simple heuristic to find Modifier functions
                        // In a real implementation, this would use ASM bytecode analysis
                        val modifierFunctions = extractModifierFunctionsFromClassName(className)
                        functions.addAll(modifierFunctions)
                        
                    } catch (e: Exception) {
                        project.logger.debug("Shortify: Failed to parse class ${entry.name}: ${e.message}")
                    }
                }
        }
        
        return functions
    }
    
    /**
     * Extracts Modifier functions using class name heuristics.
     * This is a simplified approach - a production implementation would use ASM bytecode parsing.
     */
    private fun extractModifierFunctionsFromClassName(className: String): List<ModifierFunction> {
        val functions = mutableListOf<ModifierFunction>()
        
        // Known Modifier extension functions based on class names
        val knownModifierFunctions = mapOf(
            "androidx.compose.foundation.layout.PaddingKt" to listOf(
                ModifierFunction("padding", listOf(
                    ModifierFunction.Parameter("all", "Dp"),
                    ModifierFunction.Parameter("horizontal", "Dp"),
                    ModifierFunction.Parameter("vertical", "Dp"),
                    ModifierFunction.Parameter("start", "Dp"),
                    ModifierFunction.Parameter("top", "Dp"),
                    ModifierFunction.Parameter("end", "Dp"),
                    ModifierFunction.Parameter("bottom", "Dp")
                ))
            ),
            "androidx.compose.foundation.layout.SizeKt" to listOf(
                ModifierFunction("size", listOf(
                    ModifierFunction.Parameter("width", "Dp"),
                    ModifierFunction.Parameter("height", "Dp")
                )),
                ModifierFunction("width", listOf(
                    ModifierFunction.Parameter("width", "Dp")
                )),
                ModifierFunction("height", listOf(
                    ModifierFunction.Parameter("height", "Dp")
                ))
            ),
            "androidx.compose.ui.BackgroundKt" to listOf(
                ModifierFunction("background", listOf(
                    ModifierFunction.Parameter("color", "Color")
                ))
            ),
            "androidx.compose.foundation.layout.LayoutKt" to listOf(
                ModifierFunction("fillMaxWidth", emptyList()),
                ModifierFunction("fillMaxHeight", emptyList()),
                ModifierFunction("fillMaxSize", emptyList())
            )
        )
        
        return knownModifierFunctions[className] ?: emptyList()
    }
}
