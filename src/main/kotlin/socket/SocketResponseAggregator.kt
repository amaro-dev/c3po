package socket

class SocketResponseAggregator {

    private val cache = HashMap<String, MutableList<String>>()

    private val ready: MutableList<CommandEntry> = mutableListOf()

    fun parse(line: String) {
        if (line.startsWith("BEGIN")) {
            val (_, id) = line.split(' ')
            cache[id] = mutableListOf()
        } else if (line.startsWith("END")) {
            val (_, id, command) = line.split(' ')
            if (cache.contains(id)) ready.add(CommandEntry(id, command))
        } else {
            cache[line.split(' ')[0]]?.add(line.substringAfter(' '))
        }
    }

    fun readyToDeliver(): List<Pair<CommandEntry, List<String>>> =
        ready
            .toTypedArray()
            .map {
                ready.remove(it)
                val content = cache[it.id]
                cache.remove(it.id)
                it to (content ?: emptyList())
            }
            .toList()

}

data class CommandEntry(val id: String, val command: String)
