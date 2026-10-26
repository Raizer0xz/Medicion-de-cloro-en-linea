package com.example.medicindecloroenlnea.navigation

const val RUTA_DETALLE = "detalle_page/{puntoId}"

sealed class AppRoutes(val route: String) {
    data object Login : AppRoutes("login_page")
    data object Puntos : AppRoutes("puntos_page")
    data object Alertas : AppRoutes("alertas_page")
    data object Perfil : AppRoutes("perfil_page")
    data object Resumen : AppRoutes("resumen_page")
    data object Registro : AppRoutes("registro")
    data object ResumenRegistro : AppRoutes("resumen_registro")

    data class Detalle(val puntoId: String) : AppRoutes(RUTA_DETALLE) {
        fun buildRoute(): String = route.replace("{puntoId}", puntoId)
    }
}