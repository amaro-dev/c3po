package di

import core.Action
import core.AppReducer
import core.AppState
import core.AppStateManager
import core.ClipboardMiddleware
import core.CompanionMiddleware
import core.DeviceMiddleware
import core.PluginSelectorMiddleware
import core.SettingsMiddleware
import core.SocketMiddleware
import core.StatusMiddleware
import dev.amaro.sonic.ConditionedDirectMiddleware
import dev.amaro.sonic.IMiddleware
import di.Names.MIDDLEWARE_LIST_DEPENDENCY
import di.Names.PLUGIN_LIST_DEPENDENCY
import org.koin.core.qualifier.named
import org.koin.dsl.module
import plugins.activities.ActivitiesPlugin
import plugins.attrs.DeviceAttrsPlugin
import plugins.intents.pending.PendingIntentsPlugin
import plugins.packages.PackagesPlugin
import plugins.permissions.PermissionsPlugin
import plugins.services.ServicesPlugin
import plugins.signature.SignaturePlugin

val AppModule =
    module {
        factory(named(Names.MIDDLEWARE_LIST_DEPENDENCY)) {
            arrayOf(
                DeviceMiddleware(get()),
                PluginSelectorMiddleware(get(named(PLUGIN_LIST_DEPENDENCY))),
                ClipboardMiddleware(get()),
                CompanionMiddleware(get()),
                SettingsMiddleware(get()),
                StatusMiddleware(get()),
                SocketMiddleware(get(), get()),
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
                ActivitiesPlugin(get()),
                PackagesPlugin(get()),
                DeviceAttrsPlugin(get()),
                ServicesPlugin(get()),
                PermissionsPlugin(get()),
                PendingIntentsPlugin(get()),
                SignaturePlugin(get()),
            )
        }

        single {
            AppStateManager(
                AppState(),
                AppReducer(),
                *get<Array<IMiddleware<AppState>>>(named(MIDDLEWARE_LIST_DEPENDENCY)),
            )
        }
    }
