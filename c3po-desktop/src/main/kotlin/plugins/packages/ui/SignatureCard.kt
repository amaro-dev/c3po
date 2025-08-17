package plugins.packages.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.model.Action
import core.model.SignatureInfo
import core.model.Signer
import ui.OnAction

@Composable
fun SignatureCard(
    signatureInfo: SignatureInfo,
    onAction: OnAction,
) {
    val type = if (signatureInfo.signer.isDebug) {
        Icons.Filled.Warning
    } else {
        Icons.Filled.Check
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = type,
                    contentDescription = if (signatureInfo.signer.isDebug) "Debug signature" else "Release signature",
                    tint = if (signatureInfo.signer.isDebug)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Signature Info",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(12.dp))
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
