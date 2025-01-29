package scripting

class UnknownInstructionException(instructionName: String) :
    Exception("Could not parse '$instructionName' instruction.")
