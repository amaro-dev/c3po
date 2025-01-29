package scripting

interface InstructionIdentifier {
    fun detect(line: String): Instruction
}
