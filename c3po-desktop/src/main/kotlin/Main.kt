import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import core.App
import core.logging.StructuredLogger
import di.AppModule
import di.FacadeModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import ui.AndroidGreenTheme
import ui.definitions.Dimens
import ui.screen.NewLayout

fun main() {
    // Fix for macOS hardened runtime posix_spawn failures
    System.setProperty("jdk.lang.Process.launchMechanism", "FORK")
    
    application {
        startKoin {
            modules(AppModule, FacadeModule)
        }

        // Initialize logging system
        val logger = StructuredLogger.getInstance()
        logger.start()
        logger.log("sys", "Application", "C3PO started")
        
        val myApp = App()
        myApp.start()
        Metrics().start()
        Window(
            onCloseRequest = {
                logger.log("sys", "Application", "C3PO shutting down")
                logger.stop()
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
}

