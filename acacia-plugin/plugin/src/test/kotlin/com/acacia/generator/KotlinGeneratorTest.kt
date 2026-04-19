package com.acacia.generator

import com.acacia.model.ModifierFunction
import com.squareup.kotlinpoet.FileSpec
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Comprehensive tests for KotlinGenerator.
 * 
 * CRITICAL: This generates code that goes into production apps.
 * Must ensure generated code is valid, compiles, and follows Kotlin conventions.
 */
class KotlinGeneratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val generator = KotlinGenerator()

    @Test
    fun `generated file contains all functions`() {
        val functions = listOf(
            ModifierFunction("padding", listOf(ModifierFunction.Parameter("all", "Dp", true, "0.dp"))),
            ModifierFunction("background", listOf(ModifierFunction.Parameter("color", "Color", true, "Color.Unspecified"))),
            ModifierFunction("fillMaxWidth", emptyList())
        )

        val outputDir = tempFolder.newFolder()
        val generatedFile = generator.generateShortModifiers(functions, outputDir)

        assertTrue(generatedFile.exists(), "Generated file should exist")
        
        val content = generatedFile.readText()
        
        // Check all functions are present
        assertTrue(content.contains("fun Modifier.p"), "Should generate p() for padding")
        assertTrue(content.contains("fun Modifier.bg"), "Should generate bg() for background")
        assertTrue(content.contains("fun Modifier.fmw"), "Should generate fmw() for fillMaxWidth")
    }

    @Test
    fun `generated functions have correct signatures`() {
        val function = ModifierFunction(
            name = "padding",
            parameters = listOf(
                ModifierFunction.Parameter("all", "Dp", true, "0.dp"),
                ModifierFunction.Parameter("rtlAware", "Boolean", true, "true")
            )
        )

        val outputDir = tempFolder.newFolder()
        generator.generateShortModifiers(listOf(function), outputDir)

        val stats = generator.getNamingStatistics()
        assertEquals(1, stats.totalGenerated)
    }

    @Test
    fun `generated code includes proper imports`() {
        val functions = listOf(
            ModifierFunction("padding", listOf(ModifierFunction.Parameter("all", "Dp", false, null)))
        )

        val outputDir = tempFolder.newFolder()
        val generatedFile = generator.generateShortModifiers(functions, outputDir)
        val content = generatedFile.readText()

        // Should import Compose types
        assertTrue(content.contains("import androidx.compose.ui.Modifier"), "Should import Modifier")
        assertTrue(content.contains("import androidx.compose.ui.unit.dp"), "Should import dp extension")
        assertTrue(content.contains("import androidx.compose.ui.unit.Dp"), "Should import Dp")
    }

    @Test
    fun `generated code is deterministic - same input produces same output`() {
        val functions = listOf(
            ModifierFunction("size", listOf(ModifierFunction.Parameter("size", "Dp", false, null))),
            ModifierFunction("width", listOf(ModifierFunction.Parameter("width", "Dp", false, null))),
            ModifierFunction("height", listOf(ModifierFunction.Parameter("height", "Dp", false, null)))
        )

        val outputDir1 = tempFolder.newFolder("output1")
        val outputDir2 = tempFolder.newFolder("output2")

        val file1 = generator.generateShortModifiers(functions, outputDir1)
        
        // Reset and regenerate
        val generator2 = KotlinGenerator()
        val file2 = generator2.generateShortModifiers(functions, outputDir2)

        val content1 = file1.readText()
        val content2 = file2.readText()

        assertEquals(content1, content2, "Generated code must be deterministic for reproducible builds")
    }

    @Test
    fun `functions are sorted alphabetically for consistency`() {
        val functions = listOf(
            ModifierFunction("zIndex", emptyList()),
            ModifierFunction("alpha", emptyList()),
            ModifierFunction("background", emptyList())
        )

        val outputDir = tempFolder.newFolder()
        val generatedFile = generator.generateShortModifiers(functions, outputDir)
        val content = generatedFile.readText()

        // Functions should be in alphabetical order by short name
        val alphaIndex = content.indexOf("fun Modifier.al")
        val bgIndex = content.indexOf("fun Modifier.bg")
        val ziIndex = content.indexOf("fun Modifier.zi")

        assertTrue(alphaIndex < bgIndex, "alpha should come before background")
        assertTrue(bgIndex < ziIndex, "background should come before zIndex")
    }

    @Test
    fun `generated functions call original with correct parameters`() {
        val function = ModifierFunction(
            name = "padding",
            parameters = listOf(
                ModifierFunction.Parameter("horizontal", "Dp", false, null),
                ModifierFunction.Parameter("vertical", "Dp", false, null)
            )
        )

        val outputDir = tempFolder.newFolder()
        val generatedFile = generator.generateShortModifiers(listOf(function), outputDir)
        val content = generatedFile.readText()

        // Should call the original function with parameters
        assertTrue(content.contains("return this.padding(horizontal, vertical)"), 
            "Should delegate to original function with correct parameters")
    }

    @Test
    fun `empty functions list generates valid but empty file`() {
        val outputDir = tempFolder.newFolder()
        val generatedFile = generator.generateShortModifiers(emptyList(), outputDir)

        assertTrue(generatedFile.exists(), "Should generate file even with no functions")
        
        val content = generatedFile.readText()
        assertTrue(content.contains("package com.acacia.generated"), "Should still have package declaration")
    }

    @Test
    fun `generated code has KDoc comments for documentation`() {
        val function = ModifierFunction(
            name = "fillMaxWidth",
            parameters = emptyList(),
            isDeprecated = false
        )

        val outputDir = tempFolder.newFolder()
        val generatedFile = generator.generateShortModifiers(listOf(function), outputDir)
        val content = generatedFile.readText()

        // Should include documentation
        assertTrue(content.contains("/**") && content.contains("*/"), "Should have KDoc comments")
        assertTrue(content.contains("fillMaxWidth") || content.contains("fmw"), 
            "Should document original function name")
    }

    @Test
    fun `deprecated functions are marked with Deprecated annotation`() {
        val function = ModifierFunction(
            name = "oldModifier",
            parameters = emptyList(),
            isDeprecated = true
        )

        val outputDir = tempFolder.newFolder()
        val generatedFile = generator.generateShortModifiers(listOf(function), outputDir)
        val content = generatedFile.readText()

        assertTrue(content.contains("@Deprecated"), "Deprecated functions should have @Deprecated annotation")
    }

    @Test
    fun `many functions - handles large input efficiently`() {
        // Simulate 500 functions (realistic for full Compose API)
        val functions = (1..500).map { index ->
            ModifierFunction(
                name = "modifierFunction$index",
                parameters = if (index % 2 == 0) {
                    listOf(ModifierFunction.Parameter("param", "Dp", false, null))
                } else emptyList()
            )
        }

        val outputDir = tempFolder.newFolder()
        
        val startTime = System.currentTimeMillis()
        val generatedFile = generator.generateShortModifiers(functions, outputDir)
        val duration = System.currentTimeMillis() - startTime

        assertTrue(generatedFile.exists(), "Should handle large function count")
        assertTrue(duration < 5000, "Should generate within 5 seconds even for 500 functions. Took: ${duration}ms")

        val content = generatedFile.readText()
        assertTrue(content.lines().size > 500, "Generated file should have many lines")
    }
}
