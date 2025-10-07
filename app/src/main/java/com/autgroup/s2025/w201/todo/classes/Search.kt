package com.autgroup.s2025.w201.todo.classes

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import java.io.Serializable

data class Search(
    val placeName: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val interests: List<String> = emptyList(),
    val radius: Int = 5000 // default radius in meters
) : Serializable {

    companion object {
        fun fromPlaceAndInterests(place: Place?, selectedInterests: List<String>, radius: Int = 5000): Search {
            val latLng = place?.latLng ?: LatLng(0.0, 0.0)
            return Search(
                placeName = place?.name,
                lat = latLng.latitude,
                lng = latLng.longitude,
                interests = selectedInterests,
                radius = radius
            )
        }
    }

    fun hasInterest(interest: String): Boolean = interests.contains(interest)

    fun isLocationBased(): Boolean = lat != null && lng != null
}