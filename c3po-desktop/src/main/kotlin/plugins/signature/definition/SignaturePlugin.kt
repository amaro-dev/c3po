package plugins.signature.definition

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import core.facade.SignatureExtractor
import core.model.Action
import core.model.AndroidPackageReport
import core.model.AppState
import core.model.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import plugins.signature.ComplianceBox
import plugins.signature.structure.SignatureMiddleware
import ui.OnAction
import ui.component.CustomActionButton
import ui.component.EnhancedHeaderRow
import ui.parts.FileBox

class SignaturePlugin(
    signatureExtractor: SignatureExtractor,
) : plugins.Plugin<AndroidPackageReport> {
    override val id: String = "SIGNATURE"

    override val name: String = "Signature"

    override val icon: ImageVector = Icons.Filled.Verified

    override val middleware: IMiddleware<AppState> = SignatureMiddleware(id, signatureExtractor)

    sealed interface Actions : IAction {
        data class LoadFile(
            val filePath: String,
        ) : Actions
    }

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(
        result: WindowResult<AndroidPackageReport>,
        onAction: OnAction,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (result.result.isEmpty()) {
                // Empty state with drag and drop
                SignatureEmptyState(onAction)
            } else {
                // Show signature information
                val report = result.result.first()
                SignatureReport(report, onAction)
            }
        }
    }
}

@Composable
private fun SignatureEmptyState(onAction: OnAction) {
    Card(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            FileBox { onAction(SignaturePlugin.Actions.LoadFile(it)) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Signature",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Drag an APK file here",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "to analyze its signature",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun SignatureReport(report: AndroidPackageReport, onAction: OnAction) {
    Card(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // File Information Section
                item {
                    EnhancedHeaderRow("File Information")
                }

                item {
                    SignatureInfoRow(
                        label = "File Path",
                        value = report.filePath,
                        onCopy = { onAction(Action.CopyText(it)) }
                    )
                }

                // Signature Compliance Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    EnhancedHeaderRow("Signature Version Compliance")
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ComplianceBox("v1", report.isV1Compliant)
                        ComplianceBox("v2", report.isV2Compliant)
                        ComplianceBox("v3", report.isV3Compliant)
                        ComplianceBox("v3.1", report.isV31Compliant)
                        ComplianceBox("v4", report.isV4Compliant)
                    }
                }

                // Signer Information Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    EnhancedHeaderRow("Signer Information")
                }

                item {
                    SignatureInfoRow(
                        label = "Signers Found",
                        value = report.signerCount.toString()
                    )
                }

                // Certificate Information Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    EnhancedHeaderRow("Certificate Details")
                }

                item {
                    SignatureInfoRow(
                        label = "SHA256 Digest",
                        value = report.signature.sha256Digest,
                        onCopy = { onAction(Action.CopyText(it)) }
                    )
                }

                item {
                    SignatureInfoRow(
                        label = "SHA1 Digest",
                        value = report.signature.sha1Digest,
                        onCopy = { onAction(Action.CopyText(it)) }
                    )
                }

                item {
                    SignatureInfoRow(
                        label = "MD5 Digest",
                        value = report.signature.md5Digest,
                        onCopy = { onAction(Action.CopyText(it)) }
                    )
                }

                item {
                    SignatureInfoRow(
                        label = "Is Debug",
                        value = report.signature.signer.isDebug.toString()
                    )
                }

                // Production Certificate Information (only if not debug)
                if (!report.signature.signer.isDebug) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        EnhancedHeaderRow("Certificate Subject")
                    }

                    report.signature.signer.organization?.let { organization ->
                        if (organization.isNotBlank()) {
                            item {
                                SignatureInfoRow(
                                    label = "Organization",
                                    value = organization,
                                    onCopy = { onAction(Action.CopyText(it)) }
                                )
                            }
                        }
                    }

                    report.signature.signer.organizationUnit?.let { organizationUnit ->
                        if (organizationUnit.isNotBlank()) {
                            item {
                                SignatureInfoRow(
                                    label = "Organization Unit",
                                    value = organizationUnit,
                                    onCopy = { onAction(Action.CopyText(it)) }
                                )
                            }
                        }
                    }

                    report.signature.signer.commonName?.let { commonName ->
                        if (commonName.isNotBlank()) {
                            item {
                                SignatureInfoRow(
                                    label = "Common Name",
                                    value = commonName,
                                    onCopy = { onAction(Action.CopyText(it)) }
                                )
                            }
                        }
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = listState)
            )
        }
    }
}

@Composable
private fun SignatureInfoRow(
    label: String,
    value: String,
    onCopy: ((String) -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (onCopy != null) {
                CustomActionButton(
                    icon = Icons.Filled.ContentCopy,
                    contentDescription = "Copy $label",
                    onClick = { onCopy(value) }
                )
            }
        }
    }

    Divider(
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth()
    )
}