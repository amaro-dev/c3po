package core.middleware

import core.logging.LoggingConfiguration
import core.logging.StructuredLogger
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor

/**
 * Middleware that logs action dispatching and state changes.
 * Handles "perform" and "reduce" category logging based on user configuration.
 */
class LoggingMiddleware : IMiddleware<AppState> {
    private val logger = StructuredLogger.getInstance()

    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        val config = LoggingConfiguration(state.settings)

        if (!config.isLoggingEnabled()) {
            return
        }

        // Log action performance (dispatch)
        if (config.isPerformLoggingEnabled()) {
            logActionPerform(action)
        }

        // Log state reduction if enabled
        if (config.isReduceLoggingEnabled()) {
            // Store current state for comparison after reduction
            val originalState = state

            // We can't directly intercept the reducer here, so we log the action
            // that will cause state change. The actual state change logging
            // would need to be done in the AppStateManager or reducer itself.
            logActionReduce(action, originalState)
        }
    }

    private fun logActionPerform(action: IAction) {
        val actionName = action::class.simpleName ?: "UnknownAction"
        val message = when (action) {
            is Action.CommandAction -> "Command action dispatched"
            else -> "Action dispatched"
        }

        logger.log("perform", actionName, message)
    }

    private fun logActionReduce(action: IAction, state: AppState) {
        val actionName = action::class.simpleName ?: "UnknownAction"

        // Provide context about current state for debugging
        val contextInfo = buildString {
            append("Processing for state with ")
            append("${state.devices.size} devices, ")
            append("current plugin: ${state.currentPlugin ?: "none"}")
            if (state.errorMessage != null) {
                append(", has error")
            }
        }

        logger.log("reduce", actionName, "Will reduce state - $contextInfo")
    }
}