package com.ritika.foodigo.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.ritika.foodigo.R
import com.ritika.foodigo.adapter.HomeRecyclerAdapter
import com.ritika.foodigo.database.RestaurantDatabase
import com.ritika.foodigo.database.RestaurantEntity
import com.ritika.foodigo.model.Restaurant

class FavouritesFragment : Fragment() {

    lateinit var recyclerRestaurant: RecyclerView
    lateinit var homeRecyclerAdapter: HomeRecyclerAdapter
    lateinit var rlLoading: RelativeLayout
    lateinit var rlFav: RelativeLayout
    lateinit var rlNoFav: RelativeLayout
    var restaurantList = arrayListOf<Restaurant>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        rlFav = view.findViewById(R.id.rlFavorites)
        rlNoFav = view.findViewById(R.id.rlNoFavorites)
        rlLoading = view.findViewById(R.id.rlLoading)
        rlLoading.visibility = View.VISIBLE
        setUpRecycler(view)

        return view
    }

    fun setUpRecycler(view: View) {
        recyclerRestaurant = view.findViewById(R.id.recyclerRestaurants)

        val backgroundList = FavouritesAsync(activity as Context).execute().get()
        if (backgroundList.isEmpty()) {
            rlLoading.visibility = View.GONE
            rlFav.visibility = View.GONE
            rlNoFav.visibility = View.VISIBLE
        } else {
            rlFav.visibility = View.VISIBLE
            rlLoading.visibility = View.GONE
            rlNoFav.visibility = View.GONE
            for (i in backgroundList) {
                restaurantList.add(
                    Restaurant(
                        i.restaurant_id,
                        i.restaurantName,
                        i.restaurantRating,
                        i.restaurantPrice,
                        i.restaurantImage
                    )
                )
            }

            homeRecyclerAdapter = HomeRecyclerAdapter(activity as Context, restaurantList)
            val mLayoutManager = LinearLayoutManager(activity)
            recyclerRestaurant.layoutManager = mLayoutManager
            recyclerRestaurant.itemAnimator = DefaultItemAnimator()
            recyclerRestaurant.adapter = homeRecyclerAdapter
            recyclerRestaurant.setHasFixedSize(true)

        }
    }


    class FavouritesAsync(context: Context) : AsyncTask<Void, Void, List<RestaurantEntity>>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants-db").build()

        override fun doInBackground(vararg params: Void?): List<RestaurantEntity> {

            return db.restaurantDao().getAllRestaurants()
        }

    }

}
