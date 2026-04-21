package com.acacia

import com.acacia.resolver.DependencyResolver
import com.acacia.resolver.AarExtractor
import com.acacia.resolver.ExtractedJar
import com.acacia.parser.SimpleParser
import com.acacia.index.ApiIndex
import com.acacia.index.ApiFunction
import com.acacia.index.Param
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    private val apiIndex = ApiIndex()
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
            val (report, modifierFunctions) = generateBuildReport()
            saveBuildReport(report)
            saveModifierFunctionsJson(modifierFunctions, report.reportPath)
            
            project.logger.lifecycle("========================================")
            project.logger.lifecycle("Acacia Plugin: DSL generation completed")
            project.logger.lifecycle("Report saved to: ${report.reportPath}")
            project.logger.lifecycle("JSON data saved to: ${report.reportPath}.json")
            project.logger.lifecycle("========================================")
        } catch (e: Exception) {
            project.logger.error("Acacia Plugin: Error during generation", e)
            // Don't fail the build, just log the error
        }
    }

    /**
     * Generates the build report with all steps.
     * Returns both the report and the list of modifier functions for JSON serialization.
     */
    private fun generateBuildReport(): Pair<BuildReport, List<ApiFunction>> {
        val report = BuildReport(
            timestamp = timestamp.format(Date()),
            projectName = project.name,
            reportPath = "${project.buildDir.absolutePath}/generated/source/acacia/acacia_build_report.txt"
        )
        
        var modifierFunctions: List<ApiFunction> = emptyList()

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
        val apiIndexMap = apiIndex.buildApiIndex(extractedJars)
        modifierFunctions = apiIndex.getModifierFunctions(apiIndexMap)
        val renderedModifierFunctions = modifierFunctions.map { apiIndex.renderFunction(it) }
        
        report.addStep("Step 6: Modifier Parsing",
            "Parsed all classes from ${extractedJars.size} JAR files, found ${modifierFunctions.size} Modifier functions",
            renderedModifierFunctions
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
                "Modifier functions found: ${modifierFunctions.size}",
                "API index groups: ${apiIndexMap.size}",
                "Cache directory: ${aarExtractor.getCacheDirectory().absolutePath}",
                "Next phases: Naming Engine, Code generation"
            )
        )

        return Pair(report, modifierFunctions)
    }

    /**
     * Saves Modifier functions as structured JSON file.
     */
    private fun saveModifierFunctionsJson(functions: List<ApiFunction>, reportPath: String) {
        val jsonFile = File("$reportPath.json")
        jsonFile.parentFile.mkdirs()
        
        val jsonData = ModifierFunctionsJson(
            total_count = functions.size,
            generated_at = timestamp.format(Date()),
            functions = functions.map { fn ->
                FunctionJson(
                    function_name = fn.name,
                    extension_class = fn.receiver ?: "none",
                    extension_class_full = fn.receiverTypeFull ?: "none",
                    return_type = cleanTypeForJson(fn.returnType),
                    return_type_full = fn.returnTypeFull,
                    package_name = fn.packageName,
                    module = fn.module,
                    source = fn.source,
                    annotations = fn.annotations,
                    parameters = fn.params.map { param ->
                        ParameterJson(
                            name = param.name,
                            type = cleanTypeForJson(param.type),
                            type_full = param.typeFull,
                            has_default_value = param.hasDefaultValue,
                            default_value = if (param.hasDefaultValue) inferDefaultValue(param.name, param.type) else null
                        )
                    }
                )
            }
        )
        
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        
        jsonFile.writeText(json.encodeToString(jsonData))
    }
    
    /**
     * Cleans up type names for JSON output.
     */
    private fun cleanTypeForJson(type: String): String {
        return when {
            type.contains("androidx.compose.foundation.layout.Arrangement") -> "Arrangement"
            type.contains("androidx.compose.ui.Alignment") -> "Alignment"
            type.contains("androidx.compose.runtime.Composer") -> "Composer"
            type.contains("kotlin.jvm.functions.Function") -> "Function"
            type.contains("androidx.compose.ui.unit.Dp") -> "Dp"
            type.contains("androidx.compose.ui.unit") -> "Unit"
            type.contains("androidx.compose.ui.graphics.Color") -> "Color"
            type.contains("androidx.compose.ui.graphics.Brush") -> "Brush"
            type.contains("androidx.compose.ui.graphics.Shape") -> "Shape"
            type.contains("androidx.compose.foundation.BorderStroke") -> "BorderStroke"
            type.contains("androidx.compose.foundation.layout.PaddingValues") -> "PaddingValues"
            type.contains("androidx.compose.ui.semantics.Role") -> "Role"
            type.contains("androidx.compose.ui.Modifier") -> "Modifier"
            type.startsWith("kotlin/") -> type.removePrefix("kotlin/")
            type.startsWith("kotlinx/") -> type.removePrefix("kotlinx/")
            else -> type.substringAfterLast(".").substringAfterLast("/")
        }
    }
    
    /**
     * Infers common default values for Compose parameters.
     */
    private fun inferDefaultValue(paramName: String, type: String): String? {
        return when {
            type.contains("Dp") && (paramName == "start" || paramName == "top" || paramName == "end" || paramName == "bottom" || paramName == "all" || paramName == "horizontal" || paramName == "vertical") -> "0.dp"
            type.contains("Dp") && paramName == "padding" -> "0.dp"
            type == "Float" && paramName.contains("fraction") -> "1f"
            type == "Boolean" -> "false"
            type == "Boolean" && paramName == "enabled" -> "true"
            type.contains("Alignment") -> "Alignment.Center"
            type.contains("PaddingValues") -> "PaddingValues()"
            type.contains("Brush") -> "null"
            type.contains("FiniteAnimationSpec") -> "spring()"
            else -> null
        }
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

/**
 * JSON-serializable data class for all Modifier functions.
 */
@Serializable
data class ModifierFunctionsJson(
    val total_count: Int,
    val generated_at: String,
    val functions: List<FunctionJson>
)

/**
 * JSON-serializable data class for a single function.
 */
@Serializable
data class FunctionJson(
    val function_name: String,
    val extension_class: String,
    val extension_class_full: String,
    val return_type: String,
    val return_type_full: String,
    val package_name: String,
    val module: String,
    val source: String,
    val annotations: List<String>,
    val parameters: List<ParameterJson>
)

/**
 * JSON-serializable data class for a function parameter.
 */
@Serializable
data class ParameterJson(
    val name: String,
    val type: String,
    val type_full: String,
    val has_default_value: Boolean,
    val default_value: String?
)
