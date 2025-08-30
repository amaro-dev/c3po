package plugins.device.structure

import core.command.CommandExecutor
import core.command.GetBatteryInfoCommand
import core.command.GetDensityInfoCommand
import core.command.GetDisplayInfoCommand
import core.command.GetFullDeviceInfoCommand
import core.command.GetMemoryInfoCommand
import core.command.GetStorageInfoCommand
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
                        val storageInfoResult = execute(GetStorageInfoCommand(), state, executor)

                        // Combine results
                        deviceInfoResult.handle(processor) { baseDeviceInfo ->
                            batteryInfoResult.handle(processor) { batteryInfo ->
                                memoryInfoResult.handle(processor) { memoryInfo ->
                                    displayInfoResult.handle(processor) { displayInfo ->
                                        densityInfoResult.handle(processor) { densityInfo ->
                                            storageInfoResult.handle(processor) { storageInfo ->
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
                                                        diskUsage = storageInfo
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
        }
    }
}