package com.acacia.index

import com.acacia.parser.SimpleFunction
import com.acacia.resolver.ExtractedJar
import kotlinx.metadata.jvm.KotlinClassMetadata
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes

/**
 * API Index for organizing and accessing parsed functions.
 */
class ApiIndex {

    /**
     * Builds API index by parsing ALL classes from ALL JARs.
     * Proper architecture: JAR -> parse ALL classes -> extract ALL functions -> build index
     */
    fun buildApiIndex(extractedJars: List<ExtractedJar>): Map<String, List<ApiFunction>> {
        val allApiFunctions = mutableListOf<ApiFunction>()
        
        extractedJars.forEach { jar ->
            val functions = parseAllClassesInJar(jar.jarFile, jar.originalAar.name)
            allApiFunctions.addAll(functions)
        }

        return allApiFunctions.groupBy { it.receiver ?: "no-receiver" }
    }

    /**
     * Parses all classes in a JAR and extracts all functions.
     */
    private fun parseAllClassesInJar(jarFile: java.io.File, aarName: String): List<ApiFunction> {
        val functions = mutableListOf<ApiFunction>()
        
        try {
            java.util.jar.JarFile(jarFile).use { jar ->
                jar.entries().toList()
                    .filter { it.name.endsWith(".class") && !it.name.contains("$") }
                    .forEach { entry ->
                        // Debug logging for PaddingKt
                        if (entry.name.contains("PaddingKt")) {
                            println("DEBUG: Found PaddingKt class: ${entry.name}")
                        }
                        
                        val bytes = jar.getInputStream(entry).readBytes()
                        val classFunctions = parseClass(bytes, aarName)
                        
                        // Debug logging for PaddingKt results
                        if (entry.name.contains("PaddingKt")) {
                            println("DEBUG: PaddingKt functions found: ${classFunctions.size}")
                        }
                        
                        functions.addAll(classFunctions)
                    }
            }
        } catch (e: Exception) {
            println("DEBUG: Error parsing JAR ${jarFile.name}: ${e.message}")
        }
        
        return functions
    }

    /**
     * Parses a single class and extracts all functions using Kotlin metadata.
     */
    private fun parseClass(bytes: ByteArray, aarName: String): List<ApiFunction> {
        return try {
            val metadata = extractMetadata(bytes)
            if (metadata != null) {
                parseKotlinMetadata(metadata, aarName)
            } else {
                println("DEBUG: No metadata found for class")
                emptyList()
            }
        } catch (e: Exception) {
            println("DEBUG: Error parsing class: ${e.message}")
            emptyList()
        }
    }

    /**
     * Parses Kotlin metadata to extract real function signatures.
     */
    private fun parseKotlinMetadata(metadata: KotlinClassMetadata, aarName: String): List<ApiFunction> {
        return try {
            when (metadata) {
                is KotlinClassMetadata.FileFacade -> {
                    val pkg = metadata.kmPackage
                    val functions = pkg.functions
                    
                    // Debug logging for PaddingKt
                    if (functions.isNotEmpty()) {
                        val firstFunction = functions.first()
                        println("DEBUG: FileFacade with ${functions.size} functions")
                        println("DEBUG: First function: ${firstFunction.name}")
                        println("DEBUG: Has receiver: ${firstFunction.receiverParameterType != null}")
                    }
                    
                    functions.map { fn ->
                        ApiFunction(
                            name = fn.name,
                            receiver = fn.receiverParameterType?.let { renderType(it) },
                            params = fn.valueParameters.map {
                                Param(it.name ?: "param", renderType(it.type))
                            },
                            returnType = renderType(fn.returnType),
                            source = "FileFacade ($aarName)"
                        )
                    }
                }
                is KotlinClassMetadata.Class -> {
                    val clazz = metadata.kmClass
                    val functions = clazz.functions
                    
                    if (functions.isNotEmpty()) {
                        println("DEBUG: Class with ${functions.size} functions")
                    }
                    
                    functions.map { fn ->
                        ApiFunction(
                            name = fn.name,
                            receiver = fn.receiverParameterType?.let { renderType(it) },
                            params = fn.valueParameters.map {
                                Param(it.name ?: "param", renderType(it.type))
                            },
                            returnType = renderType(fn.returnType),
                            source = "Class ($aarName)"
                        )
                    }
                }
                else -> {
                    println("DEBUG: Unknown metadata type: ${metadata::class.simpleName}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            println("DEBUG: Error parsing metadata: ${e.message}")
            emptyList()
        }
    }

    /**
     * Renders KmType to readable string.
     */
    private fun renderType(type: kotlinx.metadata.KmType): String {
        val classifier = type.classifier
        val result = when (classifier) {
            is kotlinx.metadata.KmClassifier.Class -> {
                val fullName = classifier.name
                val shortName = fullName.substringAfterLast('.')
                val isNullable = type.toString().contains("?")
                
                // Special case: return "Modifier" for the base Modifier type
                val finalName = if (fullName == "androidx/compose/ui/Modifier") {
                    "Modifier"
                } else {
                    shortName
                }
                
                // Debug logging for Modifier types
                if (fullName.contains("Modifier")) {
                    println("DEBUG: Found Modifier type: $fullName -> $finalName")
                }
                
                if (isNullable) "$finalName?" else finalName
            }
            else -> {
                println("DEBUG: Unknown classifier: $classifier")
                "Any"
            }
        }
        return result
    }

    /**
     * Extracts REAL Kotlin metadata from bytecode using ASM.
     */
    private fun extractMetadata(bytes: ByteArray): KotlinClassMetadata? {
        return try {
            val classReader = ClassReader(bytes)
            val visitor = MetadataExtractor()
            classReader.accept(visitor, 0)
            val metadata = visitor.getMetadata()
            if (metadata != null) {
                KotlinClassMetadata.readLenient(metadata)
            } else null
        } catch (e: Exception) {
            println("DEBUG: Error extracting metadata: ${e.message}")
            null
        }
    }

    /**
     * ASM visitor to extract real Kotlin @Metadata annotation.
     */
    private class MetadataExtractor : ClassVisitor(Opcodes.ASM9) {
        private var kind: Int? = null
        private var metadataVersion: IntArray? = null
        private var bytecodeVersion: IntArray? = null
        private var data1: Array<String>? = null
        private var data2: Array<String>? = null
        private var extraString: String? = null
        private var packageName: String? = null
        private var extraInt: Int? = null

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            println("DEBUG: Found annotation: $descriptor (visible: $visible)")
            if (descriptor == "Lkotlin/Metadata;") {
                println("DEBUG: Found Kotlin @Metadata annotation!")
                return MetadataAnnotationVisitor()
            }
            return null
        }

        fun getMetadata(): kotlin.Metadata? {
            return if (kind != null && metadataVersion != null) {
                @Suppress("UNCHECKED_CAST")
                kotlin.Metadata(
                    kind!!,
                    metadataVersion!!,
                    bytecodeVersion ?: intArrayOf(),
                    data1 ?: emptyArray<String>(),
                    data2 ?: emptyArray<String>(),
                    extraString ?: "",
                    packageName ?: "",
                    extraInt ?: 0
                )
            } else null
        }

        private inner class MetadataAnnotationVisitor : AnnotationVisitor(Opcodes.ASM9) {
            override fun visit(name: String, value: Any) {
                println("DEBUG: Metadata annotation value: $name = $value (${value::class.simpleName})")
                when (name) {
                    "k" -> kind = value as Int
                    "xs" -> extraString = value as String
                    "pn" -> packageName = value as String
                    "xi" -> extraInt = value as Int
                    "mv" -> {
                        // ASM sometimes passes arrays as single values
                        if (value is IntArray) {
                            metadataVersion = value
                            println("DEBUG: Directly captured mv: ${value.contentToString()}")
                        }
                    }
                    "bv" -> {
                        if (value is IntArray) {
                            bytecodeVersion = value
                            println("DEBUG: Directly captured bv: ${value.contentToString()}")
                        }
                    }
                    "d1" -> {
                        if (value is Array<*>) {
                            @Suppress("UNCHECKED_CAST")
                            data1 = value as Array<String>
                            println("DEBUG: Directly captured d1: ${data1?.size}")
                        }
                    }
                    "d2" -> {
                        if (value is Array<*>) {
                            @Suppress("UNCHECKED_CAST")
                            data2 = value as Array<String>
                            println("DEBUG: Directly captured d2: ${data2?.size}")
                        }
                    }
                }
            }

            override fun visitArray(name: String): AnnotationVisitor? {
                println("DEBUG: Metadata annotation array: $name")
                return when (name) {
                    "mv" -> {
                        IntArrayVisitor { metadataVersion = it }
                    }
                    "bv" -> {
                        IntArrayVisitor { bytecodeVersion = it }
                    }
                    "d1" -> {
                        StringArrayVisitor { data1 = it }
                    }
                    "d2" -> {
                        StringArrayVisitor { data2 = it }
                    }
                    else -> null
                }
            }
            
            override fun visitEnd() {
                println("DEBUG: Metadata annotation complete. kind=$kind, mv=${metadataVersion?.contentToString()}, d1=${data1?.size}, d2=${data2?.size}")
            }
        }

        private class IntArrayVisitor(private val onArray: (IntArray) -> Unit) : AnnotationVisitor(Opcodes.ASM9) {
            private val ints = mutableListOf<Int>()

            override fun visit(name: String?, value: Any) {
                println("DEBUG: IntArrayVisitor: $name = $value (${value::class.simpleName})")
                if (value is Int) {
                    ints.add(value)
                }
            }

            override fun visitEnd() {
                println("DEBUG: IntArrayVisitor complete: [${ints.joinToString()}]")
                onArray(ints.toIntArray())
            }
        }

        private class StringArrayVisitor(private val onArray: (Array<String>) -> Unit) : AnnotationVisitor(Opcodes.ASM9) {
            private val strings = mutableListOf<String>()

            override fun visit(name: String?, value: Any) {
                if (value is String) {
                    strings.add(value)
                }
            }

            override fun visitEnd() {
                onArray(strings.toTypedArray())
            }
        }
    }

    /**
     * Gets all Modifier extension functions.
     */
    fun getModifierFunctions(apiIndex: Map<String, List<ApiFunction>>): List<ApiFunction> {
        return apiIndex["Modifier"] ?: emptyList()
    }

    /**
     * Renders function signature properly.
     */
    fun renderFunction(fn: ApiFunction): String {
        val receiver = fn.receiver?.let { "$it." } ?: ""
        val params = fn.params.joinToString(", ") {
            "${it.name}: ${cleanType(it.type)}"
        }
        val returnType = cleanReturnType(fn.returnType, fn.receiver)
        return "fun $receiver${fn.name}($params): $returnType"
    }

    /**
     * Cleans up type names for better readability.
     */
    private fun cleanType(type: String): String {
        return when {
            type.contains("androidx.compose.foundation.layout.Arrangement") -> "Arrangement"
            type.contains("androidx.compose.ui.Alignment") -> "Alignment"
            type.contains("androidx.compose.runtime.Composer") -> "Composer"
            type.contains("kotlin.jvm.functions.Function") -> "Function"
            type.contains("androidx.compose.ui.unit.Dp") -> "Dp"
            type.contains("androidx.compose.ui.unit") -> "Unit"
            type.contains("java.lang.String") -> "String"
            type.contains("int") -> "Int"
            type.contains("boolean") -> "Boolean"
            type.contains("void") -> "Unit"
            else -> type.substringAfterLast(".")
        }
    }

    /**
     * Cleans up return type for extension functions.
     */
    private fun cleanReturnType(returnType: String, receiver: String?): String {
        return when {
            receiver == "Modifier" && returnType == "void" -> "Modifier"
            returnType == "void" -> "Unit"
            else -> cleanType(returnType)
        }
    }

    /**
     * Renders all functions in a group.
     */
    fun renderGroup(receiverType: String, functions: List<ApiFunction>): String {
        val header = "// $receiverType functions (${functions.size})"
        val rendered = functions.map { renderFunction(it) }.joinToString("\n")
        return "$header\n$rendered"
    }
}

/**
 * Represents a proper API function with full signature information.
 */
data class ApiFunction(
    val name: String,
    val receiver: String?,
    val params: List<Param>,
    val returnType: String,
    val source: String
)

/**
 * Represents a function parameter.
 */
data class Param(
    val name: String,
    val type: String
)
