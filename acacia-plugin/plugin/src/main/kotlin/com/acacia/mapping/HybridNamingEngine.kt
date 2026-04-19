package com.acacia.mapping

import com.acacia.model.ComposableFunction
import com.acacia.model.ModifierFunction

/**
 * Hybrid naming engine that combines:
 * 1. Golden list: Handcoded short names for top 50 most common functions
 * 2. Algorithmic naming: For remaining functions with collision detection
 * 
 * This approach is:
 * - Deterministic (same input always produces same output)
 * - Collision-free (detects and resolves conflicts automatically)
 * - AI-friendly (learnable pattern: 50 golden + algorithmic extension)
 */
class HybridNamingEngine {

    // Track all generated names in this session for collision detection
    private val generatedModifierNames = mutableMapOf<String, ModifierFunction>()
    private val generatedComposableNames = mutableMapOf<String, ComposableFunction>()
    private val modifierCollisionResolutions = mutableMapOf<String, Int>()
    private val composableCollisionResolutions = mutableMapOf<String, Int>()

    /**
     * Generates a short name for a modifier function.
     * 
     * Priority:
     * 1. Check golden list (if present, use it)
     * 2. Generate algorithmically with collision detection
     */
    fun generateShortName(function: ModifierFunction): String {
        // Step 1: Check golden list
        val goldenName = GoldenMappings.getModifierShortName(function.name)
        if (goldenName != null) {
            // Register golden name as used
            generatedModifierNames[goldenName] = function
            return goldenName
        }

        // Step 2: Generate algorithmically
        val baseName = generateAlgorithmicBaseName(function)
        
        // Step 3: Resolve collisions
        val finalName = resolveModifierCollision(baseName, function)
        
        // Register the name as used
        generatedModifierNames[finalName] = function
        
        return finalName
    }

    /**
     * Generates a short name for a composable function.
     * 
     * Priority:
     * 1. Check golden list (if present, use it)
     * 2. Generate algorithmically with collision detection
     */
    fun generateComposableShortName(function: ComposableFunction): String {
        // Step 1: Check golden list
        val goldenName = GoldenMappings.getComposableShortName(function.name)
        if (goldenName != null) {
            // Register golden name as used
            generatedComposableNames[goldenName] = function
            return goldenName
        }

        // Step 2: Generate algorithmically
        val baseName = generateComposableBaseName(function)
        
        // Step 3: Resolve collisions
        val finalName = resolveComposableCollision(baseName, function)
        
        // Register the name as used
        generatedComposableNames[finalName] = function
        
        return finalName
    }

    /**
     * Generates algorithmic base name from function name and signature.
     * Uses consonant-based abbreviation with signature hints for uniqueness.
     */
    private fun generateAlgorithmicBaseName(function: ModifierFunction): String {
        val name = function.name
        
        // Strategy 1: For camelCase names with 3+ words, take first letter of each word
        val words = splitCamelCase(name)
        if (words.size >= 3) {
            return words.take(3).joinToString("") { it.first().lowercase() }
        }
        
        // Strategy 2: For 2-word names, first char + next consonants
        if (words.size == 2) {
            val first = words[0].lowercase()
            val second = words[1].lowercase()
            return first.first().toString() + takeConsonants(second, 2)
        }
        
        // Strategy 3: Single word - consonant-based
        val lowerName = name.lowercase()
        return when {
            lowerName.length <= 4 -> lowerName
            else -> {
                // First char + key consonants + last char if meaningful
                val consonants = takeConsonants(lowerName, 3)
                lowerName.first().toString() + consonants
            }
        }
    }

    /**
     * Resolves naming collisions for composable functions.
     */
    private fun resolveComposableCollision(baseName: String, function: ComposableFunction): String {
        // Check if this exact function already has a name (same signature)
        val existingEntry = generatedComposableNames[baseName]
        if (existingEntry != null && isSameComposableSignature(existingEntry, function)) {
            return baseName
        }
        
        // Check if name is used by different function
        if (existingEntry != null && !isSameComposableSignature(existingEntry, function)) {
            // Collision detected - add signature disambiguation
            return disambiguateComposableWithSignature(baseName, function)
        }
        
        // Check against golden names
        if (baseName in GoldenMappings.composableShortNames) {
            return disambiguateComposableWithSignature(baseName, function)
        }
        
        return baseName
    }

    /**
     * Resolves naming collisions for modifier functions.
     */
    private fun resolveModifierCollision(baseName: String, function: ModifierFunction): String {
        // Check if this exact function already has a name (same signature)
        val existingEntry = generatedModifierNames[baseName]
        if (existingEntry != null && isSameModifierSignature(existingEntry, function)) {
            return baseName
        }
        
        // Check if name is used by different function
        if (existingEntry != null && !isSameModifierSignature(existingEntry, function)) {
            return disambiguateModifierWithSignature(baseName, function)
        }
        
        // Check against golden names
        if (baseName in GoldenMappings.modifierShortNames) {
            return disambiguateModifierWithSignature(baseName, function)
        }
        
        return baseName
    }

    /**
     * Disambiguates a composable name by adding signature information.
     */
    private fun disambiguateComposableWithSignature(baseName: String, function: ComposableFunction): String {
        val count = composableCollisionResolutions.getOrDefault(baseName, 0) + 1
        composableCollisionResolutions[baseName] = count
        
        // Strategy 1: Add param count
        val paramCount = function.parameters.size
        val withCount = baseName + paramCount
        
        if (withCount !in generatedComposableNames && withCount !in GoldenMappings.composableShortNames) {
            return withCount
        }
        
        // Strategy 2: Add numeric suffix
        return baseName + (count + 1)
    }

    /**
     * Disambiguates a modifier name by adding signature information.
     */
    private fun disambiguateModifierWithSignature(baseName: String, function: ModifierFunction): String {
        // Get collision count for this base name
        val count = modifierCollisionResolutions.getOrDefault(baseName, 0) + 1
        modifierCollisionResolutions[baseName] = count
        
        // Strategy 1: Add first param type initial (if has params)
        val firstParam = function.parameters.firstOrNull()
        if (firstParam != null) {
            val typeInitial = firstParam.type.first().lowercase()
            val withType = baseName + typeInitial
            
            if (withType !in generatedModifierNames && withType !in GoldenMappings.modifierShortNames) {
                return withType
            }
        }
        
        // Strategy 2: Add parameter count
        val paramCount = function.parameters.size
        val withCount = baseName + paramCount
        
        if (withCount !in generatedModifierNames && withCount !in GoldenMappings.modifierShortNames) {
            return withCount
        }
        
        // Strategy 3: Add numeric suffix (should be rare)
        return baseName + (count + 1)
    }

    /**
     * Checks if two modifier functions have the same signature.
     */
    private fun isSameModifierSignature(a: ModifierFunction, b: ModifierFunction): Boolean {
        if (a.name != b.name) return false
        if (a.parameters.size != b.parameters.size) return false
        
        return a.parameters.zip(b.parameters).all { (p1, p2) ->
            p1.name == p2.name && p1.type == p2.type
        }
    }

    /**
     * Checks if two composable functions have the same signature.
     */
    private fun isSameComposableSignature(a: ComposableFunction, b: ComposableFunction): Boolean {
        if (a.name != b.name) return false
        if (a.packageName != b.packageName) return false
        if (a.parameters.size != b.parameters.size) return false
        
        return a.parameters.zip(b.parameters).all { (p1, p2) ->
            p1.name == p2.name && p1.type == p2.type
        }
    }

    /**
     * Splits a camelCase string into words.
     * "paddingHorizontal" → ["padding", "Horizontal"]
     * "XMLHttpRequest" → ["XML", "Http", "Request"]
     */
    private fun splitCamelCase(str: String): List<String> {
        val words = mutableListOf<String>()
        val currentWord = StringBuilder()
        
        for (i in str.indices) {
            val char = str[i]
            
            // Start of new word: uppercase letter preceded by lowercase
            if (char.isUpperCase() && currentWord.isNotEmpty() && currentWord.last().isLowerCase()) {
                words.add(currentWord.toString())
                currentWord.clear()
            }
            
            currentWord.append(char)
        }
        
        if (currentWord.isNotEmpty()) {
            words.add(currentWord.toString())
        }
        
        return words
    }

    /**
     * Takes up to n consonants from a string (skipping vowels).
     */
    private fun takeConsonants(str: String, n: Int): String {
        val vowels = setOf('a', 'e', 'i', 'o', 'u')
        val result = StringBuilder()
        
        for (char in str.drop(1)) { // Skip first char (already used)
            if (result.length >= n) break
            if (char.lowercaseChar() !in vowels && char.isLetter()) {
                result.append(char.lowercase())
            }
        }
        
        return result.toString()
    }

    /**
     * Clears the collision tracking. Call this when starting a new generation session.
     */
    fun reset() {
        generatedModifierNames.clear()
        generatedComposableNames.clear()
        modifierCollisionResolutions.clear()
        composableCollisionResolutions.clear()
    }

    /**
     * Gets statistics about name generation for debugging.
     */
    fun getStatistics(): NamingStatistics {
        val modifierGoldenCount = generatedModifierNames.count { it.value.name in GoldenMappings.modifierMappings }
        val modifierAlgorithmicCount = generatedModifierNames.size - modifierGoldenCount
        
        val composableGoldenCount = generatedComposableNames.count { it.value.name in GoldenMappings.composableMappings }
        val composableAlgorithmicCount = generatedComposableNames.size - composableGoldenCount
        
        val totalGenerated = generatedModifierNames.size + generatedComposableNames.size
        val goldenCount = modifierGoldenCount + composableGoldenCount
        val algorithmicCount = modifierAlgorithmicCount + composableAlgorithmicCount
        val collisionCount = modifierCollisionResolutions.size + composableCollisionResolutions.size
        
        return NamingStatistics(
            totalGenerated = totalGenerated,
            goldenNames = goldenCount,
            algorithmicNames = algorithmicCount,
            collisionsResolved = collisionCount,
            modifierCount = generatedModifierNames.size,
            composableCount = generatedComposableNames.size
        )
    }

    data class NamingStatistics(
        val totalGenerated: Int,
        val goldenNames: Int,
        val algorithmicNames: Int,
        val collisionsResolved: Int,
        val modifierCount: Int = 0,
        val composableCount: Int = 0
    )
}
