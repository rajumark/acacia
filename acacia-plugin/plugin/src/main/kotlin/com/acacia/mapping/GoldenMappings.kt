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
 * Edit acacia-mapping.json to add/modify mappings. Functions not in this list
 * will use algorithmic naming with automatic collision detection.
 */
object GoldenMappings {

    private const val DEFAULT_JSON_PATH = "acacia-mapping.json"

    /**
     * JSON data class for deserialization.
     */
    @Serializable
    private data class GoldenMappingsData(
        val modifierMappings: Map<String, String> = emptyMap(),
        val composableMappings: Map<String, String> = emptyMap(),
        val typeAliasMappings: Map<String, String> = emptyMap()
    )

    /**
     * Map of original modifier function names to their golden short names.
     * Loaded from acacia-mapping.json resource file.
     */
    val modifierMappings: Map<String, String> by lazy {
        loadMappingsFromJson().modifierMappings
    }

    /**
     * Map of original composable function names to their golden short names.
     * Loaded from acacia-mapping.json resource file.
     */
    val composableMappings: Map<String, String> by lazy {
        loadMappingsFromJson().composableMappings
    }

    /**
     * Map of original type names to their golden short type aliases.
     * Loaded from acacia-mapping.json resource file.
     */
    val typeAliasMappings: Map<String, String> by lazy {
        loadMappingsFromJson().typeAliasMappings
    }

    /**
     * Set of all golden modifier short names for quick collision checking.
     */
    val modifierShortNames: Set<String> by lazy {
        modifierMappings.values.toSet()
    }

    /**
     * Set of all golden composable short names for quick collision checking.
     */
    val composableShortNames: Set<String> by lazy {
        composableMappings.values.toSet()
    }

    /**
     * Checks if a modifier function name has a golden mapping.
     */
    fun hasModifierMapping(originalName: String): Boolean = originalName in modifierMappings

    /**
     * Gets the golden short name for a modifier function, or null if not in golden list.
     */
    fun getModifierShortName(originalName: String): String? = modifierMappings[originalName]

    /**
     * Checks if a composable function name has a golden mapping.
     */
    fun hasComposableMapping(originalName: String): Boolean = originalName in composableMappings

    /**
     * Gets the golden short name for a composable function, or null if not in golden list.
     */
    fun getComposableShortName(originalName: String): String? = composableMappings[originalName]

    /**
     * Checks if a type name has a golden mapping.
     */
    fun hasTypeAliasMapping(originalName: String): Boolean = originalName in typeAliasMappings

    /**
     * Gets the golden short name for a type, or null if not in golden list.
     */
    fun getTypeAliasShortName(originalName: String): String? = typeAliasMappings[originalName]

    /**
     * Loads mappings from the JSON resource file.
     * Falls back to default mappings if JSON fails to load.
     */
    private fun loadMappingsFromJson(): GoldenMappingsData {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val inputStream = this::class.java.classLoader.getResourceAsStream(DEFAULT_JSON_PATH)
                ?: throw IllegalStateException("Could not find $DEFAULT_JSON_PATH in resources")

            inputStream.use { stream ->
                json.decodeFromStream<GoldenMappingsData>(stream)
            }
        } catch (e: Exception) {
            // Fallback to default mappings if JSON loading fails
            println("Warning: Failed to load acacia-mapping.json, using defaults: ${e.message}")
            return GoldenMappingsData(
                modifierMappings = defaultModifierMappings(),
                composableMappings = defaultComposableMappings(),
                typeAliasMappings = defaultTypeAliasMappings()
            )
        }
    }

    /**
     * Default fallback modifier mappings if JSON file fails to load.
     */
    private fun defaultModifierMappings(): Map<String, String> = mapOf(
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

    /**
     * Default fallback composable mappings if JSON file fails to load.
     */
    private fun defaultComposableMappings(): Map<String, String> = mapOf(
        "Column" to "C",
        "Row" to "R",
        "Box" to "B",
        "Text" to "T",
        "Button" to "Btn",
        "OutlinedButton" to "OBtn",
        "TextButton" to "TBtn",
        "ElevatedButton" to "EBtn",
        "FilledTonalButton" to "FTBtn",
        "Card" to "Cd",
        "ElevatedCard" to "ECd",
        "OutlinedCard" to "OCd",
        "Icon" to "Ic",
        "IconButton" to "IBtn",
        "IconToggleButton" to "ITBtn",
        "LazyColumn" to "LC",
        "LazyRow" to "LR",
        "LazyVerticalGrid" to "LVG",
        "LazyHorizontalGrid" to "LHG",
        "Surface" to "S",
        "Scaffold" to "Scaf",
        "TopAppBar" to "TAB",
        "BottomAppBar" to "BAB",
        "FloatingActionButton" to "FAB",
        "ExtendedFloatingActionButton" to "EFAB",
        "Dialog" to "Dlg",
        "AlertDialog" to "ADlg",
        "Checkbox" to "Cb",
        "Switch" to "Sw",
        "RadioButton" to "RB",
        "Slider" to "Sl",
        "TextField" to "TF",
        "OutlinedTextField" to "OTF",
        "BasicTextField" to "BTF",
        "CircularProgressIndicator" to "CPI",
        "LinearProgressIndicator" to "LPI",
        "Divider" to "Div",
        "Spacer" to "Sp",
        "VerticalDivider" to "VDiv",
        "HorizontalDivider" to "HDiv",
        "Image" to "Img",
        "AsyncImage" to "AImg",
        "Tab" to "Tb",
        "TabRow" to "TR",
        "ScrollableTabRow" to "STR",
        "Snackbar" to "SB",
        "SnackbarHost" to "SH"
    )

    /**
     * Default fallback type alias mappings if JSON file fails to load.
     */
    private fun defaultTypeAliasMappings(): Map<String, String> = mapOf(
        "Color" to "Cl",
        "Shape" to "Sh",
        "Brush" to "Br",
        "ImageVector" to "IV",
        "Painter" to "Pn",
        "Dp" to "D",
        "TextUnit" to "TU",
        "DpOffset" to "DO",
        "Offset" to "O",
        "Arrangement" to "Arr",
        "Alignment" to "Al",
        "BorderStroke" to "BS",
        "TextStyle" to "TS",
        "FontWeight" to "FW",
        "Modifier" to "M"
    )
}
