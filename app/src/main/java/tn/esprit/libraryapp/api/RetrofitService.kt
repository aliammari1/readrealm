package tn.esprit.libraryapp.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitService {
    private const val BASE_URL = "http://192.168.98.105:3000/"

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    private val okHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val userService: UserService by lazy { retrofit.create(UserService::class.java) }

    val bookService: BookService by lazy { retrofit.create(BookService::class.java) }
}
