package com.acacia

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Simple Hello World task - template for future plugin development.
 */
@DisableCachingByDefault(because = "This is a simple hello world task with no cacheable outputs")
open class GenerateDslTask : DefaultTask() {

    @get:Input
    val enabled: Property<Boolean> = project.objects.property(Boolean::class.java)

    @get:Input
    val debug: Property<Boolean> = project.objects.property(Boolean::class.java)

    @TaskAction
    fun generate() {
        val isEnabled = enabled.getOrElse(true)
        val isDebug = debug.getOrElse(false)

        if (!isEnabled) {
            project.logger.lifecycle("Shortify: Plugin disabled")
            return
        }

        project.logger.lifecycle("========================================")
        project.logger.lifecycle("Hello from Acacia Plugin!")
        project.logger.lifecycle("This is a minimal template plugin.")
        project.logger.lifecycle("Debug mode: $isDebug")
        project.logger.lifecycle("========================================")
    }
}
