import androidx.compose.ui.graphics.Color

fun Any?.toBool(): Boolean = this?.let { it == "true" } ?: false

inline fun <R> R.transformIf(
    condition: Boolean,
    block: (R) -> R,
): R = if (condition) block(this) else this

fun Color.darkenedBy(amount: Float): Color {
    if (amount > 1 || amount < 0) throw IllegalArgumentException()
    return copy(
        red = red * (1f - amount),
        green = green * (1f - amount),
        blue = blue * (1f - amount),
    )
}
