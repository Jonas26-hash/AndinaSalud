package pe.edu.upeu.andinasalud.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pe.edu.upeu.andinasalud.data.local.CitasSimuladas
import pe.edu.upeu.andinasalud.domain.model.Cita
import pe.edu.upeu.andinasalud.domain.model.EstadoCita
import pe.edu.upeu.andinasalud.domain.model.Paciente
import pe.edu.upeu.andinasalud.domain.model.Sede
import pe.edu.upeu.andinasalud.domain.model.hoyUtc
import pe.edu.upeu.andinasalud.domain.repository.CitaRepository


class CitaRepositoryFake(
    ancla: LocalDate = hoyUtc(),
    private val retardoLecturaMs: Long = RETARDO_LECTURA_MS,
    private val retardoMutacionMs: Long = RETARDO_MUTACION_MS
) : CitaRepository {

    private val candado = Mutex()

    private val allSedes = CitasSimuladas.sedes
    private val allEspecialidades = CitasSimuladas.especialidades
    private val allMedicos = CitasSimuladas.medicos

    private val citas = CitasSimuladas.citas(ancla).toMutableList()
    private var siguienteId = (citas.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun obtenerCitas(): List<Cita> {
        delay(retardoLecturaMs)
        return candado.withLock { citas.toList() }
    }

    override suspend fun obtenerCitaPorId(id: Long): Cita? {
        delay(retardoLecturaMs)
        return candado.withLock { citas.firstOrNull { it.id == id } }
    }

    override suspend fun obtenerPaciente(): Paciente {
        delay(retardoLecturaMs)
        return CitasSimuladas.paciente
    }

    override suspend fun obtenerEspecialidades(): List<String> {
        delay(retardoLecturaMs)
        return allEspecialidades.toList()
    }

    override suspend fun obtenerSedes(): List<Sede> {
        delay(retardoLecturaMs)
        return allSedes.toList()
    }

    override suspend fun contarCitasProgramadas(pacienteId: String): Int {
        delay(retardoLecturaMs)
        return candado.withLock {
            citas.count {
                it.paciente.id == pacienteId && it.estado is EstadoCita.Programada
            }
        }
    }

    override suspend fun existeProgramadaEnHorario(
        pacienteId: String,
        fecha: LocalDate,
        hora: LocalTime
    ): Boolean {
        delay(retardoLecturaMs)
        return candado.withLock {
            citas.any {
                it.paciente.id == pacienteId &&
                    it.fecha == fecha &&
                    it.hora == hora &&
                    it.estado is EstadoCita.Programada
            }
        }
    }

    override suspend fun solicitar(
        especialidad: String,
        sede: String,
        fecha: LocalDate,
        hora: LocalTime,
        motivo: String
    ): Cita {
        delay(retardoMutacionMs)
        return candado.withLock {
            val sedeAsignada = allSedes.firstOrNull { it.nombre == sede }
                ?: throw IllegalArgumentException("La sede solicitada no existe")
            val medicoAsignado = allMedicos.firstOrNull {
                it.especialidad == especialidad && it.sedes.any { s -> s.nombre == sede }
            } ?: throw IllegalArgumentException(
                "No hay médicos disponibles para $especialidad en $sede"
            )

            val nueva = Cita(
                id = siguienteId++,
                paciente = CitasSimuladas.paciente,
                especialidad = especialidad,
                medico = medicoAsignado,
                sede = sedeAsignada,
                fecha = fecha,
                hora = hora,
                estado = EstadoCita.Programada(recordatorioActivo = true)
            )

            citas.add(nueva)
            nueva
        }
    }

    override suspend fun cancelar(
        idCita: Long,
        motivo: String,
        canceladaPorPaciente: Boolean
    ): Cita {
        delay(retardoMutacionMs)
        return candado.withLock {
            val indice = citas.indexOfFirst { it.id == idCita }
            if (indice == -1) {
                throw IllegalArgumentException("La cita no existe")
            }

            val cancelada = citas[indice].copy(
                estado = EstadoCita.Cancelada(
                    motivo = motivo,
                    canceladaPorPaciente = canceladaPorPaciente
                )
            )

            citas[indice] = cancelada
            cancelada
        }
    }

    private companion object {
        const val RETARDO_LECTURA_MS = 800L
        const val RETARDO_MUTACION_MS = 400L
    }
}