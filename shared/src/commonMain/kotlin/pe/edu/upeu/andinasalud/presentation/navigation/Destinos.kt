package pe.edu.upeu.andinasalud.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector


object Destinos {

    const val INICIO = "inicio"

    const val CITAS = "citas"

    const val PERFIL = "perfil"

    const val SOLICITUD = "solicitud"

    const val DETALLE_CITA = "cita/{citaId}"


    val rutasInferiores = listOf(INICIO, CITAS, PERFIL)

    fun detalleCita(citaId: Long): String = "cita/$citaId"

    fun tituloDe(ruta: String): String = when (ruta) {
        INICIO -> "Inicio"
        CITAS -> "Citas"
        PERFIL -> "Perfil"
        SOLICITUD -> "Solicitar cita"
        else -> "Detalle de cita"
    }
}


data class DestinoTab(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
)

val tabsInferiores = listOf(
    DestinoTab(Destinos.INICIO, "Inicio", Icons.Default.Home),
    DestinoTab(Destinos.CITAS, "Citas", Icons.Default.CalendarMonth),
    DestinoTab(Destinos.PERFIL, "Perfil", Icons.Default.Person)
)