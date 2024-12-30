package core

data class CompanionState(
    private val state: Int = 0,
) {
    companion object {
        const val ACCEPTED = 1
        const val INSTALLED = 2
        const val RUNNING = 4
        const val PORT_AVAILABLE = 8
        const val ONLINE = 16
        const val SKIPPED = 32
        const val PREPARED = 64
    }

    private fun has(int: Int) = state and int == int

    fun isOnline() = has(ONLINE)

    fun isRunning() = has(RUNNING)

    fun isPortOpen() = has(PORT_AVAILABLE)

    fun isReady() = has(ACCEPTED + INSTALLED + RUNNING + PORT_AVAILABLE)

    fun hasPrepared() = has(PREPARED)
    fun hasAccepted() = has(ACCEPTED)
    fun hasSkipped() = has(SKIPPED)
    fun shouldOffer() = !(has(ACCEPTED) || has(SKIPPED) || has(INSTALLED))
    fun setHasAccepted() = CompanionState(state + ACCEPTED)
    fun setIsInstalled() = CompanionState(state + INSTALLED)

    fun setIsRunning() = CompanionState(state + RUNNING)

    fun setPortIsOpen() = CompanionState(state + PORT_AVAILABLE)

    fun setIsOnline() = CompanionState(state + ONLINE)
    fun setSkipped() = CompanionState(state + SKIPPED)
    fun setPrepared() = CompanionState(state + PREPARED)


}
