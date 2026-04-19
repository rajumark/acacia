package com.acacia.parser

import com.acacia.model.ModifierFunction
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.File
import java.io.FileInputStream
import java.util.jar.JarFile

/**
 * Advanced parser using ASM bytecode analysis to extract Modifier extension functions.
 */
class AsmModifierParser(private val project: Project) {
    
    /**
     * Parses jar files using ASM bytecode analysis to extract Modifier extension functions.
     */
    fun parseModifierFunctions(jarFiles: List<File>): List<ModifierFunction> {
        println("Shortify: Starting ASM parsing for ${jarFiles.size} jar files")
        println("Shortify: ASM Parser v2.0 - Enhanced with proper parameter extraction")
        val functions = mutableListOf<ModifierFunction>()
        
        jarFiles.forEach { jarFile ->
            try {
                val jarFunctions = parseJarFileWithAsm(jarFile)
                functions.addAll(jarFunctions)
                
                if (jarFunctions.isNotEmpty()) {
                    project.logger.debug("Shortify: Found ${jarFunctions.size} functions in ${jarFile.name}")
                }
            } catch (e: Exception) {
                project.logger.warn("Shortify: Failed to parse ${jarFile.name}: ${e.message}")
            }
        }
        
        project.logger.lifecycle("Shortify: Parsed ${functions.size} Modifier functions using ASM")
        return functions
    }
    
    /**
     * Parses a single jar file using ASM bytecode analysis.
     */
    private fun parseJarFileWithAsm(jarFile: File): List<ModifierFunction> {
        val functions = mutableListOf<ModifierFunction>()
        
        JarFile(jarFile).use { jar ->
            jar.entries().asSequence()
                .filter { entry -> 
                    entry.name.endsWith(".class") && 
                    !entry.name.contains("$") && 
                    !entry.name.contains("META-INF") &&
                    !entry.name.contains("BuildConfig") &&
                    !entry.name.contains("R$")
                }
                .forEach { entry ->
                    try {
                        jar.getInputStream(entry).use { input ->
                            try {
                                // Validate the class file by reading the magic number
                                val bytes = input.readBytes()
                                if (bytes.size < 4 || 
                                    (bytes[0].toInt() and 0xFF) != 0xCA || 
                                    (bytes[1].toInt() and 0xFF) != 0xFE ||
                                    (bytes[2].toInt() and 0xFF) != 0xBA ||
                                    (bytes[3].toInt() and 0xFF) != 0xBE) {
                                    project.logger.debug("Shortify: Invalid class file format for ${entry.name}")
                                    return@forEach
                                }
                                
                                // Parse with ASM
                                val classReader = ClassReader(bytes)
                                val visitor = ModifierFunctionVisitor()
                                classReader.accept(visitor, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
                                
                                functions.addAll(visitor.modifierFunctions)
                            } catch (asmException: Exception) {
                                project.logger.debug("Shortify: ASM parsing failed for ${entry.name}: ${asmException.message}")
                            }
                        }
                    } catch (e: Exception) {
                        project.logger.debug("Shortify: Failed to read class ${entry.name}: ${e.message}")
                    }
                }
        }
        
        return functions
    }
    
    /**
     * ASM ClassVisitor to extract Modifier extension functions.
     */
    private inner class ModifierFunctionVisitor : ClassVisitor(Opcodes.ASM9) {
        val modifierFunctions = mutableListOf<ModifierFunction>()
        
        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            // Check if this is a Kotlin file with extension functions
            // Kotlin extension functions are compiled as static methods in companion-like classes
        }
        
        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor? {
            // Look for static methods that take Modifier as first parameter (extension functions)
            if (isModifierExtensionFunction(access, name, descriptor)) {
                return ModifierMethodVisitor(name, descriptor, signature)
            }
            return null
        }
        
        /**
         * Checks if a method is a Modifier extension function.
         */
        private fun isModifierExtensionFunction(access: Int, name: String, descriptor: String): Boolean {
            // Must be public static
            if ((access and Opcodes.ACC_PUBLIC) == 0 || (access and Opcodes.ACC_STATIC) == 0) {
                return false
            }
            
            // Skip synthetic methods and constructors
            if ((access and Opcodes.ACC_SYNTHETIC) != 0 || name == "<init>" || name == "<clinit>") {
                return false
            }
            
            // Must return Modifier (check using descriptor format with slashes)
            val returnType = Type.getReturnType(descriptor)
            val expectedModifierDescriptor = "Landroidx/compose/ui/Modifier;"
            if (returnType.descriptor != expectedModifierDescriptor) {
                return false
            }
            
            // First parameter must be Modifier (the receiver) - check using descriptor
            val argumentTypes = Type.getArgumentTypes(descriptor)
            if (argumentTypes.isEmpty() || argumentTypes[0].descriptor != expectedModifierDescriptor) {
                return false
            }
            
            // Additional filtering: skip known non-modifier functions
            val skipFunctions = setOf(
                "then", "composed", "element", "foldIn", "foldOut", 
                "any", "all", "none", "count", "first", "last"
            )
            if (skipFunctions.contains(name)) {
                return false
            }
            
            return true
        }
        
        /**
         * ASM MethodVisitor to extract function details.
         */
        private inner class ModifierMethodVisitor(
            private val methodName: String,
            private val descriptor: String,
            private val signature: String?
        ) : MethodVisitor(Opcodes.ASM9) {
            
            override fun visitEnd() {
                // Extract parameter information
                val argumentTypes = Type.getArgumentTypes(descriptor)
                val parameters = mutableListOf<ModifierFunction.Parameter>()
                
                // Debug logging
                project.logger.lifecycle("Shortify: ASM found method '$methodName' with descriptor '$descriptor'")
                project.logger.lifecycle("Shortify: ASM argument types: ${argumentTypes.map { it.descriptor }}")
                
                // Skip first parameter (the Modifier receiver)
                for (i in 1 until argumentTypes.size) {
                    val argType = argumentTypes[i]
                    // For primitive types, use descriptor; for objects, use className
                    val typeInput = if (argType.sort == Type.OBJECT || argType.sort == Type.ARRAY) {
                        argType.className
                    } else {
                        argType.descriptor
                    }
                    val typeName = simplifyTypeName(typeInput)
                    val paramName = generateParameterName(methodName, i, typeName)
                    
                    project.logger.lifecycle("Shortify: ASM Parameter ${i}: ${argType.descriptor} -> $typeName (name: $paramName)")
                    
                    parameters.add(
                        ModifierFunction.Parameter(
                            name = paramName,
                            type = typeName,
                            hasDefault = false, // Could be detected from annotations
                            defaultValue = null
                        )
                    )
                }
                
                val function = ModifierFunction(
                    name = methodName,
                    parameters = parameters,
                    returnType = "Modifier",
                    isDeprecated = false // Could be detected from annotations
                )
                
                modifierFunctions.add(function)
            }
        }
        
        /**
         * Simplifies type names for better readability.
         */
        private fun simplifyTypeName(className: String): String {
            return when (className) {
                "androidx.compose.ui.unit.Dp" -> "Dp"
                "androidx.compose.ui.graphics.Color" -> "Color"
                "androidx.compose.ui.graphics.Shape" -> "Shape"
                "androidx.compose.ui.graphics.Brush" -> "Brush"
                "androidx.compose.foundation.layout.Arrangement" -> "Arrangement"
                "androidx.compose.foundation.layout.Alignment" -> "Alignment"
                "androidx.compose.foundation.BorderStroke" -> "BorderStroke"
                "androidx.compose.ui.unit.DpOffset" -> "DpOffset"
                "androidx.compose.ui.geometry.Offset" -> "Offset"
                "androidx.compose.ui.geometry.Size" -> "Size"
                "androidx.compose.ui.geometry.Rect" -> "Rect"
                "androidx.compose.ui.unit.TextUnit" -> "TextUnit"
                "androidx.compose.ui.text.font.FontWeight" -> "FontWeight"
                "androidx.compose.ui.text.TextStyle" -> "TextStyle"
                "androidx.compose.ui.graphics.vector.ImageVector" -> "ImageVector"
                "androidx.compose.ui.graphics.painter.Painter" -> "Painter"
                "kotlin.Float" -> "Float"
                "kotlin.Int" -> "Int"
                "kotlin.Double" -> "Double"
                "kotlin.Boolean" -> "Boolean"
                "kotlin.String" -> "String"
                // Handle primitive types from ASM descriptors
                "F" -> "Float"
                "I" -> "Int" 
                "D" -> "Double"
                "Z" -> "Boolean"
                "Ljava/lang/String;" -> "String"
                else -> {
                    // For class names, get simple name
                    if (className.startsWith("L") && className.endsWith(";")) {
                        className.substring(1, className.length - 1).substringAfterLast(".")
                    } else {
                        className.substringAfterLast(".")
                    }
                }
            }
        }
        
        /**
         * Generates meaningful parameter names based on function name and type.
         */
        private fun generateParameterName(functionName: String, paramIndex: Int, paramType: String): String {
            return when (functionName) {
                "padding" -> when (paramIndex) {
                    1 -> "horizontal"
                    2 -> "vertical"
                    3 -> "start"
                    4 -> "top"
                    5 -> "end"
                    6 -> "bottom"
                    else -> "param$paramIndex"
                }
                "paddingHorizontal", "paddingVertical", "paddingStart", 
                "paddingTop", "paddingEnd", "paddingBottom" -> "padding"
                "size" -> when (paramIndex) {
                    1 -> "width"
                    2 -> "height"
                    else -> "param$paramIndex"
                }
                "width" -> "width"
                "height" -> "height"
                "background" -> "color"
                "border" -> when (paramIndex) {
                    1 -> "width"
                    2 -> "brush"
                    3 -> "shape"
                    else -> "param$paramIndex"
                }
                "shadow" -> when (paramIndex) {
                    1 -> "elevation"
                    2 -> "shape"
                    3 -> "clip"
                    else -> "param$paramIndex"
                }
                "clip" -> "shape"
                "offset" -> when (paramIndex) {
                    1 -> "x"
                    2 -> "y"
                    else -> "param$paramIndex"
                }
                "rotate" -> "degrees"
                "scale" -> "scale"
                "alpha" -> "alpha"
                "clickable" -> "onClick"
                "pointerInput" -> "block"
                else -> when (paramType) {
                    "Dp" -> "dp$paramIndex"
                    "Color" -> "color$paramIndex"
                    "Float" -> "value$paramIndex"
                    "Int" -> "value$paramIndex"
                    "Boolean" -> "enabled$paramIndex"
                    "String" -> "text$paramIndex"
                    else -> "param$paramIndex"
                }
            }
        }
    }
}
