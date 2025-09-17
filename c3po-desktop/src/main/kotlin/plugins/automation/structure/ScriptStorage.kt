package plugins.automation.structure

import Settings
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

    fun getScriptFolder(name: String): File = File(scriptsFolder, name)

    fun loadScriptFromFolder(folderPath: String): Script {
        val folder = File(folderPath)
        val file = File(folder, "script.c3po")
        if (!file.exists()) throw IllegalArgumentException("script.c3po not found in $folderPath")
        val yaml = file.readText()
        return parseYamlToScript(yaml)
    }

    private fun parseYamlToScript(yaml: String): Script {
        // Minimal parser for MVP. Replace with SnakeYAML later
        val name = Regex("^name:\\s*\"(.*)\"", RegexOption.MULTILINE).find(yaml)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("Missing name")
        val version = Regex("^version:\\s*\"(.*)\"", RegexOption.MULTILINE).find(yaml)?.groupValues?.get(1) ?: "1.0"
        val format =
            Regex("^format_version:\\s*\"(.*)\"", RegexOption.MULTILINE).find(yaml)?.groupValues?.get(1) ?: "1.0"

        val steps = mutableListOf<ScriptStep>()
        val lines = yaml.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("- type:")) {
                when (val type = line.substringAfter(":").trim().trim('"')) {
                    "install_apk" -> {
                        val apk = collectValue(lines, i + 1, "apk_path")
                        steps.add(ScriptStep.InstallApk(apkPath = apk ?: ""))
                    }

                    "remove_package" -> {
                        val pkg = collectValue(lines, i + 1, "package_name")
                        steps.add(ScriptStep.RemovePackage(packageName = pkg ?: ""))
                    }

                    "start_activity" -> {
                        val pkg = collectValue(lines, i + 1, "package_name")
                        val act = collectValue(lines, i + 1, "activity_name")
                        steps.add(ScriptStep.StartActivity(packageName = pkg ?: "", activityName = act ?: ""))
                    }

                    "clear_data" -> {
                        val pkg = collectValue(lines, i + 1, "package_name")
                        steps.add(ScriptStep.ClearData(packageName = pkg ?: ""))
                    }

                    "stop_package" -> {
                        val pkg = collectValue(lines, i + 1, "package_name")
                        steps.add(ScriptStep.StopPackage(packageName = pkg ?: ""))
                    }

                    else -> {
                        throw IllegalArgumentException("Unknown step type: $type")
                    }
                }
            }
            i++
        }
        return Script(name = name, version = version, formatVersion = format, steps = steps)
    }

    private fun collectValue(lines: List<String>, startIndex: Int, key: String): String? {
        for (j in startIndex until minOf(lines.size, startIndex + 5)) {
            val l = lines[j].trim()
            if (l.startsWith("$key:")) return l.substringAfter(":").trim().trim('"')
            if (l.startsWith("- ")) break
        }
        return null
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

                is ScriptStep.StopPackage -> {
                    sb.appendLine("    package_name: \"${step.packageName}\"")
                }
            }
        }

        return sb.toString()
    }

    fun copyApkToScriptFolder(apkPath: String, scriptName: String): String {
        val scriptFolder = File(scriptsFolder, scriptName).absolutePath
        return ApkPathResolver.copyApkToScriptFolder(apkPath, scriptFolder)
    }
}