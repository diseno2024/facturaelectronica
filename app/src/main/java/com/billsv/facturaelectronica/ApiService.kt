package com.billsv.facturaelectronica

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @Headers("User-Agent: Billsv/1.0")
    @POST("seguridad/auth")
    fun authenticate(
        @Field("user") user: String,
        @Field("pwd") pwd: String
    ): Call<AuthResponse>
}