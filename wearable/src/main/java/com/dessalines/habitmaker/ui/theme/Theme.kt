package com.dessalines.habitmaker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme

@Composable
fun Theme(content: @Composable () -> Unit) {
    val dynamicColorScheme = dynamicColorScheme(LocalContext.current)
    MaterialTheme(
        content = content,
        colorScheme = dynamicColorScheme ?: MaterialTheme.colorScheme,
    )
}
