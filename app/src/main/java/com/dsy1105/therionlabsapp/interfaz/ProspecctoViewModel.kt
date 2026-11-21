package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsy1105.therionlabsapp.datos.ProspectoEntidad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.dsy1105.therionlabsapp.datos.ProspectoRepositorio

class ProspecctoViewModel(private val repositorio: ProspectoRepositorio) : ViewModel() {

    val listaProspectos: StateFlow<List<ProspectoEntidad>> = repositorio.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun obtenerPorUsuario(usuarioId: Int): Flow<List<ProspectoEntidad>> {
        return repositorio.obtenerPorUsuario(usuarioId)
    }

    var mensajeServidor by mutableStateOf("")
        private set

    fun sincronizarDatos(usuarioId: Int, usuarioServidorId: Long) {
        viewModelScope.launch {
            repositorio.sincronizarPendientes(usuarioId, usuarioServidorId)
            mensajeServidor = if (repositorio.actualizarDesdeServidor(usuarioId, usuarioServidorId))
                "Conectado al servidor" else "Sin servidor: los datos siguen en este dispositivo"
        }
    }

    suspend fun cambiarEstadoServidor(servidorId: Long, estado: String) {
        repositorio.cambiarEstadoDeServidor(servidorId, estado)
    }

    suspend fun eliminarServidor(servidorId: Long) {
        repositorio.eliminarDeServidor(servidorId)
    }

    fun cambiarEstadoPostulacion(id: Int, estado: String) {
        if (!ReglasPostulacion.estadoValido(estado)) return
        viewModelScope.launch {
            repositorio.cambiarEstadoPostulacion(id, estado)
        }
    }

    // Campos del Formulario
    var nombre by mutableStateOf("")
    var correo by mutableStateOf("")
    var telefono by mutableStateOf("")
    var nivelEducativo by mutableStateOf("")
    var areaInteres by mutableStateOf("")
    var cursoInteres by mutableStateOf("")
    var modalidad by mutableStateOf("Presencial")
    var rut by mutableStateOf("")
    var fechaNacimiento by mutableStateOf("")
    var direccion by mutableStateOf("")
    var facultad by mutableStateOf("")
    var fotoRuta by mutableStateOf("")

    // Validaciones
    var errorCorreo by mutableStateOf(false)
    var errorNombre by mutableStateOf("")
    var errorRut by mutableStateOf("")
    var errorFecha by mutableStateOf("")
    var errorEmail by mutableStateOf("")
    var errorTelefono by mutableStateOf("")
    var errorDireccion by mutableStateOf("")

    fun validarDatos(): Boolean {
        errorNombre = if (!Validaciones.nombreValido(nombre)) "Escribe un nombre válido." else ""
        errorRut = if (!Validaciones.rutValido(rut)) "RUT incorrecto. Revisa el dígito verificador." else ""
        errorFecha = if (!Validaciones.fechaValida(fechaNacimiento)) "Usa una fecha real: dd/mm/aaaa." else ""
        errorEmail = if (!Validaciones.correoValido(correo)) "Correo inválido. Ejemplo: nombre@correo.cl" else ""
        errorTelefono = if (!Validaciones.telefonoValido(telefono)) "Ingresa un celular chileno de 9 dígitos." else ""
        errorDireccion = if (!Validaciones.direccionValida(direccion)) "Escribe una dirección de al menos 5 caracteres." else ""
        return errorNombre.isEmpty() && errorRut.isEmpty() && errorFecha.isEmpty() &&
                errorEmail.isEmpty() && errorTelefono.isEmpty() && errorDireccion.isEmpty()
    }

    // Estados de Flujos Room
    val pendientes: StateFlow<List<ProspectoEntidad>> = repositorio.pendientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sincronizados: StateFlow<List<ProspectoEntidad>> = repositorio.sincronizados
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarProspecto(
        validarDatosCompletos: Boolean = false,
        usuarioId: Int = 0,
        usuarioServidorId: Long = 0,
        onError: () -> Unit = {},
        onExito: () -> Unit
    ) {
        // El formulario nuevo exige todos los datos. El formulario antiguo se conserva.
        if (validarDatosCompletos) {
            if (usuarioId <= 0) {
                onError()
                return
            }
            if (!validarDatos()) {
                errorCorreo = errorEmail.isNotEmpty()
                onError()
                return
            }
        } else if (!Validaciones.correoValido(correo)) {
            errorCorreo = true
            onError()
            return
        }
        errorCorreo = false

        viewModelScope.launch {
            val tieneFoto = fotoRuta.isNotBlank()
            val resultado = repositorio.guardar(
                ProspectoEntidad(
                    nombre = nombre,
                    correo = correo,
                    telefono = telefono,
                    nivelEducativo = nivelEducativo,
                    areaInteres = areaInteres,
                    cursoInteres = cursoInteres,
                    modalidad = modalidad,
                    rut = rut,
                    fechaNacimiento = fechaNacimiento,
                    direccion = direccion,
                    facultad = facultad,
                    fotoRuta = fotoRuta,
                    usuarioId = usuarioId,
                    estadoPostulacion = "Recibida"
                ), usuarioServidorId
            )
            mensajeServidor = when {
                !resultado.postulacionEnServidor -> "Postulación guardada localmente; pendiente de envío al servidor"
                tieneFoto && !resultado.fotoEnServidor -> "Postulación guardada en el servidor; fotografía pendiente de envío"
                else -> "Postulación guardada en el servidor"
            }
            limpiarCampos()
            onExito()
        }
    }

    // NUEVA FUNCIÓN PARA ELIMINAR REGISTROS LOCALES
    fun eliminarProspecto(prospecto: ProspectoEntidad) {
        viewModelScope.launch {
            repositorio.eliminar(prospecto)
        }
    }

    fun reiniciarFormulario() {
        limpiarCampos()
    }

    private fun limpiarCampos() {
        nombre = ""
        correo = ""
        telefono = ""
        nivelEducativo = ""
        areaInteres = ""
        cursoInteres = ""
        modalidad = "Presencial"
        rut = ""
        fechaNacimiento = ""
        direccion = ""
        facultad = ""
        fotoRuta = ""
        errorNombre = ""
        errorRut = ""
        errorFecha = ""
        errorEmail = ""
        errorTelefono = ""
        errorDireccion = ""
        errorCorreo = false
    }

    fun actualizarEstadoSincronizacion(id: Int, nuevoEstado: Boolean) {
        viewModelScope.launch {
            repositorio.actualizarEstado(id, nuevoEstado)
        }
    }
}