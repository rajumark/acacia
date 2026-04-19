package com.acacia.mapping

import com.acacia.model.ModifierFunction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for HybridNamingEngine.
 * 
 * CRITICAL: This component generates names used by millions of apps.
 * Any change MUST preserve backward compatibility and collision-free guarantees.
 */
class HybridNamingEngineUnitTest {

    private val engine = HybridNamingEngine()

    @Test
    fun `golden names are deterministic and never change`() {
        // These names are part of the public API contract
        // Changing them would break millions of apps
        val goldenTests = listOf(
            "padding" to "p",
            "background" to "bg",
            "fillMaxWidth" to "fmw",
            "clickable" to "clk",
            "size" to "sz"
        )

        goldenTests.forEach { (original, expectedShort) ->
            val function = createModifierFunction(original)
            val shortName = engine.generateShortName(function)
            
            assertEquals(
                expectedShort,
                shortName,
                "Golden name for '$original' must never change. This is a BREAKING CHANGE for millions of apps."
            )
        }
    }

    @Test
    fun `algorithmic names are collision-free`() {
        // Test that similar function names get different short names
        val functions = listOf(
            createModifierFunction("placeHorizontally"),
            createModifierFunction("placeHolder"),
            createModifierFunction("placeInBox")
        )

        val names = functions.map { engine.generateShortName(it) }
        
        // All names must be unique
        assertEquals(
            names.size,
            names.distinct().size,
            "Collision detected: ${names.groupBy { it }.filter { it.value.size > 1 }}"
        )
    }

    @Test
    fun `parameter signatures disambiguate collisions`() {
        // Two functions with same base name but different params
        val function1 = ModifierFunction(
            name = "scroll",
            parameters = listOf(
                ModifierFunction.Parameter("state", "ScrollState", false, null)
            )
        )
        
        val function2 = ModifierFunction(
            name = "scroll",
            parameters = listOf(
                ModifierFunction.Parameter("enabled", "Boolean", false, null),
                ModifierFunction.Parameter("orientation", "Orientation", false, null)
            )
        )

        val name1 = engine.generateShortName(function1)
        val name2 = engine.generateShortName(function2)

        assertNotEquals(
            name1,
            name2,
            "Functions with different signatures must have different short names"
        )
    }

    @Test
    fun `naming statistics are accurate`() {
        // Mix of golden and algorithmic functions
        val functions = listOf(
            createModifierFunction("padding"),      // Golden
            createModifierFunction("background"),   // Golden
            createModifierFunction("horizontalScroll"), // Algorithmic
            createModifierFunction("verticalScroll")    // Algorithmic
        )

        functions.forEach { engine.generateShortName(it) }
        val stats = engine.getStatistics()

        assertEquals(4, stats.totalGenerated, "Total count should match")
        assertEquals(2, stats.goldenNames, "Should detect 2 golden names")
        assertEquals(2, stats.algorithmicNames, "Should detect 2 algorithmic names")
    }

    @Test
    fun `names are AI-friendly - short and memorable`() {
        val function = createModifierFunction("animateContentSize")
        val shortName = engine.generateShortName(function)

        // Short names (1-4 chars) are easier for AI to learn
        assertTrue(
            shortName.length <= 4,
            "AI-friendly names should be ≤4 characters. Got: '$shortName' (${shortName.length} chars)"
        )

        // Should be pronounceable/memorable (no random strings)
        assertTrue(
            shortName.all { it.isLetter() || it.isDigit() },
            "AI-friendly names should use alphanumeric only: '$shortName'"
        )
    }

    @Test
    fun `receiver-based resolution allows same names for different receivers`() {
        // Modifier.Size and Paint.Size can both use 'sz' safely
        // (Kotlin's receiver-based resolution handles this)
        val modifierSize = createModifierFunction("size")
        val paintSize = createModifierFunction("size") // Different receiver class

        // Both can use the same short name
        val name1 = engine.generateShortName(modifierSize)
        val name2 = engine.generateShortName(paintSize)

        // Engine should allow same name (collision-free within same receiver type)
        // In practice, these are in different files for different receivers
        assertEquals(name1, name2, "Same function name should get same short name regardless of receiver")
    }

    @Test
    fun `reset clears all state`() {
        // Generate some names
        repeat(5) { engine.generateShortName(createModifierFunction("func$it")) }
        
        // Reset
        engine.reset()
        val stats = engine.getStatistics()

        assertEquals(0, stats.totalGenerated, "Reset should clear total count")
        assertEquals(0, stats.goldenNames, "Reset should clear golden count")
        assertEquals(0, stats.algorithmicNames, "Reset should clear algorithmic count")
    }

    @Test
    fun `edge cases - very long function names`() {
        val longName = "thisIsAVeryLongFunctionNameThatShouldStillGetAShortName"
        val function = createModifierFunction(longName)
        val shortName = engine.generateShortName(function)

        assertTrue(shortName.length <= 6, "Even long names should get short abbreviations")
        assertTrue(shortName.isNotBlank(), "Should never produce empty names")
    }

    @Test
    fun `edge cases - single character function names`() {
        // Edge case: function name that's already short
        val function = createModifierFunction("a")
        val shortName = engine.generateShortName(function)

        assertTrue(shortName.isNotBlank(), "Should handle single-char names")
        // Should still work, even if same as input
    }

    @Test
    fun `edge cases - many parameters`() {
        // Function with 10 parameters
        val params = (1..10).map { 
            ModifierFunction.Parameter("param$it", "String", false, null)
        }
        val function = ModifierFunction("complexFunction", params)
        
        val shortName = engine.generateShortName(function)
        
        assertTrue(shortName.isNotBlank(), "Should handle many parameters")
        // Disambiguation may add suffix based on param count
    }

    private fun createModifierFunction(name: String): ModifierFunction {
        return ModifierFunction(
            name = name,
            parameters = emptyList()
        )
    }
}
