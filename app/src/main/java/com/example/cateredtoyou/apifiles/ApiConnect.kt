package com.example.cateredtoyou.apifiles

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Url for the AWS web server
private const val BASE_URL = "http://54.219.249.27/api/"

// interface for all of the functions that call the API
interface ApiConnect {
    // Authentication Endpoints
    @Headers("Content-Type: application/json")
    @POST("auth/login")
    fun loginCheck(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/refresh")
    fun refreshToken(@Body refreshRequest: RefreshRequest): Call<RefreshTokenResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/logout")
    fun logout(@Body logoutRequest: LogoutRequest): Call<Void>

    // User Endpoints
    @POST("users")
    fun addUser(@Body userData: UserRequest): Call<UserResponse>

    @GET("users")
    fun getAllUsers(): Call<List<User>>

    @GET("users/{userId}")
    fun getUserById(@Path("userId") userId: Int): Call<User>

    @PUT("users/{userId}")
    fun updateUserDetails(
        @Path("userId") userId: Int,
        @Body userDetails: UpdateUserRequest
    ): Call<Void>

    @PUT("users/{userId}/role")
    fun updateUserRole(
        @Path("userId") userId: Int,
        @Body roleUpdate: RoleUpdateRequest
    ): Call<Void>

    @PUT("users/{userId}/password")
    fun updatePassword(
        @Path("userId") userId: Int,
        @Body passwordUpdate: PasswordUpdateRequest
    ): Call<Void>

    @PUT("users/{userId}/status")
    fun updateEmploymentStatus(
        @Path("userId") userId: Int,
        @Body statusUpdate: EmploymentStatusUpdateRequest
    ): Call<Void>

    // Client Endpoints
    @POST("clients")
    fun addClient(@Body clientData: ClientRequest): Call<ClientResponse>

    @GET("clients")
    fun getAllClients(): Call<List<Client>>

    @GET("clients/{clientId}")
    fun getClientById(@Path("clientId") clientId: Int): Call<Client>

    @PUT("clients/{clientId}")
    fun updateClientDetails(
        @Path("clientId") clientId: Int,
        @Body clientDetails: ClientUpdateRequest
    ): Call<Void>

    // Event Endpoints
    @POST("events")
    fun addEvent(@Body eventData: EventRequest): Call<EventResponse>

    @GET("events")
    fun getAllEvents(): Call<List<Event>>

    @GET("events/{eventId}")
    fun getEventById(@Path("eventId") eventId: Int): Call<Event>

    @PUT("events/{eventId}")
    fun updateEvent(
        @Path("eventId") eventId: Int,
        @Body eventDetails: EventUpdateRequest
    ): Call<Void>

    @GET("events/client/{clientId}")
    fun getEventsByClient(@Path("clientId") clientId: Int): Call<List<Event>>

    // Inventory Endpoints
    @POST("inventory")
    fun addInventoryItem(@Body inventoryData: InventoryItemRequest): Call<InventoryItemResponse>

    @GET("inventory")
    fun getAllInventoryItems(): Call<List<InventoryItem>>

    @GET("inventory/{inventoryId}")
    fun getInventoryItemById(@Path("inventoryId") inventoryId: Int): Call<InventoryItem>

    @PUT("inventory/{inventoryId}")
    fun updateInventoryItem(
        @Path("inventoryId") inventoryId: Int,
        @Body inventoryDetails: InventoryItemUpdateRequest
    ): Call<Void>

    @PUT("inventory/{inventoryId}/quantity")
    fun updateInventoryQuantity(
        @Path("inventoryId") inventoryId: Int,
        @Body quantityUpdate: QuantityUpdateRequest
    ): Call<Void>

    @GET("inventory/event/{eventId}")
    fun getInventoryByEvent(@Path("eventId") eventId: Int): Call<List<EventInventoryItem>>
}

// Connection object for API
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

// Request and Response Data Classes
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val data: LoginData?
)

data class LoginData(
    val jwt: String,
    val refreshToken: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val status: String,
    val data: RefreshTokenData?
)

data class RefreshTokenData(
    val jwt: String
)

data class LogoutRequest(
    val refreshToken: String
)

data class UserRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val phone: String,
    val email: String,
    @SerializedName("employment_status") val employmentStatus: String,
    val role: String,
    val password: String
)

data class UserResponse(
    val status: String,
    val data: UserResponseData?
)

data class UserResponseData(
    @SerializedName("user_id") val userId: Int
)

data class User(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val phone: String,
    val email: String,
    @SerializedName("employment_status") val employmentStatus: String,
    val role: String
)

data class UpdateUserRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val phone: String,
    val email: String,
    @SerializedName("employment_status") val employmentStatus: String
)

data class RoleUpdateRequest(
    val role: String
)

data class PasswordUpdateRequest(
    @SerializedName("new_password") val newPassword: String
)

data class EmploymentStatusUpdateRequest(
    @SerializedName("employment_status") val employmentStatus: String
)

data class ClientRequest(
    @SerializedName("client_name") val clientName: String,
    val phone: String,
    val email: String,
    @SerializedName("billing_address") val billingAddress: String,
    @SerializedName("preferred_contact_method") val preferredContactMethod: String,
    val notes: String
)

data class ClientResponse(
    val status: String,
    val data: ClientResponseData?
)

data class ClientResponseData(
    @SerializedName("client_id") val clientId: Int
)

data class Client(
    @SerializedName("client_id") val clientId: Int,
    @SerializedName("client_name") val clientName: String,
    val phone: String,
    val email: String,
    @SerializedName("billing_address") val billingAddress: String,
    @SerializedName("preferred_contact_method") val preferredContactMethod: String,
    val notes: String
)

data class ClientUpdateRequest(
    @SerializedName("client_name") val clientName: String,
    val phone: String,
    val email: String,
    @SerializedName("billing_address") val billingAddress: String,
    @SerializedName("preferred_contact_method") val preferredContactMethod: String,
    val notes: String
)

data class EventRequest(
    @SerializedName("event_description") val eventDescription: String,
    @SerializedName("event_date") val eventDate: String,
    @SerializedName("event_time") val eventTime: String,
    val location: String,
    @SerializedName("num_guests") val numGuests: Int,
    val notes: String
)

data class EventResponse(
    val status: String,
    val data: EventResponseData?
)

data class EventResponseData(
    @SerializedName("event_id") val eventId: Int
)

data class Event(
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("event_description") val eventDescription: String,
    @SerializedName("event_date") val eventDate: String,
    @SerializedName("event_time") val eventTime: String,
    val location: String,
    @SerializedName("num_guests") val numGuests: Int,
    val notes: String
)

data class EventUpdateRequest(
    @SerializedName("event_description") val eventDescription: String,
    @SerializedName("event_date") val eventDate: String,
    @SerializedName("event_time") val eventTime: String,
    val location: String,
    @SerializedName("num_guests") val numGuests: Int,
    val notes: String
)

data class InventoryItemRequest(
    @SerializedName("item_name") val itemName: String,
    val unit: String,
    @SerializedName("display_unit") val displayUnit: String,
    @SerializedName("quantity_in_stock") val quantityInStock: Int,
    @SerializedName("location_id") val locationId: Int
)

data class InventoryItemResponse(
    val status: String,
    val data: InventoryItemResponseData?
)

data class InventoryItemResponseData(
    @SerializedName("inventory_ids") val inventoryIds: List<Int>
)

data class InventoryItem(
    @SerializedName("inventory_id") val inventoryId: Int,
    @SerializedName("item_name") val itemName: String,
    val unit: String,
    @SerializedName("display_unit") val displayUnit: String,
    @SerializedName("quantity_in_stock") val quantityInStock: Int,
    @SerializedName("location_id") val locationId: Int
)

data class InventoryItemUpdateRequest(
    @SerializedName("item_name") val itemName: String,
    val unit: String,
    @SerializedName("display_unit") val displayUnit: String,
    @SerializedName("quantity_in_stock") val quantityInStock: Int,
    @SerializedName("location_id") val locationId: Int
)

data class QuantityUpdateRequest(
    @SerializedName("quantity_in_stock") val quantityInStock: Int
)

data class EventInventoryItem(
    @SerializedName("inventory_id") val inventoryId: Int,
    @SerializedName("item_name") val itemName: String,
    val quantity: Int,
    val unit: String,
    @SerializedName("display_unit") val displayUnit: String,
    @SerializedName("location_id") val locationId: Int
)