package pe.edu.upeu.andinasalud.domain.model

data class Sede(
    val id: Long,
    val nombre: String
) {

    init {
        require(nombre.isNotBlank()) { "El nombre de la sede no puede estar vacío" }
    }
}