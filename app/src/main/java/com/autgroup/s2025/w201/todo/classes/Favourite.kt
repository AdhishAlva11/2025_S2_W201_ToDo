package com.autgroup.s2025.w201.todo.classes

data class Favourite(
    val title: String = "",
    val address: String = "",
    val openHours: String = "",
    val starRating: Double = 0.0,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)
