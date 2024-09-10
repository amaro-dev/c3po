package core

data class CompanionState(private val state: Int = 0) {
    companion object {
        const val HAS_ACCEPTED = 1
        const val INSTALLED = 2
        const val RUNNING = 4
        const val PORT_AVAILABLE = 8
        const val ONLINE = 16
    }

    private fun has(int: Int) = state and int == int
    fun isOnline() = has(ONLINE)
    fun isRunning() = has(RUNNING)
    fun isPortOpen() = has(PORT_AVAILABLE)
    fun isReady() = has(HAS_ACCEPTED + INSTALLED + RUNNING + PORT_AVAILABLE)
    fun setHasAccepted() = CompanionState(state + HAS_ACCEPTED)
    fun setIsInstalled() = CompanionState(state + INSTALLED)
    fun setIsRunning() = CompanionState(state + RUNNING)
    fun setPortIsOpen() = CompanionState(state + PORT_AVAILABLE)
    fun setIsOnline() = CompanionState(state + ONLINE)


    data class Builder(private val state: Int = 0) {
        fun hasAccepted() = Builder(state + HAS_ACCEPTED)
        fun isInstalled() = Builder(state + INSTALLED)
        fun isRunning() = Builder(state + RUNNING)
        fun portIsOpen() = Builder(state + PORT_AVAILABLE)
        fun isOnline() = Builder(state + ONLINE)
        fun build() = CompanionState(state)
    }
}
