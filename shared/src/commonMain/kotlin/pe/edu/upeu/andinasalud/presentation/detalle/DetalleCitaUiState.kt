package pe.edu.upeu.andinasalud.presentation.detalle

import pe.edu.upeu.andinasalud.presentation.citas.CitaUi


data class DetalleCitaUiState(
    val fase: Fase = Fase.Cargando,
    val mostrandoDialogoCancelacion: Boolean = false,
    val cancelando: Boolean = false,
    val errorCancelacion: String? = null,
    val canceladaExitosamente: Boolean = false
) {

    sealed interface Fase {

        data object Cargando : Fase

        data class ConContenido(val cita: CitaUi) : Fase

        data object SinCitas : Fase

        data class Error(val mensaje: String) : Fase
    }
}