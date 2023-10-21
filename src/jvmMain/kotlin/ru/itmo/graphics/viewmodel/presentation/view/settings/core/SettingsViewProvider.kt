package ru.itmo.graphics.viewmodel.presentation.view.settings.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState

interface SettingsViewProvider {

    val type: SettingsType

    @Composable
    fun draw(state: ImageState, onEvent: (ImageEvent) -> Unit)

    @Composable
    fun configureWindow(window: ComposeWindow)
}
