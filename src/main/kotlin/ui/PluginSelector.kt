package ui

import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.composables.ui.Menu
import com.composables.ui.MenuButton
import com.composables.ui.MenuContent
import com.composables.ui.MenuItem
import core.Action
import dev.amaro.sonic.IAction
import ui.plugins.Plugin

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PluginSelector(plugins: List<Plugin<*>>, onSelect: (IAction) -> Unit) {
    Column() {
        Menu(Modifier.align(Alignment.End)) {
            MenuButton(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, MaterialTheme.colors.onPrimary, RoundedCornerShape(6.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Image(
                        Icons.Filled.Add, null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onPrimary)
                    )
                }
            }

            MenuContent(
                modifier = Modifier.width(320.dp)
                    .border(1.dp, MaterialTheme.colors.onSurface, RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colors.surface)
                    .clip(RoundedCornerShape(4.dp))
                    .padding(4.dp),
                hideTransition = fadeOut()
            ) {
                plugins.forEach {
                    MenuItem(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                        onClick = { onSelect(Action.StartPlugin(it.name)) }
                    ) {
                        BasicText(
                            it.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 6.dp)
                        )
                    }
                }
            }
        }
    }

}
