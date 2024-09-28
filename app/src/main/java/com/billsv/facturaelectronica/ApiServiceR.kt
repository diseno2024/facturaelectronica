package com.billsv.facturaelectronica


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
interface ApiServiceR {
    @Headers(
        "Content-Type: application/json",
        "User-Agent: Billsv/1.0",
    )
    @POST("/recepciondte")
    fun reception(@Body request: RecepcionRequest): Call<RecepcionResponse>
}