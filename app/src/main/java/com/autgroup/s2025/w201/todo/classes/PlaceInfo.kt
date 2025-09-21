package com.autgroup.s2025.w201.todo.classes

data class PlaceInfo(
    val name: String,
    val address: String,
    val rating: Double,
    val openStatus: String,
    val lat: Double,
    val lng: Double
)