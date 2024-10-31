package com.aos.beacondistance.http

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface HttpRequests {
    @POST("api/login_check")
    suspend fun checkLogin(@Body data: LoginData): Response<String>

    @POST("api/get_user_Data")
    suspend fun getUserData(): Response<String>
}

data class LoginData(
    @SerializedName("id") var userId: String,
    @SerializedName("pw") var userPw: String,
)

data class CircleData(
    @SerializedName("name") var name: String,
    @SerializedName("distance") var distance: Int,
)
