package com.ritika.foodigo.adapter

import android.content.Context
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.ritika.foodigo.R
import com.ritika.foodigo.database.ItemDatabase
import com.ritika.foodigo.database.ItemEntity
import com.ritika.foodigo.model.Item

class DetailsRecyclerAdapter(val context: Context, val itemList: ArrayList<Item>, val listener: OnItemClickListener): RecyclerView.Adapter<DetailsRecyclerAdapter.DetailsViewHolder>() {

    class DetailsViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtSerialNumber: TextView = view.findViewById(R.id.txtSerialNumber)
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtItemPrice: TextView = view.findViewById(R.id.txtItemPrice)
        val btnAddToCart: Button = view.findViewById(R.id.btnAddToCart)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_details_single_row, parent, false)
        return DetailsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    interface OnItemClickListener {
        fun cartCheck()
    }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        val item = itemList[position]
        holder.txtSerialNumber.text = (position + 1).toString()
        holder.txtItemName.text = item.itemName
        holder.txtItemPrice.text = "₹ ${item.itemPrice}"

        val listOfCartItems = GetAllCartAsyncTask(context).execute().get()

        if (listOfCartItems.isNotEmpty() && listOfCartItems.contains(item.itemId.toString())) {
            val removeCartColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
            holder.btnAddToCart.setBackgroundColor(removeCartColor)
            holder.btnAddToCart.text = "Remove"

        } else {
            val addCartColor = ContextCompat.getColor(context, R.color.colorPrimary)
            holder.btnAddToCart.setBackgroundColor(addCartColor)
            holder.btnAddToCart.text = "Add"

        }

        holder.btnAddToCart.setOnClickListener {

            val  itemEntity = ItemEntity (
                item.itemId,
                item.itemName,
                item.itemPrice,
                item.restaurantId
            )

            if (!DBAsyncTask(context, itemEntity, 1).execute().get()) {

                val async = DBAsyncTask(context, itemEntity, 2).execute()
                val result = async.get()
                if (result) {

                    Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                    val removeCartColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
                    holder.btnAddToCart.setBackgroundColor(removeCartColor)
                    holder.btnAddToCart.text = "Remove"
                    listener.cartCheck()

                } else {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }

            } else {

                val async = DBAsyncTask(context, itemEntity, 3).execute()
                val result =  async.get()

                if (result) {

                    Toast.makeText(context, "Removed from cart", Toast.LENGTH_SHORT).show()
                    val addCartColor = ContextCompat.getColor(context, R.color.colorPrimary)
                    holder.btnAddToCart.setBackgroundColor(addCartColor)
                    holder.btnAddToCart.text = "Add"
                    listener.cartCheck()

                } else {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }

            }

        }

    }

    class DBAsyncTask(val context: Context, val itemEntity: ItemEntity, val mode: Int) : AsyncTask<Void, Void, Boolean>() {

        /*
       Mode 1 -> Check DB if item is in cart or not
       Mode 2 -> Save the item into DB as cart
       Mode 3 -> Remove the cart item
       */

        val db = Room.databaseBuilder(context, ItemDatabase::class.java, "items-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {

                1 -> {
                    // Check DB if item is in cart or not
                    val item: ItemEntity? = db.itemDao().getItemById(itemEntity.item_id.toString())
                    db.close()
                    return item != null
                }

                2 -> {
                    // Save the item into DB as cart
                    db.itemDao().insertItem(itemEntity)
                    db.close()
                    return true
                }

                3 -> {
                    // Remove the cart item
                    db.itemDao().deleteItem(itemEntity)
                    db.close()
                    return true
                }

            }
            return false

        }

    }

    class GetAllCartAsyncTask(context: Context) : AsyncTask<Void, Void, List<String>>() {

        val db = Room.databaseBuilder(context, ItemDatabase::class.java, "items-db").build()

        override fun doInBackground(vararg params: Void?): List<String> {
            val list = db.itemDao().getAllItems()
            val listOfIds = arrayListOf<String>()
            for (i in list) {
                listOfIds.add(i.item_id.toString())
            }
            return listOfIds
        }

    }

}