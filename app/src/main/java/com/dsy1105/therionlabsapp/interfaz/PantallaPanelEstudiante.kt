package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsy1105.therionlabsapp.datos.ProspectoEntidad
import com.dsy1105.therionlabsapp.datos.UsuarioEntidad
import com.dsy1105.therionlabsapp.ui.theme.DuocAmarillo
import com.dsy1105.therionlabsapp.ui.theme.DuocAmarilloClaro
import com.dsy1105.therionlabsapp.ui.theme.DuocFondo
import com.dsy1105.therionlabsapp.ui.theme.DuocNegro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaPanelEstudiante(
    vm: ProspecctoViewModel,
    usuario: UsuarioEntidad,
    onNuevaPostulacion: () -> Unit,
    onMiCuenta: () -> Unit,
    onFeriados: () -> Unit,
    onSincronizar: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    // Room consulta solamente las postulaciones relacionadas con este usuario.
    val flujo = remember(usuario.id) { vm.obtenerPorUsuario(usuario.id) }
    val misPostulaciones by flujo.collectAsState(initial = emptyList())
    var detalle by remember { mutableStateOf<ProspectoEntidad?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().background(DuocFondo)
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DuocNegro),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Default.School, contentDescription = null, tint = DuocAmarillo)
                Text("¡Hola, ${usuario.nombre.substringBefore(' ')}!", color = Color.White,
                    fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Panel del estudiante", color = DuocAmarillo, fontSize = 15.sp)
                Text("Consulta tus postulaciones y revisa su estado.", color = Color.White)
            }
        }

        Button(
            onClick = onNuevaPostulacion,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DuocAmarillo, contentColor = DuocNegro)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Nueva postulación", fontWeight = FontWeight.Bold)
        }

        Text("Mis postulaciones (${misPostulaciones.size})",
            fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DuocNegro)

        if (misPostulaciones.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Default.Assignment, contentDescription = null, tint = DuocNegro)
                    Text("Todavía no tienes postulaciones.", fontWeight = FontWeight.Bold)
                    Text("Presiona Nueva postulación para comenzar. Cuando la envíes, aparecerá aquí.")
                }
            }
        } else {
            misPostulaciones.forEach { postulacion ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { detalle = postulacion },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(postulacion.cursoInteres.ifBlank { "Postulación académica" },
                            fontWeight = FontWeight.Bold, color = DuocNegro)
                        Text("Solicitud N.º ${postulacion.id} · ${fechaCorta(postulacion.fechaCreacion)}",
                            fontSize = 12.sp, color = Color.DarkGray)
                        EtiquetaEstado(postulacion.estadoPostulacion)
                        if (postulacion.fotoRuta.isNotBlank() && !postulacion.fotoEnServidor && postulacion.servidorId > 0) {
                            Text("Fotografía pendiente de subir al servidor", fontSize = 12.sp)
                        }
                        if (postulacion.servidorId == 0L) {
                            Text("Guardada localmente · pendiente de sincronización", fontSize = 12.sp)
                        } else {
                            Text("Guardada en Spring Boot", fontSize = 12.sp)
                        }
                        Text("Toca para ver los detalles", fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }

        HorizontalDivider()
        Text(vm.mensajeServidor, fontSize = 12.sp, color = DuocNegro)
        OutlinedButton(onClick = onSincronizar, modifier = Modifier.fillMaxWidth()) {
            Text("Actualizar con servidor")
        }
        OutlinedButton(onClick = onFeriados, modifier = Modifier.fillMaxWidth()) {
            Text("Feriados de Chile (API externa)")
        }
        OutlinedButton(onClick = onMiCuenta, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Mi perfil y contraseña")
        }
        TextButton(onClick = onCerrarSesion, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Cerrar sesión")
        }
        Text("Proyecto académico: guarda en Room y sincroniza con Spring Boot cuando está disponible.",
            fontSize = 11.sp, color = Color.Gray)
    }

    if (detalle != null) {
        val postulacion = detalle!!
        AlertDialog(
            onDismissRequest = { detalle = null },
            title = { Text("Detalle de tu postulación") },
            text = {
                Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(postulacion.cursoInteres, fontWeight = FontWeight.Bold)
                    Text("Solicitud N.º ${postulacion.id}")
                    Text("Enviada: ${fechaCorta(postulacion.fechaCreacion)}")
                    EtiquetaEstado(postulacion.estadoPostulacion)
                    Text("Nombre: ${postulacion.nombre}")
                    Text("Correo: ${postulacion.correo}")
                    Text("Modalidad: ${postulacion.modalidad}")
                    if (postulacion.fotoRuta.isNotBlank()) {
                        Text("Imagen adjunta", fontWeight = FontWeight.SemiBold)
                        VistaPreviaFoto(postulacion.fotoRuta)
                    } else if (postulacion.fotoEnServidor && postulacion.servidorId > 0) {
                        Text("Imagen guardada en Spring Boot", fontWeight = FontWeight.SemiBold)
                        VistaPreviaFotoServidor(postulacion.servidorId)
                    }
                    Text("El estado lo actualiza el administrador desde su panel.", fontSize = 12.sp)
                }
            },
            confirmButton = { TextButton(onClick = { detalle = null }) { Text("Cerrar") } }
        )
    }
}

@Composable
private fun EtiquetaEstado(estado: String) {
    Surface(color = DuocAmarilloClaro, shape = RoundedCornerShape(20.dp)) {
        Text("Estado: $estado", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = DuocNegro, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

private fun fechaCorta(milisegundos: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).format(Date(milisegundos))
}
