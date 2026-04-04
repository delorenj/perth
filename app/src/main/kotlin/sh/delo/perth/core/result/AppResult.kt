package sh.delo.perth.core.result

/** Unified result type for all async operations in Perth. */
sealed class AppResult<out T> {

    data class Success<T>(val data: T) : AppResult<T>()

    data class Error(val exception: AppException) : AppResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    /** Returns the data if this is [Success], or null otherwise. */
    fun getOrNull(): T? = (this as? Success)?.data

    /** Returns the exception if this is [Error], or null otherwise. */
    fun exceptionOrNull(): AppException? = (this as? Error)?.exception

    /** Applies [block] to the data if this is [Success]. */
    inline fun <R> map(block: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(block(data))
        is Error -> this
    }

    /** Returns the data if [Success], or the result of [default] if [Error]. */
    inline fun getOrElse(default: (AppException) -> @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> default(exception)
    }
}

/** Wraps [block] in a try/catch and returns [AppResult.Success] or [AppResult.Error]. */
inline fun <T> runCatchingAppResult(
    errorMapper: (Throwable) -> AppException = { AppException.Network(it.message ?: "Unknown error", it) },
    block: () -> T,
): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: AppException) {
    AppResult.Error(e)
} catch (e: Exception) {
    AppResult.Error(errorMapper(e))
}
