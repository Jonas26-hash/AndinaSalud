package pe.edu.upeu.andinasalud.data.local

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import pe.edu.upeu.andinasalud.domain.model.Cita
import pe.edu.upeu.andinasalud.domain.model.EstadoCita
import pe.edu.upeu.andinasalud.domain.model.Medico
import pe.edu.upeu.andinasalud.domain.model.Paciente
import pe.edu.upeu.andinasalud.domain.model.Sede
import pe.edu.upeu.andinasalud.domain.model.hoyUtc


object CitasSimuladas {

    val paciente = Paciente(
        id = "P-0417",
        nombre = "Lucía Quispe Mamani",
        documento = "70154823",
        correo = "lucia.quispe@correo.pe",
        telefono = "987 654 321"
    )

    val sedes = listOf(
        Sede(id = 1, nombre = "Ñaña"),
        Sede(id = 2, nombre = "Chosica"),
        Sede(id = 3, nombre = "Chaclacayo"),
        Sede(id = 4, nombre = "Santa Anita")
    )

    val especialidades = listOf(
        "Medicina General",
        "Odontología",
        "Pediatría",
        "Nutrición",
        "Psicología"
    )

    val medicos = listOf(
        Medico(id = 1, nombre = "Dr. Iván Rojas", especialidad = "Medicina General", sedes = listOf(sedes[0], sedes[1])),
        Medico(id = 2, nombre = "Dra. María Torres", especialidad = "Medicina General", sedes = listOf(sedes[2], sedes[3])),
        Medico(id = 3, nombre = "Dra. Rosa Flores", especialidad = "Odontología", sedes = listOf(sedes[1], sedes[0])),
        Medico(id = 4, nombre = "Dr. Pedro Sánchez", especialidad = "Odontología", sedes = listOf(sedes[3])),
        Medico(id = 5, nombre = "Dra. Carla Núñez", especialidad = "Pediatría", sedes = listOf(sedes[2])),
        Medico(id = 6, nombre = "Dr. Jorge Meza", especialidad = "Pediatría", sedes = listOf(sedes[0], sedes[1])),
        Medico(id = 7, nombre = "Lic. Ana Bermúdez", especialidad = "Nutrición", sedes = listOf(sedes[3], sedes[2])),
        Medico(id = 8, nombre = "Lic. Patricia Salas", especialidad = "Nutrición", sedes = listOf(sedes[0])),
        Medico(id = 9, nombre = "Ps. Luis Tapia", especialidad = "Psicología", sedes = listOf(sedes[0], sedes[1])),
        Medico(id = 10, nombre = "Ps. Carmen Vela", especialidad = "Psicología", sedes = listOf(sedes[3], sedes[2]))
    )


    fun citas(ancla: LocalDate = hoyUtc()): List<Cita> = listOf(
        cita(
            id = 1,
            especialidad = "Medicina General",
            medico = medicos[0],
            sede = sedes[0],
            fecha = ancla.plus(2, DateTimeUnit.DAY),
            hora = LocalTime(9, 0),
            estado = EstadoCita.Programada(recordatorioActivo = true)
        ),
        cita(
            id = 2,
            especialidad = "Odontología",
            medico = medicos[2],
            sede = sedes[1],
            fecha = ancla.plus(6, DateTimeUnit.DAY),
            hora = LocalTime(16, 30),
            estado = EstadoCita.Programada(recordatorioActivo = false)
        ),
        cita(
            id = 3,
            especialidad = "Nutrición",
            medico = medicos[6],
            sede = sedes[3],
            fecha = ancla.plus(10, DateTimeUnit.DAY),
            hora = LocalTime(11, 15),
            estado = EstadoCita.Programada(recordatorioActivo = true)
        ),
        cita(
            id = 4,
            especialidad = "Pediatría",
            medico = medicos[4],
            sede = sedes[2],
            fecha = ancla.minus(20, DateTimeUnit.DAY),
            hora = LocalTime(8, 45),
            estado = EstadoCita.Atendida(indicaciones = "Control en tres meses")
        ),
        cita(
            id = 5,
            especialidad = "Psicología",
            medico = medicos[8],
            sede = sedes[0],
            fecha = ancla.minus(30, DateTimeUnit.DAY),
            hora = LocalTime(15, 0),
            estado = EstadoCita.Atendida(indicaciones = "Continuar sesiones quincenales")
        ),
        cita(
            id = 6,
            especialidad = "Medicina General",
            medico = medicos[0],
            sede = sedes[1],
            fecha = ancla.minus(45, DateTimeUnit.DAY),
            hora = LocalTime(10, 30),
            estado = EstadoCita.Cancelada(
                motivo = "Viaje del paciente",
                canceladaPorPaciente = true
            )
        )
    )

    private fun cita(
        id: Long,
        especialidad: String,
        medico: Medico,
        sede: Sede,
        fecha: LocalDate,
        hora: LocalTime,
        estado: EstadoCita
    ): Cita = Cita(
        id = id,
        paciente = paciente,
        especialidad = especialidad,
        medico = medico,
        sede = sede,
        fecha = fecha,
        hora = hora,
        estado = estado
    )
}