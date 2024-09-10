import assertk.all
import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.Test
import socket.CommandEntry
import socket.SocketResponseAggregator

class SocketResponseAggregatorTest {
    @Test
    fun `Full aggregation`() {
        val aggregator = SocketResponseAggregator()
        aggregator.parse("BEGIN 123")
        aggregator.parse("123 Line 1")
        aggregator.parse("123 Line 2")
        aggregator.parse("END 123 cmd")
        assertThat(aggregator.readyToDeliver()).all {
            first().all {
                prop(Pair<CommandEntry, List<String>>::first).isEqualTo(CommandEntry("123", "cmd"))
                prop(Pair<CommandEntry, List<String>>::second).containsExactly(
                    "Line 1",
                    "Line 2"
                )
            }
            hasSize(1)
        }
        assertThat(aggregator.readyToDeliver()).hasSize(0)
        aggregator.parse("BEGIN 123")
        aggregator.parse("123 Line 1")
        aggregator.parse("123 Line 2")
        aggregator.parse("END 123 cmd")
        assertThat(aggregator.readyToDeliver()).all {
            first().all {
                prop(Pair<CommandEntry, List<String>>::first).isEqualTo(CommandEntry("123", "cmd"))
                prop(Pair<CommandEntry, List<String>>::second).all {
                    hasSize(2)
                    containsExactly(
                        "Line 1",
                        "Line 2"
                    )
                }
            }
            hasSize(1)
        }
        assertThat(aggregator.readyToDeliver()).hasSize(0)
    }

    @Test
    fun `Incomplete aggregation`() {
        val aggregator = SocketResponseAggregator()
        aggregator.parse("BEGIN 123")
        aggregator.parse("123 Line 1")
        aggregator.parse("123 Line 2")
        assertThat(aggregator.readyToDeliver()).all {
            hasSize(0)
        }
    }

    @Test
    fun `No initialization`() {
        val aggregator = SocketResponseAggregator()
        aggregator.parse("123 Line 1")
        aggregator.parse("123 Line 2")
        aggregator.parse("END 123 cmd")
        assertThat(aggregator.readyToDeliver()).all {
            hasSize(0)
        }
    }

    @Test
    fun `Empty aggregation`() {
        val aggregator = SocketResponseAggregator()
        aggregator.parse("BEGIN 123")
        aggregator.parse("END 123 cmd")
        assertThat(aggregator.readyToDeliver()).all {
            first().all {
                prop(Pair<CommandEntry, List<String>>::first).isEqualTo(CommandEntry("123", "cmd"))
                prop(Pair<CommandEntry, List<String>>::second).hasSize(0)
            }
            hasSize(1)
        }
    }
}
