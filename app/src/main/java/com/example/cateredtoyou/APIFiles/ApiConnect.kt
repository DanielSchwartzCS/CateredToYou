package com.example.cateredtoyou.apifiles

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

private const val BASE_URL = "http://ec2-54-215-193-219.us-west-1.compute.amazonaws.com/"
//http://ec2-54-215-193-219.us-west-1.compute.amazonaws.com/
private val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface ApiConnect {
    @FormUrlEncoded
    @POST("add_user.php")
    fun addUser(
        @Field("username") username: String,
        @Field("password") password: String

    ): Call<AddUserResponse>


    @GET("get_users_new.php")
    fun getUsers(): Call<List<User>>
}

object DatabaseApi{
    val retrofitService : ApiConnect by lazy {
        retrofit.create(ApiConnect::class.java)
    }
}
data class AddUserResponse(
    val status: String,
    val message: String
)

data class User(
    val id: Int,
    val username: String,
    val pass: String
)