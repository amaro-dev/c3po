package plugins.signature

import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import facade.SignatureExtractor
import handle
import models.AndroidPackageReport
import plugins.PluginMiddleware

class SignatureMiddleware(
    pluginName: String,
    private val signatureExtractor: SignatureExtractor
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        if (action is SignaturePlugin.Actions.LoadFile) {
            signatureExtractor.getCertificateFingerprint(action.filePath).handle(processor) {
                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(it)))
            }
        } else if (action is Action.StartPlugin) {
            processor.reduce(Action.DeliverPluginResult(pluginName, emptyList<AndroidPackageReport>()))
        }
    }
}
