package com.billsv.facturaelectronica

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient0 {
    private const val BASE_URL = "http://192.168.1.17:3000/recepciondte/"
    fun getInstance(token: String): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(token))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL) // Reemplaza con tu base URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

object RetrofitClient {
    //real private const val BASE_URL = "https://api.dtes.mh.gob.sv/seguridad/authw/"
    //prueba backend fake
    val url = "http://192.168.1.17:3000/auth"

    private const val BASE_URL = "http://192.168.1.17:3000/auth/"
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }
}
object RetrofitClient2 {
    //real private const val BASE_URL = "https://apitest.dtes.mh.gob.sv/seguridad/auth/"
    private const val BASE_URL = "http://192.168.1.17:3000/auth/"
    //prueba backend fake
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }
}

