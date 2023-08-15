package com.example.fishingapplication

data class MarkerData(

    val title: String? =null,
    val description : String?=null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rating: Double?=0.0,
    val numOfUsersRated : Double?=0.0,
    val sumOfRatings : Double?=0.0,
    val commonSpecie : String?=null,
    val imageMarker:String?=null,
    val user: User?=null,
    val createdAtUtc : Long?=null

)

