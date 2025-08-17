import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import core.App
import core.model.AppState
import core.model.CommandStatus
import di.AppModule
import di.FacadeModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import ui.AndroidGreenTheme
import ui.NewLayout
import ui.RunningAndroid
import ui.definitions.Dimens

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
            AndroidGreenTheme {
                NewLayout(myApp)
            }
        }
    }

@Composable
fun RunningStatus(state: AppState) {
    if (state.commandStatus == CommandStatus.Running) {
        RunningAndroid()
    }
}
