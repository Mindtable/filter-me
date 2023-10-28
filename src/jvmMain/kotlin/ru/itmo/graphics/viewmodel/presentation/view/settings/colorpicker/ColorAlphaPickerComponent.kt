package ru.itmo.graphics.viewmodel.presentation.view.settings.colorpicker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor

@Composable
fun ColorAlphaPicker(
    modifier: Modifier = Modifier,
    colorUpdate: (HsvColor) -> Unit,
    color: Color = Color.Red,
) {
    ClassicColorPicker(
        modifier = modifier
            .height(300.dp)
            .fillMaxWidth()
            .padding(10.dp),
        onColorChanged = colorUpdate,
        color = color,
    )
}
