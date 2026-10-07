package com.dsy1105.therionlabsapp.datos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prospectos")
data class ProspectoEntidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val nivelEducativo: String = "",
    val areaInteres: String = "",
    val cursoInteres: String = "",
    val modalidad: String = "Presencial",
    val estaSincronizado: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis()
)