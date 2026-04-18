package com.acacia.model

/**
 * Represents a Modifier extension function that will be converted to a short DSL function.
 */
data class ModifierFunction(
    /**
     * The original function name (e.g., "padding", "background")
     */
    val name: String,
    
    /**
     * List of function parameters
     */
    val parameters: List<Parameter>,
    
    /**
     * The return type (should always be "Modifier" for extension functions)
     */
    val returnType: String = "Modifier",
    
    /**
     * Whether this function is deprecated
     */
    val isDeprecated: Boolean = false
) {
    /**
     * Represents a function parameter
     */
    data class Parameter(
        /**
         * Parameter name (e.g., "all", "horizontal", "color")
         */
        val name: String,
        
        /**
         * Parameter type (e.g., "Dp", "Color", "Boolean")
         */
        val type: String,
        
        /**
         * Whether the parameter has a default value
         */
        val hasDefault: Boolean = false,
        
        /**
         * Default value if present (e.g., "0.dp", "Color.Unspecified")
         */
        val defaultValue: String? = null
    )
}
