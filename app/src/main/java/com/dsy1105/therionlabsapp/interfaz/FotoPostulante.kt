package com.dsy1105.therionlabsapp.interfaz

import android.content.Context
import com.dsy1105.therionlabsapp.api.ApiCliente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

// Guardamos una copia privada para que siga disponible después de cerrar la app.
fun guardarFoto(context: Context, uri: Uri): String {
    val carpeta = File(context.filesDir, "adjuntos")
    carpeta.mkdirs()
    val destino = File(carpeta, "foto_${UUID.randomUUID()}.img")
    try {
        context.contentResolver.openInputStream(uri).use { entrada ->
            requireNotNull(entrada) { "No se pudo abrir la imagen" }
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        }
    } catch (e: Exception) {
        destino.delete()
        throw e
    }
    return destino.absolutePath
}

@Composable
fun VistaPreviaFoto(ruta: String, modifier: Modifier = Modifier) {
    val imagen = remember(ruta) {
        if (ruta.isBlank()) null else {
            val archivo = File(ruta)
            if (!archivo.exists()) null else {
                val dimensiones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(ruta, dimensiones)
                val muestra = maxOf(1, maxOf(dimensiones.outWidth / 900, dimensiones.outHeight / 900))
                BitmapFactory.decodeFile(ruta, BitmapFactory.Options().apply { inSampleSize = muestra })
            }
        }
    }
    if (imagen != null) {
        Image(
            bitmap = imagen.asImageBitmap(),
            contentDescription = "Foto adjunta a la postulación",
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxWidth().height(170.dp)
        )
    }
}

@Composable
fun FotoPostulante(ruta: String, onFotoGuardada: (String) -> Unit) {
    val context = LocalContext.current
    var error by remember { mutableStateOf("") }
    var uriTemporal by remember { mutableStateOf<Uri?>(null) }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                onFotoGuardada(guardarFoto(context, uri))
                error = ""
            } catch (e: Exception) {
                error = "No se pudo leer la imagen seleccionada."
            }
        }
    }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardada ->
        val uri = uriTemporal
        if (guardada && uri != null) {
            try {
                onFotoGuardada(guardarFoto(context, uri))
                error = ""
            } catch (e: Exception) {
                error = "No se pudo guardar la foto."
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Foto de postulación (opcional)", style = MaterialTheme.typography.titleMedium)
        Text("Puedes tomar una foto o elegir una de tu galería.", style = MaterialTheme.typography.bodySmall)
        if (ruta.isNotBlank()) {
            VistaPreviaFoto(ruta)
            TextButton(onClick = { onFotoGuardada("") }) { Text("Quitar foto") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = {
                    try {
                        val archivo = File.createTempFile("captura_", ".jpg", context.cacheDir)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", archivo)
                        uriTemporal = uri
                        camara.launch(uri)
                    } catch (e: Exception) {
                        error = "No se pudo abrir la cámara de este dispositivo."
                    }
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Cámara")
            }
            OutlinedButton(
                onClick = {
                    try { galeria.launch("image/*") }
                    catch (e: Exception) { error = "No se pudo abrir la galería." }
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Galería")
            }
        }
        if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
    }
}

// Vista previa descargada desde Spring Boot: funciona aunque la foto no esté en este teléfono.
@Composable
fun VistaPreviaFotoServidor(servidorId: Long) {
    var imagen by remember(servidorId) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var error by remember(servidorId) { mutableStateOf(false) }
    LaunchedEffect(servidorId) {
        try {
            val respuesta = ApiCliente.postulaciones.descargarFoto(servidorId)
            if (respuesta.isSuccessful) {
                val datos = withContext(Dispatchers.IO) { respuesta.body()?.use { it.bytes() } }
                if (datos != null) {
                    val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(datos, 0, datos.size, opciones)
                    var muestra = 1
                    while (opciones.outWidth / muestra > 1200 || opciones.outHeight / muestra > 1200) muestra *= 2
                    imagen = BitmapFactory.decodeByteArray(datos, 0, datos.size,
                        BitmapFactory.Options().apply { inSampleSize = muestra })
                }
            }
            if (imagen == null) error = true
        } catch (_: Exception) { error = true }
    }
    if (imagen != null) {
        Image(
            bitmap = imagen!!.asImageBitmap(),
            contentDescription = "Fotografía guardada en el servidor",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(170.dp)
        )
    } else if (error) {
        Text("No se pudo descargar la fotografía del servidor.", style = MaterialTheme.typography.bodySmall)
    } else {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}
