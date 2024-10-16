package com.example.cateredtoyou.apifiles

import android.util.Log
import com.example.cateredtoyou.Client
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


// Url for the AWS web server
private const val BASE_URL = "http://ec2-13-56-230-200.us-west-1.compute.amazonaws.com/"

// Lots of functions to connect to an api that returns a json file
private val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

// interface for all of the functions that call the API
interface ApiConnect {
    @FormUrlEncoded

    @POST("login.php")
    fun loginCheck(
        @Field("username") username: String,
        @Field("password") password: String

    ): Call<LoginResponse>



    @POST("add_user.php")
    fun addUser(
        @Field("username") username: String,
        @Field("password") password: String

    ): Call<AddUserResponse>


    @GET("get_users.php")
    fun getUsers(): Call<List<User>>

    @GET("get_clients.php")
    fun getClient(): Call<List<Client>>
}

// the connection object
object DatabaseApi{
    val retrofitService : ApiConnect by lazy {
        retrofit.create(ApiConnect::class.java)
    }
}

// Data classes for the different responses based on the call

data class AddUserResponse(
    val status: String,
    val message: String
)

data class User(
    val id: Int,
    val username: String,
    val pass: String
)

data class LoginResponse(
    val status: Boolean,
    val message: String
)

fun clientCall(
    onSuccess: (List<Client>) -> Unit,
    onFailure: (Throwable) -> Unit = {t -> Log.e("ApiConnect", "Failed to connect", t)}
){
    DatabaseApi.retrofitService.getClient().enqueue(object : Callback<List<Client>> {
        override fun onResponse(call: Call<List<Client>>, response: Response<List<Client>>) {
            if (response.isSuccessful) {
                val rawResponse = response.body() ?: emptyList()
                onSuccess(rawResponse)
            }else{
                onFailure(Throwable("Failed to load clients: ${response.message()}"))
            }
        }

        override fun onFailure(call: Call<List<Client>>, t: Throwable) {
            onFailure(t)
        }
    })
}