package com.example.happy_places_app.models

data class HappyPlaceModel (
    val id : Int,
    val title : String,
    val image : String,
    val description: String,
    val date : String,
    val location : String,
    val latitude : Double,
    val longitude : Double
    )