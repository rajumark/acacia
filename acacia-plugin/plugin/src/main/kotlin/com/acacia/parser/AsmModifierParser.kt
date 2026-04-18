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
                            val classReader = ClassReader(input)
                            val visitor = ModifierFunctionVisitor()
                            classReader.accept(visitor, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
                            
                            functions.addAll(visitor.modifierFunctions)
                        }
                    } catch (e: Exception) {
                        project.logger.debug("Shortify: Failed to parse class ${entry.name}: ${e.message}")
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
            
            // Must return Modifier
            val returnType = Type.getReturnType(descriptor)
            if (returnType.className != "androidx.compose.ui.Modifier") {
                return false
            }
            
            // First parameter must be Modifier (the receiver)
            val argumentTypes = Type.getArgumentTypes(descriptor)
            if (argumentTypes.isEmpty() || argumentTypes[0].className != "androidx.compose.ui.Modifier") {
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
                
                // Skip first parameter (the Modifier receiver)
                for (i in 1 until argumentTypes.size) {
                    val argType = argumentTypes[i]
                    val paramName = "param$i" // ASM doesn't preserve parameter names without debug info
                    val typeName = simplifyTypeName(argType.className)
                    
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
                "androidx.compose.foundation.layout.Arrangement" -> "Arrangement"
                "androidx.compose.foundation.layout.Alignment" -> "Alignment"
                "kotlin.Float" -> "Float"
                "kotlin.Int" -> "Int"
                "kotlin.Boolean" -> "Boolean"
                else -> className.substringAfterLast(".")
            }
        }
    }
}
