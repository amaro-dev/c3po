package plugins.signature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import facade.SignatureExtractor
import models.AndroidPackageReport
import models.WindowResult
import ui.FileBox
import ui.OnAction

class SignaturePlugin(
    signatureExtractor: SignatureExtractor,
) : plugins.Plugin<AndroidPackageReport> {
    override val id: String = "SIGNATURE"

    override val name: String = "APK Signature"

    override val middleware: IMiddleware<AppState> = SignatureMiddleware(id, signatureExtractor)

    sealed interface Actions : IAction {
        data class LoadFile(
            val filePath: String,
        ) : Actions
    }

    override fun isResponsibleFor(action: IAction): Boolean = action is SignaturePlugin.Actions

    @Composable
    override fun present(
        result: WindowResult<AndroidPackageReport>,
        onAction: OnAction,
    ) {
        Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            FileBox { onAction(Actions.LoadFile(it)) }
            if (result.result.isEmpty()) {
                Text("Drag an APK file here")
            } else {
                val report = result.result.first()
                Report(report)
            }
        }
    }
}
