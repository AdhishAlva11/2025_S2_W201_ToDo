package com.autgroup.s2025.w201.todo.classes

import com.google.android.libraries.places.api.model.Review
import java.io.Serializable

data class PlaceInfo(
    val name: String? = null,
    val address: String? = null,
    val rating: Double? = null,
    val openStatus: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val reviews: List<Review>? = null
) : Serializable