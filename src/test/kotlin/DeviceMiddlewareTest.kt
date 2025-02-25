import commands.CommandExecutor
import core.Action
import core.AppState
import core.DeviceMiddleware
import core.IActionScheduler
import dev.amaro.sonic.IProcessor
import io.mockk.Matcher
import io.mockk.MockKMatcherScope
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import models.AdbDevice
import org.junit.jupiter.api.Test
import java.util.Properties
import kotlin.reflect.KClass

class DeviceMiddlewareTest {

    @Test
    fun `Handle RefreshDevice command when single device encountered`() = runTest {
        val device: AdbDevice = mockk(relaxed = true)
        val commander: CommandExecutor = mockk(relaxed = true) {
            coEvery { go<List<AdbDevice>>(any(), any()) } returns Result.success(listOf(device))
        }
        val settings = Properties().apply { put(Settings.ADB_PATH_PROP, "") }
        val middleware = DeviceMiddleware(commander)
        val state = AppState(settings = settings)
        val processor: IProcessor<AppState> = mockk(relaxed = true, moreInterfaces = arrayOf(IActionScheduler::class))

        middleware.asyncProcess(Action.RefreshDevices, state, processor)

        verify {
            processor.reduce(Action.DeliverDevices(listOf(device)))
            processor.perform(Action.SelectDevice(device))
        }
    }

    @Test
    fun `Handle RefreshDevice command when multiple devices encountered`() = runTest {
        val device: AdbDevice = mockk(relaxed = true)
        val commander: CommandExecutor = mockk(relaxed = true) {
            coEvery { go<List<AdbDevice>>(any(), any()) } returns Result.success(listOf(device, mockk()))
        }
        val settings = Properties().apply { put(Settings.ADB_PATH_PROP, "") }
        val middleware = DeviceMiddleware(commander)
        val state = AppState(settings = settings)
        val processor: IProcessor<AppState> = mockk(relaxed = true, moreInterfaces = arrayOf(IActionScheduler::class))

        middleware.asyncProcess(Action.RefreshDevices, state, processor)

        verify(exactly = 0) {
            processor.perform(any())
        }
    }

    @Test
    fun `Handle RefreshDevice command when fails to list devices`() = runTest {
        val commander: CommandExecutor = mockk(relaxed = true) {
            coEvery { go<List<AdbDevice>>(any(), any()) } returns Result.failure(Exception())
        }
        val settings = Properties().apply { put(Settings.ADB_PATH_PROP, "") }
        val middleware = DeviceMiddleware(commander)
        val state = AppState(settings = settings)
        val processor: IProcessor<AppState> = mockk(relaxed = true, moreInterfaces = arrayOf(IActionScheduler::class))

        middleware.asyncProcess(Action.RefreshDevices, state, processor)

        verify(exactly = 0) {
            processor.reduce(anyOfType<Action.DeliverDevices>())
        }
    }

    @Test
    fun `Handle CommandAction triggers running state`() = runTest {
        val commander: CommandExecutor = mockk(relaxed = true)
        val middleware = DeviceMiddleware(commander)
        val state = AppState()
        val processor: IProcessor<AppState> = mockk(relaxed = true, moreInterfaces = arrayOf(IActionScheduler::class))

        middleware.asyncProcess(mockk<Action.CommandAction>(relaxed = true), state, processor)

        verify {
            processor.reduce(Action.SetCommandRunning)
        }
    }

    @Test
    fun `Handle Non-CommandAction does not trigger running state`() = runTest {
        val commander: CommandExecutor = mockk(relaxed = true)
        val middleware = DeviceMiddleware(commander)
        val state = AppState()
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.asyncProcess(mockk<Action.DoNothing>(relaxed = true), state, processor)

        verify(exactly = 0) {
            processor.reduce(Action.SetCommandRunning)
        }
    }

}

class AnyOfTypeMatcher<T>(private val expectedType: KClass<*>) : Matcher<T> {
    override fun match(arg: T?): Boolean {
        return arg?.let { it.javaClass == expectedType.java } ?: false
    }
}

inline fun <reified T> MockKMatcherScope.anyOfType(): T = match(AnyOfTypeMatcher(T::class))
