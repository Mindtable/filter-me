package ru.itmo.graphics.viewmodel.presentation.view.settings.core

enum class SettingsType(
    val title: String,
    val description: String,
) {
    GAMMA("Gamma", "Gamma settings"),
    HISTOGRAM("Histogram", "Histogram image view"),
    WIDTHCOLORPICKER("Line Settings", "Line settings"),
}
