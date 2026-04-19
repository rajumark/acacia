package com.acacia.mapping

/**
 * Golden list of handcoded short names for the most common Compose Modifier functions.
 * 
 * These names are:
 * - Deterministic (always same across all projects)
 * - Short (1-3 characters)
 * - Collision-free (manually verified)
 * - AI-friendly (easy to learn)
 * 
 * Edit this file to add/modify golden mappings. Functions not in this list
 * will use algorithmic naming with automatic collision detection.
 */
object GoldenMappings {

    /**
     * Map of original function names to their golden short names.
     * 
     * To add a new mapping:
     * "originalFunctionName" to "short"
     */
    val mappings: Map<String, String> = mapOf(
        // === LAYOUT: Padding (most common) ===
        "padding" to "p",
        "paddingHorizontal" to "px",  // x/y pattern like Tailwind
        "paddingVertical" to "py",
        "paddingStart" to "ps",
        "paddingEnd" to "pe",
        "paddingTop" to "pt",
        "paddingBottom" to "pb",
        
        // === LAYOUT: Size ===
        "size" to "sz",
        "width" to "w",
        "height" to "h",
        "fillMaxWidth" to "fmw",
        "fillMaxHeight" to "fmh",
        "fillMaxSize" to "fms",
        "wrapContentWidth" to "wcw",
        "wrapContentHeight" to "wch",
        "wrapContentSize" to "wcs",
        
        // === LAYOUT: Positioning ===
        "offset" to "of",
        "absoluteOffset" to "aof",
        
        // === VISUAL: Background & Color ===
        "background" to "bg",
        "border" to "br",
        "shadow" to "sh",
        "clip" to "cp",
        "alpha" to "al",
        
        // === VISUAL: Transform ===
        "rotate" to "rt",
        "scale" to "sc",
        
        // === INTERACTION ===
        "clickable" to "clk",
        "draggable" to "dg",
        "scrollable" to "scr",
        "swipeable" to "swp",
        "toggleable" to "tg",
        "selectable" to "sl",
        
        // === FOCUS & INPUT ===
        "focusable" to "fc",
        "focused" to "fd",
        
        // === SYSTEM BARS ===
        "systemBarsPadding" to "sbp",
        "statusBarsPadding" to "stp",
        "navigationBarsPadding" to "nbp",
        "safeDrawingPadding" to "sdp",
        "safeContentPadding" to "scp",
        "safeGesturesPadding" to "sgp",
        "displayCutoutPadding" to "dcp",
        "imePadding" to "imp",
        
        // === TESTING & ACCESSIBILITY ===
        "testTag" to "tt",
        "semantics" to "sem",
        
        // === UTILITY ===
        "then" to "th",  // Modifier chaining
    )

    /**
     * Set of all golden short names for quick collision checking.
     */
    val goldenShortNames: Set<String> = mappings.values.toSet()

    /**
     * Checks if a function name has a golden mapping.
     */
    fun hasGoldenMapping(originalName: String): Boolean = originalName in mappings

    /**
     * Gets the golden short name for a function, or null if not in golden list.
     */
    fun getGoldenShortName(originalName: String): String? = mappings[originalName]
}
