package com.dsy1105.therionlabsapp.datos

import com.dsy1105.therionlabsapp.api.ApiCliente
import com.dsy1105.therionlabsapp.api.DatosUsuarioApi
import com.dsy1105.therionlabsapp.api.UsuariosApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class UsuarioRepositorio(
    private val usuarioDao: UsuarioDao,
    private val servicio: UsuariosApi = ApiCliente.usuarios
) {


    private suspend fun enviarAlServidor(nombre: String, correo: String): Long {
        return try {
            val usuario = servicio.crear(
                DatosUsuarioApi(nombre, correo)
            )
            Log.d("TH_SYNC", "Usuario enviado: ${usuario.id}")
            usuario.id
        } catch (e: Exception) {
            Log.e("TH_SYNC", "Error al crear usuario", e)

            try {
                val usuario = servicio.porCorreo(correo)
                Log.d("TH_SYNC", "Usuario encontrado: ${usuario.id}")
                usuario.id
            } catch (e2: Exception) {
                Log.e("TH_SYNC", "Error al buscar usuario", e2)
                0L
            }
        }
    }


    suspend fun buscarPorCorreo(correo: String): UsuarioEntidad? {
        return usuarioDao.buscarPorCorreo(correo)
    }

    suspend fun registrar(nombre: String, correo: String, clave: String): UsuarioEntidad {
        // El calculo de la clave se hace fuera de la pantalla.
        val (hash, salt) = withContext(Dispatchers.Default) {
            SeguridadClave.crear(clave)
        }
        val nuevo = UsuarioEntidad(nombre = nombre, correo = correo, claveHash = hash, salt = salt)
        val id = usuarioDao.insertarUsuario(nuevo).toInt()
        val remoto = enviarAlServidor(nombre, correo)
        if (remoto > 0) usuarioDao.actualizarServidorId(id, remoto)
        return nuevo.copy(id = id, servidorId = remoto)
    }

    suspend fun actualizarPerfil(usuario: UsuarioEntidad, nombre: String, correo: String): UsuarioEntidad {
        usuarioDao.actualizarPerfil(usuario.id, nombre, correo)
        var servidorId = usuario.servidorId
        try {
            if (servidorId > 0) servicio.actualizar(servidorId, DatosUsuarioApi(nombre, correo))
            else servidorId = enviarAlServidor(nombre, correo)
        } catch (_: Exception) { /* La cuenta local sigue funcionando sin conexión. */ }
        if (servidorId > 0) usuarioDao.actualizarServidorId(usuario.id, servidorId)
        return usuario.copy(nombre = nombre, correo = correo, servidorId = servidorId)
    }

    suspend fun cambiarClave(usuario: UsuarioEntidad, anterior: String, nueva: String): UsuarioEntidad? {
        val correcta = withContext(Dispatchers.Default) {
            SeguridadClave.esCorrecta(anterior, usuario.claveHash, usuario.salt)
        }
        if (!correcta) return null
        val (hash, salt) = withContext(Dispatchers.Default) { SeguridadClave.crear(nueva) }
        usuarioDao.actualizarClave(usuario.id, hash, salt)
        return usuario.copy(claveHash = hash, salt = salt)
    }

    suspend fun iniciarSesion(correo: String, clave: String): UsuarioEntidad? {
        val usuario = usuarioDao.buscarPorCorreo(correo) ?: return null
        val correcta = withContext(Dispatchers.Default) {
            SeguridadClave.esCorrecta(clave, usuario.claveHash, usuario.salt)
        }
        if (!correcta) return null
        // Recupera la relación con el servidor cuando vuelve a estar disponible.
        var servidorId = usuario.servidorId
        if (servidorId == 0L) {
            servidorId = enviarAlServidor(usuario.nombre, usuario.correo)
            if (servidorId > 0) usuarioDao.actualizarServidorId(usuario.id, servidorId)
        } else {
            try { servicio.actualizar(servidorId, DatosUsuarioApi(usuario.nombre, usuario.correo)) }
            catch (_: Exception) { /* Modo local */ }
        }
        return usuario.copy(servidorId = servidorId)
    }
}
