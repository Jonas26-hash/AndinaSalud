package pe.edu.upeu.andinasalud.domain.usecase

import pe.edu.upeu.andinasalud.domain.model.Cita
import pe.edu.upeu.andinasalud.domain.model.Paciente
import pe.edu.upeu.andinasalud.domain.model.Sede
import pe.edu.upeu.andinasalud.domain.repository.CitaRepository

class ObtenerCitasUseCase(
    private val citasRepository: CitaRepository
) {

    suspend operator fun invoke(): List<Cita> =
        citasRepository.obtenerCitas().sortedBy { it.instanteEpochMillis }

    suspend fun porId(id: Long): Cita? =
        citasRepository.obtenerCitaPorId(id)

    suspend fun paciente(): Paciente =
        citasRepository.obtenerPaciente()

    suspend fun especialidades(): List<String> =
        citasRepository.obtenerEspecialidades()

    suspend fun sedes(): List<Sede> =
        citasRepository.obtenerSedes()
}