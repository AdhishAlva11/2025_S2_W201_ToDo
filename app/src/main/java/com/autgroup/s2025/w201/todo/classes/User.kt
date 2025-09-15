package com.autgroup.s2025.w201.todo.classes

class User(
    var userFirstName: String? = null,
    var userLastName: String? = null,
    var email: String? = null,               // new: store email
    var photoUrl: String? = null,            // new: store Google photo (optional)
    var userFavourities: Favourities? = null,
    var userItineraries: List<Activity>? = null
)
