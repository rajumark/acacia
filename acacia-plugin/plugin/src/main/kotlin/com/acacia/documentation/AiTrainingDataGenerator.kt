package com.acacia.documentation

import com.acacia.model.ModifierFunction
import com.acacia.mapping.NamingEngine
import org.gradle.api.Project
import java.io.File

/**
 * Generates AI training data and examples for Acacia DSL.
 */
class AiTrainingDataGenerator(private val project: Project) {
    
    private val namingEngine = NamingEngine(project)
    
    /**
     * Generates AI training data for common UI patterns.
     */
    fun generateAiTrainingData(
        functions: List<ModifierFunction>,
        outputDir: File
    ): File {
        val nameMappings = namingEngine.generateShortNames(functions)
        
        val trainingData = buildString {
            appendLine("# Acacia DSL AI Training Data")
            appendLine()
            appendLine("This file contains training examples for AI models to learn Acacia DSL patterns.")
            appendLine("Each example shows natural language description and corresponding DSL code.")
            appendLine()
            
            appendLine("## Basic Layout Patterns")
            appendLine()
            
            // Basic layout examples
            addTrainingExample(
                "A centered box with 16dp padding",
                "Modifier.fmw().fmh().p(16.dp)",
                "Layout: Centered with padding"
            )
            
            addTrainingExample(
                "A full-width container with 8dp padding",
                "Modifier.fmw().p(8.dp)",
                "Layout: Full width with padding"
            )
            
            addTrainingExample(
                "A square 100dp by 100dp",
                "Modifier.sz(100.dp)",
                "Layout: Square size"
            )
            
            addTrainingExample(
                "A rectangle 200dp wide and 100dp tall",
                "Modifier.w(200.dp).h(100.dp)",
                "Layout: Rectangle size"
            )
            
            addTrainingExample(
                "A container that fills max width but wraps content height",
                "Modifier.fmw().wch()",
                "Layout: Fill width, wrap height"
            )
            
            appendLine("## Visual Styling Patterns")
            appendLine()
            
            // Visual styling examples
            addTrainingExample(
                "A blue background",
                "Modifier.bg(Color.Blue)",
                "Visual: Solid color background"
            )
            
            addTrainingExample(
                "A red background with 4dp padding",
                "Modifier.bg(Color.Red).p(4.dp)",
                "Visual: Background with padding"
            )
            
            addTrainingExample(
                "A gray background with rounded corners",
                "Modifier.bg(Color.Gray).cp(RoundedCornerShape(8.dp))",
                "Visual: Background with rounded corners"
            )
            
            addTrainingExample(
                "A border with 1dp width and black color",
                "Modifier.br(1.dp, Color.Black)",
                "Visual: Simple border"
            )
            
            addTrainingExample(
                "A shadow with 4dp elevation",
                "Modifier.sh(4.dp)",
                "Visual: Shadow effect"
            )
            
            appendLine("## Interaction Patterns")
            appendLine()
            
            // Interaction examples
            addTrainingExample(
                "A clickable button",
                "Modifier.clk { /* onClick */ }",
                "Interaction: Clickable"
            )
            
            addTrainingExample(
                "A clickable element with ripple effect",
                "Modifier.clk(onClick = { /* handle click */ })",
                "Interaction: Clickable with ripple"
            )
            
            addTrainingExample(
                "A draggable element",
                "Modifier.dg { /* drag state */ }",
                "Interaction: Draggable"
            )
            
            addTrainingExample(
                "A scrollable list",
                "Modifier.sc2 { /* scroll state */ }",
                "Interaction: Scrollable"
            )
            
            addTrainingExample(
                "A swipeable card",
                "Modifier.sw { /* swipe state */ }",
                "Interaction: Swipeable"
            )
            
            appendLine("## Complex UI Patterns")
            appendLine()
            
            // Complex examples
            addTrainingExample(
                "A centered card with white background, shadow, and 16dp padding",
                "Modifier.fmw().fmh().bg(Color.White).sh(8.dp).p(16.dp)",
                "Complex: Card with shadow"
            )
            
            addTrainingExample(
                "A full-width button with blue background, rounded corners, and clickable",
                "Modifier.fmw().h(48.dp).bg(Color.Blue).cp(RoundedCornerShape(8.dp)).clk { /* onClick */ }",
                "Complex: Full-width button"
            )
            
            addTrainingExample(
                "A circular avatar with background and border",
                "Modifier.sz(64.dp).bg(Color.Gray).br(2.dp, Color.White).cp(CircleShape)",
                "Complex: Circular avatar"
            )
            
            addTrainingExample(
                "A scrollable container with padding and background",
                "Modifier.sc2 { /* scroll state */ }.bg(Color.LightGray).p(16.dp)",
                "Complex: Scrollable container"
            )
            
            appendLine("## Responsive Patterns")
            appendLine()
            
            // Responsive examples
            addTrainingExample(
                "A responsive element that fills max width on large screens",
                "Modifier.fmw()",
                "Responsive: Fill width"
            )
            
            addTrainingExample(
                "An element with minimum width of 200dp",
                "Modifier.rw(200.dp)",
                "Responsive: Minimum width"
            )
            
            addTrainingExample(
                "An element with size constraints",
                "Modifier.szIn(100.dp, 200.dp)",
                "Responsive: Size constraints"
            )
            
            appendLine("## Accessibility Patterns")
            appendLine()
            
            // Accessibility examples
            addTrainingExample(
                "A button with accessibility description",
                "Modifier.semantics { this.contentDescription = \"Submit button\" }",
                "Accessibility: Content description"
            )
            
            addTrainingExample(
                "A testable element with test tag",
                "Modifier.tt(\"submit_button\")",
                "Accessibility: Test tag"
            )
            
            addTrainingExample(
                "A clickable element with role",
                "Modifier.semantics { this.role = Role.Button }",
                "Accessibility: Role"
            )
            
            appendLine("## Animation Patterns")
            appendLine()
            
            // Animation examples
            addTrainingExample(
                "An element with alpha animation",
                "Modifier.al(0.5f)",
                "Animation: Alpha transparency"
            )
            
            addTrainingExample(
                "A rotated element",
                "Modifier.rt(45f)",
                "Animation: Rotation"
            )
            
            addTrainingExample(
                "A scaled element",
                "Modifier.sl(1.2f)",
                "Animation: Scale"
            )
            
            appendLine("## Platform-Specific Patterns")
            appendLine()
            
            // Platform-specific examples
            addTrainingExample(
                "An element with system bars padding",
                "Modifier.sbp()",
                "Platform: System bars padding"
            )
            
            addTrainingExample(
                "An element with status bar padding",
                "Modifier.stp()",
                "Platform: Status bar padding"
            )
            
            addTrainingExample(
                "An element with navigation bar padding",
                "Modifier.nbp()",
                "Platform: Navigation bar padding"
            )
            
            addTrainingExample(
                "An element with safe drawing padding",
                "Modifier.sdp()",
                "Platform: Safe drawing padding"
            )
            
            appendLine("## Token Efficiency Examples")
            appendLine()
            appendLine("These examples show how to achieve common UI with minimal tokens:")
            appendLine()
            
            addTrainingExample(
                "Simple card (5 tokens)",
                "Modifier.bg(Color.White).p(16.dp)",
                "Efficient: Basic card"
            )
            
            addTrainingExample(
                "Button (4 tokens)",
                "Modifier.bg(Color.Blue).clk { }",
                "Efficient: Simple button"
            )
            
            addTrainingExample(
                "Container (3 tokens)",
                "Modifier.fmw().p(8.dp)",
                "Efficient: Container"
            )
            
            addTrainingExample(
                "Spacer (2 tokens)",
                "Modifier.sz(8.dp)",
                "Efficient: Spacer"
            )
            
            appendLine("## Chaining Best Practices")
            appendLine()
            appendLine("Recommended order for chaining modifiers:")
            appendLine("1. Layout modifiers (size, padding, constraints)")
            appendLine("2. Visual modifiers (background, border, shadow)")
            appendLine("3. Transform modifiers (offset, rotate, scale)")
            appendLine("4. Interaction modifiers (clickable, draggable, scrollable)")
            appendLine("5. Accessibility modifiers (semantics, testTag)")
            appendLine()
            
            appendLine("### Example of proper chaining:")
            appendLine("```kotlin")
            appendLine("Modifier")
            appendLine("    .fmw()           // Layout: fill max width")
            appendLine("    .h(48.dp)       // Layout: set height")
            appendLine("    .bg(Color.Blue) // Visual: background")
            appendLine("    .cp(8.dp)       // Visual: rounded corners")
            appendLine("    .clk { }        // Interaction: clickable")
            appendLine("    .tt(\"button\")   // Accessibility: test tag")
            appendLine("```")
        }
        
        outputDir.mkdirs()
        val trainingFile = File(outputDir, "AI_TRAINING_DATA.md")
        trainingFile.writeText(trainingData)
        
        project.logger.lifecycle("Shortify: Generated AI training data with comprehensive examples")
        return trainingFile
    }
    
    private fun StringBuilder.addTrainingExample(
        description: String,
        code: String,
        category: String
    ) {
        appendLine("### $description")
        appendLine()
        appendLine("**Category**: $category")
        appendLine()
        appendLine("```kotlin")
        appendLine(code)
        appendLine("```")
        appendLine()
    }
}
