package ru.itmo.graphics.viewmodel.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuScope
import ru.itmo.graphics.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.image.colorspace.CmyColorSpace
import ru.itmo.graphics.image.colorspace.RgbColorSpace
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
    activeColorSpace: ApplicationColorSpace,
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
            ColorSpaceCheckbox(RgbColorSpace, activeColorSpace, onEvent)
            ColorSpaceCheckbox(CmyColorSpace, activeColorSpace, onEvent)
        }
    }
}

@Composable
fun MenuScope.ColorSpaceCheckbox(
    colorSpaceToShow: ApplicationColorSpace,
    activeColorSpace: ApplicationColorSpace,
    onEvent: (ImageEvent) -> Unit,
) {
    CheckboxItem(
        colorSpaceToShow.name,
        onCheckedChange = { onEvent(ApplicationColorSpaceChanged(colorSpaceToShow)) },
        checked = colorSpaceToShow == activeColorSpace,
    )
}
