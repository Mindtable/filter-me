package ru.itmo.graphics.viewmodel.presentation.view.settings.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.HsvColor

@Composable
fun ColorPaletteBar(
    modifier: Modifier = Modifier,
    colors: List<HsvColor>,
) {
    LazyVerticalGrid(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        columns = GridCells.Fixed(colors.size),
        modifier = modifier
            .fillMaxWidth(),
        content = {
            items(colors) { color ->
                Canvas(modifier = Modifier.size(48.dp)) {
                    drawRect(color.toColor())
                }
            }
        },
    )
}
