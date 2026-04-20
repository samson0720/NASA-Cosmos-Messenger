package io.github.samson0720.cosmosmessenger.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

// Intentionally no HttpLoggingInterceptor: the NASA api_key travels as a
// query parameter, so request logging would leak it. Keep the network
// stack quiet until a redacting interceptor is introduced.
object NetworkModule {

    private const val BASE_URL = "https://api.nasa.gov/"

    // Chat-style UX: keep waits short. 10s connect covers slow TLS on a
    // rough network; 12s read gives NASA APOD a little extra grace since
    // the read (JSON generation upstream) is the usual slow path.
    // Combined with the single retry in ApodRepositoryImpl, worst-case
    // latency on a terminal failure stays well under ~45s.
    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val apodService: ApodService by lazy { retrofit.create(ApodService::class.java) }
}
