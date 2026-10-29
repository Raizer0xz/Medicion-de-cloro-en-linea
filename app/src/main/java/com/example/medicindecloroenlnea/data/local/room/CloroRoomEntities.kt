package com.example.medicindecloroenlnea.data.local.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "granjas")
data class GranjaRoomEntity(
    @PrimaryKey val id: Int,
    val nombre: String
)

@Entity(
    tableName = "puntos",
    indices = [Index(value = ["granja_id"], name = "idx_puntos_granja_id")],
    foreignKeys = [
        ForeignKey(
            entity = GranjaRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["granja_id"]
        )
    ]
)
data class PuntoRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "granja_id") val granjaId: Int,
    val nombre: String,
    val codigo: String,
    val galpon: String,
    val tipo: String,
    val nivel: Double,
    val actualizado: String,
    @ColumnInfo(name = "critico_bajo", defaultValue = "2") val criticoBajo: Double = 2.0,
    @ColumnInfo(name = "normal_bajo", defaultValue = "3") val normalBajo: Double = 3.0,
    @ColumnInfo(name = "normal_alto", defaultValue = "5") val normalAlto: Double = 5.0,
    @ColumnInfo(name = "critico_alto", defaultValue = "6") val criticoAlto: Double = 6.0
)

@Entity(
    tableName = "mediciones",
    indices = [
        Index(
            value = ["punto_id", "fecha_hora"],
            orders = [Index.Order.ASC, Index.Order.DESC],
            name = "idx_mediciones_punto_fecha"
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = PuntoRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["punto_id"]
        )
    ]
)
data class MedicionRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "punto_id") val puntoId: Int,
    val nivel: Double,
    @ColumnInfo(name = "fecha_hora") val fechaHora: String,
    val estado: String,
    val direccion: String?,
    @ColumnInfo(name = "sincronizada", defaultValue = "0") val sincronizada: Boolean = false,
    @ColumnInfo(name = "usuario_id") val usuarioId: Long? = null
)

@Entity(
    tableName = "alertas",
    indices = [
        Index(
            value = ["atendida", "fecha_hora"],
            orders = [Index.Order.ASC, Index.Order.DESC],
            name = "idx_alertas_estado_fecha"
        ),
        Index(value = ["punto_id"], name = "idx_alertas_punto_id"),
        Index(value = ["medicion_id"], name = "idx_alertas_medicion_id"),
        Index(value = ["medicion_verificada_id"], name = "idx_alertas_medicion_verificada_id")
    ],
    foreignKeys = [
        ForeignKey(
            entity = PuntoRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["punto_id"]
        ),
        ForeignKey(
            entity = MedicionRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicion_id"]
        ),
        ForeignKey(
            entity = MedicionRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicion_verificada_id"]
        )
    ]
)
data class AlertaRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "punto_id") val puntoId: Int,
    @ColumnInfo(name = "medicion_id") val medicionId: Long,
    val nivel: Double,
    @ColumnInfo(name = "fecha_hora") val fechaHora: String,
    val estado: String,
    val direccion: String,
    @ColumnInfo(name = "atendida", defaultValue = "0") val atendida: Boolean = false,
    @ColumnInfo(name = "cerrada_por") val cerradaPor: String? = null,
    @ColumnInfo(name = "medicion_verificada_id") val medicionVerificadaId: Long? = null
)
