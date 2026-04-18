package com.acacia

import org.gradle.api.Plugin
import org.gradle.api.Project

class ShortifyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("shortify", ShortifyExtension::class.java)
        val task = target.tasks.register("generateShortModifiers", GenerateDslTask::class.java)
        task.configure { 
            it.enabled.set(extension.enabled)
            it.debug.set(extension.debug)
        }
        
        // For now, just focus on generating the DSL file
        // Android sourceSets integration will be added in next step
    }
}
