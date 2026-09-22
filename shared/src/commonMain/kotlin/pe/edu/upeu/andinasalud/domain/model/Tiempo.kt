package pe.edu.upeu.andinasalud.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val MILISEGUNDOS_POR_DIA = 86_400_000L
internal fun LocalDate.aEpochMillis(hora: LocalTime): Long =
    toEpochDays() * MILISEGUNDOS_POR_DIA + hora.toSecondOfDay() * 1000L
@OptIn(ExperimentalTime::class)
internal fun ahoraMillis(): Long = Clock.System.now().toEpochMilliseconds()
internal fun hoyUtc(): LocalDate =
    LocalDate.fromEpochDays((ahoraMillis() / MILISEGUNDOS_POR_DIA).toInt())