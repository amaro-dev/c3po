import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import core.App
import di.AppModule
import di.FacadeModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import ui.AndroidGreenTheme
import ui.definitions.Dimens
import ui.screen.NewLayout

fun main() =
    application {
        startKoin {
            modules(AppModule, FacadeModule)
        }
        val myApp = App()
        myApp.start()
        Metrics().start()
        Window(
            onCloseRequest = {
                stopKoin()
                myApp.exit()
                exitApplication()
            },
            title = "C3PO - The Android Explorer",
            state =
                WindowState(
                    width = Dimens.WINDOW_WIDTH.dp,
                    height = Dimens.WINDOW_HEIGHT.dp,
                ),
        ) {
            val state = myApp.listen().collectAsState().value
            val darkMode = state.settings.getProperty(Settings.DARK_MODE_PROP)?.toBoolean() ?: false

            AndroidGreenTheme(useDarkTheme = darkMode) {
                NewLayout(myApp)
            }
        }
    }

