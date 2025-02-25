import assertk.assertThat
import assertk.assertions.isEqualTo
import core.Action
import core.App
import core.AppState
import core.AppStateManager
import dev.amaro.sonic.IAction
import di.Names
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import plugins.Plugin
import socket.SocketClient

class AppTest {

    private val stateManager: AppStateManager = mockk(relaxed = true)
    private val plugin: List<Plugin<*>> = mockk(relaxed = true)
    private val socketClient: SocketClient = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        startKoin {
            modules(
                module {
                    single { stateManager }
                    single(named(Names.PLUGIN_LIST_DEPENDENCY)) { plugin }
                    single<SocketClient> { socketClient }
                }
            )
        }
    }

    @Test
    fun `On start, perform load settings`() {
        val app = App()
        app.start()

        verify { stateManager.perform(Action.LoadSettings) }
    }

    @Test
    fun `On perform, call perform on state manager`() {
        val app = App()
        val action: IAction = mockk(relaxed = true)
        app.perform(action)

        verify { stateManager.perform(action) }
    }

    @Test
    fun `On listen, calls state manager `() {
        val app = App()
        val state: MutableStateFlow<AppState> = mockk(relaxed = true)
        every { stateManager.listen() } returns state

        assertThat(app.listen())
            .isEqualTo(state)
    }

    @Test
    fun `On exit, socket client closes`() {
        val app = App()

        app.exit()

        verify { socketClient.close() }
    }

    @AfterEach
    fun tearDown() {
        clearMocks(stateManager, plugin, socketClient)
        stopKoin()
    }
}
