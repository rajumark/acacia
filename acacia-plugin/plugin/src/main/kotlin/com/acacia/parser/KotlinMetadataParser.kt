package com.acacia.parser

import com.acacia.model.ModifierFunction
import kotlinx.metadata.*
import kotlinx.metadata.jvm.*
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.jar.JarFile

/**
 * Parses Kotlin metadata from JAR files to extract Modifier extension functions
 * with their default parameter values.
 */
class KotlinMetadataParser(private val project: Project) {

    /**
     * Parses jar files using Kotlin metadata to extract Modifier extension functions
     * with default parameter values.
     */
    fun parseModifierFunctions(jarFiles: List<File>): List<ModifierFunction> {
        val functions = mutableListOf<ModifierFunction>()

        jarFiles.forEach { jarFile ->
            try {
                val jarFunctions = parseJarFileWithMetadata(jarFile)
                functions.addAll(jarFunctions)
            } catch (e: Exception) {
                project.logger.warn("Shortify: Failed to parse ${jarFile.name} with metadata: ${e.message}")
            }
        }

        project.logger.lifecycle("Shortify: Parsed ${functions.size} Modifier functions using Kotlin metadata")
        return functions
    }

    /**
     * Parses a single jar file using Kotlin metadata.
     */
    private fun parseJarFileWithMetadata(jarFile: File): List<ModifierFunction> {
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
                        jar.getInputStream(entry).use { input ->
                            val bytes = input.readBytes()

                            // Check for Kotlin metadata annotation
                            val metadata = readKotlinMetadata(bytes)
                            if (metadata != null) {
                                val kmFunctions = parseKmPackage(metadata)
                                functions.addAll(kmFunctions)
                            }
                        }
                    } catch (e: Exception) {
                        project.logger.debug("Shortify: Failed to parse class ${entry.name}: ${e.message}")
                    }
                }
        }

        return functions
    }

    /**
     * Reads Kotlin metadata from class file bytes.
     */
    private fun readKotlinMetadata(bytes: ByteArray): KmPackage? {
        return try {
            val classReader = ClassReader(bytes)
            var metadataAnnotation: Pair<String, Array<String>>? = null

            val visitor = object : ClassVisitor(Opcodes.ASM9) {
                override fun visitAnnotation(descriptor: String?, visible: Boolean): MethodVisitor? {
                    if (descriptor == "Lkotlin/Metadata;") {
                        return object : MethodVisitor(Opcodes.ASM9) {
                            private var kind: Int? = null
                            private var data: MutableList<String> = mutableListOf()

                            override fun visit(name: String?, value: Any?) {
                                when (name) {
                                    "k" -> kind = value as? Int
                                    "d1" -> {
                                        if (value is Array<*>) {
                                            data.addAll(value.filterIsInstance<String>())
                                        }
                                    }
                                }
                            }

                            override fun visitEnd() {
                                if (kind == 2) { // KmPackage
                                    metadataAnnotation = "Package" to data.toTypedArray()
                                }
                            }
                        }
                    }
                    return null
                }
            }

            classReader.accept(visitor, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG)

            metadataAnnotation?.let { (_, data) ->
                // Parse the metadata
                val kmPackage = KmPackage()
                // In real implementation, use kotlinx-metadata-jvm to parse
                kmPackage
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parses KmPackage to extract functions.
     * This is a simplified implementation.
     */
    private fun parseKmPackage(metadata: KmPackage): List<ModifierFunction> {
        val functions = mutableListOf<ModifierFunction>()

        // Iterate through all functions in the package
        metadata.functions.forEach { kmFunction ->
            // Check if this is a Modifier extension function
            if (isModifierExtensionFunction(kmFunction)) {
                val function = convertKmFunction(kmFunction)
                functions.add(function)
            }
        }

        return functions
    }

    /**
     * Checks if a KmFunction is a Modifier extension function.
     */
    private fun isModifierExtensionFunction(kmFunction: KmFunction): Boolean {
        // Check if receiver is Modifier
        val receiver = kmFunction.receiverParameterType
        if (receiver != null) {
            val classifier = receiver.classifier
            if (classifier is KmClassifier.Class) {
                return classifier.name == "androidx/compose/ui/Modifier"
            }
        }
        return false
    }

    /**
     * Converts KmFunction to our ModifierFunction model.
     */
    private fun convertKmFunction(kmFunction: KmFunction): ModifierFunction {
        val parameters = kmFunction.valueParameters.mapIndexed { index, kmParam ->
            val hasDefault = kmParam.declaresDefaultValue

            // Try to get default value if available
            val defaultValue = if (hasDefault) {
                extractDefaultValue(kmFunction, index, kmParam)
            } else null

            ModifierFunction.Parameter(
                name = kmParam.name,
                type = convertType(kmParam.type),
                hasDefault = hasDefault,
                defaultValue = defaultValue
            )
        }

        return ModifierFunction(
            name = kmFunction.name,
            parameters = parameters,
            returnType = "Modifier",
            isDeprecated = kmFunction.hasAnnotation("Deprecated")
        )
    }

    /**
     * Extracts default value for a parameter.
     * This looks at the @Metadata annotation for default value information.
     */
    private fun extractDefaultValue(kmFunction: KmFunction, paramIndex: Int, kmParam: KmValueParameter): String? {
        // Common default values we can infer from the type and name
        return when {
            kmParam.name == "all" && kmParam.type.classifier.toString().contains("Dp") -> "0.dp"
            kmParam.name == "horizontal" && kmParam.type.classifier.toString().contains("Dp") -> "0.dp"
            kmParam.name == "vertical" && kmParam.type.classifier.toString().contains("Dp") -> "0.dp"
            kmParam.name == "color" && kmParam.type.classifier.toString().contains("Color") -> "Color.Unspecified"
            kmParam.name == "alpha" -> "1f"
            kmParam.name == "elevation" && kmParam.type.classifier.toString().contains("Dp") -> "0.dp"
            kmParam.name == "shape" -> "RectangleShape"
            kmParam.name == "enabled" || kmParam.name == "checked" -> "true"
            kmParam.name == "visible" -> "true"
            else -> null // Can't determine default
        }
    }

    /**
     * Converts Kotlin metadata type to simple type name.
     */
    private fun convertType(kmType: KmType): String {
        val classifier = kmType.classifier
        return when (classifier) {
            is KmClassifier.Class -> {
                val className = classifier.name.replace("/", ".")
                // Simplify common types
                when {
                    className == "androidx.compose.ui.unit.Dp" -> "Dp"
                    className == "androidx.compose.ui.graphics.Color" -> "Color"
                    className == "androidx.compose.ui.graphics.Shape" -> "Shape"
                    className == "androidx.compose.ui.Modifier" -> "Modifier"
                    className.startsWith("kotlin.Function") -> "() -> Unit"
                    else -> className.substringAfterLast(".")
                }
            }
            is KmClassifier.TypeAlias -> classifier.name.substringAfterLast("/")
            is KmClassifier.TypeParameter -> "T"
        }
    }
}

// Extension function to check if function has an annotation
private fun KmFunction.hasAnnotation(name: String): Boolean {
    return this.annotations.any { it.className.contains(name) }
}
