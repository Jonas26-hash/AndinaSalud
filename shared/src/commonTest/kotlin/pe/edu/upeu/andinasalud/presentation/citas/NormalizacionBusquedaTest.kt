package pe.edu.upeu.andinasalud.presentation.citas

import kotlin.test.Test
import kotlin.test.assertEquals


class NormalizacionBusquedaTest {

    @Test
    fun `quita las tildes de las vocales`() {
        assertEquals("odontologia", normalizarParaBusqueda("Odontología"))
        assertEquals("pediatria", normalizarParaBusqueda("Pediatría"))
        assertEquals("nutricion", normalizarParaBusqueda("Nutrición"))
        assertEquals("cirugia", normalizarParaBusqueda("CIRUGÍA"))
    }

    @Test
    fun `mantiene la ene enie`() {
        assertEquals("nana", normalizarParaBusqueda("Ñaña"))
    }

    @Test
    fun `pasa todo a minusculas`() {
        assertEquals("medicina general", normalizarParaBusqueda("MEDICINA GENERAL"))
    }

    @Test
    fun `recorta los espacios de los extremos`() {
        assertEquals("psicologia", normalizarParaBusqueda("  Psicología  "))
    }

    @Test
    fun `deja intacto el texto sin acentos`() {
        assertEquals("ivan rojas", normalizarParaBusqueda("Ivan Rojas"))
    }

    @Test
    fun `el texto vacio sigue vacio`() {
        assertEquals("", normalizarParaBusqueda(""))
    }
}


class FormatoFechasTest {

    @Test
    fun `formatea la fecha como dia-mes-anio`() {
        assertEquals("24/09/2026", kotlinx.datetime.LocalDate(2026, 9, 24).formatoDiaMesAnio())
        assertEquals("05/01/2026", kotlinx.datetime.LocalDate(2026, 1, 5).formatoDiaMesAnio())
    }

    @Test
    fun `formatea la hora siempre con dos digitos`() {
        assertEquals("16:30", kotlinx.datetime.LocalTime(16, 30).formatoHora())
        assertEquals("09:05", kotlinx.datetime.LocalTime(9, 5).formatoHora())
    }
}