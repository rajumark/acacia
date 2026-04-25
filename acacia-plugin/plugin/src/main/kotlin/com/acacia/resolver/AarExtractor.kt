package com.acacia.resolver

import org.gradle.api.Project
import java.io.File
import java.util.zip.ZipFile

/**
 * Extracts classes.jar from AAR files.
 */
class AarExtractor(private val project: Project) {

    private val cacheDir = File(project.buildDir, "acacia/cache/extracted-jars")

    /**
     * Extracts classes.jar from all AAR files.
     */
    fun extractClassesJars(aarFiles: List<File>): List<ExtractedJar> {
        return extractClassesJarsWithCacheInfo(aarFiles)
    }
    
    /**
     * Extracts classes.jar from all AAR files with cache information.
     */
    fun extractClassesJarsWithCacheInfo(aarFiles: List<File>): List<ExtractedJar> {
        val extractedJars = mutableListOf<ExtractedJar>()
        
        // Ensure cache directory exists
        cacheDir.mkdirs()
        
        aarFiles.forEach { aarFile ->
            try {
                val extractedJar = extractClassesJar(aarFile)
                if (extractedJar != null) {
                    extractedJars.add(extractedJar)
                    project.logger.lifecycle("Acacia: Extracted ${extractedJar.jarFile.name} from ${aarFile.name}")
                }
            } catch (e: Exception) {
                project.logger.error("Acacia: Failed to extract from ${aarFile.name}", e)
            }
        }
        
        return extractedJars.sortedBy { it.originalAar.name }
    }

    /**
     * Extracts classes.jar from a single AAR file.
     */
    private fun extractClassesJar(aarFile: File): ExtractedJar? {
        if (!aarFile.exists() || aarFile.extension.lowercase() != "aar") {
            return null
        }

        // Create cache subdirectory for this AAR
        val aarCacheDir = File(cacheDir, aarFile.nameWithoutExtension)
        aarCacheDir.mkdirs()

        // Expected classes.jar path
        val classesJarFile = File(aarCacheDir, "classes.jar")
        
        // Check if already extracted and up-to-date
        val fromCache = classesJarFile.exists() && classesJarFile.lastModified() >= aarFile.lastModified()
        if (fromCache) {
            project.logger.debug("Acacia: Using cached ${classesJarFile.name}")
            return ExtractedJar(aarFile, classesJarFile, fromCache = true)
        }

        // Extract classes.jar from AAR
        return try {
            ZipFile(aarFile).use { zipFile ->
                val entry = zipFile.getEntry("classes.jar")
                if (entry != null) {
                    zipFile.getInputStream(entry).use { input ->
                        classesJarFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    ExtractedJar(aarFile, classesJarFile, fromCache = false)
                } else {
                    project.logger.warn("Acacia: No classes.jar found in ${aarFile.name}")
                    null
                }
            }
        } catch (e: Exception) {
            project.logger.error("Acacia: Error extracting classes.jar from ${aarFile.name}", e)
            null
        }
    }

    /**
     * Gets the cache directory for inspection.
     */
    fun getCacheDirectory(): File = cacheDir
}

/**
 * Represents an extracted JAR file from an AAR.
 */
data class ExtractedJar(
    val originalAar: File,
    val jarFile: File,
    val fromCache: Boolean = false
) {
    val size: Long get() = jarFile.length()
    val name: String get() = jarFile.nameWithoutExtension
}
