package plugins.packages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.Action
import models.SignatureInfo
import models.Signer
import ui.OnAction
import ui.definitions.Dimens
import ui.horizontalPadding
import ui.verticalPadding

@Composable
fun SignatureCard(
    signatureInfo: SignatureInfo,
    onAction: OnAction,
) {
    val type =
        if (signatureInfo.signer.isDebug) {
            androidx.compose.material.icons.Icons.Filled.Warning
        } else {
            androidx.compose.material.icons.Icons.Filled.Check
        }
    Surface(color = MaterialTheme.colors.surface) {
        Column(Modifier.fillMaxWidth().horizontalPadding().verticalPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Signature Info", style = MaterialTheme.typography.subtitle1)
                Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
                Icon(type, "")
            }
            Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
            SignatureInfoRow("Name", signatureInfo.signer.commonName ?: "Unspecified")
            SignatureInfoRow("Organization", signatureInfo.signer.organization ?: "Unspecified")
            SignatureInfoRow("Unit", signatureInfo.signer.organizationUnit ?: "Unspecified")
            SignatureInfoRow("MD5", signatureInfo.md5Digest) { onAction(Action.CopyText(it)) }
            SignatureInfoRow("SHA1", signatureInfo.sha1Digest) { onAction(Action.CopyText(it)) }
            SignatureInfoRow("SHA256", signatureInfo.sha256Digest) { onAction(Action.CopyText(it)) }
        }
    }
}

@Composable
@Preview
fun previewSign() {
    SignatureCard(
        SignatureInfo(
            Signer("commonName", "organizationUnit", "organization", "locality", "state", "CC"),
            "test",
            "test",
            "test",
        ),
        {},
    )
}
