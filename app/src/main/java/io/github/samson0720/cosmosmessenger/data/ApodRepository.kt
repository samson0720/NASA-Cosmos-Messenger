package io.github.samson0720.cosmosmessenger.data

import io.github.samson0720.cosmosmessenger.domain.model.Apod
import java.time.LocalDate

interface ApodRepository {
    // A null date means "today" — NASA's APOD endpoint defaults to today
    // when no date query is supplied.
    suspend fun getApod(date: LocalDate?): Result<Apod>
}
