package com.acacia

import com.acacia.resolver.DependencyResolver
import com.acacia.resolver.AarExtractor
import com.acacia.resolver.ExtractedJar
import com.acacia.parser.SimpleParser
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main task for generating Acacia DSL and build reports.
 */
@DisableCachingByDefault(because = "Acacia plugin generates reports and code based on dependencies")
open class GenerateDslTask : DefaultTask() {

    @get:Input
    val enabled: Property<Boolean> = project.objects.property(Boolean::class.java)

    @get:Input
    val debug: Property<Boolean> = project.objects.property(Boolean::class.java)

    private val dependencyResolver = DependencyResolver(project)
    private val aarExtractor = AarExtractor(project)
    private val simpleParser = SimpleParser()
    private val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    @TaskAction
    fun generate() {
        val isEnabled = enabled.getOrElse(true)
        val isDebug = debug.getOrElse(false)

        if (!isEnabled) {
            project.logger.lifecycle("Acacia: Plugin disabled")
            return
        }

        project.logger.lifecycle("========================================")
        project.logger.lifecycle("Acacia Plugin: Starting DSL generation")
        project.logger.lifecycle("Debug mode: $isDebug")
        project.logger.lifecycle("========================================")

        try {
            val report = generateBuildReport()
            saveBuildReport(report)
            
            project.logger.lifecycle("========================================")
            project.logger.lifecycle("Acacia Plugin: DSL generation completed")
            project.logger.lifecycle("Report saved to: ${report.reportPath}")
            project.logger.lifecycle("========================================")
        } catch (e: Exception) {
            project.logger.error("Acacia Plugin: Error during generation", e)
            // Don't fail the build, just log the error
        }
    }

    /**
     * Generates the build report with all steps.
     */
    private fun generateBuildReport(): BuildReport {
        val report = BuildReport(
            timestamp = timestamp.format(Date()),
            projectName = project.name,
            reportPath = "${project.buildDir.absolutePath}/generated/source/acacia/acacia_build_report.txt"
        )

        // Step 1: Resolve Dependencies
        project.logger.lifecycle("Step 1: Resolving dependencies...")
        val composeArtifacts = dependencyResolver.resolveComposeArtifacts()
        report.addStep("Step 1: Resolve Dependencies", 
            "Found ${composeArtifacts.size} Compose artifacts",
            composeArtifacts.map { "${it.group}:${it.name}:${it.version} (${if (it.isAar) "AAR" else "JAR"})" }
        )

        // Step 2: Filter Compose Artifacts
        project.logger.lifecycle("Step 2: Filtering Compose artifacts...")
        val filteredArtifacts = composeArtifacts.filter { it.isJar || it.isAar }
        report.addStep("Step 2: Filter Compose Artifacts",
            "Filtered to ${filteredArtifacts.size} valid artifacts",
            filteredArtifacts.map { "${it.name} (${if (it.isAar) "AAR" else "JAR"})" }
        )

        // Step 3: Get Library Files
        project.logger.lifecycle("Step 3: Getting library files...")
        val libraryFiles = filteredArtifacts.map { it.file }
        report.addStep("Step 3: Get Library Files (JARs and AARs)",
            "Collected ${libraryFiles.size} library files",
            libraryFiles.map { "${it.name} (${it.length()} bytes)" }
        )

        // Step 5: AAR Extraction
        project.logger.lifecycle("Step 5: Extracting JARs from AAR files...")
        val aarFiles = filteredArtifacts.filter { it.isAar }.map { it.file }
        val extractedJars = aarExtractor.extractClassesJars(aarFiles)
        report.addStep("Step 5: AAR Extraction",
            "Extracted ${extractedJars.size} JARs from ${aarFiles.size} AAR files",
            extractedJars.map { "${it.name}.jar (${it.size} bytes) from ${it.originalAar.name}" }
        )

        // Step 6: Modifier Parsing
        project.logger.lifecycle("Step 6: Parsing Modifier functions from extracted JARs...")
        val allFunctions = simpleParser.parseJars(extractedJars)
        val paddingFunctions = allFunctions.filter { it.name == "padding" }
        report.addStep("Step 6: Modifier Parsing",
            "Parsed ${allFunctions.size} functions from ${extractedJars.size} JAR files",
            allFunctions.map { "${it.name} from ${it.fileName} (${it.jarFile})" }
        )

        // Step 7: Summary
        report.addStep("Step 7: Summary",
            "Acacia plugin completed Modifier parsing phase",
            listOf(
                "Total artifacts found: ${composeArtifacts.size}",
                "Valid library files: ${libraryFiles.size}",
                "AAR files: ${filteredArtifacts.count { it.isAar }}",
                "JAR files: ${filteredArtifacts.count { it.isJar }}",
                "Extracted JARs: ${extractedJars.size}",
                "Functions found: ${allFunctions.size}",
                "Padding functions found: ${allFunctions.filter { it.name == "padding" }.size}",
                "Cache directory: ${aarExtractor.getCacheDirectory().absolutePath}",
                "Next phases: Naming Engine, Code generation"
            )
        )

        return report
    }

    /**
     * Saves the build report to file.
     */

    /**
     * Saves the build report to file.
     */
    private fun saveBuildReport(report: BuildReport) {
        val reportFile = File(report.reportPath)
        reportFile.parentFile.mkdirs()
        
        reportFile.writeText(report.generateReportContent())
        
        if (debug.getOrElse(false)) {
            project.logger.lifecycle("Acacia Report Content:\n${report.generateReportContent()}")
        }
    }
}

/**
 * Data class for build report.
 */
data class BuildReport(
    val timestamp: String,
    val projectName: String,
    val reportPath: String
) {
    private val steps = mutableListOf<ReportStep>()

    fun addStep(title: String, description: String, details: List<String>) {
        steps.add(ReportStep(title, description, details))
    }

    fun generateReportContent(): String {
        return buildString {
            appendLine("=".repeat(60))
            appendLine("ACACIA PLUGIN BUILD REPORT")
            appendLine("=".repeat(60))
            appendLine("Generated: $timestamp")
            appendLine("Project: $projectName")
            appendLine()
            
            steps.forEach { step ->
                appendLine(step.title)
                appendLine("-".repeat(step.title.length))
                appendLine(step.description)
                appendLine()
                step.details.forEach { detail ->
                    appendLine("  - $detail")
                }
                appendLine()
            }
            
            appendLine("=".repeat(60))
            appendLine("END OF REPORT")
            appendLine("=".repeat(60))
        }
    }
}

/**
 * Represents a step in the build report.
 */
data class ReportStep(
    val title: String,
    val description: String,
    val details: List<String>
)
