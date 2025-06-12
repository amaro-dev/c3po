package ui.rows

import core.IAction

data class RowAction(
    val icon: String,
    val description: String,
    val action: IAction,
)
