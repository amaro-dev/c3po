package plugins.device.structure

import core.command.CommandExecutor
import core.command.GetBatteryInfoCommand
import core.command.GetDensityInfoCommand
import core.command.GetDiskStatsCommand
import core.command.GetDisplayInfoCommand
import core.command.GetFullDeviceInfoCommand
import core.command.GetMemoryInfoCommand
import core.command.PullFileCommand
import core.command.RemoveDeviceFileCommand
import core.facade.ScreenshotFileManager
import core.handle
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import plugins.PluginMiddleware

class DevicePluginMiddleware(
    pluginName: String,
    private val executor: CommandExecutor
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>
    ) {
        when (action) {
            is Action.StartPlugin -> {
                if (action.pluginName == pluginName && state.currentDevice != null) {
                    try {
                        // Execute all commands concurrently
                        val deviceInfoResult = execute(GetFullDeviceInfoCommand(), state, executor)
                        val batteryInfoResult = execute(GetBatteryInfoCommand(), state, executor)
                        val memoryInfoResult = execute(GetMemoryInfoCommand(), state, executor)
                        val displayInfoResult = execute(GetDisplayInfoCommand(), state, executor)
                        val densityInfoResult = execute(GetDensityInfoCommand(), state, executor)
                        val diskStatsResult = execute(GetDiskStatsCommand(), state, executor)

                        // Combine results
                        deviceInfoResult.handle(processor) { baseDeviceInfo ->
                            batteryInfoResult.handle(processor) { batteryInfo ->
                                memoryInfoResult.handle(processor) { memoryInfo ->
                                    displayInfoResult.handle(processor) { displayInfo ->
                                        densityInfoResult.handle(processor) { densityInfo ->
                                            diskStatsResult.handle(processor) { diskStats ->
                                                val enhancedDeviceInfo = baseDeviceInfo.copy(
                                                    device = baseDeviceInfo.device.copy(
                                                        ramSize = memoryInfo,
                                                        screenSize = displayInfo.size,
                                                        screenResolution = densityInfo.removeSuffix(" DPI")
                                                    ),
                                                    status = baseDeviceInfo.status.copy(
                                                        batteryLevel = batteryInfo.level,
                                                        batteryHealth = batteryInfo.health,
                                                        batteryTemperature = batteryInfo.temperature,
                                                        chargingStatus = batteryInfo.chargingStatus,
                                                        batteryVoltage = batteryInfo.voltage,
                                                        diskUsage = diskStats
                                                    )
                                                )
                                                processor.reduce(
                                                    Action.DeliverPluginResult(
                                                        pluginName,
                                                        listOf(enhancedDeviceInfo)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Fallback to basic command if enhanced fails
                        execute(GetFullDeviceInfoCommand(), state, executor).handle(processor) { deviceInfo ->
                            processor.reduce(Action.DeliverPluginResult(pluginName, listOf(deviceInfo)))
                        }
                    }
                } else {
                    processor.reduce(Action.DeliverPluginResult(pluginName, emptyList<Any>()))
                }
            }

            is Action.TakeScreenshot -> {
                if (state.currentDevice != null) {
                    try {
                        // Step 1: Take screenshot on device
                        val screenshotCommand = ScreenshotFileManager.createScreenshotCommand()
                        execute(screenshotCommand, state, executor)
                            .onSuccess { devicePath ->
                                // Step 2: Pull screenshot to local file
                                val localFile = ScreenshotFileManager.createScreenshotFile()
                                val pullCommand = PullFileCommand(devicePath, localFile)
                                execute(pullCommand, state, executor)
                                    .onSuccess { localPath ->
                                        // Step 3: Clean up device file
                                        val removeCommand = RemoveDeviceFileCommand(devicePath)
                                        execute(removeCommand, state, executor)
                                            .onSuccess {
                                                // Try to open the screenshot in the default image viewer
                                                ScreenshotFileManager.openScreenshotInViewer(localPath)
                                                    .onSuccess {
                                                        processor.reduce(Action.SetSuccess("Screenshot saved and opened: $localPath"))
                                                    }
                                                    .onFailure { error ->
                                                        processor.reduce(Action.SetSuccess("Screenshot saved to: $localPath"))
                                                        // Still consider it successful even if opening fails
                                                    }

                                                processor.reduce(Action.ScreenshotCaptured(localPath))
                                                processor.reduce(Action.SetCommandCompleted) // Clear loading state
                                            }
                                            .onFailure { error ->
                                                // Screenshot saved but cleanup failed - still success
                                                processor.reduce(Action.SetSuccess("Screenshot saved to: $localPath (cleanup failed)"))
                                                processor.reduce(Action.ScreenshotCaptured(localPath))
                                                // TODO: I think there's no need to reduce the CommandCompleted when success is already informed
                                                processor.reduce(Action.SetCommandCompleted) // Clear loading state
                                            }
                                    }
                                    .onFailure { error ->
                                        processor.reduce(Action.SetCommandError("Failed to pull screenshot: ${error.message}"))
                                        // TODO: I think there's no need to reduce the CommandCompleted when error is already informed
                                        processor.reduce(Action.SetCommandCompleted) // Clear loading state
                                    }
                            }
                            .onFailure { error ->
                                processor.reduce(Action.SetCommandError("Failed to capture screenshot: ${error.message}"))
                                // TODO: I think there's no need to reduce the CommandCompleted when error is already informed
                                processor.reduce(Action.SetCommandCompleted) // Clear loading state
                            }
                    } catch (e: Exception) {
                        processor.reduce(Action.SetCommandError("Screenshot error: ${e.message}"))
                        // TODO: I think there's no need to reduce the CommandCompleted when error is already informed
                        processor.reduce(Action.SetCommandCompleted) // Clear loading state
                    }
                } else {
                    processor.reduce(Action.SetCommandError("No device connected for screenshot"))
                    // TODO: I think there's no need to reduce the CommandCompleted when error is already informed
                    processor.reduce(Action.SetCommandCompleted) // Clear loading state
                }
            }
        }
    }
}