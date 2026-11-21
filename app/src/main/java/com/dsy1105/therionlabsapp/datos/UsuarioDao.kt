package com.dsy1105.therionlabsapp.datos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertarUsuario(usuario: UsuarioEntidad): Long

    @Query("UPDATE usuarios SET nombre = :nombre, correo = :correo WHERE id = :id")
    suspend fun actualizarPerfil(id: Int, nombre: String, correo: String)

    @Query("UPDATE usuarios SET claveHash = :hash, salt = :salt WHERE id = :id")
    suspend fun actualizarClave(id: Int, hash: String, salt: String)

    @Query("UPDATE usuarios SET servidorId = :servidorId WHERE id = :id")
    suspend fun actualizarServidorId(id: Int, servidorId: Long)

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntidad?
}
