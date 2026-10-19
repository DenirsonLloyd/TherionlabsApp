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
    suspend fun insertarProspecto(prospecto: ProspectoEntidad)

    @Query("SELECT * FROM prospectos ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<ProspectoEntidad>>

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