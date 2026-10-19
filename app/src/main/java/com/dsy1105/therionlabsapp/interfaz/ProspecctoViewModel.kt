package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsy1105.therionlabsapp.datos.ProspectoEntidad
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.dsy1105.therionlabsapp.datos.ProspectoRepositorio

class ProspecctoViewModel(private val repositorio: ProspectoRepositorio) : ViewModel() {

    val listaProspectos: StateFlow<List<ProspectoEntidad>> = repositorio.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Campos del Formulario
    var nombre by mutableStateOf("")
    var correo by mutableStateOf("")
    var telefono by mutableStateOf("")
    var nivelEducativo by mutableStateOf("")
    var areaInteres by mutableStateOf("")
    var cursoInteres by mutableStateOf("")
    var modalidad by mutableStateOf("Presencial")

    // Validaciones
    var errorCorreo by mutableStateOf(false)

    // Estados de Flujos Room
    val pendientes: StateFlow<List<ProspectoEntidad>> = repositorio.pendientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sincronizados: StateFlow<List<ProspectoEntidad>> = repositorio.sincronizados
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarProspecto(onExito: () -> Unit) {
        if (!correo.contains("@")) {
            errorCorreo = true
            return
        }
        errorCorreo = false

        viewModelScope.launch {
            repositorio.guardar(
                ProspectoEntidad(
                    nombre = nombre,
                    correo = correo,
                    telefono = telefono,
                    nivelEducativo = nivelEducativo,
                    areaInteres = areaInteres,
                    cursoInteres = cursoInteres,
                    modalidad = modalidad
                )
            )
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

    private fun limpiarCampos() {
        nombre = ""
        correo = ""
        telefono = ""
        nivelEducativo = ""
        areaInteres = ""
        cursoInteres = ""
        modalidad = "Presencial"
    }

    fun actualizarEstadoSincronizacion(id: Int, nuevoEstado: Boolean) {
        viewModelScope.launch {
            repositorio.actualizarEstado(id, nuevoEstado)
        }
    }
}