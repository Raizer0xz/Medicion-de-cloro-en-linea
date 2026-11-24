package com.example.medicindecloroenlnea.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import com.example.medicindecloroenlnea.domain.model.calcularDireccion
import com.example.medicindecloroenlnea.domain.model.calcularEstado
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal object CloroLocalSeedData {
    fun populate(db: SQLiteDatabase) {
        listOf("Granja Los Aromos", "Granja El Roble", "Granja Santa Rosa")
            .forEachIndexed { index, name ->
                db.insertOrThrow(
                    "granjas",
                    null,
                    ContentValues().apply {
                        put("id", index + 1)
                        put("nombre", name)
                    }
                )
            }
        val seed = listOf(
            Triple(1, "Galpón 1 - Bebederos", 4.12), Triple(1, "Galpón 2 - Bebederos", 2.71),
            Triple(1, "Estanque principal", 6.24), Triple(1, "Sala de proceso", 3.86),
            Triple(2, "Galpón 1 - Bebederos", 4.83), Triple(2, "Galpón 2 - Bebederos", 5.54),
            Triple(2, "Estanque principal", 1.76), Triple(2, "Sala de proceso", 3.28),
            Triple(3, "Galpón 1 - Bebederos", 5.23), Triple(3, "Galpón 2 - Bebederos", 2.84),
            Triple(3, "Estanque principal", 6.11), Triple(3, "Sala de proceso", 4.37)
        )
        val start = LocalDateTime.now().minusMinutes(20)
        seed.forEachIndexed { index, (farmId, name, current) ->
            val pointId = index + 1
            db.insertOrThrow(
                "puntos",
                null,
                ContentValues().apply {
                    put("id", pointId)
                    put("granja_id", farmId)
                    put("nombre", name)
                    put("codigo", "P-%02d".format(pointId))
                    put("galpon", name.substringBefore(" - "))
                    put("tipo", if (name.contains("Estanque")) "Estanque" else "Bebedero")
                    put("nivel", current)
                    put(
                        "actualizado",
                        start.plusMinutes(20).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    )
                }
            )
            listOf(-0.18, 0.09, -0.07, 0.14, 0.0).forEachIndexed { sample, delta ->
                val timestamp = start.plusMinutes(sample * 5L)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val value = (current + delta).coerceAtLeast(0.0)
                val state = calcularEstado(value)
                val measurementId = db.insertOrThrow(
                    "mediciones",
                    null,
                    ContentValues().apply {
                        put("punto_id", pointId)
                        put("nivel", value)
                        put("fecha_hora", timestamp)
                        put("estado", state.name)
                        put("direccion", calcularDireccion(value)?.name)
                        put("sincronizada", 1)
                    }
                )
                if (sample == 4 && state != EstadoCloro.NORMAL) {
                    db.insertOrThrow(
                        "alertas",
                        null,
                        ContentValues().apply {
                            put("punto_id", pointId)
                            put("medicion_id", measurementId)
                            put("nivel", value)
                            put("fecha_hora", timestamp)
                            put("estado", state.name)
                            put("direccion", calcularDireccion(value)!!.name)
                        }
                    )
                }
            }
        }
    }
}
