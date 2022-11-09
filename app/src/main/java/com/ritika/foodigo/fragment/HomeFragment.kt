package com.ritika.foodigo.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ritika.foodigo.R
import com.ritika.foodigo.adapter.HomeRecyclerAdapter
import com.ritika.foodigo.model.Restaurant
import com.ritika.foodigo.util.ConnectionManager
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class HomeFragment : Fragment() {

    lateinit var recyclerHome: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: HomeRecyclerAdapter

    val restaurantList = arrayListOf<Restaurant>()

    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    val costComparator = Comparator<Restaurant> { Restaurant1, Restaurant2 ->
        if (Restaurant1.restaurantPrice.compareTo(Restaurant2.restaurantPrice, true) == 0) {
            Restaurant1.restaurantName.compareTo(Restaurant2.restaurantName, true)
        } else {
            Restaurant1.restaurantPrice.compareTo(Restaurant2.restaurantPrice, true)
        }
    }

    val ratingComparator = Comparator<Restaurant> { Restaurant1, Restaurant2 ->
        if (Restaurant1.restaurantRating.compareTo(Restaurant2.restaurantRating, true) == 0) {
            Restaurant1.restaurantName.compareTo(Restaurant2.restaurantName, true)
        } else {
            Restaurant1.restaurantRating.compareTo(Restaurant2.restaurantRating, true)
        }
    }

    val nameComparator = Comparator<Restaurant> { Restaurant1, Restaurant2 ->
        if (Restaurant1.restaurantName.compareTo(Restaurant2.restaurantName, true) == 0) {
            Restaurant1.restaurantRating.compareTo(Restaurant2.restaurantRating, true)
        } else {
            Restaurant1.restaurantName.compareTo(Restaurant2.restaurantName, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        setHasOptionsMenu(true)

        recyclerHome = view.findViewById(R.id.recyclerHome)
        layoutManager = LinearLayoutManager(activity)

        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)

        progressLayout.visibility = View.VISIBLE

        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest = object: JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {

                // Handle the response
                try {

                    progressLayout.visibility = View.GONE

                    val mainData = it.getJSONObject("data")
                    val success = mainData.getBoolean("success")

                    if (success) {

                        val data = mainData.getJSONArray("data")
                        for (i in 0 until data.length()) {
                            val restaurantJsonObject = data.getJSONObject(i)
                            val restaurantObject = Restaurant (
                                restaurantJsonObject.getInt("id"),
                                restaurantJsonObject.getString("name"),
                                restaurantJsonObject.getString("rating"),
                                restaurantJsonObject.getString("cost_for_one"),
                                restaurantJsonObject.getString("image_url")
                            )
                            restaurantList.add(restaurantObject)

                        }

                        recyclerAdapter = HomeRecyclerAdapter(activity as Context, restaurantList)

                        recyclerHome.adapter = recyclerAdapter
                        recyclerHome.layoutManager = layoutManager

                    } else {
                        Toast.makeText(activity as Context, "Some error occurred!", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: JSONException) {
                    Toast.makeText(activity as Context, "Some unexpected error occurred!", Toast.LENGTH_SHORT).show()
                }

            }, Response.ErrorListener {

                // Handle the errors
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sort, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item?.itemId

        when (id) {

            R.id.sortCostLow -> {
                Collections.sort(restaurantList, costComparator)
            }

            R.id.sortCostHigh -> {
                Collections.sort(restaurantList, costComparator)
                restaurantList.reverse()
            }

            R.id.sortRating -> {
                Collections.sort(restaurantList, ratingComparator)
                restaurantList.reverse()
            }

            R.id.sortName -> {
                Collections.sort(restaurantList, nameComparator)
            }

        }
        recyclerAdapter.notifyDataSetChanged()
        return super.onOptionsItemSelected(item)

    }

}
