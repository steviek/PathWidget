package com.sixbynine.transit.path.ktx

import com.sixbynine.transit.path.logging.Logging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.Call
import retrofit2.awaitResponse

suspend fun <T> Call<T>.awaitWithTimeoutAndCatchingErrors(
    logging: Logging
): Result<T> = withContext(Dispatchers.IO) {
    val response = try {
        withTimeout(5.seconds.toMillis()) { awaitResponse() }
    } catch (e: TimeoutCancellationException) {
        logging.warn("Timed out trying to ${request().method()} ${request().url()}")
        return@withContext Result.failure(e)
    } catch (t: Throwable) {
        logging.warn("Unexpected error trying to ${request().method()} ${request().url()}", t)
        return@withContext Result.failure(t)
    }

    val body = response.body()
    if (!response.isSuccessful || body == null) {
        logging.warn(
            "Request was not successful: " +
                    "[errorBody=${response.errorBody().toString()}]"
        )
        return@withContext Result.failure(RuntimeException(response.errorBody().toString()))
    }

    logging.debug("Successfully performed ${request().method()} of ${request().url()}")

    Result.success(body)
}
