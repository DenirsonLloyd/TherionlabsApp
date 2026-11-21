package com.dsy1105.therionlabsapp.datos

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Los postulantes pueden crear una cuenta propia.
@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class UsuarioEntidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val claveHash: String,
    @androidx.room.ColumnInfo(defaultValue = "0") val servidorId: Long = 0,
    val salt: String
)
