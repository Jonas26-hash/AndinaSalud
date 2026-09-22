package pe.edu.upeu.andinasalud.presentation.citas

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime


fun LocalDate.formatoDiaMesAnio(): String {
    val dia = dayOfMonth.toString().padStart(2, '0')
    val mes = monthNumber.toString().padStart(2, '0')
    return "$dia/$mes/$year"
}


fun LocalTime.formatoHora(): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"