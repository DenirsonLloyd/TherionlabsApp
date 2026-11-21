package com.dsy1105.therionlabsapp.datos

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import com.dsy1105.therionlabsapp.api.ApiCliente
import com.dsy1105.therionlabsapp.api.DatosPostulacionApi
import com.dsy1105.therionlabsapp.api.PostulacionesApi
import com.dsy1105.therionlabsapp.api.EstadoApi
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType

data class ResultadoGuardado(val postulacionEnServidor: Boolean, val fotoEnServidor: Boolean)

class ProspectoRepositorio(
    private val prospectoDao: ProspectoDao,
    private val servicio: PostulacionesApi = ApiCliente.postulaciones,
    private val convertirImagen: ((String) -> ByteArray)? = null
) {

    fun obtenerTodos(): Flow<List<ProspectoEntidad>> {
        return prospectoDao.obtenerTodos()
    }

    fun obtenerPorUsuario(usuarioId: Int): Flow<List<ProspectoEntidad>> {
        return prospectoDao.obtenerPorUsuario(usuarioId)
    }

    suspend fun cambiarEstadoPostulacion(id: Int, estado: String) {
        prospectoDao.cambiarEstadoPostulacion(id, estado)
        val local = prospectoDao.obtenerTodos().first().find { it.id == id }
        if (local != null && local.servidorId > 0) {
            try { servicio.cambiarEstado(local.servidorId, EstadoApi(estado)) }
            catch (_: Exception) { /* Se conserva el cambio local. */ }
        }
    }

    val pendientes: Flow<List<ProspectoEntidad>> = prospectoDao.obtenerPendientes()
    val sincronizados: Flow<List<ProspectoEntidad>> = prospectoDao.obtenerSincronizados()

    suspend fun guardar(prospecto: ProspectoEntidad, usuarioServidorId: Long = 0): ResultadoGuardado {
        val idLocal = prospectoDao.insertarProspecto(prospecto)
        if (usuarioServidorId <= 0L) return ResultadoGuardado(false, false)
        return intentarEnviar(prospecto.copy(id = idLocal.toInt()), usuarioServidorId)
    }

    private suspend fun intentarEnviar(p: ProspectoEntidad, usuarioServidorId: Long): ResultadoGuardado {
        return try {
            val respuesta = servicio.crear(
                DatosPostulacionApi(usuarioServidorId, p.nombre, p.correo,
                    p.telefono, p.rut, p.cursoInteres, p.modalidad)
            )
            prospectoDao.marcarEnServidor(p.id.toLong(), respuesta.id)
            val fotoSubida = if (p.fotoRuta.isNotBlank()) subirFoto(p, respuesta.id) else false
            ResultadoGuardado(true, fotoSubida)
        } catch (_: Exception) { ResultadoGuardado(false, false) }
    }

    // Si la foto falló pero los datos ya están en el servidor, no duplicamos la postulación.
    private suspend fun subirFoto(p: ProspectoEntidad, servidorId: Long): Boolean {
        return try {
            val imagen = withContext(Dispatchers.IO) {
                convertirImagen?.invoke(p.fotoRuta) ?: prepararImagen(p.fotoRuta)
            }
            val cuerpo = imagen.toRequestBody("image/jpeg".toMediaType())
            val parte = MultipartBody.Part.createFormData("foto", "postulacion.jpg", cuerpo)
            servicio.subirFoto(servidorId, parte)
            prospectoDao.marcarFotoEnServidor(p.id.toLong())
            true
        } catch (_: Exception) { false }
    }

    private fun prepararImagen(ruta: String): ByteArray {
        val archivo = File(ruta)
        require(archivo.isFile) { "La fotografía local no existe" }
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, medidas)
        require(medidas.outWidth > 0 && medidas.outHeight > 0) { "Imagen inválida" }
        var muestra = 1
        while (medidas.outWidth / muestra > 1600 || medidas.outHeight / muestra > 1600) muestra *= 2
        val bitmap = BitmapFactory.decodeFile(ruta, BitmapFactory.Options().apply { inSampleSize = muestra })
            ?: error("No se pudo leer la imagen")
        return try {
            val salida = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, salida)
            val datos = salida.toByteArray()
            require(datos.size <= 5 * 1024 * 1024) { "La foto supera el máximo de 5 MB" }
            datos
        } finally { bitmap.recycle() }
    }

    suspend fun sincronizarPendientes(usuarioId: Int, usuarioServidorId: Long) {
        if (usuarioServidorId <= 0L) return
        val locales = prospectoDao.obtenerPorUsuario(usuarioId).first()
        locales.forEach { local ->
            if (local.servidorId == 0L) intentarEnviar(local, usuarioServidorId)
            else if (local.fotoRuta.isNotBlank() && !local.fotoEnServidor)
                subirFoto(local, local.servidorId)
        }
    }

    suspend fun actualizarDesdeServidor(usuarioId: Int, usuarioServidorId: Long): Boolean {
        if (usuarioServidorId <= 0L) return false
        return try {
            val remotas = servicio.deUsuario(usuarioServidorId)
            remotas.forEach { r ->
                val local = prospectoDao.buscarPorServidorId(r.id)
                if (local != null) {
                    prospectoDao.cambiarEstadoEnServidor(r.id, r.estado)
                    if (r.tieneFoto) prospectoDao.marcarFotoRemota(r.id)
                }
                else prospectoDao.insertarProspecto(
                    ProspectoEntidad(
                        nombre = r.nombre, correo = r.correo, telefono = r.telefono,
                        rut = r.rut, cursoInteres = r.carrera, modalidad = r.modalidad,
                        usuarioId = usuarioId, servidorId = r.id,
                        estadoPostulacion = r.estado, esSincronizado = true, fotoEnServidor = r.tieneFoto,
                        fechaCreacion = r.fechaCreacion
                    )
                )
            }
            true
        } catch (_: Exception) { false }
    }

    suspend fun cambiarEstadoDeServidor(servidorId: Long, estado: String) {
        servicio.cambiarEstado(servidorId, EstadoApi(estado))
        prospectoDao.cambiarEstadoEnServidor(servidorId, estado)
    }

    suspend fun eliminarDeServidor(servidorId: Long) {
        servicio.eliminar(servidorId)
        prospectoDao.eliminarPorServidorId(servidorId)
    }

    suspend fun marcarComoSincronizado(id: Int) {
        prospectoDao.marcarComoSincronizado(id)
    }

    suspend fun actualizarEstado(id: Int, estado: Boolean) {
        prospectoDao.actualizarEstado(id, estado)
    }

    suspend fun eliminar(prospecto: ProspectoEntidad) {
        prospectoDao.eliminar(prospecto)
    }
}