package com.ritika.foodigo.model

import org.json.JSONArray

data class Order (
    val order_id: String,
    val restaurantName: String,
    val totalCost: String,
    val time: String,
    val food: JSONArray
)