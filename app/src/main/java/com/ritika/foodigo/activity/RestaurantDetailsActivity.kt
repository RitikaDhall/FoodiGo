package com.ritika.foodigo.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
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
import com.ritika.foodigo.adapter.DetailsRecyclerAdapter
import com.ritika.foodigo.database.ItemDatabase
import com.ritika.foodigo.model.Item
import com.ritika.foodigo.util.ConnectionManager

class RestaurantDetailsActivity : AppCompatActivity() {

    lateinit var recyclerDetails: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: DetailsRecyclerAdapter

    val itemList = arrayListOf<Item>()

    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var toolbar: Toolbar
    lateinit var btnGoToCart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_details)

        recyclerDetails = findViewById(R.id.recyclerDetails)
        layoutManager = LinearLayoutManager(this@RestaurantDetailsActivity)

        progressLayout = findViewById(R.id.progressLayoutDet)
        progressBar = findViewById(R.id.progressBarDet)

        toolbar = findViewById(R.id.detToolbar)
        btnGoToCart = findViewById(R.id.btnGoToCart)

        progressLayout.visibility = View.VISIBLE

        if (DBAsyncTask(this@RestaurantDetailsActivity, 2).execute().get())
            btnGoToCart.visibility = View.GONE
        else
            btnGoToCart.visibility = View.VISIBLE

        val restaurantId = intent.getStringExtra("restaurantId")
        val restaurantName = intent.getStringExtra("restaurantName")

        setUpToolbar(toolbar)
        supportActionBar?.title = restaurantName

        val queue = Volley.newRequestQueue(this@RestaurantDetailsActivity)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/${restaurantId}"

        if (ConnectionManager().checkConnectivity(this@RestaurantDetailsActivity)) {

            val jsonRequest = object: JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {

                try {
                    progressLayout.visibility = View.GONE

                    val mainData = it.getJSONObject("data")
                    val success = mainData.getBoolean("success")
                    if (success) {

                        val data = mainData.getJSONArray("data")
                        for (i in 0 until data.length()) {
                            val itemJsonObject = data.getJSONObject(i)
                            val itemObject = Item (
                                itemJsonObject.getInt("id"),
                                itemJsonObject.getString("name"),
                                itemJsonObject.getString("cost_for_one"),
                                itemJsonObject.getString("restaurant_id")
                            )
                            itemList.add(itemObject)

                        }
                        recyclerAdapter = DetailsRecyclerAdapter(this@RestaurantDetailsActivity, itemList, object: DetailsRecyclerAdapter.OnItemClickListener{
                            override fun cartCheck() {
                                if (DBAsyncTask(this@RestaurantDetailsActivity, 2).execute().get())
                                    btnGoToCart.visibility = View.GONE
                                else
                                    btnGoToCart.visibility = View.VISIBLE
                            }
                        })

                        recyclerDetails.adapter = recyclerAdapter
                        recyclerDetails.layoutManager = layoutManager


                    } else {
                        Toast.makeText(this@RestaurantDetailsActivity, "Some error occurred", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@RestaurantDetailsActivity, "Catch error occurred", Toast.LENGTH_SHORT).show()

                }

            }, Response.ErrorListener {
                Toast.makeText(this@RestaurantDetailsActivity, "Volley error occurred", Toast.LENGTH_SHORT).show()

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
            val dialog = AlertDialog.Builder(this@RestaurantDetailsActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") {text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit") {text, listener ->
                ActivityCompat.finishAffinity(this@RestaurantDetailsActivity)
            }
            dialog.create()
            dialog.show()
        }

        btnGoToCart.setOnClickListener {
            val intent = Intent(this@RestaurantDetailsActivity, CartActivity::class.java)
            intent.putExtra("restaurantName", restaurantName)
            intent.putExtra("restaurantId", restaurantId)
            startActivity(intent)
        }

    }

    fun setUpToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Toolbar Title"
    }

    override fun onBackPressed() {

        if (!DBAsyncTask(this@RestaurantDetailsActivity, 2).execute().get()) {
            val dialog = android.app.AlertDialog.Builder(this@RestaurantDetailsActivity)
            dialog.setTitle("Confirmation")
            dialog.setMessage("Leaving the page will empty all items from the cart. Are you sure you want to leave?")

            dialog.setPositiveButton("Yes") { text, listener ->
                DBAsyncTask(this@RestaurantDetailsActivity, 1).execute()
                val intent = Intent(this@RestaurantDetailsActivity, MainActivity::class.java)
                startActivity(intent)
            }

            dialog.setNegativeButton("Cancel") {text, listener ->

            }

            dialog.create()
            dialog.show()
        } else {
            super.onBackPressed()
            finish()
        }

    }


    class DBAsyncTask(val context: Context, val mode: Int): AsyncTask<Void, Void, Boolean>() {
        val db: ItemDatabase = Room.databaseBuilder(context, ItemDatabase::class.java, "items-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    db.itemDao().emptyCart()
                    db.close()
                }
                2 -> {
                    val cart = db.itemDao().getAllItems()
                    val size = cart.size
                    db.close()
                    return size == 0
                }
            }
            return false
        }

    }

}