package com.autgroup.s2025.w201.todo.classes

import java.io.Serializable

data class PlaceInfo(
    val name: String? = null,
    val address: String? = null,
    val rating: Double? = null,
    val openStatus: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val reviews: List<Review>? = null,
    val priceLevel: Int? = null,
    val completed: Boolean = false,
    var firebaseKey: String? = null
) : Serializable

