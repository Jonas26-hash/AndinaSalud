package pe.edu.upeu.andinasalud.domain.model

data class Paciente(
    val id: String,
    val nombre: String,
    val documento: String,
    val correo: String,
    val telefono: String
) {

    init {
        require(id.isNotBlank()) { "El identificador del paciente no puede estar vacío" }
        require(nombre.isNotBlank()) { "El nombre del paciente no puede estar vacío" }
        require(documento.isNotBlank()) { "El documento del paciente no puede estar vacío" }
        require(correo.isNotBlank()) { "El correo del paciente no puede estar vacío" }
        require(telefono.isNotBlank()) { "El teléfono del paciente no puede estar vacío" }
    }
}