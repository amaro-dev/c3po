package ui.plugins.signature

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import core.AppState
import core.WindowResult
import darkenedBy
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import ui.FileBox
import ui.OnAction
import ui.definitions.Dimens
import ui.plugins.Plugin

class SignaturePlugin(
    signatureExtractor: SignatureExtractor
) : Plugin<AndroidPackageReport> {
    override val id: String = "SIGNATURE"

    override val name: String = "APK Signature"

    override val middleware: IMiddleware<AppState> = AndroidPackageMiddleware(id, signatureExtractor)


    sealed interface Actions : IAction {
        data class LoadFile(val filePath: String) : Actions
    }

    override fun isResponsibleFor(action: IAction): Boolean =
        action is SignaturePlugin.Actions

    @Composable
    override fun present(result: WindowResult<AndroidPackageReport>, onAction: OnAction) {
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

@Composable
fun CompliantCheck(value: BoolState) {
    when (value) {
        BoolState.TRUE -> Image(Icons.Default.Check, "", colorFilter = ColorFilter.tint(Color.Green.darkenedBy(0.1f)))
        BoolState.FALSE -> Image(Icons.Default.Close, "", colorFilter = ColorFilter.tint(Color.Red))
        BoolState.NOT_FOUND -> Image(
            Icons.Default.Warning,
            "",
            colorFilter = ColorFilter.tint(Color.Yellow.darkenedBy(0.1f))
        )
    }
}

@Composable
fun Report(reportData: AndroidPackageReport) {
    Column(Modifier.fillMaxSize()) {
        Text("File:", style = MaterialTheme.typography.subtitle2)
        Text(reportData.filePath, style = MaterialTheme.typography.body2)
        Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
        Text("Signature version compliance:", style = MaterialTheme.typography.subtitle2)
        Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
        Row {
            ComplianceBox("v1", reportData.isV1Compliant)
            Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
            ComplianceBox("v2", reportData.isV2Compliant)
            Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
            ComplianceBox("v3", reportData.isV3Compliant)
            Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
            ComplianceBox("v3.1", reportData.isV31Compliant)
            Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
            ComplianceBox("v4", BoolState.NOT_FOUND)
        }
        Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
        Text("Signatures found:", style = MaterialTheme.typography.subtitle2)
        Text(reportData.signerCount.toString(), style = MaterialTheme.typography.body2)
        Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
        reportData.signature
        Text("Signature #1", style = MaterialTheme.typography.subtitle2)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("SHA256 digest:", style = MaterialTheme.typography.subtitle2)
            Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
            Text(reportData.signature.sha256Digest, style = MaterialTheme.typography.body2)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("SHA1 digest:", style = MaterialTheme.typography.subtitle2)
            Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
            Text(reportData.signature.sha1Digest, style = MaterialTheme.typography.body2)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("MD5 digest:", style = MaterialTheme.typography.subtitle2)
            Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
            Text(reportData.signature.md5Digest, style = MaterialTheme.typography.body2)
        }

    }
}

@Composable
fun ComplianceBox(label: String, value: BoolState) {
    Column(
        Modifier.border(1.dp, MaterialTheme.colors.onSurface, shape = RoundedCornerShape(Dimens.ROUNDED_CORNER.dp))
            .width(64.dp)
            .padding(Dimens.HORIZONTAL_SPACER.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(label)
        Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
        CompliantCheck(value)
    }
}
