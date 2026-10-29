package com.example.medicindecloroenlnea.data.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PuntoRoomDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarGranja(granja: GranjaRoomEntity)

    @Query("SELECT * FROM granjas WHERE id IN (:granjaIds) ORDER BY nombre")
    fun observarGranjas(granjaIds: List<Int>): Flow<List<GranjaRoomEntity>>

    @Query("SELECT * FROM puntos WHERE granja_id IN (:granjaIds) ORDER BY nombre")
    fun observarPuntos(granjaIds: List<Int>): Flow<List<PuntoRoomEntity>>

    @Query("SELECT * FROM puntos WHERE id = :puntoId LIMIT 1")
    suspend fun obtenerPunto(puntoId: Int): PuntoRoomEntity?

    @Query("SELECT COUNT(*) FROM mediciones WHERE punto_id = :puntoId")
    suspend fun contarMediciones(puntoId: Int): Int

    @Query("SELECT COUNT(*) FROM alertas WHERE punto_id = :puntoId")
    suspend fun contarAlertas(puntoId: Int): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarPunto(punto: PuntoRoomEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarMedicion(medicion: MedicionRoomEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarAlerta(alerta: AlertaRoomEntity)

    @Update
    suspend fun actualizarPunto(punto: PuntoRoomEntity): Int

    @Delete
    suspend fun eliminarPunto(punto: PuntoRoomEntity): Int

    @Transaction
    suspend fun crearPuntoConMedicionInicial(
        punto: PuntoRoomEntity,
        medicion: MedicionRoomEntity,
        alerta: AlertaRoomEntity?
    ) {
        val puntoId = insertarPunto(punto).toInt()
        val medicionId = insertarMedicion(medicion.copy(puntoId = puntoId))
        if (alerta != null) {
            insertarAlerta(alerta.copy(puntoId = puntoId, medicionId = medicionId))
        }
    }

    @Transaction
    suspend fun eliminarSiNoTieneHistorial(punto: PuntoRoomEntity): Boolean {
        if (contarMediciones(punto.id) > 0) return false
        return eliminarPunto(punto) > 0
    }
}
