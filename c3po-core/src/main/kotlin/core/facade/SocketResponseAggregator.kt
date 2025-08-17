package core.facade

import debug

class SocketResponseAggregator {
    private val cache = HashMap<String, MutableList<String>>()
    private val ready: MutableList<CommandEntry> = mutableListOf()
    private val commandTimestamps = HashMap<String, Long>()

    fun parse(line: String) {
        debug("Parsing socket line: $line")
        try {
            if (line.startsWith("BEGIN")) {
                val parts = line.split(' ')
                if (parts.size >= 2) {
                    val id = parts[1]
                    cache[id] = mutableListOf()
                    commandTimestamps[id] = System.currentTimeMillis()
                    debug("Started aggregating response for command: $id")
                } else {
                    debug("Malformed BEGIN line: $line")
                }
            } else if (line.startsWith("END")) {
                val params = line.split(' ')
                if (params.size >= 3) {
                    val id = params[1]
                    val command = params[2]
                    val arg = if (params.size > 3) params[3] else null
                    if (cache.contains(id)) {
                        ready.add(CommandEntry(id, command, arg))
                        commandTimestamps.remove(id)
                        debug("Command $id ($command) ready for delivery")
                    } else {
                        debug("END received for unknown command: $id")
                    }
                } else {
                    debug("Malformed END line: $line")
                }
            } else {
                val parts = line.split(' ', limit = 2)
                if (parts.size >= 2) {
                    val id = parts[0]
                    val content = parts[1]
                    cache[id]?.add(content) ?: debug("Content received for unknown command: $id")
                } else {
                    debug("Malformed content line: $line")
                }
            }
        } catch (e: Exception) {
            debug("Error parsing socket line '$line': ${e.message}")
        }
    }

    fun checkForTimedOutCommands(): List<String> {
        val currentTime = System.currentTimeMillis()
        val timeoutMs = 15000 // 15 seconds timeout for individual commands
        val timedOut = mutableListOf<String>()

        commandTimestamps.entries.removeAll { (id, timestamp) ->
            if (currentTime - timestamp > timeoutMs) {
                debug("Command $id timed out after ${timeoutMs}ms")
                cache.remove(id)
                timedOut.add(id)
                true
            } else {
                false
            }
        }

        return timedOut
    }

    fun readyToDeliver(): List<Pair<CommandEntry, List<String>>> =
        ready
            .toTypedArray()
            .map {
                ready.remove(it)
                val content = cache[it.id]
                cache.remove(it.id)
                it to (content ?: emptyList())
            }.toList()
}

data class CommandEntry(
    val id: String,
    val command: String,
    val arg: String? = null,
)
