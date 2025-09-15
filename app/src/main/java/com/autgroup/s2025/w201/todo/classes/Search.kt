package com.autgroup.s2025.w201.todo.classes

import com.google.android.libraries.places.api.model.Place
import java.io.Serializable

data class Search(
    val placeName: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val interests: List<String> = emptyList()
) : Serializable {
    companion object {
        fun fromPlaceAndInterests(place: Place?, selectedInterests: List<String>): Search {
            return Search(
                placeName = place?.name,
                lat = place?.latLng?.latitude,
                lng = place?.latLng?.longitude,
                interests = selectedInterests
            )
        }
    }

    fun hasInterest(interest: String): Boolean = interests.contains(interest)
    fun isLocationBased(): Boolean = lat != null && lng != null
}