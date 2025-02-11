package ui.plugins.signature

import androidx.compose.runtime.Composable
import core.AppState
import core.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import ui.OnAction
import ui.plugins.Plugin

class SignaturePlugin(
    private val signatureExtractor: SignatureExtractor
) : Plugin<AndroidPackageReport> {
    override val id: String = "SIGNATURE"

    override val name: String = "APK Signature"

    override val mainAction: IAction
        get() = TODO()
    override val middleware: IMiddleware<AppState>
        get() = TODO()

    sealed interface Actions : IAction {
        data class LoadFile(val filePath: String) : Actions
    }

    override fun isResponsibleFor(action: IAction): Boolean {
        TODO("Not yet implemented")
    }

    @Composable
    override fun present(result: WindowResult<AndroidPackageReport>, onAction: OnAction) {
        TODO("Not yet implemented")
    }
}
