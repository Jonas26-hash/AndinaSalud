package pe.edu.upeu.andinasalud.domain.usecase

import pe.edu.upeu.andinasalud.domain.model.Cita
import pe.edu.upeu.andinasalud.domain.model.EstadoCita
import pe.edu.upeu.andinasalud.domain.model.ahoraMillis
import pe.edu.upeu.andinasalud.domain.repository.CitaRepository

class CancelacionInvalidaException(
    mensaje: String
) : IllegalArgumentException(mensaje)
class CancelarCitaUseCase(
    private val citasRepository: CitaRepository
) {

    suspend operator fun invoke(
        idCita: Long,
        motivo: String = MOTIVO_POR_DEFECTO,
        canceladaPorPaciente: Boolean = true,
        ahoraMillis: Long = ahoraMillis()
    ): Result<Cita> {

        val cita = runCatching { citasRepository.obtenerCitaPorId(idCita) }
            .getOrElse { return Result.failure(it) }
            ?: return Result.failure(CancelacionInvalidaException("La cita no existe"))

        if (cita.estado !is EstadoCita.Programada) {
            return Result.failure(
                CancelacionInvalidaException("Solo se puede cancelar una cita programada")
            )
        }

        val faltanMasDe24Horas =
            cita.instanteEpochMillis - ahoraMillis > HORAS_CANCELACION_MS

        if (!faltanMasDe24Horas) {
            return Result.failure(
                CancelacionInvalidaException(
                    "Solo puedes cancelar si faltan más de $HORAS_CANCELACION_HORAS horas para la cita"
                )
            )
        }

        return resultadoDe {
            citasRepository.cancelar(
                idCita = idCita,
                motivo = motivo,
                canceladaPorPaciente = canceladaPorPaciente
            )
        }
    }

    companion object {
        const val HORAS_CANCELACION_HORAS = 24L
        const val HORAS_CANCELACION_MS = HORAS_CANCELACION_HORAS * 60L * 60L * 1000L
        const val MOTIVO_POR_DEFECTO = "Cancelada por el paciente"
    }
}