package com.acacia

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class SimpleTest {

    @Test
    fun `basic addition works`() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `plugin is accessible`() {
        val plugin = ShortifyPlugin()
        assertEquals("com.acacia.ShortifyPlugin", plugin.javaClass.name)
    }
}
