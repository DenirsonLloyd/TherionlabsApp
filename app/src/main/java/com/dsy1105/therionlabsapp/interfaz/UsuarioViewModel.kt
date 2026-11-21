package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsy1105.therionlabsapp.datos.UsuarioEntidad
import com.dsy1105.therionlabsapp.datos.UsuarioRepositorio
import kotlinx.coroutines.launch
import java.util.Locale

class UsuarioViewModel(private val repositorio: UsuarioRepositorio) : ViewModel() {

    var nombre by mutableStateOf("")
    var correo by mutableStateOf("")
    var clave by mutableStateOf("")
    var confirmarClave by mutableStateOf("")

    var errorNombre by mutableStateOf("")
    var errorCorreo by mutableStateOf("")
    var errorClave by mutableStateOf("")
    var errorConfirmar by mutableStateOf("")
    var errorGeneral by mutableStateOf("")

    var correoLogin by mutableStateOf("")
    var claveLogin by mutableStateOf("")
    var errorLogin by mutableStateOf("")
    var errorCorreoLogin by mutableStateOf("")
    var errorClaveLogin by mutableStateOf("")

    var cargando by mutableStateOf(false)
        private set

    var usuarioActual by mutableStateOf<UsuarioEntidad?>(null)
        private set

    fun registrarUsuario(onExito: () -> Unit) {
        errorNombre = if (!Validaciones.nombreValido(nombre)) "Ingresa un nombre válido (mínimo 3 caracteres)." else ""
        errorCorreo = if (!Validaciones.correoValido(correo)) "Ejemplo de correo: nombre@correo.cl" else ""
        errorClave = if (!Validaciones.claveValida(clave)) "Mínimo 8 caracteres, una letra y un número." else ""
        errorConfirmar = if (clave != confirmarClave || confirmarClave.isBlank()) "Las contraseñas deben coincidir." else ""
        errorGeneral = ""

        if (errorNombre.isNotEmpty() || errorCorreo.isNotEmpty() || errorClave.isNotEmpty() || errorConfirmar.isNotEmpty()) return
        if (cargando) return

        cargando = true
        viewModelScope.launch {
            try {
                val correoNormalizado = correo.trim().lowercase(Locale.ROOT)
                if (repositorio.buscarPorCorreo(correoNormalizado) != null) {
                    errorCorreo = "Ya existe una cuenta con este correo."
                } else {
                    usuarioActual = repositorio.registrar(nombre.trim(), correoNormalizado, clave)
                    limpiarRegistro()
                    onExito()
                }
            } catch (e: Exception) {
                // Si dos cuentas se intentan guardar con el mismo correo, Room las rechaza.
                errorGeneral = "No fue posible registrar la cuenta. Revisa si el correo ya está registrado."
            } finally {
                cargando = false
            }
        }
    }

    fun iniciarSesion(onExito: () -> Unit) {
        errorLogin = ""
        errorCorreoLogin = if (!Validaciones.correoValido(correoLogin)) "Ingresa un correo válido." else ""
        errorClaveLogin = if (claveLogin.isBlank()) "Ingresa tu contraseña." else ""
        if (errorCorreoLogin.isNotEmpty() || errorClaveLogin.isNotEmpty()) return
        if (cargando) return

        cargando = true
        viewModelScope.launch {
            try {
                val correoNormalizado = correoLogin.trim().lowercase(Locale.ROOT)
                val usuario = repositorio.iniciarSesion(correoNormalizado, claveLogin)
                if (usuario == null) {
                    errorLogin = "Correo o contraseña incorrectos."
                } else {
                    usuarioActual = usuario
                    claveLogin = ""
                    onExito()
                }
            } catch (e: Exception) {
                errorLogin = "No se pudo ingresar. Inténtalo nuevamente."
            } finally {
                cargando = false
            }
        }
    }

    var mensajePerfil by mutableStateOf("")
    var mensajeClave by mutableStateOf("")
    var guardandoPerfil by mutableStateOf(false)
        private set

    fun actualizarPerfil(nombreNuevo: String, correoNuevo: String, onExito: (String, String) -> Unit) {
        val usuario = usuarioActual ?: return
        mensajePerfil = ""
        if (!Validaciones.nombreValido(nombreNuevo)) {
            mensajePerfil = "El nombre debe tener al menos 3 letras y no incluir números."
            return
        }
        if (!Validaciones.correoValido(correoNuevo)) {
            mensajePerfil = "Ingresa un correo válido."
            return
        }
        if (guardandoPerfil) return
        guardandoPerfil = true
        viewModelScope.launch {
            try {
                val correoNormal = correoNuevo.trim().lowercase(Locale.ROOT)
                val otro = repositorio.buscarPorCorreo(correoNormal)
                if (otro != null && otro.id != usuario.id) {
                    mensajePerfil = "Este correo ya está registrado."
                } else {
                    usuarioActual = repositorio.actualizarPerfil(usuario, nombreNuevo.trim(), correoNormal)
                    mensajePerfil = "Perfil actualizado correctamente."
                    onExito(nombreNuevo.trim(), correoNormal)
                }
            } catch (e: Exception) {
                mensajePerfil = "No se pudo guardar. Revisa si el correo está en uso."
            } finally {
                guardandoPerfil = false
            }
        }
    }

    fun cambiarClave(actual: String, nueva: String, confirmar: String) {
        val usuario = usuarioActual ?: return
        mensajeClave = ""
        if (!Validaciones.claveValida(nueva)) {
            mensajeClave = "La nueva contraseña debe tener 8 caracteres, letras y números."
            return
        }
        if (actual == nueva) {
            mensajeClave = "Elige una contraseña diferente a la anterior."
            return
        }
        if (nueva != confirmar) {
            mensajeClave = "Las nuevas contraseñas no coinciden."
            return
        }
        if (guardandoPerfil) return
        guardandoPerfil = true
        viewModelScope.launch {
            try {
                val resultado = repositorio.cambiarClave(usuario, actual, nueva)
                if (resultado == null) mensajeClave = "La contraseña actual no es correcta."
                else {
                    usuarioActual = resultado
                    mensajeClave = "Contraseña actualizada correctamente."
                }
            } catch (e: Exception) {
                mensajeClave = "No se pudo cambiar la contraseña."
            } finally {
                guardandoPerfil = false
            }
        }
    }

    fun cerrarSesion() {
        usuarioActual = null
        claveLogin = ""
    }

    private fun limpiarRegistro() {
        nombre = ""
        correo = ""
        clave = ""
        confirmarClave = ""
        errorNombre = ""
        errorCorreo = ""
        errorClave = ""
        errorConfirmar = ""
    }
}
