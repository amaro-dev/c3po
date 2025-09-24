package di

import core.PluginSelectorMiddleware
import core.analytics.AnalyticsService
import core.facade.update.DefaultPlatformDetector
import core.facade.update.UpdateChecker
import core.facade.update.UpdateDownloader
import core.facade.update.UpdateFileManager
import core.facade.update.UpdateInstaller
import core.facade.update.UpdatePathManager
import core.facade.update.UpdateValidator
import core.middleware.AnalyticsMiddleware
import core.middleware.ClipboardMiddleware
import core.middleware.DeviceMiddleware
import core.middleware.LoggingMiddleware
import core.middleware.RestartMiddleware
import core.middleware.SettingsMiddleware
import core.middleware.StatusMiddleware
import core.middleware.USBMonitorMiddleware
import core.middleware.UpdateMiddleware
import core.middleware.UrlMiddleware
import core.model.Action
import core.model.AppReducer
import core.model.AppState
import core.model.AppStateManager
import dev.amaro.sonic.ConditionedDirectMiddleware
import dev.amaro.sonic.IMiddleware
import di.Names.MIDDLEWARE_LIST_DEPENDENCY
import di.Names.PLUGIN_LIST_DEPENDENCY
import org.koin.core.qualifier.named
import org.koin.dsl.module
import plugins.activities.definition.ActivitiesPlugin
import plugins.attrs.definition.DeviceAttrsPlugin
import plugins.automation.definition.AutomationPlugin
import plugins.automation.structure.AutomationMiddleware
import plugins.automation.structure.ScriptPackageService
import plugins.device.definition.DevicePlugin
import plugins.packages.definition.PackagesPlugin
import plugins.permissions.definition.PermissionsPlugin
import plugins.services.definition.ServicesPlugin
import plugins.signature.definition.SignaturePlugin
import plugins.automation.structure.ScriptStorage as AutomationDataScriptStorage

val AppModule =
    module {
        factory(named(MIDDLEWARE_LIST_DEPENDENCY)) {
            arrayOf(
                LoggingMiddleware(), // Add logging as first middleware to capture all actions
                AnalyticsMiddleware(get()), // Add analytics early in chain
                DeviceMiddleware(get()),
                PluginSelectorMiddleware(get(named(PLUGIN_LIST_DEPENDENCY))), // Does not exist
                ClipboardMiddleware(get()),
                UrlMiddleware(),
                SettingsMiddleware(get(), get(), get(), get()),
                StatusMiddleware(get()),
                USBMonitorMiddleware(get()),
                UpdateMiddleware(get(), get(), get()),
                RestartMiddleware(),
                ConditionedDirectMiddleware(
                    Action.SelectPlugin::class,
                    Action.SelectDevice::class,
                    Action.ChangeFilter::class,
                    Action.ClearError::class,
                    Action.SetSuccess::class,
                    Action.ClearSuccess::class,
                    Action.SetAppVersion::class,
                ),
            )
        }

        // Analytics Service (no hardcoded initialization)
        single { AnalyticsService() }

        // Runtime analytics config from env/system (no persistence)
        single { AnalyticsDefaultsProvider.loadFromRuntime() }
        // Analytics Facade with runtime config
        single { core.facade.SettingsAnalyticsFacade(get()) }

        // Update Services
        single { UpdateValidator() }
        single { UpdateFileManager() }
        single { UpdatePathManager() }
        single { UpdateChecker(get()) }
        single { UpdateDownloader(get(), get()) }
        single { UpdateInstaller(get(), platformDetector = DefaultPlatformDetector(), pathManager = get()) }

        factory(named(PLUGIN_LIST_DEPENDENCY)) {
            listOf(
                DevicePlugin(get()),
                ActivitiesPlugin(get()),
                PackagesPlugin(get(), get()),
                DeviceAttrsPlugin(get()),
                ServicesPlugin(get()),
                PermissionsPlugin(get()),
                SignaturePlugin(get()),
                AutomationPlugin(get()),
            )
        }

        single { AutomationDataScriptStorage() }
        single { ScriptPackageService(get()) }

        single { AutomationMiddleware("AUTOMATION", get(), get(), get()) }

        single {
            AppStateManager(
                AppState(),
                AppReducer(),
                *get<Array<IMiddleware<AppState>>>(named(MIDDLEWARE_LIST_DEPENDENCY)),
            )
        }
    }
