package com.acacia.mapping

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

/**
 * Golden list of handcoded short names for the most common Compose Modifier functions.
 *
 * These names are:
 * - Deterministic (always same across all projects)
 * - Short (1-3 characters)
 * - Collision-free (manually verified)
 * - AI-friendly (easy to learn)
 *
 * Edit golden-mappings.json to add/modify mappings. Functions not in this list
 * will use algorithmic naming with automatic collision detection.
 */
object GoldenMappings {

    private const val DEFAULT_JSON_PATH = "golden-mappings.json"

    /**
     * JSON data class for deserialization.
     */
    @Serializable
    private data class GoldenMappingsData(
        val mappings: Map<String, String>
    )

    /**
     * Map of original function names to their golden short names.
     * Loaded from golden-mappings.json resource file.
     *
     * To add a new mapping, edit golden-mappings.json:
     * "originalFunctionName": "short"
     */
    val mappings: Map<String, String> by lazy {
        loadMappingsFromJson()
    }

    /**
     * Set of all golden short names for quick collision checking.
     */
    val goldenShortNames: Set<String> by lazy {
        mappings.values.toSet()
    }

    /**
     * Checks if a function name has a golden mapping.
     */
    fun hasGoldenMapping(originalName: String): Boolean = originalName in mappings

    /**
     * Gets the golden short name for a function, or null if not in golden list.
     */
    fun getGoldenShortName(originalName: String): String? = mappings[originalName]

    /**
     * Loads mappings from the JSON resource file.
     * Falls back to default mappings if JSON fails to load.
     */
    private fun loadMappingsFromJson(): Map<String, String> {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val inputStream = this::class.java.classLoader.getResourceAsStream(DEFAULT_JSON_PATH)
                ?: throw IllegalStateException("Could not find $DEFAULT_JSON_PATH in resources")

            inputStream.use { stream ->
                val data = json.decodeFromStream<GoldenMappingsData>(stream)
                data.mappings
            }
        } catch (e: Exception) {
            // Fallback to default mappings if JSON loading fails
            println("Warning: Failed to load golden-mappings.json, using defaults: ${e.message}")
            defaultMappings()
        }
    }

    /**
     * Default fallback mappings if JSON file fails to load.
     */
    private fun defaultMappings(): Map<String, String> = mapOf(
        "padding" to "p",
        "paddingHorizontal" to "px",
        "paddingVertical" to "py",
        "paddingStart" to "ps",
        "paddingEnd" to "pe",
        "paddingTop" to "pt",
        "paddingBottom" to "pb",
        "size" to "sz",
        "width" to "w",
        "height" to "h",
        "fillMaxWidth" to "fmw",
        "fillMaxHeight" to "fmh",
        "fillMaxSize" to "fms",
        "wrapContentWidth" to "wcw",
        "wrapContentHeight" to "wch",
        "wrapContentSize" to "wcs",
        "offset" to "of",
        "absoluteOffset" to "aof",
        "background" to "bg",
        "border" to "br",
        "shadow" to "sh",
        "clip" to "cp",
        "alpha" to "al",
        "rotate" to "rt",
        "scale" to "sc",
        "clickable" to "clk",
        "draggable" to "dg",
        "scrollable" to "scr",
        "swipeable" to "swp",
        "toggleable" to "tg",
        "selectable" to "sl",
        "focusable" to "fc",
        "focused" to "fd",
        "systemBarsPadding" to "sbp",
        "statusBarsPadding" to "stp",
        "navigationBarsPadding" to "nbp",
        "safeDrawingPadding" to "sdp",
        "safeContentPadding" to "scp",
        "safeGesturesPadding" to "sgp",
        "displayCutoutPadding" to "dcp",
        "imePadding" to "imp",
        "testTag" to "tt",
        "semantics" to "sem",
        "then" to "th"
    )
}
