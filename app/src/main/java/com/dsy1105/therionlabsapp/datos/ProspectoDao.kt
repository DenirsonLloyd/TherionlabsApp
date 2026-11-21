package com.dsy1105.therionlabsapp.datos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProspectoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProspecto(prospecto: ProspectoEntidad): Long

    @Query("SELECT * FROM prospectos ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<ProspectoEntidad>>

    // Cada estudiante consulta solamente las postulaciones de su cuenta.
    @Query("SELECT * FROM prospectos WHERE servidorId = :servidorId LIMIT 1")
    suspend fun buscarPorServidorId(servidorId: Long): ProspectoEntidad?

    @Query("UPDATE prospectos SET servidorId = :servidorId, esSincronizado = 1 WHERE id = :id")
    suspend fun marcarEnServidor(id: Long, servidorId: Long)

    @Query("UPDATE prospectos SET fotoEnServidor = 1 WHERE id = :id")
    suspend fun marcarFotoEnServidor(id: Long)

    @Query("UPDATE prospectos SET fotoEnServidor = 1 WHERE servidorId = :servidorId")
    suspend fun marcarFotoRemota(servidorId: Long)

    @Query("UPDATE prospectos SET estadoPostulacion = :estado WHERE servidorId = :servidorId")
    suspend fun cambiarEstadoEnServidor(servidorId: Long, estado: String)

    @Query("DELETE FROM prospectos WHERE servidorId = :servidorId")
    suspend fun eliminarPorServidorId(servidorId: Long)

    @Query("SELECT * FROM prospectos WHERE usuarioId = :usuarioId ORDER BY id DESC")
    fun obtenerPorUsuario(usuarioId: Int): Flow<List<ProspectoEntidad>>

    @Query("UPDATE prospectos SET estadoPostulacion = :estado WHERE id = :id")
    suspend fun cambiarEstadoPostulacion(id: Int, estado: String)

    @Query("SELECT * FROM prospectos WHERE esSincronizado = 0")
    fun obtenerPendientes(): Flow<List<ProspectoEntidad>>

    @Query("SELECT * FROM prospectos WHERE esSincronizado = 1")
    fun obtenerSincronizados(): Flow<List<ProspectoEntidad>>

    @Query("UPDATE prospectos SET esSincronizado = 1 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Int)

    @Delete
    suspend fun eliminar(prospecto: ProspectoEntidad)

    @Query("UPDATE prospectos SET esSincronizado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: Boolean)
}