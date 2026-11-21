package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dsy1105.therionlabsapp.api.ApiCliente
import com.dsy1105.therionlabsapp.api.FeriadoApi
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun PantallaFeriados(onVolver: () -> Unit) {
    var feriados by remember { mutableStateOf<List<FeriadoApi>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val anio = remember { Calendar.getInstance().get(Calendar.YEAR) }

    fun cargar() {
        scope.launch {
            cargando = true
            mensaje = ""
            try {
                feriados = ApiCliente.feriados.obtenerChile(anio).sortedBy { it.date }
            } catch (_: Exception) {
                mensaje = "No se pudo consultar la API externa. Revisa internet."
            } finally { cargando = false }
        }
    }
    LaunchedEffect(anio) { cargar() }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row {
            TextButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, null); Text("Volver") }
        }
        Text("Feriados de Chile $anio", style = MaterialTheme.typography.headlineSmall)
        Text("Información externa de Nager.Date consultada con Retrofit.")
        OutlinedButton(onClick = { cargar() }, enabled = !cargando) { Text("Actualizar feriados") }
        if (cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (mensaje.isNotBlank()) Text(mensaje, color = MaterialTheme.colorScheme.error)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(feriados) { feriado ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(feriado.localName, style = MaterialTheme.typography.titleMedium)
                        Text(feriado.date)
                    }
                }
            }
        }
    }
}
