package com.acacia.parser

import com.acacia.model.ModifierFunction
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.File
import java.util.jar.JarFile

/**
 * Parses jar files to extract Modifier extension functions.
 * Uses bytecode analysis instead of reflection for build-time safety.
 */
class ModifierParser(private val project: Project) {
    
    /**
     * Parses jar files and extracts Modifier extension functions.
     */
    fun parseModifierFunctions(jarFiles: List<File>): List<ModifierFunction> {
        val functions = mutableListOf<ModifierFunction>()
        
        jarFiles.forEach { jarFile ->
            try {
                val jarFunctions = parseJarFile(jarFile)
                functions.addAll(jarFunctions)
                
                if (jarFunctions.isNotEmpty()) {
                    project.logger.debug("Shortify: Found ${jarFunctions.size} functions in ${jarFile.name}")
                }
            } catch (e: Exception) {
                project.logger.warn("Shortify: Failed to parse ${jarFile.name}: ${e.message}")
            }
        }
        
        project.logger.lifecycle("Shortify: Parsed ${functions.size} Modifier functions from ${jarFiles.size} jar files")
        return functions
    }
    
    /**
     * Parses a single jar file for Modifier extension functions using ASM bytecode analysis.
     */
    private fun parseJarFile(jarFile: File): List<ModifierFunction> {
        val functions = mutableListOf<ModifierFunction>()
        
        JarFile(jarFile).use { jar ->
            jar.entries().asSequence()
                .filter { entry -> 
                    entry.name.endsWith(".class") && 
                    !entry.name.contains("$") && 
                    !entry.name.contains("META-INF")
                }
                .forEach { entry ->
                    try {
                        val inputStream = jar.getInputStream(entry)
                        val classReader = ClassReader(inputStream)
                        val visitor = ModifierClassVisitor()
                        
                        classReader.accept(visitor, ClassReader.SKIP_CODE)
                        functions.addAll(visitor.modifierFunctions)
                        
                        inputStream.close()
                        
                    } catch (e: Exception) {
                        project.logger.debug("Shortify: Failed to parse class ${entry.name}: ${e.message}")
                    }
                }
        }
        
        return functions
    }
    
    /**
     * ASM ClassVisitor to extract Modifier extension functions from bytecode.
     */
    private inner class ModifierClassVisitor : ClassVisitor(Opcodes.ASM9) {
        val modifierFunctions = mutableListOf<ModifierFunction>()
        
        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            super.visit(version, access, name, signature, superName, interfaces)
        }
        
        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor? {
            
            // Check if this is a static method that returns Modifier
            // Modifier extension functions are compiled as static functions taking Modifier as first parameter
            if (isModifierExtensionFunction(descriptor)) {
                return ModifierMethodVisitor(name, descriptor)
            }
            
            return null
        }
        
        private fun isModifierExtensionFunction(descriptor: String): Boolean {
            // Check if method returns Modifier and takes Modifier as first parameter
            val methodType = Type.getMethodType(descriptor)
            val returnType = methodType.returnType.className
            
            // Check if return type is Modifier
            if (returnType != "androidx.compose.ui.Modifier") {
                return false
            }
            
            // Check if first parameter is Modifier (extension function receiver)
            val argumentTypes = methodType.argumentTypes
            return argumentTypes.isNotEmpty() && 
                   argumentTypes[0].className == "androidx.compose.ui.Modifier"
        }
        
        private inner class ModifierMethodVisitor(
            private val methodName: String,
            private val methodDescriptor: String
        ) : MethodVisitor(Opcodes.ASM9) {
            
            override fun visitEnd() {
                super.visitEnd()
                
                val methodType = Type.getMethodType(methodDescriptor)
                val argumentTypes = methodType.argumentTypes
                
                // Debug logging
                project.logger.lifecycle("Shortify: Found method '$methodName' with descriptor '$methodDescriptor'")
                project.logger.lifecycle("Shortify: Argument types: ${argumentTypes.map { it.className }}")
                
                // Skip the first parameter (Modifier receiver)
                val parameters = argumentTypes.drop(1).mapIndexed { index, argType ->
                    val typeName = mapAsmTypeToKotlinType(argType)
                    project.logger.lifecycle("Shortify: Parameter ${index + 1}: ${argType.className} -> $typeName")
                    ModifierFunction.Parameter(
                        name = "param${index + 1}", // Will be enhanced with real parameter names
                        type = typeName
                    )
                }
                
                val modifierFunction = ModifierFunction(
                    name = methodName,
                    parameters = parameters
                )
                
                modifierFunctions.add(modifierFunction)
            }
            
            private fun mapAsmTypeToKotlinType(asmType: Type): String {
                return when (asmType.className) {
                    "androidx.compose.ui.unit.Dp" -> "Dp"
                    "androidx.compose.ui.graphics.Color" -> "Color"
                    "androidx.compose.ui.graphics.Shape" -> "Shape"
                    "androidx.compose.ui.graphics.Brush" -> "Brush"
                    "androidx.compose.foundation.BorderStroke" -> "BorderStroke"
                    "kotlin.Float" -> "Float"
                    "kotlin.Int" -> "Int"
                    "kotlin.Boolean" -> "Boolean"
                    "kotlin.Function1" -> "Function1"
                    "kotlin.Function2" -> "Function2"
                    "kotlin.Function3" -> "Function3"
                    else -> {
                        // Extract simple name from fully qualified class name
                        val className = asmType.className
                        className.substringAfterLast(".")
                    }
                }
            }
        }
    }
}
