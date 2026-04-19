package com.acacia

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ShortifyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("shortify", ShortifyExtension::class.java)
        val task = target.tasks.register("generateShortModifiers", GenerateDslTask::class.java)
        task.configure { 
            it.enabled.set(extension.enabled)
            it.debug.set(extension.debug)
        }
        
        target.afterEvaluate {
            // Find all Kotlin compile tasks and make them depend on generation
            target.tasks.matching { it.name.startsWith("compile") && it.name.endsWith("Kotlin") }.configureEach {
                it.dependsOn(task)
            }
            
            // Use dynamic extension access for Android
            try {
                val android = target.extensions.getByName("android")
                val sourceSets = android.javaClass.getMethod("getSourceSets").invoke(android)
                val main = sourceSets.javaClass.getMethod("getByName", String::class.java).invoke(sourceSets, "main")
                val java = main.javaClass.getMethod("getJava").invoke(main)
                
                // Use task.map for lazy evaluation
                task.configure { generateTask ->
                    val outputDir = generateTask.outputDir
                    java.javaClass.getMethod("srcDir", Any::class.java).invoke(java, outputDir)
                }
                
                target.logger.lifecycle("Shortify: Added generated source directory to Android sourceSets")
            } catch (e: Exception) {
                target.logger.warn("Shortify: Could not add source directory automatically: ${e.message}")
                task.configure { generateTask ->
                    target.logger.info("Shortify: Generated files are available at: ${generateTask.outputDir}")
                }
                target.logger.info("Shortify: Add this directory manually to your sourceSets if needed")
            }
        }
    }
}
