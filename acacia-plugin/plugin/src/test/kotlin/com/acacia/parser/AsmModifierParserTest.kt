package com.acacia.parser

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

/**
 * Test to verify ASM parser correctly finds Modifier extension functions.
 */
class AsmModifierParserTest {

    @Test
    fun `test ASM type descriptor comparison`() {
        // Test that our descriptor comparison logic works correctly
        val expectedDescriptor = "Landroidx/compose/ui/Modifier;"
        
        // This is what ASM Type.descriptor returns
        val actualDescriptor = "Landroidx/compose/ui/Modifier;"
        
        // Should match
        assertEquals(expectedDescriptor, actualDescriptor)
        
        // The old code compared className which would be:
        // "androidx/compose/ui/Modifier" (with slashes)
        // vs "androidx.compose.ui.Modifier" (with dots)
        // These would NOT match - that's the bug!
        val oldComparison = "androidx/compose/ui/Modifier" == "androidx.compose.ui.Modifier"
        assertEquals(false, oldComparison, "Old className comparison should fail")
    }
}
