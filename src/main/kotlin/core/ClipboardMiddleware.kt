package core

import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection

class ClipboardMiddleware(
    private val clipboard: Clipboard
) : IMiddleware<AppState> {
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        if (action is Action.CopyText) {
            clipboard.setContents(StringSelection(action.content), null)
        }
    }
}
