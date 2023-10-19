package ru.itmo.graphics.viewmodel.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import ru.itmo.graphics.model.Actions
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ApplicationColorSpaceChanged
import ru.itmo.graphics.viewmodel.presentation.viewmodel.Channel
import ru.itmo.graphics.viewmodel.presentation.viewmodel.Channel.ALL
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ChannelSettingsChanged
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.OpenFileEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.SaveEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.StartSaveAsEvent

@Composable
fun FrameWindowScope.MenuBarView(
    showChannels: Map<Channel, Boolean>,
    onEvent: (ImageEvent) -> Unit,
) {
    MenuBar {
        Menu(
            text = "File",
            mnemonic = 'F',
        ) {
            Item(
                Actions.OPEN.toString(),
                onClick = { onEvent(OpenFileEvent) },
                shortcut = KeyShortcut(Key.O, ctrl = true),
            )
            Item(
                Actions.SAVE.toString(),
                onClick = { onEvent(SaveEvent) },
                shortcut = KeyShortcut(Key.S, ctrl = true),
            )
            Item(
                Actions.SAVEAS.toString(),
                onClick = { onEvent(StartSaveAsEvent) },
                shortcut = KeyShortcut(Key.S, ctrl = true, shift = true),
            )
        }
        Menu(
            text = "Channels",
            mnemonic = 'F',
        ) {
            val allActive = showChannels.values.all { value -> value }

            showChannels.entries.forEach { (channel, isActive) ->
                CheckboxItem(
                    channel.text,
                    onCheckedChange = { onEvent(ChannelSettingsChanged(channel)) },
                    checked = !allActive && isActive,
                )
            }

            CheckboxItem(
                ALL.text,
                onCheckedChange = { onEvent(ChannelSettingsChanged(ALL)) },
                checked = allActive,
            )
        }
        Menu(
            text = "Colorspaces",
            mnemonic = 'F',
        ) {
            Item(
                "Switch colorspace",
                onClick = { onEvent(ApplicationColorSpaceChanged) },
            )
        }
    }
}
