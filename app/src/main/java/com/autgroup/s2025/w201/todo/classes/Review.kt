package com.autgroup.s2025.w201.todo.classes

import java.io.Serializable

data class Review(
    val authorName: String? = null,
    val rating: Double? = null,
    val time: String? = null,
    val text: String? = null
) : Serializable