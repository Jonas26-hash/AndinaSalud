package pe.edu.upeu.andinasalud.domain.repository

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pe.edu.upeu.andinasalud.domain.model.Cita
import pe.edu.upeu.andinasalud.domain.model.Paciente
import pe.edu.upeu.andinasalud.domain.model.Sede

interface CitaRepository {
    suspend fun obtenerCitas(): List<Cita>
    suspend fun obtenerCitaPorId(id: Long): Cita?
    suspend fun obtenerPaciente(): Paciente
    suspend fun obtenerEspecialidades(): List<String>
    suspend fun obtenerSedes(): List<Sede>
    suspend fun contarCitasProgramadas(pacienteId: String): Int
    suspend fun existeProgramadaEnHorario(
        pacienteId: String,
        fecha: LocalDate,
        hora: LocalTime
    ): Boolean

    suspend fun solicitar(
        especialidad: String,
        sede: String,
        fecha: LocalDate,
        hora: LocalTime,
        motivo: String
    ): Cita

    suspend fun cancelar(
        idCita: Long,
        motivo: String,
        canceladaPorPaciente: Boolean
    ): Cita
}