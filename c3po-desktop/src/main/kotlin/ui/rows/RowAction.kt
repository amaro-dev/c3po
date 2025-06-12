package ui.rows

import dev.amaro.sonic.IAction

data class RowAction(
    val icon: String,
    val description: String,
    val action: IAction,
)
