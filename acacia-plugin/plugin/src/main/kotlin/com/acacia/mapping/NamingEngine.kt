package com.acacia.mapping

import com.acacia.model.ModifierFunction
import org.gradle.api.Project

/**
 * Generates deterministic short names for Modifier functions with collision detection.
 */
class NamingEngine(private val project: Project) {
    
    private val nameMappings = mutableMapOf<String, String>()
    private val usedNames = mutableSetOf<String>()
    
    /**
     * Maps Modifier functions to short DSL names.
     */
    fun generateShortNames(functions: List<ModifierFunction>): Map<String, String> {
        val mappings = mutableMapOf<String, String>()
        
        functions.sortedBy { it.name }.forEach { function ->
            val shortName = generateShortName(function.name)
            mappings[function.name] = shortName
        }
        
        project.logger.lifecycle("Shortify: Generated ${mappings.size} short name mappings")
        
        if (project.extensions.findByName("shortify")?.let { 
            (it as? com.acacia.ShortifyExtension)?.debug == true 
        } == true) {
            mappings.forEach { (original, short) ->
                project.logger.lifecycle("Shortify: $original -> $short")
            }
        }
        
        return mappings
    }
    
    /**
     * Generates a short name for a given function name with collision detection.
     */
    private fun generateShortName(originalName: String): String {
        // First, try the primary mapping
        val primaryName = getPrimaryMapping(originalName)
        
        if (!usedNames.contains(primaryName)) {
            usedNames.add(primaryName)
            nameMappings[originalName] = primaryName
            return primaryName
        }
        
        // If collision exists, generate alternative
        val alternativeName = generateAlternativeName(originalName)
        usedNames.add(alternativeName)
        nameMappings[originalName] = alternativeName
        
        project.logger.debug("Shortify: Name collision resolved: $originalName -> $alternativeName (was $primaryName)")
        
        return alternativeName
    }
    
    /**
     * Primary deterministic mappings for common Modifier functions.
     */
    private fun getPrimaryMapping(originalName: String): String {
        return when (originalName) {
            // Layout Modifiers
            "padding" -> "p"
            "paddingHorizontal" -> "ph"
            "paddingVertical" -> "pv"
            "paddingStart" -> "ps"
            "paddingTop" -> "pt"
            "paddingEnd" -> "pe"
            "paddingBottom" -> "pb"
            "margin" -> "m"
            "marginHorizontal" -> "mh"
            "marginVertical" -> "mv"
            "size" -> "sz"
            "width" -> "w"
            "height" -> "h"
            "fillMaxWidth" -> "fmw"
            "fillMaxHeight" -> "fmh"
            "fillMaxSize" -> "fms"
            
            // Visual Modifiers
            "background" -> "bg"
            "border" -> "br"
            "shadow" -> "sh"
            "clip" -> "cp"
            "alpha" -> "al"
            
            // Interaction Modifiers
            "clickable" -> "clk"
            "pointerInput" -> "pi"
            "draggable" -> "dg"
            "scrollable" -> "sc"
            "swipeable" -> "sw"
            
            // Other common modifiers
            "offset" -> "of"
            "rotate" -> "rt"
            "scale" -> "sl"
            "translate" -> "tl"
            
            // Fallback for unknown functions
            else -> generateFallbackName(originalName)
        }
    }
    
    /**
     * Generates alternative names for collisions.
     */
    private fun generateAlternativeName(originalName: String): String {
        val primaryName = getPrimaryMapping(originalName)
        
        // Try adding numbers first
        for (i in 2..9) {
            val numberedName = "${primaryName}$i"
            if (!usedNames.contains(numberedName)) {
                return numberedName
            }
        }
        
        // Try adding suffixes
        val suffixes = listOf("x", "y", "z", "a", "b", "c")
        for (suffix in suffixes) {
            val suffixedName = "${primaryName}_$suffix"
            if (!usedNames.contains(suffixedName)) {
                return suffixedName
            }
        }
        
        // Last resort: use hash
        return "${primaryName}_${originalName.takeLast(3).hashCode().toString(36).uppercase()}"
    }
    
    /**
     * Generates fallback names for unknown functions.
     */
    private fun generateFallbackName(originalName: String): String {
        return when {
            originalName.length <= 3 -> originalName.lowercase()
            originalName.length <= 6 -> {
                // First 2 letters + last 2 letters
                val firstTwo = originalName.take(2).lowercase()
                val lastTwo = originalName.takeLast(2).lowercase()
                "$firstTwo$lastTwo"
            }
            else -> {
                // First letter + last letter + middle letter count
                val first = originalName.first().lowercase()
                val last = originalName.last().lowercase()
                val middle = originalName.length - 2
                "$first$middle$last"
            }
        }
    }
    
    /**
     * Clears the naming state (useful for testing).
     */
    fun clear() {
        nameMappings.clear()
        usedNames.clear()
    }
}
