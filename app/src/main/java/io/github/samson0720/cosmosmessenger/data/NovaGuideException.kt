package io.github.samson0720.cosmosmessenger.data

sealed class NovaGuideException : Exception() {
    data object NotConfigured : NovaGuideException()
    data object Network : NovaGuideException()
    data object ServiceUnavailable : NovaGuideException()
    data object InvalidResponse : NovaGuideException()
}
