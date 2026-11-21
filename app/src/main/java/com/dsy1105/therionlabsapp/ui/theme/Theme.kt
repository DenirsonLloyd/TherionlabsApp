package com.dsy1105.therionlabsapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColoresTherion = lightColorScheme(
    primary = DuocAmarillo,
    onPrimary = DuocNegro,
    primaryContainer = DuocAmarilloClaro,
    onPrimaryContainer = DuocNegro,
    secondary = DuocNegro,
    onSecondary = DuocBlanco,
    background = DuocFondo,
    onBackground = DuocNegro,
    surface = DuocBlanco,
    onSurface = DuocNegro,
    outline = DuocGris
)

@Composable
fun TherionlabsAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColoresTherion, typography = Typography, content = content)
}
