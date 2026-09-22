package pe.edu.upeu.andinasalud.domain.model

sealed class EstadoCita {

    data class Programada(
        val recordatorioActivo: Boolean
    ) : EstadoCita()

    data class Atendida(
        val indicaciones: String
    ) : EstadoCita()

    data class Cancelada(
        val motivo: String,
        val canceladaPorPaciente: Boolean
    ) : EstadoCita()
}