package com.dsy1105.therionlabsapp.datos

import kotlinx.coroutines.flow.Flow

class ProspectoRepositorio(private val prospectoDao: ProspectoDao) {

    fun obtenerTodos(): Flow<List<ProspectoEntidad>> {
        return prospectoDao.obtenerTodos()
    }

    val pendientes: Flow<List<ProspectoEntidad>> = prospectoDao.obtenerPendientes()
    val sincronizados: Flow<List<ProspectoEntidad>> = prospectoDao.obtenerSincronizados()

    suspend fun guardar(prospecto: ProspectoEntidad) {
        prospectoDao.insertarProspecto(prospecto)
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