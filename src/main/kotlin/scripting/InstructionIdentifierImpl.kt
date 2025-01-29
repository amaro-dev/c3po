package scripting

class InstructionIdentifierImpl : InstructionIdentifier {
    private val instructions = mapOf<String, InstructionFactory>(
        UninstallInstruction.TOKEN to { UninstallInstruction(it) },
        InstallInstruction.TOKEN to { InstallInstruction(it) },
        ClearInstruction.TOKEN to { ClearInstruction(it) }
    )

    override fun detect(line: String): Instruction {
        val tokens = line.split(' ')
        return instructions[tokens[0]]?.invoke(tokens.drop(1))
            ?: throw UnknownInstructionException(tokens[0])
    }
}
