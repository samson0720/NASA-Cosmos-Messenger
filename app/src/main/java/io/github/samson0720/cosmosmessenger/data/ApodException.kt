package io.github.samson0720.cosmosmessenger.data

// Typed failures from the APOD data layer. Keeping classification here means
// the ViewModel branches on domain error kinds instead of raw HTTP codes.
sealed class ApodException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    data object RateLimited : ApodException("rate_limited")
    data object NotFound : ApodException("not_found")
    data object Network : ApodException("network")
    class Unknown(cause: Throwable) : ApodException(cause.message, cause)
}
