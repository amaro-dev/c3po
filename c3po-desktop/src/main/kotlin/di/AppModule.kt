package di

// import core.middleware.PluginSelectorMiddleware // Does not exist
import core.PluginSelectorMiddleware
import core.middleware.ClipboardMiddleware
import core.middleware.DeviceMiddleware
import core.middleware.SettingsMiddleware
import core.middleware.StatusMiddleware
import core.middleware.USBMonitorMiddleware
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
                DeviceMiddleware(get()),
                PluginSelectorMiddleware(get(named(PLUGIN_LIST_DEPENDENCY))), // Does not exist
                ClipboardMiddleware(get()),
                SettingsMiddleware(get()),
                StatusMiddleware(get()),
                USBMonitorMiddleware(get()),
                ConditionedDirectMiddleware(
                    Action.SelectPlugin::class,
                    Action.SelectDevice::class,
                    Action.ChangeFilter::class,
                    Action.ClearError::class,
                ),
            )
        }

        factory(named(PLUGIN_LIST_DEPENDENCY)) {
            listOf(
                DevicePlugin(),
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

        single { AutomationMiddleware("AUTOMATION", get(), get()) }

        single {
            AppStateManager(
                AppState(),
                AppReducer(),
                *get<Array<IMiddleware<AppState>>>(named(MIDDLEWARE_LIST_DEPENDENCY)),
            )
        }
    }
