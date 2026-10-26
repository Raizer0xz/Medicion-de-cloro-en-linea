package com.example.medicindecloroenlnea.domain.model

enum class EstadoCloro { NORMAL, ADVERTENCIA, CRITICO }
enum class DireccionDesviacion { INSUFICIENTE, EXCESIVO }
enum class RolUsuario { OPERARIO, SUPERVISOR, JEFATURA }

data class RangoCloro(
    val criticoBajo: Double = 2.0,
    val normalBajo: Double = 3.0,
    val normalAlto: Double = 5.0,
    val criticoAlto: Double = 6.0
) {
    init {
        require(criticoBajo < normalBajo && normalBajo < normalAlto && normalAlto < criticoAlto)
    }

    fun estado(nivel: Double): EstadoCloro = when {
        nivel < criticoBajo || nivel > criticoAlto -> EstadoCloro.CRITICO
        nivel < normalBajo || nivel > normalAlto -> EstadoCloro.ADVERTENCIA
        else -> EstadoCloro.NORMAL
    }
}

data class Granja(
    val id: Int,
    val nombre: String
)

data class PuntoMedicion(
    val id: Int,
    val granjaId: Int,
    val nombre: String,
    val nivelActual: Double,          // cloro libre en mg/L
    val estado: EstadoCloro,
    val ultimaActualizacion: String,
    val codigo: String = "P-$id",
    val galpon: String = "Área general",
    val tipo: String = "Bebedero",
    val rango: RangoCloro = RangoCloro()
)

data class Medicion(
    val id: Int,
    val puntoId: Int,
    val nivelCloro: Double,
    val fechaHora: String,
    val estado: EstadoCloro,
    val direccion: DireccionDesviacion? = null,
    val sincronizada: Boolean = true,
    val registradaPor: String? = null
)

data class Alerta(
    val id: Int,
    val puntoId: Int,
    val nivelCloro: Double,
    val estado: EstadoCloro,
    val fechaHora: String,
    val atendida: Boolean,
    val medicionId: Int? = null,
    val direccion: DireccionDesviacion? = null,
    val cerradaPor: String? = null,
    val medicionVerificadaId: Int? = null
)

data class Accion(
    val id: Int,
    val puntoId: Int,
    val descripcion: String,
    val responsable: String,
    val fechaHora: String,
    val alertaId: Int = 0,
    val estado: String = "ABIERTA",
    val medicionId: Int? = null
)

fun calcularEstado(nivel: Double, rango: RangoCloro = RangoCloro()): EstadoCloro =
    rango.estado(nivel)

fun calcularDireccion(nivel: Double, rango: RangoCloro = RangoCloro()): DireccionDesviacion? =
    when {
        nivel < rango.normalBajo -> DireccionDesviacion.INSUFICIENTE
        nivel > rango.normalAlto -> DireccionDesviacion.EXCESIVO
        else -> null
    }

fun formatearNivel(nivel: Double): String = String.format(java.util.Locale.ROOT, "%.2f", nivel)