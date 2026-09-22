package pe.edu.upeu.andinasalud.domain.model

data class Medico(
    val id: Long,
    val nombre: String,
    val especialidad: String,
    val sedes: List<Sede>
) {

    init {
        require(nombre.isNotBlank()) { "El nombre del médico no puede estar vacío" }
        require(especialidad.isNotBlank()) { "La especialidad del médico no puede estar vacía" }
        require(sedes.isNotEmpty()) { "El médico debe estar asignado a al menos una sede" }
    }
}