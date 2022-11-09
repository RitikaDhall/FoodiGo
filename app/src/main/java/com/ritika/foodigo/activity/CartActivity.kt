package com.ritika.foodigo.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ritika.foodigo.R
import com.ritika.foodigo.adapter.CartRecyclerAdapter
import com.ritika.foodigo.database.ItemDatabase
import com.ritika.foodigo.database.ItemEntity
import com.ritika.foodigo.model.Item
import com.ritika.foodigo.util.ConnectionManager
import org.json.JSONArray
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    lateinit var recyclerItems: RecyclerView
    lateinit var recyclerAdapter: CartRecyclerAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var toolbar: Toolbar

    lateinit var sharedPreferences: SharedPreferences
    lateinit var restaurantId: String
    lateinit var restaurantName: String

    lateinit var rlCart: RelativeLayout
    lateinit var btnPlaceOrder: Button
    lateinit var txtCartRestaurantName: TextView

    var itemList = arrayListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        recyclerItems = findViewById(R.id.recyclerItems)
        layoutManager = LinearLayoutManager(this@CartActivity)

        toolbar = findViewById(R.id.cartToolbar)
        setUpToolbar(toolbar)

        if (intent != null) {
            restaurantId = intent.getStringExtra("restaurantId")!!
            restaurantName = intent.getStringExtra("restaurantName")!!
        }

        supportActionBar?.title = restaurantName

        rlCart = findViewById(R.id.rlCart)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        txtCartRestaurantName = findViewById(R.id.txtCartRestaurantName)

        txtCartRestaurantName.text = "Ordering from: ${restaurantName}"

        sharedPreferences = getSharedPreferences("Foodigo Preferences", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("UserId", "null")

        var total = 0
        val itemArray = JSONArray()
        val backgroundList = CartAsync(this@CartActivity).execute().get()
        for (i in backgroundList) {
            itemList.add(
                Item(
                    i.item_id,
                    i.itemName,
                    i.itemPrice,
                    i.restaurantId
                )
            )
            recyclerAdapter = CartRecyclerAdapter(this@CartActivity, itemList)
            recyclerItems.adapter = recyclerAdapter
            recyclerItems.layoutManager = layoutManager

            total += i.itemPrice.toInt()

            val item = JSONObject()
            item.put("food_item_id", i.item_id.toString())
            itemArray.put(item)
        }

        val queue = Volley.newRequestQueue(this@CartActivity)
        val url = "http://13.235.250.119/v2/place_order/fetch_result/"

        val jsonParams = JSONObject()
        jsonParams.put("user_id", userId)
        jsonParams.put("restaurant_id", restaurantId)
        jsonParams.put("total_cost", total)
        jsonParams.put("food", itemArray)

        btnPlaceOrder.text = "Place Order (Total: ₹ ${total})"

        btnPlaceOrder.setOnClickListener {

            if (ConnectionManager().checkConnectivity(this@CartActivity)) {

                val jsonRequest = object: JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {

                    try {

                        val mainData = it.getJSONObject("data")
                        val success = mainData.getBoolean("success")

                        if (success) {

                            ClearAsync(this@CartActivity).execute()
                            val intent = Intent(this@CartActivity, OrderPlacedActivity::class.java)
                            startActivity(intent)

                        } else {
                            Toast.makeText(this@CartActivity, "Some error occurred", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(this@CartActivity, "Catch error occurred", Toast.LENGTH_SHORT).show()
                    }

                }, Response.ErrorListener {
                    Toast.makeText(this@CartActivity, "Volley error occurred", Toast.LENGTH_SHORT).show()

                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "3e0843894b9247"
                        return headers
                    }
                }
                queue.add(jsonRequest)

            } else {
                // Internet not found
                val dialog = AlertDialog.Builder(this@CartActivity)
                dialog.setTitle("Error")
                dialog.setMessage("Internet Connection Is Not Found")

                dialog.setPositiveButton("Open settings") { text, listener ->
                    val settingsIntent = Intent (Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }

                dialog.setNegativeButton("Exit") { text, listener ->
                    ActivityCompat.finishAffinity(this@CartActivity)
                }
                dialog.create()
                dialog.show()
            }

        }

    }

    fun setUpToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Toolbar Title"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    class CartAsync(context: Context) : AsyncTask<Void, Void, List<ItemEntity>>() {
        val db = Room.databaseBuilder(context, ItemDatabase::class.java, "items-db").build()
        override fun doInBackground(vararg params: Void?): List<ItemEntity> {
            return db.itemDao().getAllItems()
        }
    }


    class ClearAsync(context: Context) : AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, ItemDatabase::class.java, "items-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            db.itemDao().emptyCart()
            db.close()
            return true
        }
    }

}