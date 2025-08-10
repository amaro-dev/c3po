package ui.toolwindows

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Tool Window Factory for C3PO Android Explorer.
 * Creates the main tool window with tabbed interface for device exploration.
 */
class C3POToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val c3poToolWindow = C3POToolWindow(project)
        val content =
            ContentFactory.getInstance().createContent(
                c3poToolWindow.getContent(),
                "",
                false,
            )
        toolWindow.contentManager.addContent(content)
    }
}
