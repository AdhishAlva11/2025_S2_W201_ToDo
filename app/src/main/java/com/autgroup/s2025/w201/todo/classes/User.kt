package com.autgroup.s2025.w201.todo.classes

class User (
    var userFirstName: String? = null,
    var userLastName: String? = null,
    var userFavourities: Favourities?,
    var userItineraries: List<Activity>?
)
