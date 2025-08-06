package plugins.signature

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.AndroidPackageReport
import ui.definitions.Dimens

@Composable
fun Report(reportData: AndroidPackageReport) {
    Column(Modifier.fillMaxSize()) {
        Text("File:", style = MaterialTheme.typography.subtitle2)
        Text(reportData.filePath, style = MaterialTheme.typography.body2.copy(fontSize = 12.sp))
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
            ComplianceBox("v4", reportData.isV4Compliant)
        }
        Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
        Text("Signatures found:", style = MaterialTheme.typography.subtitle2)
        Text(reportData.signerCount.toString(), style = MaterialTheme.typography.body2)
        Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
        Text("Signature #1", style = MaterialTheme.typography.subtitle2)
        Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
        RowInfo("SHA256 digest:", reportData.signature.sha256Digest)
        RowInfo("SHA1 digest:", reportData.signature.sha1Digest)
        RowInfo("MD5 digest:", reportData.signature.md5Digest)
        RowInfo(
            "Is Debug:",
            reportData.signature.signer.isDebug
                .toString(),
        )

        if (!reportData.signature.signer.isDebug) {
            RowInfo("Organization:", reportData.signature.signer.organization ?: "Not found")
            RowInfo("Organization unit:", reportData.signature.signer.organizationUnit ?: "Not found")
            RowInfo("Common name:", reportData.signature.signer.commonName ?: "Not found")
        }
    }
}
