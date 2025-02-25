package plugins.packages

import core.Action
import core.AppState
import models.AppPackage

class PackagesCompanionInterface {

    fun buildGetSignatureAction(packageName: String): Action.SendSocketRequest =
        Action.SendSocketRequest(
            PackagesPlugin.EXTRACT_KEY_INSTRUCTION,
            packageName,
        )

    fun parseCompanionResponse(action: Action.DeliverSocketResponse, state: AppState): Result<List<AppPackage>> {
        if (action.reference.command == PackagesPlugin.EXTRACT_KEY_INSTRUCTION) {
            val signature = SignatureResponseParser().parse(action.content)
            val response = state.windows["PACKAGES"]?.result?.map {
                if ((it as AppPackage).packageName == action.reference.arg)
                    it.copy(signerInfo = signature)
                else
                    it
            } ?: emptyList()
            return Result.success(response)
        }
        return Result.failure(UnknownError())
    }
}
