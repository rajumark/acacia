package com.acacia

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import com.acacia.parser.ModifierParser
import com.acacia.model.ModifierFunction

class ModifierParserTest {

    @Test
    fun `should parse basic modifier function`() {
        val source = """
            fun Modifier.padding(dp: Int): Modifier = this.then(Padding(dp.dp))
        """.trimIndent()
        
        val functions = ModifierParser.parseSource(source)
        
        assertEquals(1, functions.size)
        val function = functions.first()
        assertEquals("padding", function.name)
        assertEquals("Modifier", function.receiver)
        assertEquals("Modifier", function.returnType)
    }

    @Test
    fun `should parse multiple modifier functions`() {
        val source = """
            fun Modifier.padding(dp: Int): Modifier = this.then(Padding(dp.dp))
            fun Modifier.background(color: Color): Modifier = this.then(Background(color))
        """.trimIndent()
        
        val functions = ModifierParser.parseSource(source)
        
        assertEquals(2, functions.size)
        assertTrue(functions.any { it.name == "padding" })
        assertTrue(functions.any { it.name == "background" })
    }

    @Test
    fun `should handle empty source`() {
        val functions = ModifierParser.parseSource("")
        assertTrue(functions.isEmpty())
    }

    @Test
    fun `should ignore non-modifier functions`() {
        val source = """
            fun regularFunction(): String = "test"
            fun Modifier.padding(dp: Int): Modifier = this.then(Padding(dp.dp))
        """.trimIndent()
        
        val functions = ModifierParser.parseSource(source)
        
        assertEquals(1, functions.size)
        assertEquals("padding", functions.first().name)
    }
}
