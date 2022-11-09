package com.ritika.foodigo.adapter

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.ritika.foodigo.R
import com.ritika.foodigo.activity.RestaurantDetailsActivity
import com.ritika.foodigo.database.RestaurantDatabase
import com.ritika.foodigo.database.RestaurantEntity
import com.ritika.foodigo.model.Restaurant
import com.squareup.picasso.Picasso

class HomeRecyclerAdapter(val context: Context, val itemList: ArrayList<Restaurant>): RecyclerView.Adapter<HomeRecyclerAdapter.HomeViewHolder>() {

    class HomeViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtRestaurantPrice: TextView = view.findViewById(R.id.txtRestaurantPrice)
        val txtRestaurantRating: TextView = view.findViewById(R.id.txtRestaurantRating)
        val imgRestaurantHome: ImageView = view.findViewById(R.id.imgRestaurantHome)
        val imgBtnFavourite: ImageButton = view.findViewById(R.id.imgBtnFavourite)

        val llContent: LinearLayout = view.findViewById(R.id.llContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_home_single_row, parent, false)
        return HomeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        val restaurant = itemList[position]
        holder.txtRestaurantName.text = restaurant.restaurantName
        holder.txtRestaurantPrice.text = "\u20B9 ${restaurant.restaurantPrice} / person"
        holder.txtRestaurantRating.text = restaurant.restaurantRating
        Picasso.get().load(restaurant.restaurantImage).error(R.drawable.restaurant_default).into(holder.imgRestaurantHome)

        holder.llContent.setOnClickListener {

            val intent = Intent(context, RestaurantDetailsActivity::class.java)
            intent.putExtra("restaurantId", restaurant.restaurantId.toString())
            intent.putExtra("restaurantName", restaurant.restaurantName)
            startActivity(context, intent, null)

        }


        val listOfFavourites = GetAllFavAsyncTask(context).execute().get()

        if (listOfFavourites.isNotEmpty() && listOfFavourites.contains(restaurant.restaurantId.toString())) {
            holder.imgBtnFavourite.setImageResource(R.drawable.btn_favourite_checked)
        } else {
            holder.imgBtnFavourite.setImageResource(R.drawable.btn_favourite_default)
        }

        holder.imgBtnFavourite.setOnClickListener {

            val restaurantEntity = RestaurantEntity(
                restaurant.restaurantId,
                restaurant.restaurantName,
                restaurant.restaurantRating,
                restaurant.restaurantPrice,
                restaurant.restaurantImage
            )

            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {

                val async = DBAsyncTask(context, restaurantEntity, 2).execute()
                val result = async.get()
                if (result) {
                    Toast.makeText(context, "Added to favourites", Toast.LENGTH_SHORT).show()
                    holder.imgBtnFavourite.setImageResource(R.drawable.btn_favourite_checked)
                } else {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }

            } else {

                val async = DBAsyncTask(context, restaurantEntity, 3).execute()
                val result = async.get()

                if (result) {
                    Toast.makeText(context, "Removed from favourites", Toast.LENGTH_SHORT).show()
                    holder.imgBtnFavourite.setImageResource(R.drawable.btn_favourite_default)
                } else {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }

            }

        }

    }

    class DBAsyncTask(val context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) : AsyncTask<Void, Void, Boolean>() {

        /*
        Mode 1 -> Check DB if restaurant is fav or not
        Mode 2 -> Save the restaurant into DB as fav
        Mode 3 -> Remove the fav restaurant
        */

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {

                1 -> {
                    // Check DB if restaurant is fav or not
                    val restaurant: RestaurantEntity? = db.restaurantDao().getRestaurantById(restaurantEntity.restaurant_id.toString())
                    db.close()
                    return restaurant != null
                }

                2 -> {
                    // Save the restaurant into DB as fav
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }

                3 -> {
                    // Remove the fav restaurant
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }

            }
            return false
        }

    }

    class GetAllFavAsyncTask(context: Context) : AsyncTask<Void, Void, List<String>>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants-db").build()

        override fun doInBackground(vararg params: Void?): List<String> {
            val list = db.restaurantDao().getAllRestaurants()
            val listOfIds = arrayListOf<String>()
            for (i in list) {
                listOfIds.add(i.restaurant_id.toString())
            }
            return listOfIds
        }


    }

}