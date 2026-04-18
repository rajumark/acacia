package com.acacia.resolver

import org.gradle.api.Project
import java.io.File
import java.util.zip.ZipFile
import java.util.zip.ZipEntry

/**
 * Extracts classes.jar from .aar files since Compose libraries ship as .aar archives.
 */
class AarExtractor(private val project: Project) {
    
    private val cacheDir = project.layout.buildDirectory.dir("shortify/cache/jars").get().asFile
    
    /**
     * Converts .aar files to .jar files by extracting classes.jar.
     * Returns a list of .jar files ready for parsing.
     */
    fun extractJars(artifacts: List<DependencyResolver.Artifact>): List<File> {
        cacheDir.mkdirs()
        val jarFiles = mutableListOf<File>()
        
        artifacts.forEach { artifact ->
            val jarFile = when {
                artifact.file.extension == "aar" -> extractFromAar(artifact)
                artifact.file.extension == "jar" -> artifact.file
                else -> null
            }
            
            jarFile?.let { jarFiles.add(it) }
        }
        
        project.logger.lifecycle("Shortify: Extracted ${jarFiles.size} jar files from ${artifacts.size} artifacts")
        return jarFiles
    }
    
    /**
     * Extracts classes.jar from an .aar file.
     */
    private fun extractFromAar(artifact: DependencyResolver.Artifact): File? {
        val aarFile = artifact.file
        val outputJar = File(cacheDir, "${artifact.name}-${artifact.version}.jar")
        
        // Return cached jar if it exists and is newer than the aar
        if (outputJar.exists() && outputJar.lastModified() >= aarFile.lastModified()) {
            return outputJar
        }
        
        try {
            ZipFile(aarFile).use { zip ->
                val classesJarEntry = zip.getEntry("classes.jar")
                if (classesJarEntry != null) {
                    zip.getInputStream(classesJarEntry).use { input ->
                        outputJar.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    project.logger.debug("Shortify: Extracted ${aarFile.name} -> ${outputJar.name}")
                    return outputJar
                } else {
                    project.logger.warn("Shortify: No classes.jar found in ${aarFile.name}")
                }
            }
        } catch (e: Exception) {
            project.logger.warn("Shortify: Failed to extract ${aarFile.name}: ${e.message}")
        }
        
        return null
    }
    
    /**
     * Clears the extraction cache.
     */
    fun clearCache() {
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
            project.logger.lifecycle("Shortify: Cleared AAR extraction cache")
        }
    }
}
