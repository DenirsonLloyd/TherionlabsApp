package com.dsy1105.therionlabsapp.interfazAdmin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsy1105.therionlabsapp.datos.ProspectoEntidad
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ListadoMaestroProspectos(
    prospectos: List<ProspectoEntidad>,
    onVolver: () -> Unit,
    onExportarCsv: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var busqueda by remember { mutableStateOf("") }
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es",
        "CL")) }
    val filtrados = remember(prospectos, busqueda) {
        prospectos.filter {
            val q = busqueda.trim()
            q.isEmpty() || it.nombre.contains(q, true) ||
                    it.correo.contains(q, true) || it.telefono.contains(q,
                true) ||
                    it.cursoInteres.contains(q, true)
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("LISTADO MAESTRO DE PROSPECTOS", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${filtrados.size} registro(s)", color = Color.Gray, fontSize = 13.sp)
            }
            OutlinedButton(onClick = onVolver) { Text("VOLVER") }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.weight(1f),
                label = { Text("Buscar prospecto") },
                singleLine = true
            )
            Button(
                onClick = { onExportarCsv(generarCsv(filtrados)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD5E9FF))
            ) {
                Text("EXPORTAR CSV", color = Color(0xFF17212B))
            }
        }

        Row(
            Modifier.fillMaxWidth().background(Color(0xFFE6E9EC),
                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Nombre / contacto", Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Curso / interés", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Fecha", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Estado", Modifier.weight(.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        if (filtrados.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (prospectos.isEmpty())
                    "Todavía no hay prospectos registrados." else "No hay resultados para esa búsqueda.",
                    color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtrados, key = { it.id }) { prospecto ->
                    Row(
                        Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(Modifier.weight(1.5f)) {
                            Text(prospecto.nombre, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(prospecto.correo, fontSize = 11.sp, color = Color.DarkGray)
                            Text(prospecto.telefono, fontSize = 11.sp, color = Color.DarkGray)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(prospecto.cursoInteres.ifBlank { "Sin curso" }, fontSize = 12.sp)
                            Text(prospecto.areaInteres.ifBlank { "Sin área" }, fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(formatoFecha.format(Date(prospecto.fechaCreacion)), Modifier.weight(1f), fontSize = 11.sp)
                        Text(
                            if (prospecto.esSincronizado) "Enviado" else "Pendiente",
                            Modifier.weight(.8f),
                            fontSize = 11.sp,
                            color = if (prospecto.esSincronizado) Color(0xFF16804A) else Color(0xFFB56B00),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private fun generarCsv(prospectos: List<ProspectoEntidad>): String {
    fun limpiar(valor: String) = "\"" + valor.replace("\"", "\"\"").replace("\n", " ") + "\""
    val encabezado = "ID,Nombre,Correo,Telefono,Nivel educativo,Area de interes,Curso,Modalidad,Estado,Fecha"
    val filas = prospectos.map {
        listOf(
            it.id.toString(), limpiar(it.nombre), limpiar(it.correo), limpiar(it.telefono),
            limpiar(it.nivelEducativo), limpiar(it.areaInteres), limpiar(it.cursoInteres),
            limpiar(it.modalidad), limpiar(if (it.esSincronizado) "Enviado" else "Pendiente"),
            limpiar(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "CL")).format(Date(it.fechaCreacion)))
        ).joinToString(",")
    }
    return (listOf(encabezado) + filas).joinToString("\n")
}
