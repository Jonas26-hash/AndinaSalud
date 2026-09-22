package pe.edu.upeu.andinasalud.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pe.edu.upeu.andinasalud.domain.model.Cita
import pe.edu.upeu.andinasalud.domain.model.aEpochMillis
import pe.edu.upeu.andinasalud.domain.model.ahoraMillis
import pe.edu.upeu.andinasalud.domain.repository.CitaRepository

data class ErroresSolicitud(
    val especialidad: String? = null,
    val sede: String? = null,
    val fecha: String? = null,
    val hora: String? = null,
    val motivo: String? = null,
    val general: String? = null
) {
    val hayErrores: Boolean
        get() = especialidad != null || sede != null || fecha != null ||
            hora != null || motivo != null || general != null
}

class SolicitudInvalidaException(
    val errores: ErroresSolicitud
) : IllegalArgumentException("La solicitud no cumple las reglas de negocio")

class SolicitarCitaUseCase(
    private val citasRepository: CitaRepository
) {

    suspend operator fun invoke(
        especialidad: String,
        sede: String,
        fecha: String,
        hora: String,
        motivo: String,
        ahoraMillis: Long = ahoraMillis()
    ): Result<Cita> {

        val fechaInstante = fecha.toLocalDateOrNull()
        val horaInstante = hora.toLocalTimeOrNull()

        val errores = ErroresSolicitud(
            especialidad = validarEspecialidad(especialidad),
            sede = validarSede(sede),
            fecha = validarFecha(fecha, fechaInstante),
            hora = validarHora(hora, horaInstante),
            motivo = validarMotivo(motivo)
        )

        val erroresFinales = erroresConReglasGlobales(
            errores = errores,
            fechaInstante = fechaInstante,
            horaInstante = horaInstante,
            ahoraMillis = ahoraMillis
        )

        if (erroresFinales.hayErrores) {
            return Result.failure(SolicitudInvalidaException(erroresFinales))
        }

        return resultadoDe {
            citasRepository.solicitar(
                especialidad = especialidad.trim(),
                sede = sede.trim(),
                fecha = fechaInstante!!,
                hora = horaInstante!!,
                motivo = motivo.trim()
            )
        }
    }

    private fun validarEspecialidad(especialidad: String): String? =
        if (especialidad.isBlank()) "Selecciona una especialidad" else null

    private fun validarSede(sede: String): String? =
        if (sede.isBlank()) "Selecciona una sede" else null

    private fun validarFecha(fecha: String, instante: LocalDate?): String? =
        when {
            fecha.isBlank() -> "La fecha es obligatoria"
            instante == null -> "Usa el formato AAAA-MM-DD"
            else -> null
        }

    private fun validarHora(hora: String, instante: LocalTime?): String? =
        when {
            hora.isBlank() -> "La hora es obligatoria"
            instante == null -> "Usa el formato HH:MM"
            else -> null
        }

    private fun validarMotivo(motivo: String): String? =
        if (motivo.trim().length !in MINIMO_MOTIVO..MAXIMO_MOTIVO) {
            "El motivo debe tener entre $MINIMO_MOTIVO y $MAXIMO_MOTIVO caracteres"
        } else {
            null
        }

    private suspend fun erroresConReglasGlobales(
        errores: ErroresSolicitud,
        fechaInstante: LocalDate?,
        horaInstante: LocalTime?,
        ahoraMillis: Long
    ): ErroresSolicitud {

        if (fechaInstante == null || horaInstante == null) {
            return errores
        }

        val instanteSolicitado = fechaInstante.aEpochMillis(horaInstante)
        val conReglaHoraria = if (instanteSolicitado <= ahoraMillis && errores.fecha == null) {
            errores.copy(fecha = "La fecha y hora deben ser posteriores al momento actual")
        } else {
            errores
        }

        if (conReglaHoraria.hayErrores) {
            return conReglaHoraria
        }

        return runCatching {
            val paciente = citasRepository.obtenerPaciente()
            val programadas = citasRepository.contarCitasProgramadas(paciente.id)
            val duplicada = citasRepository.existeProgramadaEnHorario(
                paciente.id, fechaInstante, horaInstante
            )
            programadas to duplicada
        }.fold(
            onSuccess = { (programadas, duplicada) ->
                when {
                    programadas >= LIMITE_PROGRAMADAS ->
                        errores.copy(general = "Ya tienes el máximo de $LIMITE_PROGRAMADAS citas programadas")
                    duplicada ->
                        errores.copy(general = "Ya tienes una cita programada en ese mismo horario")
                    else -> errores
                }
            },
            onFailure = { fallo ->
                errores.copy(general = fallo.message ?: "No se pudieron validar las reglas de negocio")
            }
        )
    }

    private fun String.toLocalDateOrNull(): LocalDate? =
        runCatching { LocalDate.parse(this) }.getOrNull()

    private fun String.toLocalTimeOrNull(): LocalTime? =
        runCatching { LocalTime.parse(this) }.getOrNull()

    companion object {
        const val LIMITE_PROGRAMADAS = 3
        const val MINIMO_MOTIVO = 10
        const val MAXIMO_MOTIVO = 200
    }
}