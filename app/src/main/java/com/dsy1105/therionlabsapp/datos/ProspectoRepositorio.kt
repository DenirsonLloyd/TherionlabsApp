package com.dsy1105.therionlabsapp.datos

import kotlinx.coroutines.flow.Flow

class ProspectoRepositorio(private val dao: ProspectoDao) {

    val todosLosProspectos: Flow<List<ProspectoEntidad>> = dao.obtenerTodos()
    val pendientes: Flow<List<ProspectoEntidad>> = dao.obtenerPendientes()
    val sincronizados: Flow<List<ProspectoEntidad>> = dao.obtenerSincronizados()

    suspend fun guardar(prospecto: ProspectoEntidad) {
        dao.insertarProspecto(prospecto)
    }

    suspend fun marcarComoSincronizado(id: Int) {
        dao.marcarComoSincronizado(id)
    }
}