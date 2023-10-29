package ru.itmo.graphics.viewmodel.presentation.view.settings.core

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DescriptionText(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        text = text,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )
}
