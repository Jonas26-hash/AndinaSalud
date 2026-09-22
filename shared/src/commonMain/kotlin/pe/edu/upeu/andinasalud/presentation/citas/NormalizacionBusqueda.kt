package pe.edu.upeu.andinasalud.presentation.citas


internal fun normalizarParaBusqueda(texto: String): String =
    texto.lowercase().map { ACENTOS[it] ?: it }.joinToString("").trim()

private val ACENTOS = mapOf(
    'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u', 'ñ' to 'n',
    'Á' to 'a', 'É' to 'e', 'Í' to 'i', 'Ó' to 'o', 'Ú' to 'u', 'Ü' to 'u', 'Ñ' to 'n'
)