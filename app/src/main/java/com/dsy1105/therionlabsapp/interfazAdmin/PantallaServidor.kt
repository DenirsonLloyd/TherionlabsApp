package com.dsy1105.therionlabsapp.interfazAdmin

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
import com.dsy1105.therionlabsapp.api.PostulacionApi
import com.dsy1105.therionlabsapp.interfaz.ProspecctoViewModel
import com.dsy1105.therionlabsapp.interfaz.VistaPreviaFotoServidor
import kotlinx.coroutines.launch

// Pantalla administrativa para demostrar GET, PUT y DELETE desde Android hacia Spring Boot.
// La creación POST se ejecuta al enviar el formulario de un estudiante.
@Composable
fun PantallaServidor(vm: ProspecctoViewModel, onVolver: () -> Unit) {
    var postulaciones by remember { mutableStateOf<List<PostulacionApi>>(emptyList()) }
    var numeroUsuarios by remember { mutableIntStateOf(0) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun recargar() {
        scope.launch {
            cargando = true
            try {
                postulaciones = ApiCliente.postulaciones.listar().sortedByDescending { it.id }
                numeroUsuarios = ApiCliente.usuarios.listar().size
                mensaje = "Datos obtenidos desde Spring Boot"
            } catch (_: Exception) {
                mensaje = "No se pudo conectar con Spring Boot. Enciende los servicios 8081 y 8082."
            } finally { cargando = false }
        }
    }
    LaunchedEffect(Unit) { recargar() }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, null); Text("Volver al administrador") }
        Text("Servidor Spring Boot", style = MaterialTheme.typography.headlineSmall)
        Text("Usuarios remotos: $numeroUsuarios · Postulaciones remotas: ${postulaciones.size}")
        OutlinedButton(onClick = { recargar() }, enabled = !cargando) { Text("Consultar datos del servidor") }
        if (cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (mensaje.isNotBlank()) Text(mensaje)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(postulaciones, key = { it.id }) { p ->
                var opciones by remember { mutableStateOf(false) }
                var mostrarFoto by remember { mutableStateOf(false) }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("#${p.id} ${p.nombre}", style = MaterialTheme.typography.titleMedium)
                        Text(p.carrera)
                        Text("Estado: ${p.estado}")
                        if (p.tieneFoto) {
                            TextButton(onClick = { mostrarFoto = !mostrarFoto }) {
                                Text(if (mostrarFoto) "Ocultar fotografía" else "Ver fotografía del servidor")
                            }
                            if (mostrarFoto) VistaPreviaFotoServidor(p.id)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box {
                                OutlinedButton(onClick = { opciones = true }) { Text("Cambiar estado") }
                                DropdownMenu(expanded = opciones, onDismissRequest = { opciones = false }) {
                                    listOf("Recibida", "En revisión", "Aceptada", "Rechazada").forEach { estado ->
                                        DropdownMenuItem(text = { Text(estado) }, onClick = {
                                            opciones = false
                                            scope.launch {
                                                try {
                                                    vm.cambiarEstadoServidor(p.id, estado)
                                                    recargar()
                                                } catch (_: Exception) { mensaje = "Error al actualizar en el servidor" }
                                            }
                                        })
                                    }
                                }
                            }
                            TextButton(onClick = {
                                scope.launch {
                                    try {
                                        vm.eliminarServidor(p.id)
                                        recargar()
                                    } catch (_: Exception) { mensaje = "No se pudo eliminar del servidor" }
                                }
                            }) { Text("Eliminar") }
                        }
                    }
                }
            }
        }
    }
}
