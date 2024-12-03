package com.example.cateredtoyou.apifiles

import android.util.Log
import com.example.cateredtoyou.Client
import com.example.cateredtoyou.TaskItem
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query


// Url for the AWS web server
private const val BASE_URL = "http://54.215.252.172/"

// Lots of functions to connect to an api that returns a json file
private val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

// interface for all of the functions that call the API
interface ApiConnect {
    @Headers("Content-Type: application/json")
    @POST("auth")
    fun loginCheck(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @FormUrlEncoded
    @POST("login2.php")
    fun loginCheck2(
        @Field("username") username: String,
        @Field("password") password: String

    ): Call<LoginResponse>


    @FormUrlEncoded
    @POST("add_employee.php")
    fun addUser(
        @Field("username") username: String,
        @Field("password") password: String

    ): Call<AddUserResponse>

    @GET("get_tasks.php")
    fun getTasks(@Query("filter") filter: String): Call<List<TaskItem>>

    @FormUrlEncoded
    @POST("update_task_status.php")
    fun updateTasks(@Field("task_id") taskId: Int): Call<Void>


    @GET("get_employees.php")
    fun getUser(): Call<List<User>>

    @FormUrlEncoded
    @POST("add_client.php")
    fun addClient(
        @Field("firstname") firstname: String,
        @Field("lastname") lastname: String,
        @Field("email") email : String,
        @Field("phonenumber") phonenumber : String,
        @Field("billing") billing : String,
        @Field("contactMethod") contactMethod : String,
        @Field("notes") notes : String
    ): Call<AddClientResponse>

    @GET("get_clients.php")
    fun getClient(): Call<List<Client>>

    @FormUrlEncoded
    @POST("add_event.php")
    fun addEvent(
        @Field("name") name: String,
        @Field("event_date") eventDate: String,
        @Field("event_start_time") startTime: String,
        @Field("event_end_time") endTime: String,
        @Field("location") location: String,
        @Field("status") status: String,
        @Field("number_of_guests") numberOfGuests: Int,
        @Field("client_id") clientId: Int,
        @Field("employee_id") employeeId: Int,
        @Field("additional_info") additionalInfo: String
    ): Call<EventResponse>


    @GET("get_events.php")
    fun getEvents(): Call<EventsResponse>

    @GET("get_inventory.php")
    fun getInventory(): Call<List<InventoryItem>>

    @FormUrlEncoded
    @POST("add_event_inventory.php")
    fun addEventInventory(
        @Field("event_id") eventId: Int,
        @Field("inventory_items") inventoryItems: String
    ): Call<BaseResponse>

    @POST("/refreshtoken")
    suspend fun refreshToken(@Body refreshRequest: RefreshRequest): Response<TokenResponse>


    @GET("get_event_inventory.php")
    fun getEventInventory(@Query("event_id") eventId: Int): Call<EventInventoryResponse>

    @POST("delete_event.php")
    @FormUrlEncoded
    fun deleteEvent(
        @Field("event_id") eventId: Int
    ): Call<DeleteResponse>

    @GET("get_raw_inventory.php")
    fun getRawInventory(): Call<List<InventoryItem>>

    @POST("update_inventory.php")
    @Headers("Content-Type: application/json")
    fun updateInventory(@Body request: UpdateInventoryRequest): Call<BaseResponse>

    companion object {
        fun refreshToken(refreshToken: String): Any {
            return TODO("Provide the return value")
        }
    }


}

// the connection object
object DatabaseApi {
    val retrofitService: ApiConnect by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // Create a lenient Gson instance
        val gson = GsonBuilder()
            .setLenient()
            .create()

        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .client(client)
            .build()
            .create(ApiConnect::class.java)
    }
}
// Data classes for the different responses based on the call

data class AddUserResponse(
    val status: String,
    val message: String
)

data class User(
    @SerializedName("user_id") val userId: Int,
    val username: String,
    val password: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = null,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
) {
    override fun toString(): String = "$firstName $lastName ($role)"
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val status: Boolean,
    val message: String,
    val token: String? = null,
    val refreshToken: String? = null
)

data class RefreshRequest(
    val refreshToken: String
)


data class AddClientResponse(
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
//fun addClient(
//    firstname: String,
//    lastname: String,
//    email: String,
//    phonenumber: String,
//    onSuccess: (AddClientResponse) -> Unit,
//    onPartialSuccess: (AddClientResponse) -> Unit,
//    onFailure: (Throwable) -> Unit
//){
//    DatabaseApi.retrofitService.addClient(firstname, lastname, email, phonenumber).enqueue(object : Callback<AddClientResponse>{
//        override fun onResponse(
//            call: Call<AddClientResponse>,
//            response: Response<AddClientResponse>
//        ) {
//            if(response.isSuccessful){
//                val rawResponse = response.body()
//                if (rawResponse != null && rawResponse.status) {
//                    onSuccess(rawResponse)
//                }else if(rawResponse != null){
//                    onPartialSuccess(rawResponse)
//                }
//            }else{
//                onFailure(Throwable("Failed to add client: ${response.message()}"))
//            }
//        }
//
//        override fun onFailure(call: Call<AddClientResponse>, t: Throwable) {
//            onFailure(t)
//        }
//    })
//
//}

data class EventResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("event_id") val eventId: Int?
)

data class EventsResponse(
    val status: Boolean,
    val events: List<EventData>? = null,
    val message: String? = null
)

data class EventData(
    val event_id: Int,
    val event_name: String,
    val event_description: String,
    val start_date: String,
    val end_date: String,
    val location: String,
    val client_id: Int,
    val client: Client,
    val guest_count: Int,
    val waiters_provided: Int,
    val min_staff: Int,
    val notes: String,
    val event_status: String,
    val start_time: String,
    val end_time: String
)

data class ClientData(
    val client_id: Int,
    val first_name: String,
    val last_name: String,
    val phone_number: String,
    val email_address: String,
    val billing_address: String,
    val preferred_contact_method: String,
    val notes: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

data class InventoryItem(
    @SerializedName("inventory_id") val inventory_id: Int,
    @SerializedName("item_name") val item_name: String,
    @SerializedName("category") val category: String,
    @SerializedName("display_unit") val display_unit: String?,
    @SerializedName("quantity_in_stock") val quantity_in_stock: Float,
    @SerializedName("cost_per_unit") val cost_per_unit: Float,
    @SerializedName("location_id") val location_id: Int,
    @SerializedName("notes") val notes: String?,
) {
    override fun toString(): String = item_name
}


data class BaseResponse(
    val status: Boolean,
    val message: String
)

data class EventInventoryResponse(
    val status: Boolean,
    val message: String?,
    val items: List<EventInventoryItem>?
)

data class EventInventoryItem(
    val id: Int,
    val itemName: String,
    val quantity: Int,
    val category: String?,
    val unitOfMeasurement: String?
)
data class DeleteResponse(
    val status: Boolean,
    val message: String
)
data class UpdateInventoryRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("quantity") val quantity: Float
)