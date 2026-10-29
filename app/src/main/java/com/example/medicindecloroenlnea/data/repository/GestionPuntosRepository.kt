package com.example.medicindecloroenlnea.data.repository

import android.content.Context
import com.example.medicindecloroenlnea.data.local.CloroLocalDatabase
import com.example.medicindecloroenlnea.data.local.room.AlertaRoomEntity
import com.example.medicindecloroenlnea.data.local.room.CloroRoomDatabase
import com.example.medicindecloroenlnea.data.local.room.MedicionRoomEntity
import com.example.medicindecloroenlnea.data.local.room.PuntoRoomEntity
import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import com.example.medicindecloroenlnea.domain.model.Granja
import com.example.medicindecloroenlnea.domain.model.PuntoMedicion
import com.example.medicindecloroenlnea.domain.model.RangoCloro
import com.example.medicindecloroenlnea.domain.model.RolUsuario
import com.example.medicindecloroenlnea.domain.model.calcularDireccion
import com.example.medicindecloroenlnea.domain.model.calcularEstado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ResultadoGestionPuntos(
    val exitoso: Boolean,
    val mensaje: String
)

class GestionPuntosRepository(context: Context) {
    private val roomDatabase = CloroRoomDatabase.getInstance(context)
    private val helper = CloroLocalDatabase.getInstance(context)
    private val dao = roomDatabase.puntoDao()

    fun observarGranjas(granjaIds: List<Int>): Flow<List<Granja>> =
        if (granjaIds.isEmpty()) {
            flowOf(emptyList())
        } else {
            dao.observarGranjas(granjaIds).map { farms ->
                farms.map { Granja(it.id, it.nombre) }
            }
        }

    fun observarPuntos(granjaIds: List<Int>): Flow<List<PuntoMedicion>> =
        if (granjaIds.isEmpty()) {
            flowOf(emptyList())
        } else {
            dao.observarPuntos(granjaIds).map { points -> points.map { it.toDomain() } }
        }

    suspend fun crearPunto(
        puntoIdUsuario: Long?,
        rol: RolUsuario,
        granjasPermitidas: List<Int>,
        granjaId: Int,
        nombre: String,
        codigo: String,
        galpon: String,
        tipo: String,
        nivelInicial: Double
    ): ResultadoGestionPuntos {
        if (!puedeAdministrar(rol, puntoIdUsuario, granjasPermitidas, granjaId)) {
            return ResultadoGestionPuntos(false, "No tienes permiso para administrar esta granja.")
        }
        if (!datosValidos(nombre, codigo, galpon, tipo, nivelInicial)) {
            return ResultadoGestionPuntos(false, "Completa los campos y usa un nivel entre 0 y 20 mg/L.")
        }

        val rango = RangoCloro()
        val fecha = ahora()
        val estado = calcularEstado(nivelInicial, rango)
        val direccion = calcularDireccion(nivelInicial, rango)
        val punto = PuntoRoomEntity(
            granjaId = granjaId,
            nombre = nombre.trim(),
            codigo = codigo.trim(),
            galpon = galpon.trim(),
            tipo = tipo.trim(),
            nivel = nivelInicial,
            actualizado = fecha
        )
        val medicion = MedicionRoomEntity(
            puntoId = 0,
            nivel = nivelInicial,
            fechaHora = fecha,
            estado = estado.name,
            direccion = direccion?.name,
            usuarioId = puntoIdUsuario
        )
        val alerta = if (estado == EstadoCloro.NORMAL) null else AlertaRoomEntity(
            puntoId = 0,
            medicionId = 0,
            nivel = nivelInicial,
            fechaHora = fecha,
            estado = estado.name,
            direccion = requireNotNull(direccion).name
        )
        dao.crearPuntoConMedicionInicial(punto, medicion, alerta)
        helper.notificarCambios()
        return ResultadoGestionPuntos(true, "Punto registrado con su medición inicial.")
    }

    suspend fun actualizarPunto(
        puntoIdUsuario: Long?,
        rol: RolUsuario,
        granjasPermitidas: List<Int>,
        puntoId: Int,
        nombre: String,
        codigo: String,
        galpon: String,
        tipo: String
    ): ResultadoGestionPuntos {
        val punto = dao.obtenerPunto(puntoId)
            ?: return ResultadoGestionPuntos(false, "El punto ya no está disponible.")
        if (!puedeAdministrar(rol, puntoIdUsuario, granjasPermitidas, punto.granjaId)) {
            return ResultadoGestionPuntos(false, "No tienes permiso para administrar este punto.")
        }
        if (listOf(nombre, codigo, galpon, tipo).any(String::isBlank)) {
            return ResultadoGestionPuntos(false, "Completa todos los campos.")
        }

        val actualizado = punto.copy(
            nombre = nombre.trim(),
            codigo = codigo.trim(),
            galpon = galpon.trim(),
            tipo = tipo.trim()
        )
        if (dao.actualizarPunto(actualizado) == 0) {
            return ResultadoGestionPuntos(false, "No se pudo actualizar el punto.")
        }
        helper.notificarCambios()
        return ResultadoGestionPuntos(true, "Punto actualizado.")
    }

    suspend fun eliminarPunto(
        puntoIdUsuario: Long?,
        rol: RolUsuario,
        granjasPermitidas: List<Int>,
        puntoId: Int
    ): ResultadoGestionPuntos {
        val punto = dao.obtenerPunto(puntoId)
            ?: return ResultadoGestionPuntos(false, "El punto ya no está disponible.")
        if (!puedeAdministrar(rol, puntoIdUsuario, granjasPermitidas, punto.granjaId)) {
            return ResultadoGestionPuntos(false, "No tienes permiso para administrar este punto.")
        }
        if (!dao.eliminarSiNoTieneHistorial(punto)) {
            return ResultadoGestionPuntos(
                false,
                "No se puede eliminar: el punto ya tiene mediciones registradas."
            )
        }
        helper.notificarCambios()
        return ResultadoGestionPuntos(true, "Punto eliminado.")
    }

    private fun puedeAdministrar(
        rol: RolUsuario,
        usuarioId: Long?,
        granjasPermitidas: List<Int>,
        granjaId: Int
    ): Boolean =
        usuarioId != null &&
            (rol == RolUsuario.SUPERVISOR || rol == RolUsuario.JEFATURA) &&
            granjaId in granjasPermitidas

    private fun datosValidos(
        nombre: String,
        codigo: String,
        galpon: String,
        tipo: String,
        nivel: Double
    ): Boolean =
        listOf(nombre, codigo, galpon, tipo).none(String::isBlank) &&
            nivel.isFinite() &&
            nivel in 0.0..CloroLocalDatabase.MAX_NIVEL

    private fun ahora(): String =
        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    private fun PuntoRoomEntity.toDomain(): PuntoMedicion {
        val rango = RangoCloro(criticoBajo, normalBajo, normalAlto, criticoAlto)
        return PuntoMedicion(
            id = id,
            granjaId = granjaId,
            nombre = nombre,
            nivelActual = nivel,
            estado = calcularEstado(nivel, rango),
            ultimaActualizacion = actualizado,
            codigo = codigo,
            galpon = galpon,
            tipo = tipo,
            rango = rango
        )
    }
}
