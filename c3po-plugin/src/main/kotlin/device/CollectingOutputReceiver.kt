package device

import com.android.ddmlib.MultiLineReceiver

/**
 * Output receiver that collects shell command output for processing.
 * Used with Android Studio's IDevice.executeShellCommand() API.
 */
class CollectingOutputReceiver : MultiLineReceiver() {
    private val output = StringBuilder()
    private var cancelled = false

    override fun processNewLines(lines: Array<out String>) {
        for (line in lines) {
            output.appendLine(line)
        }
    }

    override fun isCancelled(): Boolean = cancelled

    fun cancel() {
        cancelled = true
    }

    fun getOutput(): String = output.toString().trim()
}
