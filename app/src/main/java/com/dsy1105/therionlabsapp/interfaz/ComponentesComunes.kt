package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dsy1105.therionlabsapp.datos.ProspectoEntidad

@Composable
fun TarjetaProspecto(prospecto: ProspectoEntidad) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (prospecto.esSincronizado) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (prospecto.esSincronizado) "Sincronizado" else "Pendiente",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = prospecto.areaInteres, style = MaterialTheme.typography.titleMedium)
                Text(text = prospecto.nombre, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}