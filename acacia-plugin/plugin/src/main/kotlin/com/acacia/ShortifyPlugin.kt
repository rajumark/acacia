package com.acacia

import org.gradle.api.Plugin
import org.gradle.api.Project

class ShortifyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("shortify", ShortifyExtension::class.java)
        val task = target.tasks.register("generateShortModifiers", GenerateDslTask::class.java)
        task.configure { target ->
            target.enabled.set(extension.enabled)
            target.debug.set(extension.debug)
        }
    }
}
