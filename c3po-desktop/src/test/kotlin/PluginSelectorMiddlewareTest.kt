/* TODO: Fix compilation errors
import core.model.Action
import core.model.AppState
import core.model.IMiddleware
import core.model.IProcessor
import core.model.PluginSelectorMiddleware
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class PluginSelectorMiddlewareTest {
    @Test
    fun `Handling StartPlugin action sets running status`() {
        val action = Action.StartPlugin("plugin-id")
        val plugin: plugins.Plugin<String> =
            mockk(relaxed = true) {
                every { id } returns "plugin-id"
            }
        val middleware = PluginSelectorMiddleware(listOf(plugin))
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(action, mockk(), processor)

        verify { processor.reduce(Action.SetCommandRunning) }
    }

    @Test
    fun `Handling StartPlugin action sets current plugin`() {
        val action = Action.StartPlugin("plugin-id")
        val plugin: plugins.Plugin<String> =
            mockk(relaxed = true) {
                every { id } returns "plugin-id"
            }
        val middleware = PluginSelectorMiddleware(listOf(plugin))
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(action, mockk(), processor)

        verify { processor.reduce(Action.SelectPlugin("plugin-id")) }
    }

    @Test
    fun `Handling StartPlugin action calls plugin's middleware`() {
        val action = Action.StartPlugin("plugin-id")
        val pluginMiddleware: IMiddleware<AppState> = mockk(relaxed = true)
        val plugin: plugins.Plugin<String> =
            mockk(relaxed = true) {
                every { id } returns "plugin-id"
                every { middleware } returns pluginMiddleware
            }
        val middleware = PluginSelectorMiddleware(listOf(plugin))
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(action, mockk(), processor)

        verify { pluginMiddleware.process(action, any(), processor) }
    }

    @Test
    fun `Handling StartPlugin for unknown plugin does nothing`() {
        val action = Action.StartPlugin("unknown-plugin")
        val pluginMiddleware: IMiddleware<AppState> = mockk(relaxed = true)

        val plugin: plugins.Plugin<String> =
            mockk(relaxed = true) {
                every { id } returns "plugin-id"
                every { middleware } returns pluginMiddleware
            }
        val middleware = PluginSelectorMiddleware(listOf(plugin))
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(action, mockk(), processor)

        verify(exactly = 0) {
            processor.reduce(Action.SetCommandRunning)
            processor.reduce(Action.SelectPlugin("plugin-id"))
            pluginMiddleware.process(action, any(), processor)
        }
    }

    @Test
    fun `Handling other actions which the plugin is responsible calls its middleware`() {
        val action = Action.DoNothing
        val pluginMiddleware: IMiddleware<AppState> = mockk(relaxed = true)
        val plugin: plugins.Plugin<String> =
            mockk(relaxed = true) {
                every { id } returns "plugin-id"
                every { middleware } returns pluginMiddleware
                every { isResponsibleFor(any()) } returns true
            }
        val middleware = PluginSelectorMiddleware(listOf(plugin))
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(action, mockk(), processor)

        verify { pluginMiddleware.process(action, any(), processor) }
    }

    @Test
    fun `Handling other actions which the plugin is NOT responsible does not call its middleware`() {
        val action = Action.DoNothing
        val pluginMiddleware: IMiddleware<AppState> = mockk(relaxed = true)
        val plugin: plugins.Plugin<String> =
            mockk(relaxed = true) {
                every { id } returns "plugin-id"
                every { middleware } returns pluginMiddleware
                every { isResponsibleFor(any()) } returns false
            }
        val middleware = PluginSelectorMiddleware(listOf(plugin))
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(action, mockk(), processor)

        verify(exactly = 0) { pluginMiddleware.process(action, any(), processor) }
    }
}
*/
