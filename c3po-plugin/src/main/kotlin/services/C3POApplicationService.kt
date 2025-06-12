package services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger

/**
 * Main application service for C3PO Android Studio plugin.
 * Manages plugin lifecycle and coordinates with Android Studio's infrastructure.
 */
@Service
class C3POApplicationService {

    companion object {
        private val LOG = Logger.getInstance(C3POApplicationService::class.java)

        fun getInstance(): C3POApplicationService {
            return ApplicationManager.getApplication().getService(C3POApplicationService::class.java)
        }
    }

    init {
        LOG.info("C3PO Application Service initialized")
    }

    /**
     * Called when the application service is disposed
     */
    fun dispose() {
        LOG.info("C3PO Application Service disposed")
    }
}
