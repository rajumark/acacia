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
     * Enhanced primary deterministic mappings with AI-friendly patterns.
     */
    private fun getPrimaryMapping(originalName: String): String {
        return when (originalName) {
            // Core Layout Modifiers (highest priority)
            "padding" -> "p"
            "size" -> "sz"
            "width" -> "w"
            "height" -> "h"
            "fillMaxWidth" -> "fmw"
            "fillMaxHeight" -> "fmh"
            "fillMaxSize" -> "fms"
            
            // Padding Variants (semantic naming)
            "paddingHorizontal" -> "ph"
            "paddingVertical" -> "pv"
            "paddingStart" -> "ps"
            "paddingTop" -> "pt"
            "paddingEnd" -> "pe"
            "paddingBottom" -> "pb"
            
            // Visual Modifiers (intuitive names)
            "background" -> "bg"
            "border" -> "br"
            "shadow" -> "sh"
            "clip" -> "cp"
            "alpha" -> "al"
            
            // Interaction Modifiers (action-oriented)
            "clickable" -> "clk"
            "pointerInput" -> "pi"
            "draggable" -> "dg"
            "scrollable" -> "sc"
            "swipeable" -> "sw"
            "toggleable" -> "tg"
            "selectable" -> "sl"
            
            // Transform Modifiers (descriptive)
            "offset" -> "of"
            "rotate" -> "rt"
            "scale" -> "sl"
            
            // System & Layout Modifiers (system-oriented)
            "wrapContentWidth" -> "wcw"
            "wrapContentHeight" -> "wch"
            "wrapContentSize" -> "wcs"
            "requiredWidth" -> "rw"
            "requiredHeight" -> "rh"
            "requiredSize" -> "rs"
            
            // Window Insets (semantic grouping)
            "windowInsetsPadding" -> "wip"
            "systemBarsPadding" -> "sbp"
            "statusBarsPadding" -> "stp"
            "navigationBarsPadding" -> "nbp"
            "safeDrawingPadding" -> "sdp"
            
            // Advanced Modifiers (categorical)
            "semantics" -> "sem"
            "testTag" -> "tt"
            "zIndex" -> "zi"
            
            // Fallback for unknown functions
            else -> generateSmartFallbackName(originalName)
        }
    }
    
    /**
     * Generates alternative names for collisions with semantic approach.
     */
    private fun generateAlternativeName(originalName: String): String {
        val primaryName = getPrimaryMapping(originalName)
        
        // Try semantic variations first
        val semanticVariations = generateSemanticVariations(originalName, primaryName)
        for (variation in semanticVariations) {
            if (!usedNames.contains(variation)) {
                return variation
            }
        }
        
        // Try adding descriptive suffixes
        val descriptiveSuffixes = listOf("2", "x", "v2", "alt", "b")
        for (suffix in descriptiveSuffixes) {
            val suffixedName = "${primaryName}$suffix"
            if (!usedNames.contains(suffixedName)) {
                return suffixedName
            }
        }
        
        // Last resort: use hash
        return "${primaryName}_${originalName.takeLast(3).hashCode().toString(36).uppercase()}"
    }
    
    /**
     * Generates semantic variations for collision resolution.
     */
    private fun generateSemanticVariations(originalName: String, primaryName: String): List<String> {
        val variations = mutableListOf<String>()
        
        // Add context-based variations
        when {
            originalName.contains("padding") -> {
                variations.addAll(listOf("${primaryName}2", "${primaryName}v", "${primaryName}alt"))
            }
            originalName.contains("background") -> {
                variations.addAll(listOf("${primaryName}2", "${primaryName}v", "${primaryName}alt"))
            }
            originalName.contains("clickable") -> {
                variations.addAll(listOf("${primaryName}2", "${primaryName}v", "${primaryName}alt"))
            }
            originalName.contains("scrollable") -> {
                variations.addAll(listOf("${primaryName}2", "${primaryName}v", "${primaryName}alt"))
            }
            originalName.contains("fillMax") -> {
                variations.addAll(listOf("${primaryName}2", "${primaryName}v", "${primaryName}alt"))
            }
            originalName.contains("wrapContent") -> {
                variations.addAll(listOf("${primaryName}2", "${primaryName}v", "${primaryName}alt"))
            }
            else -> {
                variations.addAll(listOf("${primaryName}2", "${primaryName}v"))
            }
        }
        
        return variations
    }
    
    /**
     * Generates smart fallback names for unknown functions with AI-friendly patterns.
     */
    private fun generateSmartFallbackName(originalName: String): String {
        // Handle special cases with hash suffixes (generated functions)
        if (originalName.contains("-")) {
            val baseName = originalName.split("-").first()
            val baseMapping = getPrimaryMapping(baseName)
            if (baseMapping != generateSmartFallbackName(baseName)) {
                return "${baseMapping}_alt"
            }
        }
        
        return when {
            originalName.length <= 3 -> originalName.lowercase()
            originalName.length <= 6 -> {
                // First 2 letters + last 2 letters for better memorability
                val firstTwo = originalName.take(2).lowercase()
                val lastTwo = originalName.takeLast(2).lowercase()
                "$firstTwo$lastTwo"
            }
            originalName.length <= 10 -> {
                // First letter + middle pattern + last letter
                val first = originalName.first().lowercase()
                val middle = originalName.drop(1).dropLast(1).take(3).lowercase()
                val last = originalName.last().lowercase()
                "$first$middle$last"
            }
            else -> {
                // Smart abbreviation for long names
                val words = originalName.split("(?=[A-Z])".toRegex())
                when (words.size) {
                    1 -> {
                        // Single word: take first, middle, last letters
                        val name = words[0]
                        if (name.length <= 8) {
                            return name.lowercase()
                        }
                        val first = name.first().lowercase()
                        val middle = name.drop(1).dropLast(1).take(2).lowercase()
                        val last = name.last().lowercase()
                        "$first$middle$last"
                    }
                    2 -> {
                        // Two words: first letter of each + middle of second
                        val first = words[0].first().lowercase()
                        val second = words[1]
                        val secondMiddle = if (second.length > 3) {
                            second.take(2).lowercase()
                        } else {
                            second.lowercase()
                        }
                        "$first$secondMiddle"
                    }
                    else -> {
                        // Multiple words: take first letter of first 2-3 words
                        words.take(3).joinToString("") { it.first().lowercase() }
                    }
                }
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
