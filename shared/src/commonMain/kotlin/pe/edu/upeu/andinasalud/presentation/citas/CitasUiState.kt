package pe.edu.upeu.andinasalud.presentation.citas

import pe.edu.upeu.andinasalud.domain.model.Paciente


data class CitasUiState(
    val fase: Fase = Fase.Cargando,
    val paciente: Paciente? = null,
    val filtro: FiltroCita = FiltroCita.Todas,
    val busqueda: String = "",
    val citasVisibles: List<CitaUi> = emptyList(),
    val proximaCita: CitaUi? = null
) {


    sealed interface Fase {

        data object Cargando : Fase

        data class ConContenido(val citas: List<CitaUi>) : Fase

        data object SinCitas : Fase

        data class Error(val mensaje: String) : Fase
    }


    enum class FiltroCita {
        Todas,
        Programadas,
        Atendidas,
        Canceladas
    }
}