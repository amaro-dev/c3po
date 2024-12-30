package ui

import Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import core.Action
import ui.definitions.Dimens

@Composable
fun CompanionDialog(fullDisclaimer: Boolean, onAction: OnAction) {
    Dialog({}, properties = DialogProperties()) {
        Column(Modifier.clip(RoundedCornerShape(Dimens.ROUNDED_CORNER.dp))) {
            Surface(color = MaterialTheme.colors.primary) {
                Row(Modifier.padding(Dimens.ROW_HORIZONTAL_MARGIN.dp, Dimens.ROW_VERTICAL_MARGIN.dp).fillMaxWidth()) {
                    Text("C3PO works even better with R2D2")
                }
            }
            Surface(
                color = MaterialTheme.colors.background
            ) {
                Column(modifier = Modifier.padding(20.dp, 16.dp)) {
                    if (fullDisclaimer) {
                        Text("R2D2 is a companion app that can be installed on devices providing even more info about it.")
                        Text("Once it's installed, C3PO can instrument R2D2 to gather all the data you want.")
                    } else {
                        Text("R2D2 is not installed on this device. Do you want to install it now?.")
                    }
                    Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button({
                            onAction(Action.ChangeSettingsProperty(Settings.ACCEPT_COMPANION, "true"))
                            onAction(Action.InstallCompanion)
                        }) {
                            Text("Install")
                        }
                        Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
                        Button({ onAction(Action.SkipCompanionForDevice) }) {
                            Text("Not now")
                        }
                        if (fullDisclaimer) {
                            Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
                            Button({
                                onAction(Action.ChangeSettingsProperty(Settings.ACCEPT_COMPANION, "false"))
                                onAction(Action.DoNotUseCompanion)
                            }) {
                                Text("Don't use R2D2")
                            }
                        }
                    }
                }
            }
        }
    }
}
// Caso A - Tudo certo / primeiro uso
// Contexto:
// - Não existe aceite para uso do R2D2
// - Existe um terminal conectado
// Etapas:
// 1) Usuário inicia a aplicação
// 2) Exibimos a dialog explicando sobre o R2D2
// 3) Usuário seleciona opção instalar
// 4) Sistema salva a opção do usuário
// 5) Sistema instala a aplicação
// 6) Sistema esconde a dialog
// 7) Sistema valida a instalação
// 8) Sistema conecta ao R2D2

// Caso B - Tudo certo / segundo uso / aparelho sem R2D2
// Contexto:
// - Já existe aceite para uso do R2D2
// - Existe um terminal conectado
// - O terminal conectado não possui R2D2
// Etapas:
// 1) Usuário inicia a aplicação
// 2) Exibimos a dialog informando que R2D2 não está instalado
// 3) Usuário seleciona opção instalar
// 4) Sistema instala a aplicação
// 5) Sistema esconde a dialog
// 6) Sistema valida a instalação
// 7) Sistema conecta ao R2D2

// Caso C - Tudo certo / segundo uso / aparelho já possui R2D2
// Contexto:
// - Já existe aceite para uso do R2D2
// - Existe um terminal conectado
// - O terminal conectado já possui R2D2
// Etapas:
// 1) Usuário inicia a aplicação
// 2) Sistema valida a instalação
// 3) Sistema conecta ao R2D2

// Caso D - Uso Negado
// Contexto:
// - Não existe aceite para uso do R2D2
// - Existe um terminal conectado
// Etapas:
// 1) Usuário inicia a aplicação
// 2) Exibimos a dialog explicando sobre o R2D2
// 3) Usuário seleciona opção "Não usar R2D2"
// 4) Sistema salva a opção do usuário
// 5) Sistema esconde a dialog

// Caso E - Uso Negado / segundo momento
// Contexto:
// - Usuário rejeitou uso do R2D2
// - Existe um terminal conectado
// Etapas:
// 1) Usuário inicia a aplicação
// 2) Nada é apresentado e a aplicação não conecta ao R2D2

// Caso F - Uso Negado neste momento / primeira vez
// Contexto:
// - Não existe aceite para uso do R2D2
// - Existe um terminal conectado
// Etapas:
// 1) Usuário inicia a aplicação
// 2) Exibimos a dialog explicando sobre o R2D2
// 3) Usuário seleciona opção "Agora não"
// 4) Sistema salva a opção do usuário
// 5) Sistema esconde a dialog

// Caso G - Uso Negado neste momento / segunda vez
// Contexto:
// - Já existe aceite para uso do R2D2
// - Existe um terminal conectado
// - O terminal conectado não possui R2D2
// Etapas:
// 1) Usuário inicia a aplicação
// 2) Exibimos a dialog informando que R2D2 não está instalado
// 3) Usuário seleciona opção "Agora não"
// 4) Sistema salva a opção do usuário
// 5) Sistema esconde a dialog

// Condições
// DEVICE && !ACEITE && !(BYPASS || INSTALL)
