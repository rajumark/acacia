package com.acacia

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/**
 * Acacia Plugin - Minimal Hello World template.
 */
class ShortifyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Create the extension for configuration
        val extension = target.extensions.create("shortify", ShortifyExtension::class.java)

        // Register the simple task
        val helloTask: TaskProvider<GenerateDslTask> = target.tasks.register("generateShortModifiers", GenerateDslTask::class.java) {
            it.enabled.set(extension.enabled)
            it.debug.set(extension.debug)
        }

        // Make all Kotlin compilation tasks depend on our task
        target.tasks.matching { it.name.startsWith("compile") && it.name.endsWith("Kotlin") }.configureEach {
            it.dependsOn(helloTask)
        }

        target.logger.lifecycle("Acacia Plugin: Hello World plugin applied to ${target.name}")
    }
}
