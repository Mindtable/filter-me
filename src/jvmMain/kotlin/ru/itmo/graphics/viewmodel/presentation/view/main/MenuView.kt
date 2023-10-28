package ru.itmo.graphics.viewmodel.presentation.view.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuScope
import ru.itmo.graphics.viewmodel.domain.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.viewmodel.domain.image.colorspace.CmyColorSpace
import ru.itmo.graphics.viewmodel.domain.image.colorspace.HslColorSpace
import ru.itmo.graphics.viewmodel.domain.image.colorspace.HsvColorSpace
import ru.itmo.graphics.viewmodel.domain.image.colorspace.RgbColorSpace
import ru.itmo.graphics.viewmodel.domain.image.colorspace.YCbCr601ColorSpace
import ru.itmo.graphics.viewmodel.domain.image.colorspace.YCbCr709ColorSpace
import ru.itmo.graphics.viewmodel.domain.image.colorspace.YCoCgColorSpace
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ApplicationColorSpaceChanged
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ChannelSettingsChanged
import ru.itmo.graphics.viewmodel.presentation.viewmodel.DrawingModeSwitch
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState
import ru.itmo.graphics.viewmodel.presentation.viewmodel.MonochromeModeChanged
import ru.itmo.graphics.viewmodel.presentation.viewmodel.OpenFileEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.OpenSettings
import ru.itmo.graphics.viewmodel.presentation.viewmodel.SaveEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.StartSaveAsEvent

@Composable
fun FrameWindowScope.MenuBarView(
    state: ImageState,
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
        ) {
            ImageChannel.entries.forEach { channel ->
                CheckboxItem(
                    channel.text,
                    onCheckedChange = { onEvent(ChannelSettingsChanged(channel)) },
                    checked = channel == state.channel,
                )
            }

            CheckboxItem(
                "Monochrome mode",
                onCheckedChange = { onEvent(MonochromeModeChanged) },
                checked = state.isMonochromeMode,
            )
        }
        Menu(
            text = "Colorspaces",
        ) {
            ColorSpaceCheckbox(RgbColorSpace, state.colorSpace, onEvent)
            ColorSpaceCheckbox(CmyColorSpace, state.colorSpace, onEvent)
            ColorSpaceCheckbox(HslColorSpace, state.colorSpace, onEvent)
            ColorSpaceCheckbox(HsvColorSpace, state.colorSpace, onEvent)
            ColorSpaceCheckbox(YCoCgColorSpace, state.colorSpace, onEvent)
            ColorSpaceCheckbox(YCbCr601ColorSpace, state.colorSpace, onEvent)
            ColorSpaceCheckbox(YCbCr709ColorSpace, state.colorSpace, onEvent)
        }
        Menu(
            text = "Drawing",
        ) {
            CheckboxItem(
                "Enabled",
                onCheckedChange = { onEvent(DrawingModeSwitch) },
                checked = state.drawingModeEnable,
            )
        }
        Menu(
            text = "Settings",
        ) {
            SettingsType.entries.forEach {
                Item(
                    text = it.title,
                    onClick = { onEvent(OpenSettings(it)) },
                )
            }
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
