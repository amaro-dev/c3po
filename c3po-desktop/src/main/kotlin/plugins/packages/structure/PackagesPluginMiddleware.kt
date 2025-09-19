package plugins.packages.structure

import Settings
import core.command.ClearDataCommand
import core.command.CommandExecutor
import core.command.GetPackageSleepStateCommand
import core.command.ListPackagesCommand
import core.command.StopAppCommand
import core.command.UninstallAppCommand
import core.handle
import core.model.Action
import core.model.AppPackage
import core.model.AppState
import core.update
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import plugins.PluginMiddleware
import plugins.packages.definition.PackagesPlugin

class PackagesPluginMiddleware(
    pluginName: String,
    private val executor: CommandExecutor,
    private val apkSignatureExtractor: core.facade.ApkSignatureExtractor,
) : PluginMiddleware(pluginName) {
    // Limit concurrent sleep-state checks to reduce device load and status churn
    private val sleepStateLimiter = Semaphore(permits = 5)
    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.StartPlugin,
            PackagesPlugin.Actions.List,
                -> {
                execute(ListPackagesCommand(), state, executor)
                    .handle(processor) { packages ->
                        processor.reduce(
                            Action.DeliverPluginResult(pluginName, packages),
                        )
                        // Auto-load sleep states for all packages
                        packages.forEach { pkg ->
                            processor.perform(PackagesPlugin.Actions.CheckAsleep(pkg))
                        }
                    }
            }

            is PackagesPlugin.Actions.Stop -> {
                execute(StopAppCommand(action.packageInfo), state, executor)
                    .onSuccess {
                        processor.reduce(Action.SetSuccess("App '${action.packageInfo.packageName}' stopped successfully"))
                    }
                    .handle(processor)
            }

            is PackagesPlugin.Actions.Uninstall -> {
                execute(UninstallAppCommand(action.packageInfo), state, executor)
                    .onSuccess {
                        processor.perform(PackagesPlugin.Actions.List)
                        processor.reduce(Action.SetSuccess("App '${action.packageInfo.packageName}' uninstalled successfully"))
                    }
                    .handle(processor)
            }

            is PackagesPlugin.Actions.ExtractKey -> {
                // Use the new APK-based signature extraction
                try {
                    val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)
                    val result = apkSignatureExtractor.extractAndAnalyzeSignature(
                        action.packageInfo,
                        adbPath,
                        state.currentDevice
                    )
                    result.fold(
                        onSuccess = { androidPackageReport ->
                            // Update the package with signature information
                            val packages = (state.windows[pluginName]?.result as? List<AppPackage>) ?: emptyList()
                            val updatedPackages = packages.map { pkg ->
                                if (pkg.packageName == action.packageInfo.packageName) {
                                    pkg.copy(signerInfo = androidPackageReport.signature)
                                } else {
                                    pkg
                                }
                            }
                            processor.reduce(Action.DeliverPluginResult(pluginName, updatedPackages))
                        },
                        onFailure = { exception ->
                            processor.reduce(Action.SetCommandError("Failed to extract signature: ${exception.message}"))
                        }
                    )
                } catch (e: Exception) {
                    processor.reduce(Action.SetCommandError("Failed to extract APK signature: ${e.message}"))
                }
            }

            is PackagesPlugin.Actions.CheckAsleep -> {
                sleepStateLimiter.withPermit {
                    execute(GetPackageSleepStateCommand(action.packageInfo.packageName), state, executor)
                        .handle(processor) { sleepState ->
                            // Update individual package sleep state without race conditions
                            processor.reduce(
                                Action.UpdatePackageSleepState(pluginName, action.packageInfo.packageName, sleepState)
                            )
                        }
                }
            }

            is PackagesPlugin.Actions.LoadAllSleepStates -> {
                val packages = (state.windows[pluginName]?.result as? List<AppPackage>) ?: emptyList()
                packages.forEach { pkg ->
                    processor.perform(PackagesPlugin.Actions.CheckAsleep(pkg))
                }
            }

            is PackagesPlugin.Actions.ClearData -> {
                execute(ClearDataCommand(action.packageInfo), state, executor)
                    .onSuccess {
                        processor.reduce(Action.SetSuccess("Data cleared for '${action.packageInfo.packageName}'"))
                    }
                    .handle(processor)
            }

        }
    }

    private fun updateByPackageName(
        state: AppState,
        packageName: String,
        transform: (AppPackage) -> AppPackage,
    ): List<AppPackage> =
        (
                state.windows[pluginName]
                    ?.result as? List<AppPackage>
                )?.update({ it.packageName == packageName }) { transform(it) }
            ?: emptyList()
}
