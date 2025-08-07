package plugins.automation

import Settings
import models.Script
import models.ScriptStep
import java.io.File

/**
 * Handles script storage operations
 */
class ScriptStorage {
    private val scriptsFolder: File by lazy {
        val settingsFolder =
            if (Settings.isDebug()) {
                File(".")
            } else {
                File(Settings.productionSettingsFolder())
            }
        File(settingsFolder, "scripts").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    fun saveScript(script: Script) {
        val scriptFolder =
            File(scriptsFolder, script.name).apply {
                if (!exists()) {
                    mkdirs()
                }
            }

        val scriptFile = File(scriptFolder, "script.c3po")
        val yamlContent = generateYaml(script)

        scriptFile.writeText(yamlContent)
    }

    private fun generateYaml(script: Script): String {
        val sb = StringBuilder()
        sb.appendLine("name: \"${script.name}\"")
        sb.appendLine("version: \"${script.version}\"")
        sb.appendLine("format_version: \"${script.formatVersion}\"")
        sb.appendLine("steps:")

        script.steps.forEach { step ->
            sb.appendLine("  - type: \"${step.type}\"")
            when (step) {
                is ScriptStep.InstallApk -> {
                    sb.appendLine("    apk_path: \"${step.apkPath}\"")
                }

                is ScriptStep.RemovePackage -> {
                    sb.appendLine("    package_name: \"${step.packageName}\"")
                }

                is ScriptStep.StartActivity -> {
                    sb.appendLine("    package_name: \"${step.packageName}\"")
                    sb.appendLine("    activity_name: \"${step.activityName}\"")
                }

                is ScriptStep.ClearData -> {
                    sb.appendLine("    package_name: \"${step.packageName}\"")
                }
            }
        }

        return sb.toString()
    }

    fun copyApkToScriptFolder(apkPath: String, scriptName: String): String {
        val sourceFile = File(apkPath)
        if (!sourceFile.exists()) {
            throw IllegalArgumentException("APK file does not exist: $apkPath")
        }

        val scriptFolder = File(scriptsFolder, scriptName)
        if (!scriptFolder.exists()) {
            scriptFolder.mkdirs()
        }

        val fileName = sourceFile.name
        val targetFile = File(scriptFolder, fileName)

        // Copy file
        sourceFile.copyTo(targetFile, overwrite = true)

        // Return relative path
        return "./$fileName"
    }
}
