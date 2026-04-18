package com.acacia.documentation

import com.acacia.model.ModifierFunction
import com.acacia.mapping.NamingEngine
import org.gradle.api.Project
import java.io.File

/**
 * Generates AI-friendly documentation for Acacia DSL functions.
 */
class AiDocumentationGenerator(private val project: Project) {
    
    private val namingEngine = NamingEngine(project)
    
    /**
     * Generates comprehensive AI documentation for all DSL functions.
     */
    fun generateAiDocumentation(
        functions: List<ModifierFunction>,
        outputDir: File
    ): File {
        val nameMappings = namingEngine.generateShortNames(functions)
        
        val documentation = buildString {
            appendLine("# Acacia DSL AI Documentation")
            appendLine()
            appendLine("This documentation is designed to help AI models understand and generate Acacia DSL code.")
            appendLine("Each function follows consistent patterns for easy AI learning.")
            appendLine()
            appendLine("## Core Principles")
            appendLine("- **Deterministic**: Same names every time")
            appendLine("- **Predictable**: Consistent naming patterns")
            appendLine("- **Token-Efficient**: Short names reduce AI token costs")
            appendLine("- **Semantic**: Names reflect function purpose")
            appendLine()
            appendLine("## Function Categories")
            appendLine()
            
            // Categorize and document functions
            val categorizedFunctions = categorizeFunctions(functions, nameMappings)
            
            categorizedFunctions.forEach { (category, categoryFunctions) ->
                appendLine("### $category")
                appendLine()
                
                categoryFunctions.forEach { function ->
                    val shortName = nameMappings[function.name] ?: function.name
                    val documentation = generateFunctionDocumentation(function, shortName)
                    append(documentation)
                    appendLine()
                }
            }
            
            appendLine("## Common Patterns")
            appendLine()
            appendLine("### Layout Patterns")
            appendLine("```kotlin")
            appendLine("// Centered box with padding")
            appendLine("Modifier.fmw().fmh().p(16.dp)")
            appendLine()
            appendLine("// Full-width container with background")
            appendLine("Modifier.fmw().bg(Color.Blue)")
            appendLine()
            appendLine("// Square with rounded corners")
            appendLine("Modifier.sz(100.dp).bg(Color.Gray)")
            appendLine("```")
            appendLine()
            
            appendLine("### Interaction Patterns")
            appendLine("```kotlin")
            appendLine("// Clickable element")
            appendLine("Modifier.clk { /* onClick */ }")
            appendLine()
            appendLine("// Scrollable content")
            appendLine("Modifier.sc2 { /* scroll state */ }")
            appendLine()
            appendLine("// Draggable item")
            appendLine("Modifier.dg { /* drag state */ }")
            appendLine("```")
            appendLine()
            
            appendLine("### Visual Patterns")
            appendLine("```kotlin")
            appendLine("// Colored background")
            appendLine("Modifier.bg(Color.Red)")
            appendLine()
            appendLine("// Shadow effect")
            appendLine("Modifier.sh(4.dp)")
            appendLine()
            appendLine("// Border")
            appendLine("Modifier.br(1.dp, Color.Black)")
            appendLine("```")
            appendLine()
            
            appendLine("## AI Training Tips")
            appendLine()
            appendLine("1. **Consistent Naming**: Always use the same short names")
            appendLine("2. **Chaining**: Multiple modifiers can be chained")
            appendLine("3. **Order**: Layout modifiers first, then visual, then interaction")
            appendLine("4. **Parameters**: Use appropriate parameter types (Dp, Color, etc.)")
            appendLine()
            
            appendLine("## Complete Function Reference")
            appendLine()
            
            // Add complete reference
            functions.sortedBy { it.name }.forEach { function ->
                val shortName = nameMappings[function.name] ?: function.name
                appendLine("### $shortName")
                appendLine("**Function**: `${function.name}`")
                appendLine("**Parameters**: ${function.parameters.joinToString(", ") { "${it.name}: ${it.type}" }}")
                appendLine("**Usage**: `Modifier.$shortName(${function.parameters.joinToString(", ") { it.name }})`")
                appendLine()
            }
        }
        
        outputDir.mkdirs()
        val documentationFile = File(outputDir, "AI_DOCUMENTATION.md")
        documentationFile.writeText(documentation)
        
        project.logger.lifecycle("Shortify: Generated AI documentation with ${functions.size} functions")
        return documentationFile
    }
    
    /**
     * Categorizes functions for better AI understanding.
     */
    private fun categorizeFunctions(
        functions: List<ModifierFunction>,
        nameMappings: Map<String, String>
    ): Map<String, List<ModifierFunction>> {
        val categories = mutableMapOf<String, MutableList<ModifierFunction>>()
        
        functions.forEach { function ->
            val category = when {
                function.name.contains("padding") -> "Layout - Padding"
                function.name.contains("size") || function.name.contains("width") || function.name.contains("height") -> "Layout - Size"
                function.name.contains("fillMax") || function.name.contains("wrapContent") -> "Layout - Constraints"
                function.name.contains("background") || function.name.contains("border") || function.name.contains("shadow") -> "Visual - Styling"
                function.name.contains("clickable") || function.name.contains("pointer") || function.name.contains("draggable") -> "Interaction"
                function.name.contains("scrollable") || function.name.contains("swipeable") -> "Interaction - Gestures"
                function.name.contains("offset") || function.name.contains("rotate") || function.name.contains("scale") -> "Transform"
                function.name.contains("semantics") || function.name.contains("testTag") -> "Testing & Accessibility"
                function.name.contains("ime") || function.name.contains("systemBars") || function.name.contains("statusBars") -> "Platform - System"
                else -> "Other"
            }
            
            categories.getOrPut(category) { mutableListOf() }.add(function)
        }
        
        return categories
    }
    
    /**
     * Generates documentation for a single function.
     */
    private fun generateFunctionDocumentation(function: ModifierFunction, shortName: String): String {
        val builder = StringBuilder()
        
        builder.appendLine("#### $shortName")
        builder.appendLine()
        builder.appendLine("**Full Name**: `${function.name}`")
        builder.appendLine()
        
        if (function.parameters.isNotEmpty()) {
            builder.appendLine("**Parameters**:")
            function.parameters.forEach { param ->
                builder.appendLine("- `${param.name}`: `${param.type}`")
            }
            builder.appendLine()
        }
        
        builder.appendLine("**AI Usage Example**:")
        builder.appendLine("```kotlin")
        builder.appendLine("Modifier.$shortName(${function.parameters.joinToString(", ") { it.name }})")
        builder.appendLine("```")
        builder.appendLine()
        
        // Add AI-friendly description
        val description = generateAiDescription(function, shortName)
        builder.appendLine("**AI Description**: $description")
        builder.appendLine()
        
        return builder.toString()
    }
    
    /**
     * Generates AI-friendly descriptions for functions.
     */
    private fun generateAiDescription(function: ModifierFunction, shortName: String): String {
        return when {
            function.name.contains("padding") -> "Adds spacing around the element"
            function.name.contains("size") -> "Sets the size of the element"
            function.name.contains("width") -> "Sets the width of the element"
            function.name.contains("height") -> "Sets the height of the element"
            function.name.contains("fillMaxWidth") -> "Makes element fill available width"
            function.name.contains("fillMaxHeight") -> "Makes element fill available height"
            function.name.contains("fillMaxSize") -> "Makes element fill available size"
            function.name.contains("background") -> "Sets background color or brush"
            function.name.contains("border") -> "Adds border around the element"
            function.name.contains("shadow") -> "Adds shadow effect"
            function.name.contains("clickable") -> "Makes element respond to clicks"
            function.name.contains("pointerInput") -> "Handles pointer input events"
            function.name.contains("draggable") -> "Makes element draggable"
            function.name.contains("scrollable") -> "Makes element scrollable"
            function.name.contains("offset") -> "Offsets the element position"
            function.name.contains("rotate") -> "Rotates the element"
            function.name.contains("scale") -> "Scales the element"
            function.name.contains("alpha") -> "Sets transparency"
            function.name.contains("semantics") -> "Adds accessibility information"
            function.name.contains("testTag") -> "Adds test identifier"
            else -> "Applies ${function.name} modifier"
        }
    }
}
