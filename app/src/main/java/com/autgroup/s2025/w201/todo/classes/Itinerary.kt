package com.autgroup.s2025.w201.todo.classes

import java.util.Date

class Itinerary (
    val itineraryID: String,
    val itineraryName: String,
    val itineraryDataRange: Date
)
{
    //Reference to a list of Activity objects
    private val itineraryActivities: MutableList<Activity> = mutableListOf()

    //Add an activity to the itinerary
    fun addActivity(activity: Activity){
        itineraryActivities.add(activity)
    }

    //Remove an activity by its ID
    fun removeActivity(activityID: String): Boolean {
        val iterator = itineraryActivities.iterator()
        while (iterator.hasNext()) {
            val activity = iterator.next()
            if (activity.activityID == activityID) {
                iterator.remove()
                return true
            }
        }
        return false
    }

    //Return a copy of the activities list
    fun getItineraryActivities(): List<Activity> {
        return itineraryActivities.toList()
    }
}