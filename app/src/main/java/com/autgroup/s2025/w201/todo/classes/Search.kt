package com.autgroup.s2025.w201.todo.classes

import com.google.android.libraries.places.api.model.Place

data class Search(
    val placeName: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val interests: List<String> = emptyList()
) {
    companion object {
        // Factory to build a Search from a Place and checkboxes
        fun fromPlaceAndInterests(place: Place?, selectedInterests: List<String>): Search {
            return Search(
                placeName = place?.name,
                lat = place?.latLng?.latitude,
                lng = place?.latLng?.longitude,
                interests = selectedInterests
            )
        }
    }

    fun hasInterest(interest: String): Boolean {
        return interests.contains(interest)
    }

    fun isLocationBased(): Boolean {
        return lat != null && lng != null
    }
}