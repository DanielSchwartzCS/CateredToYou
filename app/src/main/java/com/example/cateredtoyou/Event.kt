package com.example.cateredtoyou

import java.util.Date

data class Event(
    val name: String,
    val date: Date,
    val startTime: String,
    val endTime: String,
    val location: String,
    val status: EventStatus,
    val client: Client,
    val expectedGuests: Int,
    val menu: List<MenuItem> = emptyList(),
    val staffAssigned: List<Staff> = emptyList(),
    val equipmentNeeded: List<Equipment> = emptyList()
) {
    enum class EventStatus {
        PLANNED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    override fun toString(): String {
        return "$name - ${date.toFormattedString()} $startTime-$endTime - $status"
    }

    private fun Date.toFormattedString(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(this)
    }
}

data class Client(
    val id: Int,
    val firstname: String,
    val lastname: String,
    val email: String,
    val phonenumber: String
) {
    override fun toString(): String {
        return "$firstname $lastname"
    }
}

data class Staff(
    val id: Int,
    val name: String,
    val role: String
) {
    override fun toString(): String = "$name ($role)"
}

data class Equipment(
    val id: Int,
    val name: String,
    val quantity: Int
) {
    override fun toString(): String = "$name (Qty: $quantity)"
}

data class MenuItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val category: String
) {
    override fun toString(): String = "$name - $${String.format("%.2f", price)}"
}