package com.acacia

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider

/**
 * Acacia Shortify Plugin - Modern implementation without afterEvaluate.
 *
 * Uses Android Gradle Plugin's Variant API for proper generated source directory registration.
 * Supports incremental builds by tracking input JARs.
 */
class ShortifyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("shortify", ShortifyExtension::class.java)

        // Register the task without configuring inputs yet (will be done per-variant)
        val generateTask: TaskProvider<GenerateDslTask> = target.tasks.register("generateShortModifiers", GenerateDslTask::class.java) {
            it.enabled.set(extension.enabled)
            it.debug.set(extension.debug)
        }

        // Make all Kotlin compilation tasks depend on generation
        target.tasks.matching { it.name.startsWith("compile") && it.name.endsWith("Kotlin") }.configureEach {
            it.dependsOn(generateTask)
        }

        // Register generated sources with Android using the modern Variant API
        val androidComponents = target.extensions.findByType(AndroidComponentsExtension::class.java)
        if (androidComponents != null) {
            androidComponents.onVariants { variant ->
                // Configure input JARs from the variant's compile classpath for incremental builds
                variant.compileClasspath?.let { classpath ->
                    generateTask.configure { task ->
                        // Filter to only include Compose-related JARs
                        task.inputJars.from(classpath.filter { file ->
                            file.name.contains("compose") ||
                            file.name.startsWith("androidx.compose")
                        })
                    }
                }

                // Register the output directory as a generated source
                variant.sources.java?.addGeneratedSourceDirectory(
                    generateTask,
                    GenerateDslTask::outputDir
                )

                target.logger.lifecycle("Shortify: Registered generated source directory for variant '${variant.name}'")
            }
        } else {
            // Fallback for non-Android projects: configure from compileClasspath configuration
            target.configurations.findByName("compileClasspath")?.let { config ->
                generateTask.configure { task ->
                    task.inputJars.from(config.filter { file ->
                        file.name.contains("compose") ||
                        file.name.startsWith("androidx.compose")
                    })
                }
            }
            
            target.logger.info("Shortify: Android Components extension not found. Skipping automatic source registration.")
            target.logger.info("Shortify: For non-Android projects, manually add the generated directory to your source sets.")
        }
    }
}
