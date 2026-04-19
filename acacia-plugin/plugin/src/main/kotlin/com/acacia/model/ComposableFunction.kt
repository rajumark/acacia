package com.acacia.model

/**
 * Represents a @Composable function that will be converted to a short DSL wrapper.
 * Examples: Column, Row, Box, Text, Button, etc.
 */
data class ComposableFunction(
    /**
     * The original function name (e.g., "Column", "Row", "Text")
     */
    val name: String,
    
    /**
     * Fully qualified package name (e.g., "androidx.compose.foundation.layout")
     */
    val packageName: String,
    
    /**
     * List of function parameters
     */
    val parameters: List<Parameter>,
    
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
         * Parameter name (e.g., "modifier", "content")
         */
        val name: String,
        
        /**
         * Parameter type (e.g., "Modifier", "ColumnScope.() -> Unit")
         */
        val type: String,
        
        /**
         * Whether the parameter has a default value
         */
        val hasDefault: Boolean = false,
        
        /**
         * Default value if present
         */
        val defaultValue: String? = null
    )
}
