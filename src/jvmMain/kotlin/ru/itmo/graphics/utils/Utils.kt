package ru.itmo.graphics.utils

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

fun chooseFileDialog(parent: Frame, mode: Int): File = FileDialog(parent, "Select File", mode)
    .apply {
        isMultipleMode = false
        isVisible = true
    }.files.first()