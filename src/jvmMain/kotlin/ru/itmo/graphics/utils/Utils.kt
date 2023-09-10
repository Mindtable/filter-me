package ru.itmo.graphics.utils

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

fun chooseFileDialog(parent: Frame): File = FileDialog(parent, "Select File", FileDialog.LOAD)
    .apply {
        isMultipleMode = false
        isVisible = true
    }.files.first()