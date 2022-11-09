package com.ritika.foodigo.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.navigation.NavigationView
import com.ritika.foodigo.R
import com.ritika.foodigo.fragment.HomeFragment

class OrderPlacedActivity : AppCompatActivity() {

    lateinit var btnOrderPlaced: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_placed)
        supportActionBar?.hide()

        btnOrderPlaced = findViewById(R.id.btnOrderPlaced)

        btnOrderPlaced.setOnClickListener {
            val intent = Intent (this@OrderPlacedActivity, MainActivity::class.java)
            startActivity(intent)
        }

    }

}
