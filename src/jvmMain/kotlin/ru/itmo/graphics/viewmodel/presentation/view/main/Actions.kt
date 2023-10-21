package ru.itmo.graphics.viewmodel.presentation.view.main

enum class Actions(private val actionString: String) {
    OPEN("Open"),
    SAVE("Save"),
    SAVEAS("Save as"),
    ;

    override fun toString(): String = actionString
}
