package com.acacia

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/**
 * Acacia Shortify Plugin - Modern implementation without afterEvaluate.
 *
 * Uses Android Gradle Plugin's Variant API for proper generated source directory registration.
 */
class ShortifyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("shortify", ShortifyExtension::class.java)
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
                // Register the output directory as a generated source
                variant.sources.java?.addGeneratedSourceDirectory(
                    generateTask,
                    GenerateDslTask::outputDir
                )

                target.logger.lifecycle("Shortify: Registered generated source directory for variant '${variant.name}'")
            }
        } else {
            // Fallback: Log warning for non-Android projects
            target.logger.info("Shortify: Android Components extension not found. Skipping automatic source registration.")
            target.logger.info("Shortify: For non-Android projects, manually add the generated directory to your source sets.")
        }
    }
}
