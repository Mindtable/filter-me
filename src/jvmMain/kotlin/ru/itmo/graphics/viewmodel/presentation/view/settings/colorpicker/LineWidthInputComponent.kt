package ru.itmo.graphics.viewmodel.presentation.view.settings.colorpicker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LineSettingInput(
    modifier: Modifier = Modifier,
    textToInput: String,
    placeholder: String = "",
    units: String = "",
    textUpdate: (String) -> Unit,
) {
    TextField(
        value = textToInput,
        onValueChange = textUpdate,
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
            cursorColor = MaterialTheme.colorScheme.onSecondaryContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        trailingIcon = {
            Text(units, color = MaterialTheme.colorScheme.onSecondaryContainer)
        },
        singleLine = true,
        placeholder = { Text(placeholder) },
    )
}
