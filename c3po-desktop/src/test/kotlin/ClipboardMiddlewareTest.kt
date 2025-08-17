import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import core.model.Action
import core.model.AppState
import core.model.ClipboardMiddleware
import core.model.IProcessor
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

class ClipboardMiddlewareTest {
    @Test
    fun `Process CopyText action`() {
        val clipboard: Clipboard = mockk(relaxed = true)
        val middleware = ClipboardMiddleware(clipboard)
        val processor: IProcessor<AppState> = mockk(relaxed = true)
        val slot = CapturingSlot<StringSelection>()
        every { clipboard.setContents(capture(slot), null) } just runs

        middleware.process(Action.CopyText("teste"), AppState(), processor)

        assertThat(slot.captured).all {
            isInstanceOf(StringSelection::class)
            transform { it.getTransferData(DataFlavor.stringFlavor) }.isEqualTo("teste")
        }
    }

    @Test
    fun `Do not process other actions`() {
        val clipboard: Clipboard = mockk(relaxed = true)
        val middleware = ClipboardMiddleware(clipboard)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(mockk(), AppState(), processor)

        verify(exactly = 0) {
            clipboard.setContents(any(), any())
            processor.reduce(any())
            processor.perform(any())
        }
    }
}
