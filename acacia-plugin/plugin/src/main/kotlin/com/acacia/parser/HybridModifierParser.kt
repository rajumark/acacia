package com.acacia.parser

import com.acacia.model.ComposableFunction
import com.acacia.model.ModifierFunction
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmValueParameter
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.Metadata
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.File
import java.util.jar.JarFile

/**
 * Hybrid parser that uses ASM to find Modifier extension functions
 * and Kotlin metadata to extract default parameter values.
 */
class HybridModifierParser(private val project: Project) {

    /**
     * Parses jar files using ASM + Kotlin metadata to extract Modifier extension functions
     * with accurate default parameter values.
     */
    fun parseModifierFunctions(jarFiles: List<File>): List<ModifierFunction> {
        val functions = mutableListOf<ModifierFunction>()
        val metadataMap = mutableMapOf<String, Metadata>() // class name -> metadata

        // First pass: collect all Kotlin metadata
        jarFiles.forEach { jarFile ->
            try {
                collectMetadata(jarFile, metadataMap)
            } catch (e: Exception) {
                project.logger.debug("Shortify: Failed to collect metadata from ${jarFile.name}: ${e.message}")
            }
        }

        // Second pass: parse functions with ASM and enrich with metadata
        jarFiles.forEach { jarFile ->
            try {
                val jarFunctions = parseJarFile(jarFile, metadataMap)
                functions.addAll(jarFunctions)
            } catch (e: Exception) {
                project.logger.warn("Shortify: Failed to parse ${jarFile.name}: ${e.message}")
            }
        }

        project.logger.lifecycle("Shortify: Parsed ${functions.size} Modifier functions (with default values from metadata)")
        return functions
    }

    /**
     * Parses jar files to extract @Composable functions.
     * Targets: Column, Row, Box, Text, Button, etc.
     */
    fun parseComposableFunctions(jarFiles: List<File>): List<ComposableFunction> {
        val functions = mutableListOf<ComposableFunction>()
        val metadataMap = mutableMapOf<String, Metadata>()

        // First pass: collect all Kotlin metadata
        jarFiles.forEach { jarFile ->
            try {
                collectMetadata(jarFile, metadataMap)
            } catch (e: Exception) {
                project.logger.debug("Shortify: Failed to collect metadata from ${jarFile.name}: ${e.message}")
            }
        }

        // Second pass: parse composable functions
        jarFiles.forEach { jarFile ->
            try {
                val jarFunctions = parseJarFileForComposables(jarFile, metadataMap)
                functions.addAll(jarFunctions)
            } catch (e: Exception) {
                project.logger.debug("Shortify: Failed to parse composables from ${jarFile.name}: ${e.message}")
            }
        }

        project.logger.lifecycle("Shortify: Parsed ${functions.size} Composable functions")
        return functions
    }

    /**
     * Parses a single jar file for @Composable functions.
     */
    private fun parseJarFileForComposables(jarFile: File, metadataMap: Map<String, Metadata>): List<ComposableFunction> {
        val functions = mutableListOf<ComposableFunction>()

        JarFile(jarFile).use { jar ->
            jar.entries().asSequence()
                .filter { entry ->
                    entry.name.endsWith(".class") &&
                    !entry.name.contains("$") &&
                    !entry.name.contains("META-INF") &&
                    !entry.name.contains("BuildConfig")
                }
                .forEach { entry ->
                    try {
                        jar.getInputStream(entry).use { input ->
                            val bytes = input.readBytes()
                            val className = entry.name.removeSuffix(".class").replace("/", ".")
                            val packageName = className.substringBeforeLast(".", "")
                            val classMetadata = metadataMap[className]

                            val classFunctions = parseClassForComposables(bytes, packageName, classMetadata)
                            functions.addAll(classFunctions)
                        }
                    } catch (e: Exception) {
                        project.logger.debug("Shortify: Failed to parse composables from class ${entry.name}: ${e.message}")
                    }
                }
        }

        return functions
    }

    /**
     * Parses a class to find @Composable functions.
     */
    private fun parseClassForComposables(bytes: ByteArray, packageName: String, metadata: Metadata?): List<ComposableFunction> {
        val functions = mutableListOf<ComposableFunction>()

        // Parse Kotlin metadata if available
        val kmFunctions = if (metadata != null) {
            try {
                when (val km = KotlinClassMetadata.read(metadata)) {
                    is KotlinClassMetadata.Class -> km.kmClass.functions
                    is KotlinClassMetadata.FileFacade -> emptyList() // Simplified for compatibility
                    is KotlinClassMetadata.SyntheticClass -> emptyList()
                    is KotlinClassMetadata.MultiFileClassFacade -> emptyList() // Simplified for compatibility
                    is KotlinClassMetadata.MultiFileClassPart -> emptyList() // Simplified for compatibility
                    is KotlinClassMetadata.Unknown -> emptyList()
                    else -> emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()

        // Create a map of function signature to KmFunction
        val kmFunctionMap = kmFunctions.associateBy { kmFunc ->
            val paramTypes = kmFunc.valueParameters.joinToString(",") { param ->
                param.type.toString()
            }
            "${kmFunc.name}($paramTypes)"
        }

        // Parse with ASM to find @Composable annotations
        val classReader = ClassReader(bytes)
        val visitor = ComposableFunctionVisitor(packageName, kmFunctionMap)
        classReader.accept(visitor, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)

        return visitor.composableFunctions
    }

    /**
     * ASM ClassVisitor that extracts @Composable functions.
     */
    private inner class ComposableFunctionVisitor(
        private val packageName: String,
        private val kmFunctionMap: Map<String, kotlinx.metadata.KmFunction>
    ) : ClassVisitor(Opcodes.ASM9) {
        val composableFunctions = mutableListOf<ComposableFunction>()

        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor? {
            // Must be public static
            if ((access and Opcodes.ACC_PUBLIC) == 0 || (access and Opcodes.ACC_STATIC) == 0) {
                return null
            }

            // Skip synthetic methods and constructors
            if ((access and Opcodes.ACC_SYNTHETIC) != 0 || name == "<init>" || name == "<clinit>") {
                return null
            }

            return ComposableMethodVisitor(name, descriptor, packageName)
        }

        /**
         * ASM MethodVisitor to extract @Composable function details.
         */
        private inner class ComposableMethodVisitor(
            private val methodName: String,
            private val descriptor: String,
            private val packageName: String
        ) : MethodVisitor(Opcodes.ASM9) {
            private var isComposable = false

            override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
                // Check for @Composable annotation
                if (descriptor == "Landroidx/compose/runtime/Composable;") {
                    isComposable = true
                }
                return null
            }

            override fun visitEnd() {
                if (!isComposable) return

                // Skip internal/synthetic functions
                if (methodName.contains("-")) return

                // Target only commonly used composable functions
                if (!isTargetedComposable(methodName)) return

                val argumentTypes = Type.getArgumentTypes(descriptor)
                val parameters = mutableListOf<ComposableFunction.Parameter>()

                // Create signature to lookup in metadata
                val asmParamTypes = argumentTypes.joinToString(",") { it.descriptor }
                val signature = "$methodName($asmParamTypes)"
                val kmFunction = kmFunctionMap[signature]

                // Parse parameters
                for (i in argumentTypes.indices) {
                    val argType = argumentTypes[i]
                    val typeName = simplifyComposableTypeName(argType.descriptor)
                    val kmParam = kmFunction?.valueParameters?.getOrNull(i)
                    val hasDefault = false // Simplified for compatibility
                    val defaultValue = if (hasDefault) {
                        inferComposableDefaultValue(methodName, kmParam, typeName)
                    } else null

                    parameters.add(
                        ComposableFunction.Parameter(
                            name = kmParam?.name ?: generateComposableParamName(methodName, i, typeName),
                            type = typeName,
                            hasDefault = hasDefault,
                            defaultValue = defaultValue
                        )
                    )
                }

                val function = ComposableFunction(
                    name = methodName,
                    packageName = packageName,
                    parameters = parameters,
                    isDeprecated = false
                )

                composableFunctions.add(function)
            }

            private fun isTargetedComposable(name: String): Boolean {
                // Target the most common composable functions
                val targetFunctions = setOf(
                    "Column", "Row", "Box", "Text", "Button", "OutlinedButton", "TextButton",
                    "ElevatedButton", "FilledTonalButton",
                    "Card", "ElevatedCard", "OutlinedCard",
                    "Icon", "IconButton", "IconToggleButton",
                    "LazyColumn", "LazyRow", "LazyVerticalGrid", "LazyHorizontalGrid",
                    "Surface", "Scaffold", "TopAppBar", "BottomAppBar",
                    "NavigationBar", "NavigationBarItem",
                    "FloatingActionButton", "ExtendedFloatingActionButton",
                    "Dialog", "AlertDialog", "Popup",
                    "Checkbox", "Switch", "RadioButton", "Slider",
                    "TextField", "OutlinedTextField", "BasicTextField",
                    "CircularProgressIndicator", "LinearProgressIndicator",
                    "Divider", "Spacer", "VerticalDivider", "HorizontalDivider",
                    "Image", "AsyncImage",
                    "Tab", "TabRow", "ScrollableTabRow",
                    "DrawerSheet", "ModalDrawerSheet", "PermanentDrawerSheet",
                    "ModalNavigationDrawer", "PermanentNavigationDrawer", "DismissibleNavigationDrawer",
                    "DropDownMenu", "DropDownMenuItem",
                    "TooltipBox", "PlainTooltip", "RichTooltip",
                    "Badge", "BadgedBox",
                    "Chip", "AssistChip", "FilterChip", "InputChip", "SuggestionChip",
                    "ListItem", "ListItemDefaults",
                    "Snackbar", "SnackbarHost"
                )
                return name in targetFunctions
            }

            private fun simplifyComposableTypeName(descriptor: String): String {
                return when (descriptor) {
                    "F" -> "Float"
                    "I" -> "Int"
                    "D" -> "Double"
                    "Z" -> "Boolean"
                    "J" -> "Long"
                    "Ljava/lang/String;" -> "String"
                    else -> {
                        if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
                            val className = descriptor.substring(1, descriptor.length - 1).replace("/", ".")
                            when {
                                className == "androidx.compose.ui.Modifier" -> "Modifier"
                                className == "androidx.compose.ui.unit.Dp" -> "Dp"
                                className == "androidx.compose.ui.graphics.Color" -> "Color"
                                className == "androidx.compose.ui.graphics.Shape" -> "Shape"
                                className == "androidx.compose.ui.graphics.painter.Painter" -> "Painter"
                                className == "androidx.compose.ui.graphics.vector.ImageVector" -> "ImageVector"
                                className == "androidx.compose.ui.Alignment" -> "Alignment"
                                className == "androidx.compose.ui.unit.TextUnit" -> "TextUnit"
                                className == "androidx.compose.ui.text.TextStyle" -> "TextStyle"
                                className == "androidx.compose.foundation.layout.Arrangement" -> "Arrangement"
                                className == "androidx.compose.foundation.layout.PaddingValues" -> "PaddingValues"
                                className == "androidx.compose.foundation.gestures.Orientation" -> "Orientation"
                                className == "androidx.compose.ui.window.DialogProperties" -> "DialogProperties"
                                className == "androidx.compose.foundation.interaction.MutableInteractionSource" -> "MutableInteractionSource"
                                className == "androidx.compose.foundation.Indication" -> "Indication"
                                className == "androidx.compose.material3.ButtonColors" -> "ButtonColors"
                                className == "androidx.compose.material3.ButtonElevation" -> "ButtonElevation"
                                className == "androidx.compose.material3.CardColors" -> "CardColors"
                                className == "androidx.compose.material3.CardElevation" -> "CardElevation"
                                className == "androidx.compose.material3.TextFieldColors" -> "TextFieldColors"
                                className == "androidx.compose.material3.TopAppBarColors" -> "TopAppBarColors"
                                className == "androidx.compose.material3.NavigationBarItemColors" -> "NavigationBarItemColors"
                                className.contains("Function0") -> "() -> Unit"
                                className.contains("Function1") -> "(Any) -> Unit"
                                className.contains("Function2") -> "(Any, Any) -> Unit"
                                className.contains("ColumnScope") -> "ColumnScope.() -> Unit"
                                className.contains("RowScope") -> "RowScope.() -> Unit"
                                className.contains("BoxScope") -> "BoxScope.() -> Unit"
                                className.contains("LazyListScope") -> "LazyListScope.() -> Unit"
                                className.contains("LazyGridScope") -> "LazyGridScope.() -> Unit"
                                className.contains("DrawerState") -> "DrawerState"
                                className.contains("SnackbarHostState") -> "SnackbarHostState"
                                else -> className.substringAfterLast(".")
                            }
                        } else {
                            descriptor
                        }
                    }
                }
            }

            private fun inferComposableDefaultValue(
                functionName: String,
                kmParam: KmValueParameter?,
                typeName: String
            ): String? {
                val paramName = kmParam?.name ?: return null

                return when {
                    typeName == "Modifier" -> "Modifier"
                    typeName == "Dp" && paramName == "elevation" -> "0.dp"
                    typeName == "Shape" -> "RectangleShape"
                    typeName == "Boolean" && paramName in listOf("enabled", "checked") -> "true"
                    typeName.contains("Scope.() -> Unit") -> "{}"
                    typeName == "() -> Unit" -> "{}"
                    typeName == "(Any) -> Unit" -> "{}"
                    typeName == "PaddingValues" -> "PaddingValues()"
                    typeName == "MutableInteractionSource" -> "null"
                    typeName == "Indication" -> "null"
                    else -> null
                }
            }

            private fun generateComposableParamName(functionName: String, paramIndex: Int, paramType: String): String {
                return when (paramType) {
                    "Modifier" -> "modifier"
                    "Dp" -> "size"
                    "Color" -> "color"
                    "Float" -> "value"
                    "Int" -> "value"
                    "Boolean" -> "enabled"
                    "String" -> "text"
                    "Shape" -> "shape"
                    "TextStyle" -> "style"
                    "Arrangement" -> "arrangement"
                    "Alignment" -> "alignment"
                    "PaddingValues" -> "contentPadding"
                    "Painter" -> "painter"
                    "ImageVector" -> "imageVector"
                    "() -> Unit" -> "onClick"
                    "ColumnScope.() -> Unit", "RowScope.() -> Unit", "BoxScope.() -> Unit" -> "content"
                    "LazyListScope.() -> Unit", "LazyGridScope.() -> Unit" -> "content"
                    else -> "param$paramIndex"
                }
            }
        }
    }

    /**
     * Collects Kotlin metadata from all classes in a JAR.
     */
    private fun collectMetadata(jarFile: File, metadataMap: MutableMap<String, Metadata>) {
        JarFile(jarFile).use { jar ->
            jar.entries().asSequence()
                .filter { it.name.endsWith(".class") && !it.name.contains("$") }
                .forEach { entry ->
                    try {
                        jar.getInputStream(entry).use { input ->
                            val bytes = input.readBytes()
                            val metadata = extractMetadata(bytes)
                            if (metadata != null) {
                                val className = entry.name.removeSuffix(".class").replace("/", ".")
                                metadataMap[className] = metadata
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
        }
    }

    /**
     * Extracts Kotlin @Metadata annotation from class bytes.
     */
    private fun extractMetadata(bytes: ByteArray): Metadata? {
        var metadata: Metadata? = null

        val classReader = ClassReader(bytes)
        val visitor = object : ClassVisitor(Opcodes.ASM9) {
            override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
                if (descriptor == "Lkotlin/Metadata;") {
                    return object : AnnotationVisitor(Opcodes.ASM9) {
                        private var kind: Int = 0
                        private var metadataVersion: IntArray = intArrayOf()
                        private var data1: Array<String> = emptyArray()
                        private var data2: Array<String> = emptyArray()
                        private var extraString: String? = null
                        private var packageName: String? = null
                        private var extraInt: Int = 0

                        override fun visit(name: String?, value: Any?) {
                            when (name) {
                                "k" -> kind = value as Int
                                "mv" -> metadataVersion = (value as List<*>).filterIsInstance<Int>().toIntArray()
                                "d1" -> data1 = (value as List<*>).filterIsInstance<String>().toTypedArray()
                                "d2" -> data2 = (value as List<*>).filterIsInstance<String>().toTypedArray()
                                "xs" -> extraString = value as String
                                "pn" -> packageName = value as String
                                "xi" -> extraInt = value as Int
                            }
                        }

                        override fun visitEnd() {
                            metadata = Metadata(
                                kind = kind,
                                metadataVersion = metadataVersion,
                                data1 = data1,
                                data2 = data2,
                                extraString = extraString,
                                packageName = packageName,
                                extraInt = extraInt
                            )
                        }
                    }
                }
                return null
            }
        }

        classReader.accept(visitor, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG)
        return metadata
    }

    /**
     * Parses a single jar file using ASM + metadata.
     */
    private fun parseJarFile(jarFile: File, metadataMap: Map<String, Metadata>): List<ModifierFunction> {
        val functions = mutableListOf<ModifierFunction>()

        JarFile(jarFile).use { jar ->
            jar.entries().asSequence()
                .filter { entry ->
                    entry.name.endsWith(".class") &&
                    !entry.name.contains("$") &&
                    !entry.name.contains("META-INF") &&
                    !entry.name.contains("BuildConfig")
                }
                .forEach { entry ->
                    try {
                        jar.getInputStream(entry).use { input ->
                            val bytes = input.readBytes()
                            val className = entry.name.removeSuffix(".class").replace("/", ".")
                            val classMetadata = metadataMap[className]

                            val classFunctions = parseClassWithMetadata(bytes, classMetadata)
                            functions.addAll(classFunctions)
                        }
                    } catch (e: Exception) {
                        project.logger.debug("Shortify: Failed to parse class ${entry.name}: ${e.message}")
                    }
                }
        }

        return functions
    }

    /**
     * Parses a class using ASM and enriches with Kotlin metadata.
     */
    private fun parseClassWithMetadata(bytes: ByteArray, metadata: Metadata?): List<ModifierFunction> {
        val functions = mutableListOf<ModifierFunction>()

        // Parse Kotlin metadata if available
        val kmFunctions = if (metadata != null) {
            try {
                when (val km = KotlinClassMetadata.read(metadata)) {
                    is KotlinClassMetadata.Class -> km.kmClass.functions
                    is KotlinClassMetadata.FileFacade -> emptyList() // Simplified for compatibility
                    is KotlinClassMetadata.SyntheticClass -> emptyList()
                    is KotlinClassMetadata.MultiFileClassFacade -> emptyList() // Simplified for compatibility
                    is KotlinClassMetadata.MultiFileClassPart -> emptyList() // Simplified for compatibility
                    is KotlinClassMetadata.Unknown -> emptyList()
                    else -> emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()

        // Create a map of function signature to KmFunction
        val kmFunctionMap = kmFunctions.associateBy { kmFunc ->
            // Create signature from name and parameters
            val paramTypes = kmFunc.valueParameters.joinToString(",") { param ->
                param.type.toString()
            }
            "${kmFunc.name}($paramTypes)"
        }

        // Parse with ASM
        val classReader = ClassReader(bytes)
        val visitor = ModifierFunctionVisitor(kmFunctionMap)
        classReader.accept(visitor, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)

        return visitor.modifierFunctions
    }

    /**
     * ASM ClassVisitor that extracts Modifier extension functions.
     */
    private inner class ModifierFunctionVisitor(
        private val kmFunctionMap: Map<String, kotlinx.metadata.KmFunction>
    ) : ClassVisitor(Opcodes.ASM9) {
        val modifierFunctions = mutableListOf<ModifierFunction>()

        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor? {
            if (isModifierExtensionFunction(access, name, descriptor)) {
                return ModifierMethodVisitor(name, descriptor, kmFunctionMap)
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

            // Skip synthetic methods
            if ((access and Opcodes.ACC_SYNTHETIC) != 0 || name == "<init>" || name == "<clinit>") {
                return false
            }

            // Must return Modifier
            val returnType = Type.getReturnType(descriptor)
            if (returnType.descriptor != "Landroidx/compose/ui/Modifier;") {
                return false
            }

            // First parameter must be Modifier
            val argumentTypes = Type.getArgumentTypes(descriptor)
            if (argumentTypes.isEmpty() || argumentTypes[0].descriptor != "Landroidx/compose/ui/Modifier;") {
                return false
            }

            // Skip internal functions
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
         * ASM MethodVisitor to extract function details with metadata enrichment.
         */
        private inner class ModifierMethodVisitor(
            private val methodName: String,
            private val descriptor: String,
            private val kmFunctionMap: Map<String, kotlinx.metadata.KmFunction>
        ) : MethodVisitor(Opcodes.ASM9) {

            override fun visitEnd() {
                val argumentTypes = Type.getArgumentTypes(descriptor)
                val parameters = mutableListOf<ModifierFunction.Parameter>()

                // Create signature to lookup in metadata
                val asmParamTypes = argumentTypes.drop(1).joinToString(",") { it.descriptor }
                val signature = "$methodName($asmParamTypes)"

                // Find matching Kotlin metadata function
                val kmFunction = kmFunctionMap[signature]

                // Skip first parameter (Modifier receiver)
                for (i in 1 until argumentTypes.size) {
                    val argType = argumentTypes[i]
                    val typeName = simplifyTypeName(argType.descriptor)
                    val paramIndex = i - 1

                    // Get parameter info from metadata if available
                    val kmParam = kmFunction?.valueParameters?.getOrNull(paramIndex)
                    val hasDefault = false // Simplified for compatibility
                    val defaultValue = if (hasDefault) {
                        inferDefaultValue(methodName, kmParam, typeName)
                    } else null

                    parameters.add(
                        ModifierFunction.Parameter(
                            name = kmParam?.name ?: generateParameterName(methodName, i, typeName),
                            type = typeName,
                            hasDefault = hasDefault,
                            defaultValue = defaultValue
                        )
                    )
                }

                val function = ModifierFunction(
                    name = methodName,
                    parameters = parameters,
                    returnType = "Modifier",
                    isDeprecated = false
                )

                modifierFunctions.add(function)
            }

            /**
             * Infers default value from metadata or common patterns.
             */
            private fun inferDefaultValue(
                functionName: String,
                kmParam: KmValueParameter?,
                typeName: String
            ): String? {
                val paramName = kmParam?.name ?: return null

                // Common default value patterns
                return when {
                    // Dp parameters
                    typeName == "Dp" && paramName == "all" -> "0.dp"
                    typeName == "Dp" && paramName in listOf("horizontal", "vertical", "start", "end", "top", "bottom") -> "0.dp"
                    typeName == "Dp" && paramName == "elevation" -> "0.dp"
                    typeName == "Dp" && paramName == "width" -> "0.dp"
                    typeName == "Dp" && paramName == "height" -> "0.dp"
                    typeName == "Dp" && paramName == "size" -> "0.dp"
                    typeName == "Dp" && paramName == "padding" -> "0.dp"

                    // Color parameters
                    typeName == "Color" && paramName == "color" -> "Color.Unspecified"
                    typeName == "Color" && paramName == "backgroundColor" -> "Color.Unspecified"

                    // Float parameters
                    typeName == "Float" && paramName == "alpha" -> "1f"
                    typeName == "Float" && paramName == "scale" -> "1f"
                    typeName == "Float" && paramName == "weight" -> "1f"

                    // Boolean parameters
                    typeName == "Boolean" && paramName in listOf("enabled", "checked", "visible", "selected") -> "true"
                    typeName == "Boolean" && paramName in listOf("clip", "fill", "matchHeightConstraintsFirst") -> "true"

                    // Shape parameters
                    typeName == "Shape" -> "RectangleShape"

                    // Brush parameters
                    typeName == "Brush" -> "null"

                    // Alignment parameters
                    typeName == "Alignment" -> "Alignment.Center"
                    typeName == "Alignment.Vertical" -> "Alignment.Top"
                    typeName == "Alignment.Horizontal" -> "Alignment.Start"

                    // ContentScale
                    typeName == "ContentScale" -> "ContentScale.Fit"

                    // Other common patterns
                    typeName == "BorderStroke" -> "null"
                    typeName == "MutableInteractionSource" -> "null"
                    typeName == "Indication" -> "null"

                    // Function types (lambdas)
                    typeName.contains("->") -> "{}"

                    else -> null
                }
            }

            /**
             * Simplifies type names for better readability.
             */
            private fun simplifyTypeName(descriptor: String): String {
                return when (descriptor) {
                    "F" -> "Float"
                    "I" -> "Int"
                    "D" -> "Double"
                    "Z" -> "Boolean"
                    "J" -> "Long"
                    "Ljava/lang/String;" -> "String"
                    else -> {
                        if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
                            val className = descriptor.substring(1, descriptor.length - 1).replace("/", ".")
                            // Simplify common Compose types
                            when {
                                className == "androidx.compose.ui.unit.Dp" -> "Dp"
                                className == "androidx.compose.ui.graphics.Color" -> "Color"
                                className == "androidx.compose.ui.graphics.Shape" -> "Shape"
                                className == "androidx.compose.ui.graphics.Brush" -> "Brush"
                                className == "androidx.compose.ui.Modifier" -> "Modifier"
                                className == "androidx.compose.ui.Alignment" -> "Alignment"
                                className == "androidx.compose.ui.layout.ContentScale" -> "ContentScale"
                                className == "androidx.compose.ui.unit.Constraints" -> "Constraints"
                                className == "androidx.compose.foundation.BorderStroke" -> "BorderStroke"
                                className == "androidx.compose.foundation.gestures.Orientation" -> "Orientation"
                                className == "androidx.compose.foundation.interaction.MutableInteractionSource" -> "MutableInteractionSource"
                                className == "androidx.compose.foundation.Indication" -> "Indication"
                                className.startsWith("kotlin.jvm.functions.Function") -> "() -> Unit"
                                else -> className.substringAfterLast(".")
                            }
                        } else {
                            descriptor
                        }
                    }
                }
            }

            /**
             * Generates meaningful parameter names.
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
                    "paddingFrom" -> when (paramIndex) {
                        1 -> "alignment"
                        2 -> "before"
                        3 -> "after"
                        else -> "param$paramIndex"
                    }
                    "size", "requiredSize" -> when (paramIndex) {
                        1 -> "width"
                        2 -> "height"
                        else -> "param$paramIndex"
                    }
                    "width", "requiredWidth", "height", "requiredHeight" -> "value"
                    "widthIn", "heightIn" -> when (paramIndex) {
                        1 -> "min"
                        2 -> "max"
                        else -> "param$paramIndex"
                    }
                    "background" -> when (paramIndex) {
                        1 -> "color"
                        2 -> "shape"
                        else -> "param$paramIndex"
                    }
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
                    "alpha" -> "alpha"
                    "rotate" -> "degrees"
                    "scale" -> when (paramIndex) {
                        1 -> "scaleX"
                        2 -> "scaleY"
                        else -> "param$paramIndex"
                    }
                    "offset" -> when (paramIndex) {
                        1 -> "x"
                        2 -> "y"
                        else -> "param$paramIndex"
                    }
                    "absoluteOffset" -> when (paramIndex) {
                        1 -> "x"
                        2 -> "y"
                        else -> "param$paramIndex"
                    }
                    "aspectRatio" -> when (paramIndex) {
                        1 -> "ratio"
                        2 -> "matchHeightConstraintsFirst"
                        else -> "param$paramIndex"
                    }
                    "weight" -> when (paramIndex) {
                        1 -> "weight"
                        2 -> "fill"
                        else -> "param$paramIndex"
                    }
                    "defaultMinSize" -> when (paramIndex) {
                        1 -> "minWidth"
                        2 -> "minHeight"
                        else -> "param$paramIndex"
                    }
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
}
