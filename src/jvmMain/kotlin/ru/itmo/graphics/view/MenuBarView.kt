package ru.itmo.graphics.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.model.Actions
import ru.itmo.graphics.model.ApplicationState
import ru.itmo.graphics.model.ImageModel
import ru.itmo.graphics.utils.chooseFileDialog
import java.awt.FileDialog

@Composable
fun FrameWindowScope.MenuBarView(applicationState: ApplicationState, imageModel: ImageModel?) {
    MenuBar {
        Menu(
            text = "File",
            mnemonic = 'F',
        ) {
            Item(
                Actions.OPEN.toString(),
                onClick = {
                    applicationState.onOpenFileClick(chooseFileDialog(window, FileDialog.LOAD).absolutePath)
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
                onClick = {
                    applicationState.onSavedAsButtonClick(chooseFileDialog(window, FileDialog.SAVE).absolutePath)
                },
                shortcut = KeyShortcut(Key.S, ctrl = true, shift = true),
            )
            Item(
                "KEK",
                onClick = {
                    log.info { imageModel?.bitmap == null }
                    log.info { imageModel?.file }
                    log.info { imageModel?.type?.let { it::class } }
                },
            )
        }
    }
}

private val log = KotlinLogging.logger { }