package pe.edu.upeu.andinasalud.presentation.solicitud


data class FormularioSolicitud(
    val especialidad: String = "",
    val sede: String = "",
    val fecha: String = "",
    val hora: String = "",
    val motivo: String = "",
    val especialidadError: String? = null,
    val sedeError: String? = null,
    val fechaError: String? = null,
    val horaError: String? = null,
    val motivoError: String? = null,
    val errorGeneral: String? = null
)


data class SolicitudUiState(
    val fase: Fase = Fase.Cargando,
    val enviando: Boolean = false,
    val registradaExitosamente: Boolean = false,
    val formulario: FormularioSolicitud = FormularioSolicitud()
) {

    sealed interface Fase {

        data object Cargando : Fase

        data class ListaCargada(
            val especialidades: List<String>,
            val sedes: List<String>
        ) : Fase

        data class Error(val mensaje: String) : Fase
    }
}