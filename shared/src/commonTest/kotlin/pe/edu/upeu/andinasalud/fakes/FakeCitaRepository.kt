package pe.edu.upeu.andinasalud.fakes

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pe.edu.upeu.andinasalud.data.local.CitasSimuladas
import pe.edu.upeu.andinasalud.domain.model.Cita
import pe.edu.upeu.andinasalud.domain.model.EstadoCita
import pe.edu.upeu.andinasalud.domain.model.Paciente
import pe.edu.upeu.andinasalud.domain.repository.CitaRepository


class FakeCitaRepository(
    listaInicial: List<Cita>,
    private val pacienteFijo: Paciente = CitasSimuladas.paciente
) : CitaRepository {

    val registros: MutableList<Cita> = listaInicial.toMutableList()


    var errorSimulado: Exception? = null

    private fun exigirDisponible() {
        errorSimulado?.let { throw it }
    }

    override suspend fun obtenerCitas(): List<Cita> {
        exigirDisponible()
        return registros.toList()
    }

    override suspend fun obtenerCitaPorId(id: Long): Cita? {
        exigirDisponible()
        return registros.firstOrNull { it.id == id }
    }

    override suspend fun obtenerPaciente(): Paciente {
        exigirDisponible()
        return pacienteFijo
    }

    override suspend fun obtenerEspecialidades(): List<String> {
        exigirDisponible()
        return CitasSimuladas.especialidades.toList()
    }

    override suspend fun obtenerSedes(): List<pe.edu.upeu.andinasalud.domain.model.Sede> {
        exigirDisponible()
        return CitasSimuladas.sedes.toList()
    }

    override suspend fun contarCitasProgramadas(pacienteId: String): Int {
        exigirDisponible()
        return registros.count {
            it.paciente.id == pacienteId && it.estado is EstadoCita.Programada
        }
    }

    override suspend fun existeProgramadaEnHorario(
        pacienteId: String,
        fecha: LocalDate,
        hora: LocalTime
    ): Boolean {
        exigirDisponible()
        return registros.any {
            it.paciente.id == pacienteId &&
                it.fecha == fecha &&
                it.hora == hora &&
                it.estado is EstadoCita.Programada
        }
    }

    override suspend fun solicitar(
        especialidad: String,
        sede: String,
        fecha: LocalDate,
        hora: LocalTime,
        motivo: String
    ): Cita {
        exigirDisponible()
        val nueva = Cita(
            id = siguienteId(),
            paciente = pacienteFijo,
            especialidad = especialidad,
            medico = CitasSimuladas.medicos.first {
                it.especialidad == especialidad &&
                    it.sedes.any { s -> s.nombre == sede }
            },
            sede = CitasSimuladas.sedes.first { it.nombre == sede },
            fecha = fecha,
            hora = hora,
            estado = EstadoCita.Programada(recordatorioActivo = true)
        )
        registros.add(nueva)
        return nueva
    }

    override suspend fun cancelar(
        idCita: Long,
        motivo: String,
        canceladaPorPaciente: Boolean
    ): Cita {
        exigirDisponible()
        val indice = registros.indexOfFirst { it.id == idCita }
        if (indice == -1) throw IllegalArgumentException("La cita no existe")
        val cancelada = registros[indice].copy(
            estado = EstadoCita.Cancelada(motivo = motivo, canceladaPorPaciente = canceladaPorPaciente)
        )
        registros[indice] = cancelada
        return cancelada
    }

    private fun siguienteId(): Long =
        (registros.maxOfOrNull { it.id } ?: 0L) + 1L
}