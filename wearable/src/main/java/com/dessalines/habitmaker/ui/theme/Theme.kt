package com.dessalines.habitmaker.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content,
    )
}
