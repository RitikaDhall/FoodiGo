package com.ritika.foodigo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ritika.foodigo.R
import com.ritika.foodigo.model.Item

class CartRecyclerAdapter(val context: Context, val itemList: ArrayList<Item>): RecyclerView.Adapter<CartRecyclerAdapter.CartViewHolder>() {

    class CartViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtItemName: TextView = view.findViewById(R.id.txtCartItemName)
        val txtItemPrice: TextView = view.findViewById(R.id.txtCartItemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_cart_single_row, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = itemList[position]
        holder.txtItemName.text = item.itemName
        holder.txtItemPrice.text = "₹ ${item.itemPrice}"

    }
}