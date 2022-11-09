package com.ritika.foodigo.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ritika.foodigo.R
import com.ritika.foodigo.adapter.OrderHistoryRecyclerAdapter
import com.ritika.foodigo.model.Order
import com.ritika.foodigo.util.ConnectionManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class OrderHistoryFragment : Fragment() {

    lateinit var recyclerOrderHistory: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: OrderHistoryRecyclerAdapter

    val orderList = arrayListOf<Order>()

    lateinit var sharedPreferences: SharedPreferences
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    lateinit var noOrderLayout: RelativeLayout
    lateinit var txtNoOrder: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)

        recyclerOrderHistory = view.findViewById(R.id.recyclerOrderHistory)
        layoutManager = LinearLayoutManager(activity)

        noOrderLayout = view.findViewById(R.id.noOrderLayout)
        txtNoOrder = view.findViewById(R.id.txtNoOrder)
        progressLayout = view.findViewById(R.id.progressLayoutOH)
        progressBar = view.findViewById(R.id.progressBarOH)

        progressLayout.visibility = View.VISIBLE
        noOrderLayout.visibility = View.GONE
        txtNoOrder.visibility = View.GONE

        sharedPreferences = context!!.getSharedPreferences("Foodigo Preferences", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("UserId", null)

        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/orders/fetch_result/${userId}"

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest = object: JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {

                try {

                    progressLayout.visibility = View.GONE

                    val mainData = it.getJSONObject("data")
                    val success = mainData.getBoolean("success")

                    if (success) {

                        val data = mainData.getJSONArray("data")
                        if (data.length() == 0) {
                            noOrderLayout.visibility = View.VISIBLE
                            txtNoOrder.visibility = View.VISIBLE
                        } else {
                            for (i in 0 until data.length()) {
                                val orderJsonObject = data.getJSONObject(i)
                                val time = orderJsonObject.getString("order_placed_at")
                                val orderObject = Order (
                                    orderJsonObject.getString("order_id"),
                                    orderJsonObject.getString("restaurant_name"),
                                    orderJsonObject.getString("total_cost"),
                                    formatDate(time),
                                    orderJsonObject.getJSONArray("food_items")
                                )
                                orderList.add(orderObject)

                            }
                            recyclerAdapter = OrderHistoryRecyclerAdapter(activity as Context, orderList)

                            recyclerOrderHistory.adapter = recyclerAdapter
                            recyclerOrderHistory.layoutManager = layoutManager
                        }

                    } else {
                        Toast.makeText(activity as Context, "Some error occurred!", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(activity as Context, "Catch error occurred!", Toast.LENGTH_SHORT).show()
                }

            }, Response.ErrorListener {
                Toast.makeText(activity as Context, "Volley error occurred!", Toast.LENGTH_SHORT).show()

            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "3e0843894b9247"
                    return headers
                }

            }
            queue.add (jsonObjectRequest)

        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") {text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") {text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }

        return view
    }

    private fun formatDate(dateString: String): String {
        val inputFormatter = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.ENGLISH)
        val date: Date = inputFormatter.parse(dateString) as Date

        val outputFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        return outputFormatter.format(date)
    }

}