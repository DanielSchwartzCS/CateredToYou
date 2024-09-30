package com.example.cateredtoyou
import com.example.cateredtoyou.R
import java.util.Date

data class Event(
    val name: String,
    val date: Date,
    val location: String,
    val status: EventStatus,
    val clientName: String,
    val expectedGuests: Int
) {
    enum class EventStatus {
        PLANNED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    override fun toString(): String {
        return "$name - $date - $status"
    }
}