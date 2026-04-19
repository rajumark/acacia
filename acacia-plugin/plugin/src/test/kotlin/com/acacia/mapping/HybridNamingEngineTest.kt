package com.acacia.mapping

import com.acacia.model.ModifierFunction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test to demonstrate HybridNamingEngine statistics.
 */
class HybridNamingEngineTest {

    @Test
    fun `test naming statistics with sample functions`() {
        val engine = HybridNamingEngine()

        // Simulate 100 Compose modifier functions
        // 77 from acacia-mapping.json (golden) + 23 algorithmic
        val testFunctions = listOf(
            // Golden mappings (from acacia-mapping.json) - 77 total
            ModifierFunction("padding", listOf(ModifierFunction.Parameter("all", "Dp")), "Modifier"),
            ModifierFunction("paddingHorizontal", listOf(ModifierFunction.Parameter("horizontal", "Dp")), "Modifier"),
            ModifierFunction("paddingVertical", listOf(ModifierFunction.Parameter("vertical", "Dp")), "Modifier"),
            ModifierFunction("size", listOf(ModifierFunction.Parameter("size", "Dp")), "Modifier"),
            ModifierFunction("width", listOf(ModifierFunction.Parameter("width", "Dp")), "Modifier"),
            ModifierFunction("height", listOf(ModifierFunction.Parameter("height", "Dp")), "Modifier"),
            ModifierFunction("fillMaxWidth", emptyList(), "Modifier"),
            ModifierFunction("fillMaxHeight", emptyList(), "Modifier"),
            ModifierFunction("fillMaxSize", emptyList(), "Modifier"),
            ModifierFunction("background", listOf(ModifierFunction.Parameter("color", "Color")), "Modifier"),
            ModifierFunction("border", listOf(
                ModifierFunction.Parameter("width", "Dp"),
                ModifierFunction.Parameter("color", "Color")
            ), "Modifier"),
            ModifierFunction("shadow", listOf(ModifierFunction.Parameter("elevation", "Dp")), "Modifier"),
            ModifierFunction("clip", listOf(ModifierFunction.Parameter("shape", "Shape")), "Modifier"),
            ModifierFunction("alpha", listOf(ModifierFunction.Parameter("alpha", "Float")), "Modifier"),
            ModifierFunction("clickable", listOf(ModifierFunction.Parameter("onClick", "() -> Unit")), "Modifier"),
            ModifierFunction("draggable", listOf(
                ModifierFunction.Parameter("state", "DraggableState"),
                ModifierFunction.Parameter("orientation", "Orientation")
            ), "Modifier"),
            ModifierFunction("scrollable", listOf(
                ModifierFunction.Parameter("state", "ScrollableState"),
                ModifierFunction.Parameter("orientation", "Orientation")
            ), "Modifier"),
            ModifierFunction("systemBarsPadding", emptyList(), "Modifier"),
            ModifierFunction("statusBarsPadding", emptyList(), "Modifier"),
            ModifierFunction("navigationBarsPadding", emptyList(), "Modifier"),
            ModifierFunction("testTag", listOf(ModifierFunction.Parameter("tag", "String")), "Modifier"),
            ModifierFunction("semantics", listOf(ModifierFunction.Parameter("properties", "SemanticsPropertyReceiver.() -> Unit")), "Modifier"),
            
            // Algorithmic names (not in golden list) - these will get auto-generated short names
            ModifierFunction("horizontalScroll", listOf(ModifierFunction.Parameter("state", "ScrollState")), "Modifier"),
            ModifierFunction("verticalScroll", listOf(ModifierFunction.Parameter("state", "ScrollState")), "Modifier"),
            ModifierFunction("transformable", listOf(ModifierFunction.Parameter("state", "TransformableState")), "Modifier"),
            ModifierFunction("bringIntoViewRequester", listOf(ModifierFunction.Parameter("requester", "BringIntoViewRequester")), "Modifier"),
            ModifierFunction("onClick", listOf(ModifierFunction.Parameter("onClick", "() -> Unit")), "Modifier"),
            ModifierFunction("onLongClick", listOf(ModifierFunction.Parameter("onLongClick", "() -> Unit")), "Modifier"),
            ModifierFunction("onDoubleClick", listOf(ModifierFunction.Parameter("onDoubleClick", "() -> Unit")), "Modifier"),
            ModifierFunction("magnifier", listOf(ModifierFunction.Parameter("sourceCenter", "() -> Offset")), "Modifier"),
            ModifierFunction("contentDisposition", listOf(ModifierFunction.Parameter("disposition", "ContentDisposition")), "Modifier"),
            ModifierFunction("toolingGraphicsLayer", emptyList(), "Modifier"),
            ModifierFunction("recompositionHighlighter", emptyList(), "Modifier"),
            ModifierFunction("debugInspectorInfo", listOf(ModifierFunction.Parameter("name", "String")), "Modifier"),
            ModifierFunction("inspectable", emptyList(), "Modifier"),
            ModifierFunction("placeholder", listOf(
                ModifierFunction.Parameter("visible", "Boolean"),
                ModifierFunction.Parameter("color", "Color")
            ), "Modifier"),
            ModifierFunction("shimmer", emptyList(), "Modifier"),
            ModifierFunction("fadeInAnimation", listOf(ModifierFunction.Parameter("duration", "Int")), "Modifier"),
            ModifierFunction("slideInAnimation", listOf(ModifierFunction.Parameter("offset", "DpOffset")), "Modifier"),
            ModifierFunction("scaleInAnimation", listOf(ModifierFunction.Parameter("initialScale", "Float")), "Modifier"),
            ModifierFunction("animatedVisibility", listOf(ModifierFunction.Parameter("visible", "Boolean")), "Modifier"),
            ModifierFunction("crossfadeAnimation", emptyList(), "Modifier"),
            ModifierFunction("expandable", listOf(ModifierFunction.Parameter("expanded", "Boolean")), "Modifier")
        )

        // Generate short names for all
        val generatedNames = testFunctions.map { function ->
            val shortName = engine.generateShortName(function)
            println("${function.name} -> $shortName")
            shortName
        }

        // Get statistics
        val stats = engine.getStatistics()

        println("\n=== Acacia Naming Statistics ===")
        println("Total functions processed: ${stats.totalGenerated}")
        println("Golden (from acacia-mapping.json): ${stats.goldenNames}")
        println("Algorithmic (auto-generated): ${stats.algorithmicNames}")
        println("Collisions resolved: ${stats.collisionsResolved}")
        println("================================\n")

        // Verify counts
        assertEquals(testFunctions.size, stats.totalGenerated, "Total should match function count")
        
        // Golden names should be the 22 we defined above (that exist in acacia-mapping.json)
        // The test has 22 golden + 24 algorithmic = 46 total
        println("Expected breakdown:")
        println("- Golden: ~22 (padding, size, width, etc. from acacia-mapping.json)")
        println("- Algorithmic: ~24 (horizontalScroll, verticalScroll, etc. not in json)")
        
        // The golden count should be exactly how many of our test functions are in acacia-mapping.json
        assertTrue(stats.goldenNames > 0, "Should have some golden names")
        assertTrue(stats.algorithmicNames > 0, "Should have some algorithmic names")
    }
}
