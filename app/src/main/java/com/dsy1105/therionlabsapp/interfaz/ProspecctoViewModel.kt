package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsy1105.therionlabsapp.datos.ProspectoEntidad
import com.dsy1105.therionlabsapp.datos.ProspectoRepositorio
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProspecctoViewModel(private val repositorio: ProspectoRepositorio) : ViewModel() {

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
        .stateIn(viewModelScope, SharingStarted.WhileSubsubscribed(5000), emptyList())

    val sincronizados: StateFlow<List<ProspectoEntidad>> = repositorio.sincronizados
        .stateIn(viewModelScope, SharingStarted.WhileSubsubscribed(5000), emptyList())

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

    private fun limpiarCampos() {
        nombre = ""
        correo = ""
        telefono = ""
        nivelEducativo = ""
        areaInteres = ""
        cursoInteres = ""
        modalidad = "Presencial"
    }
}