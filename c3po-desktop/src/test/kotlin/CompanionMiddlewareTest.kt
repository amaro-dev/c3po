/* TODO: Fix compilation errors
import core.model.Action
import core.model.AppState
import core.model.CompanionMiddleware
import core.model.CompanionState
import core.model.IActionScheduler
import core.model.IProcessor
import facade.CompanionCommander
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import models.AdbDevice
import org.junit.jupiter.api.Test
import java.util.Properties

class CompanionMiddlewareTest {
    @Test
    fun `Handle SelectDevice action when there's a new device`() =
        runBlocking {
            val commander: CompanionCommander = mockk(relaxed = true)
            val middleware = CompanionMiddleware(commander)
            val device: AdbDevice = mockk(relaxed = true)
            val state = AppState(currentDevice = device)
            val processor: IProcessor<AppState> =
                mockk(relaxed = true, moreInterfaces = arrayOf(IActionScheduler::class))

            middleware.asyncProcess(Action.SelectDevice(mockk(relaxed = true)), state, processor)

            verify { (processor as IActionScheduler).schedule(Action.Companion.CheckInstalled) }
        }

    @Test
    fun `Handle SelectDevice action when it's the same device`() =
        runBlocking {
            val commander: CompanionCommander = mockk(relaxed = true)
            val middleware = CompanionMiddleware(commander)
            val device: AdbDevice = mockk(relaxed = true)
            val state = AppState(currentDevice = device)
            val processor: IProcessor<AppState> =
                mockk(relaxed = true, moreInterfaces = arrayOf(IActionScheduler::class))

            middleware.asyncProcess(Action.SelectDevice(device), state, processor)

            verify(exactly = 0) { (processor as IActionScheduler).schedule(Action.Companion.CheckInstalled) }
        }

    @Test
    fun `Handle Install action when has a current device`() =
        runBlocking {
            val commander: CompanionCommander =
                mockk(relaxed = true) {
                    coEvery { installAndPrepare(any(), any()) } returns Result.success(Unit)
                }
            val settings = Properties().apply { put(Settings.ADB_PATH_PROP, "") }
            val middleware = CompanionMiddleware(commander)
            val device: AdbDevice = mockk(relaxed = true)
            val state = AppState(currentDevice = device, settings = settings)
            val processor: IProcessor<AppState> = mockk(relaxed = true)

            middleware.asyncProcess(Action.Companion.Install, state, processor)

            coVerify {
                commander.installAndPrepare(any(), device)
                processor.perform(Action.Companion.Connect)
            }
        }

    @Test
    fun `Handle Install action when install fails`() =
        runBlocking {
            val commander: CompanionCommander =
                mockk(relaxed = true) {
                    coEvery { installAndPrepare(any(), any()) } returns Result.failure(Exception())
                }
            val settings = Properties().apply { put(Settings.ADB_PATH_PROP, "") }
            val middleware = CompanionMiddleware(commander)
            val device: AdbDevice = mockk(relaxed = true)
            val state = AppState(currentDevice = device, settings = settings)
            val processor: IProcessor<AppState> = mockk(relaxed = true)

            middleware.asyncProcess(Action.Companion.Install, state, processor)

            coVerify(exactly = 0) {
                processor.perform(Action.Companion.Connect)
            }
        }

    @Test
    fun `Handle Install action when there's no current device`() =
        runBlocking {
            val commander: CompanionCommander = mockk(relaxed = true)
            val settings = Properties().apply { put(Settings.ADB_PATH_PROP, "") }
            val middleware = CompanionMiddleware(commander)
            val state = AppState(settings = settings)
            val processor: IProcessor<AppState> = mockk(relaxed = true)

            middleware.asyncProcess(Action.Companion.Install, state, processor)

            coVerify(exactly = 0) {
                commander.installAndPrepare(any(), any())
                processor.perform(any())
                processor.reduce(any())
            }
        }

    @Test
    fun `Handle Prepare action when has a current device`() =
        runBlocking {
            val commander: CompanionCommander =
                mockk(relaxed = true) {
                    coEvery { prepare(any(), any()) } returns Result.success(Unit)
                }
            val settings = Properties().apply { put(Settings.ADB_PATH_PROP, "") }
            val middleware = CompanionMiddleware(commander)
            val device: AdbDevice = mockk(relaxed = true)
            val state = AppState(currentDevice = device, settings = settings)
            val processor: IProcessor<AppState> = mockk(relaxed = true)

            middleware.asyncProcess(Action.Companion.Prepare, state, processor)

            coVerify {
                commander.prepare(any(), device)
                processor.perform(Action.Companion.Connect)
            }
        }

    @Test
    fun `Handle Prepare action when procedure fails`() =
        runBlocking {
            val commander: CompanionCommander =
                mockk(relaxed = true) {
                    coEvery { prepare(any(), any()) } returns Result.failure(Exception())
                }
            val settings = Properties().apply { put(Settings.ADB_PATH_PROP, "") }
            val middleware = CompanionMiddleware(commander)
            val device: AdbDevice = mockk(relaxed = true)
            val state = AppState(currentDevice = device, settings = settings)
            val processor: IProcessor<AppState> = mockk(relaxed = true)

            middleware.asyncProcess(Action.Companion.Prepare, state, processor)

            coVerify(exactly = 0) {
                processor.perform(Action.Companion.Connect)
            }
        }

    @Test
    fun `Handle Prepare action when there's no current device`() =
        runBlocking {
            val commander: CompanionCommander = mockk(relaxed = true)
            val settings = Properties().apply { put(Settings.ADB_PATH_PROP, "") }
            val middleware = CompanionMiddleware(commander)
            val state = AppState(settings = settings)
            val processor: IProcessor<AppState> = mockk(relaxed = true)

            middleware.asyncProcess(Action.Companion.Prepare, state, processor)

            coVerify(exactly = 0) {
                commander.prepare(any(), any())
                processor.perform(any())
                processor.reduce(any())
            }
        }

    @Test
    fun `Handle CheckInstalled action when companion app is installed`() =
        runBlocking {
            val commander: CompanionCommander =
                mockk(relaxed = true) {
                    coEvery { isInstalled(any(), any()) } returns Result.success(true)
                }
            val settings = Properties().apply { put(Settings.ADB_PATH_PROP, "") }
            val middleware = CompanionMiddleware(commander)
            val state = AppState(settings = settings, currentDevice = mockk(relaxed = true))
            val processor: IProcessor<AppState> =
                mockk(relaxed = true, moreInterfaces = arrayOf(IActionScheduler::class))

            middleware.asyncProcess(Action.Companion.CheckInstalled, state, processor)

            verify {
                (processor as IActionScheduler).schedule(Action.Companion.Prepare)
                processor.reduce(
                    Action.Companion.UpdateState(
                        CompanionState().setIsInstalled().setCheckedForPresence()
                    )
                )
            }
        }

    @Test
    fun `Handle CheckInstalled action when companion app is NOT installed`() =
        runBlocking {
            val commander: CompanionCommander =
                mockk(relaxed = true) {
                    coEvery { isInstalled(any(), any()) } returns Result.success(false)
                }
            val settings = Properties().apply { put(Settings.ADB_PATH_PROP, "") }
            val middleware = CompanionMiddleware(commander)
            val state = AppState(settings = settings, currentDevice = mockk(relaxed = true))
            val processor: IProcessor<AppState> =
                mockk(relaxed = true, moreInterfaces = arrayOf(IActionScheduler::class))

            middleware.asyncProcess(Action.Companion.CheckInstalled, state, processor)

            verify {
                (processor as IActionScheduler).schedule(Action.Companion.Install)
                processor.reduce(Action.Companion.UpdateState(CompanionState().setCheckedForPresence()))
            }
        }

    @Test
    fun `Handle CheckInstalled action when there's no current device`() =
        runBlocking {
            val commander: CompanionCommander = mockk(relaxed = true)
            val settings = Properties().apply { put(Settings.ADB_PATH_PROP, "") }
            val middleware = CompanionMiddleware(commander)
            val state = AppState(settings = settings)
            val processor: IProcessor<AppState> =
                mockk(relaxed = true, moreInterfaces = arrayOf(IActionScheduler::class))

            middleware.asyncProcess(Action.Companion.CheckInstalled, state, processor)

            coVerify(exactly = 0) {
                commander.prepare(any(), any())
                processor.perform(any())
                processor.reduce(any())
                (processor as IActionScheduler).schedule(any())
            }
        }
}
*/
