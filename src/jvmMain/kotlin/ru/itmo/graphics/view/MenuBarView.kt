package ru.itmo.graphics.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import ru.itmo.graphics.model.Actions
import ru.itmo.graphics.model.ApplicationState
import ru.itmo.graphics.utils.chooseFileDialog

@Composable
fun FrameWindowScope.MenuBarView(applicationState: ApplicationState) {
    MenuBar {
        Menu(
            text = "File",
            mnemonic = 'F',
        ) {
            Item(
                Actions.OPEN.toString(),
                onClick = {
                    applicationState.onOpenFileClick(chooseFileDialog(window).absolutePath)
                },
                shortcut = KeyShortcut(Key.O, ctrl = true),
            )
            Item(
                Actions.SAVE.toString(),
                onClick = applicationState::onSaveButtonClick,
                shortcut = KeyShortcut(Key.S, ctrl = true),
            )
            Item(
                Actions.SAVEAS.toString(),
                onClick = { applicationState.onSavedAsButtonClick("") },
                shortcut = KeyShortcut(Key.S, ctrl = true, shift = true),
            )
        }
    }
}