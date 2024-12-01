package com.billsv.facturaelectronica

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient0 {
    private const val BASE_URL = "https://apitest.dtes.mh.gob.sv/" //TEST *///private const val BASE_URL = "http://192.168.1.17:3000/recepciondte/"//(Bakend)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    fun getInstance(token: String): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(token))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL) // Reemplaza con tu base URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
object RetrofitClient1 {
    /*private const val BASE_URL = "https://api.dtes.mh.gob.sv/fesv/recepciondte/" //PROD*/ //private const val BASE_URL = "http://192.168.1.17:3000/recepciondte/"//(Bakend)
    private const val BASE_URL = "https://apitest.dtes.mh.gob.sv/"
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    fun getInstance(token: String): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
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
    private const val BASE_URL = "https://apitest.dtes.mh.gob.sv/"//TEST */private const val BASE_URL = "http://192.168.1.17:3000/auth/"//(Bakend)
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
    /*private const val BASE_URL = "https://api.dtes.mh.gob.sv/seguridad/auth/"PROD*/ //private const val BASE_URL = "http://192.168.1.17:3000/auth/"//(Bakend)
    //prueba backend fake
    private const val BASE_URL = "https://apitest.dtes.mh.gob.sv/"
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

