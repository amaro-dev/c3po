package core.facade

import java.io.File

/**
 * Utility for checking file system permissions on macOS and providing user guidance
 */
object PermissionChecker {

    /**
     * Checks if the application has access to the user's Documents folder
     */
    fun checkDocumentFolderAccess(): Boolean {
        return try {
            val documentsFolder = File(System.getProperty("user.home") + "/Documents")
            documentsFolder.canRead() && documentsFolder.canWrite()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if the application has access to the user's Downloads folder
     */
    fun checkDownloadFolderAccess(): Boolean {
        return try {
            val downloadsFolder = File(System.getProperty("user.home") + "/Downloads")
            downloadsFolder.canRead() && downloadsFolder.canWrite()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if the application has access to the user's Desktop folder
     */
    fun checkDesktopFolderAccess(): Boolean {
        return try {
            val desktopFolder = File(System.getProperty("user.home") + "/Desktop")
            desktopFolder.canRead() && desktopFolder.canWrite()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns true if running on macOS
     */
    fun isMacOS(): Boolean {
        return System.getProperty("os.name").lowercase().contains("mac")
    }

    /**
     * Gets overall permission status for key folders
     */
    fun getPermissionStatus(): PermissionStatus {
        if (!isMacOS()) {
            return PermissionStatus(
                hasDocumentsAccess = true,
                hasDownloadsAccess = true,
                hasDesktopAccess = true,
                needsAttention = false
            )
        }

        val documentsAccess = checkDocumentFolderAccess()
        val downloadsAccess = checkDownloadFolderAccess()
        val desktopAccess = checkDesktopFolderAccess()

        return PermissionStatus(
            hasDocumentsAccess = documentsAccess,
            hasDownloadsAccess = downloadsAccess,
            hasDesktopAccess = desktopAccess,
            needsAttention = !documentsAccess || !downloadsAccess || !desktopAccess
        )
    }

    /**
     * Returns user-friendly instructions for granting file permissions on macOS
     */
    fun getPermissionInstructions(): String {
        return if (isMacOS()) {
            """
            To allow C3PO to access files and folders:

            1. Open System Preferences (or System Settings on macOS 13+)
            2. Go to Security & Privacy → Privacy
            3. Select "Files and Folders" from the left sidebar
            4. Find C3PO in the list and check the boxes for:
               • Documents Folder
               • Downloads Folder
               • Desktop Folder

            If C3PO doesn't appear in the list, try:
            • Using file dialogs in the app first (they may trigger permission requests)
            • Adding Java to "Full Disk Access" instead

            After granting permissions, restart C3PO for changes to take effect.
            """.trimIndent()
        } else {
            "File permission management is not required on this operating system."
        }
    }
}

/**
 * Data class representing the current permission status
 */
data class PermissionStatus(
    val hasDocumentsAccess: Boolean,
    val hasDownloadsAccess: Boolean,
    val hasDesktopAccess: Boolean,
    val needsAttention: Boolean
)