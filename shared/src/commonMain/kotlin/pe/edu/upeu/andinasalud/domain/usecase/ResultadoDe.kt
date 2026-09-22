package pe.edu.upeu.andinasalud.domain.usecase

import kotlin.coroutines.cancellation.CancellationException

internal suspend fun <T> resultadoDe(bloque: suspend () -> T): Result<T> {

    return try {
        Result.success(bloque())
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (fallo: Throwable) {
        Result.failure(fallo)
    }
}