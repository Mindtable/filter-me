package ru.itmo.graphics.model

enum class Actions(private val actionString: String) {
    OPEN("Open"),
    SAVE("Save"),
    SAVEAS("Save as"),
    ;

    override fun toString(): String = actionString
}