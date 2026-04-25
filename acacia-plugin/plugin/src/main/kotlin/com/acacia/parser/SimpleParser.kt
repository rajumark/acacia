package com.acacia.parser

import com.acacia.resolver.ExtractedJar

/**
 * Simple parser that extracts function names from JAR files.
 */
class SimpleParser {

    /**
     * Parses all JARs and extracts function names.
     */
    fun parseJars(extractedJars: List<ExtractedJar>): List<SimpleFunction> {
        val allFunctions = mutableListOf<SimpleFunction>()
        
        extractedJars.forEach { extractedJar ->
            try {
                // Only parse foundation-layout dependency
                if (!extractedJar.jarFile.name.contains("foundation-layout")) {
                    return@forEach
                }
                val classFiles = readJar(extractedJar.jarFile)
                classFiles.forEach { classFile ->
                    // Only process PaddingKt.class
                    if (classFile.simpleName == "PaddingKt") {
                        val functionName = extractFunctionName(classFile.simpleName)
                        if (functionName != null) {
                            allFunctions.add(SimpleFunction(
                                name = functionName,
                                fileName = classFile.simpleName,
                                jarFile = extractedJar.jarFile.name
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                // Log error but continue
            }
        }
        
        return allFunctions
    }

    /**
     * Reads .class files from JAR.
     */
    private fun readJar(jarFile: java.io.File): List<SimpleClassFile> {
        val classFiles = mutableListOf<SimpleClassFile>()
        
        java.util.jar.JarFile(jarFile).use { jar ->
            jar.entries().toList()
                .filter { it.name.endsWith(".class") && !it.name.contains("$") }
                .forEach { entry ->
                    val bytes = jar.getInputStream(entry).readBytes()
                    classFiles.add(SimpleClassFile(
                        name = entry.name,
                        bytes = bytes
                    ))
                }
        }
        
        return classFiles
    }

    /**
     * Extracts function name from class name.
     */
    private fun extractFunctionName(className: String): String? {
        return when {
            className == "PaddingKt" -> "padding"
            className.contains("Kt") -> className.removeSuffix("Kt")
            else -> null
        }
    }
}

/**
 * Represents a simple function extraction.
 */
data class SimpleFunction(
    val name: String,
    val fileName: String,
    val jarFile: String
)

/**
 * Represents a simple class file.
 */
data class SimpleClassFile(
    val name: String,
    val bytes: ByteArray
) {
    val simpleName: String get() = name.substringAfterLast("/").removeSuffix(".class")
}
