import commands.CommandResult

object CommandRunner {
    fun run(command: String): CommandResult {
        //println("Command: '$command'")
        val process = ProcessBuilder().command(command.split(' ')).start()
        val response = process.inputReader().readText().trim()
        val error = process.errorReader().readText().trim()
        process.waitFor()
//        println("Result: ${process.exitValue()}")
//        response.takeIf { it.isNotEmpty() }?.run { println("Response: $this") }
//        error.takeIf { it.isNotEmpty() }?.run { println("Error: $this") }
        return CommandResult(response)
    }
}
