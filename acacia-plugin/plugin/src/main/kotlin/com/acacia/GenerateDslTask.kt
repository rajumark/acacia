package com.acacia

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.CacheableTask

@CacheableTask
open class GenerateDslTask : DefaultTask() {
    @get:Input
    val enabled: Property<Boolean> = project.objects.property(Boolean::class.java)
    
    @get:Input
    val debug: Property<Boolean> = project.objects.property(Boolean::class.java)

    @TaskAction
    fun generate() {
        if (!enabled.getOrElse(true)) {
            project.logger.lifecycle("Shortify plugin is disabled")
            return
        }
        
        val isDebug = debug.getOrElse(false)
        if (isDebug) {
            project.logger.lifecycle("Shortify: Generating DSL in debug mode")
        }
        
        // For now, just print a message to verify it works
        project.logger.lifecycle("Shortify: DSL generation task executed successfully!")
    }
}
