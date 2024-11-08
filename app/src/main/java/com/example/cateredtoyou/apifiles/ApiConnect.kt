package com.example.cateredtoyou.apifiles

import com.example.cateredtoyou.Client
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

// Url for the AWS web server
private const val BASE_URL = "http://ec2-13-56-230-200.us-west-1.compute.amazonaws.com/php/"


// Interface for all of the functions that call the API
interface ApiConnect {
    @POST("login.php")
    @Headers("Content-Type: application/json")
    fun loginCheck(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("add_employee.php")
    @Headers("Content-Type: application/json")
    fun addUser(@Body addUserRequest: AddUserRequest): Call<AddUserResponse>

    @GET("get_employees.php")
    fun getUser(): Call<List<User>>

    @POST("add_client.php")
    @Headers("Content-Type: application/json")
    fun addClient(@Body addClientRequest: AddClientRequest): Call<AddClientResponse>

    @GET("get_clients.php")
    fun getClient(): Call<List<Client>>

    @POST("add_event.php")
    @Headers("Content-Type: application/json")
    fun addEvent(@Body addEventRequest: AddEventRequest): Call<EventResponse>

    @GET("get_events.php")
    fun getEvents(): Call<EventsResponse>

    @GET("get_inventory.php")
    fun getInventory(): Call<List<InventoryItem>>

    @POST("add_event_inventory.php")
    @Headers("Content-Type: application/json")
    fun addEventInventory(@Body addEventInventoryRequest: AddEventInventoryRequest): Call<BaseResponse>

    @GET("get_event_inventory.php")
    fun getEventInventory(@Query("event_id") eventId: Int): Call<EventInventoryResponse>

    @POST("delete_event.php")
    @Headers("Content-Type: application/json")
    fun deleteEvent(@Body deleteEventRequest: DeleteEventRequest): Call<DeleteResponse>

    @GET("get_raw_inventory.php")
    fun getRawInventory(): Call<List<InventoryItem>>

    @POST("update_inventory.php")
    @Headers("Content-Type: application/json")
    fun updateInventory(@Body request: UpdateInventoryRequest): Call<BaseResponse>
}

// The connection object
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

// Data classes for the different requests and responses

data class LoginRequest(
    val username: String,
    val password: String
)

data class AddUserRequest(
    val username: String,
    val password: String
)

data class AddClientRequest(
    val firstname: String,
    val lastname: String,
    val email: String,
    val phonenumber: String
)

data class AddEventRequest(
    val name: String,
    val eventDate: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val status: String,
    val numberOfGuests: Int,
    val clientId: Int,
    val employeeId: Int,
    val additionalInfo: String
)

data class AddEventInventoryRequest(
    val eventId: Int,
    val inventoryItems: String
)

data class DeleteEventRequest(
    val eventId: Int
)

// Existing response data classes
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
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String
) {
    override fun toString(): String = "$firstName $lastName ($role)"
}

data class LoginResponse(
    val status: Boolean,
    val message: String
)

data class AddClientResponse(
    val status: Boolean,
    val message: String
)

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
    val id: Int,
    val name: String,
    val eventDate: String,
    val eventStartTime: String,
    val eventEndTime: String,
    val location: String,
    val status: String,
    val numberOfGuests: Int,
    val client: ClientData,
    val employeeId: Int,
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
    @SerializedName("quantity") val quantity: Int
)
