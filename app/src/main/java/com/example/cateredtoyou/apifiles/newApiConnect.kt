package com.example.cateredtoyou.apifiles

import android.util.Log
import com.example.cateredtoyou.Client
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
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path
/*
// Base URL for the PHP server
private const val BASE_URL = "http://ec2-13-56-230-200.us-west-1.compute.amazonaws.com/php/"

// Retrofit instance
private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
    .build()

// API interface
interface ApiConnect {
    @FormUrlEncoded
    @POST("login.php")
    fun loginCheck(
        @Field("username") username: String,
        @Field("password_hash") password: String //pw or pw_hash?
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("add_employee.php")
    fun addUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<AddUserResponse>

    @GET("get_employees.php")
    fun getUser(): Call<List<User>>

    @FormUrlEncoded
    @POST("add_client.php")
    fun addClient(
        @Field("firstname") firstname: String,
        @Field("lastname") lastname: String,
        @Field("email") email: String,
        @Field("phonenumber") phonenumber: String
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

    @GET("get_event_inventory.php")
    fun getEventInventory(@Path("event_id") eventId: Int): Call<EventInventoryResponse>
}

// Singleton for Database API
object DatabaseApi {
    val retrofitService: ApiConnect by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .baseUrl(BASE_URL)
            .client(client)
            .build()
            .create(ApiConnect::class.java)
    }
}

// Data classes for API responses
data class AddUserResponse(
    val status: String,
    val message: String
)

data class User(
    val id: Int,
    val username: String,
    val role: String?,
    val pass: String
)

data class LoginResponse(
    val status: Boolean,
    val message: String
)

data class AddClientResponse(
    val status: Boolean,
    val message: String
)

data class EventResponse(
    val status: Boolean,
    val message: String,
    val eventId: Int? = null
)

data class EventsResponse(
    val status: Boolean,
    val events: List<EventData>? = null,
    val message: String? = null
)

data class EventData(
    val id: Int,
    val name: String,
    val eventDate: String,
    val eventStartTime: String,
    val eventEndTime: String,
    val location: String,
    val status: String,
    val numberOfGuests: Int,
    val client: ClientData,
    val additionalInfo: String?
)

data class ClientData(
    val id: Int,
    val firstname: String,
    val lastname: String,
    val email: String,
    val phonenumber: String
)

data class InventoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("item_name") val itemName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("category") val category: String,
    @SerializedName("unit_of_measurement") val unitOfMeasurement: String?,
    @SerializedName("cost_per_unit") val costPerUnit: Double?,
    @SerializedName("minimum_stock") val minimumStock: Int?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("last_restocked") val lastRestocked: String?,
    @SerializedName("updated_at") val updatedAt: String?
) {
    override fun toString(): String = itemName
}

data class BaseResponse(
    val status: Boolean,
    val message: String
)

data class EventInventoryResponse(
    val status: Boolean,
    val items: List<EventInventoryItem>
)

data class EventInventoryItem(
    val id: Int,
    val itemName: String,
    val quantity: Int,
    val category: String,
    val unitOfMeasurement: String?
)

// Function to fetch clients
fun clientCall(
    onSuccess: (List<Client>) -> Unit,
    onFailure: (Throwable) -> Unit = { t -> Log.e("ApiConnect", "Failed to connect", t) }
) {
    DatabaseApi.retrofitService.getClient().enqueue(object : Callback<List<Client>> {
        override fun onResponse(call: Call<List<Client>>, response: Response<List<Client>>) {
            if (response.isSuccessful) {
                val rawResponse = response.body() ?: emptyList()
                onSuccess(rawResponse)
            } else {
                onFailure(Throwable("Failed to load clients: ${response.message()}"))
            }
        }

        override fun onFailure(call: Call<List<Client>>, t: Throwable) {
            onFailure(t)
        }
    })
}

// Function to add a client
fun addClient(
    firstname: String,
    lastname: String,
    email: String,
    phonenumber: String,
    onSuccess: (AddClientResponse) -> Unit,
    onPartialSuccess: (AddClientResponse) -> Unit,
    onFailure: (Throwable) -> Unit
) {
    DatabaseApi.retrofitService.addClient(firstname, lastname, email, phonenumber).enqueue(object : Callback<AddClientResponse> {
        override fun onResponse(call: Call<AddClientResponse>, response: Response<AddClientResponse>) {
            if (response.isSuccessful) {
                val rawResponse = response.body()
                if (rawResponse != null && rawResponse.status) {
                    onSuccess(rawResponse)
                } else if (rawResponse != null) {
                    onPartialSuccess(rawResponse)
                }
            } else {
                onFailure(Throwable("Failed to add client: ${response.message()}"))
            }
        }

        override fun onFailure(call: Call<AddClientResponse>, t: Throwable) {
            onFailure(t)
        }
    })
}
*/