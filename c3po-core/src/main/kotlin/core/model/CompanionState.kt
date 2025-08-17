package core.model

data class CompanionState(
    private val state: Int = 0,
) {
    companion object {
        const val ASKED_FOR_PERMISSION = 1
        const val CHECKED_FOR_PRESENCE = 2
        const val ACCEPTED = 4
        const val SKIPPED = 8
        const val INSTALLED = 16
        const val ONLINE = 32
    }

    fun has(int: Int) = state and int == int

    fun isOnline() = has(ONLINE)

    fun shouldOffer() = !has(INSTALLED) && has(CHECKED_FOR_PRESENCE) && !has(ASKED_FOR_PERMISSION)

    fun setHasAccepted() = CompanionState(state + ACCEPTED)

    fun setIsInstalled() = CompanionState(state + INSTALLED)

    fun setIsOnline() = CompanionState(state + ONLINE)

    fun setIsOffline() = CompanionState(state - ONLINE)

    fun setSkipped() = CompanionState(state + SKIPPED)

    fun setAsked() = CompanionState(state + ASKED_FOR_PERMISSION)

    fun setCheckedForPresence() = CompanionState(state + CHECKED_FOR_PRESENCE)
}

sealed interface CompanionS {
    data object Disconnected : CompanionS

    data object Preparing : CompanionS

    data object Connecting : CompanionS

    data object ConnectionFailed : CompanionS

    data class Connected(
        val version: String,
    ) : CompanionS
}
