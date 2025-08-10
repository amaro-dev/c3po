package models

enum class CommandStatus {
    Idle,
    Running,
    Completed,
    Failed,
    ;

    fun isResult() = this in arrayOf(Completed, Failed)
}
