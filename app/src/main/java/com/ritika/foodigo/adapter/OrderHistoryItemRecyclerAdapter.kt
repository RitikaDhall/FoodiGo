package com.ritika.foodigo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ritika.foodigo.R
import com.ritika.foodigo.model.OrderItem

class OrderHistoryItemRecyclerAdapter(context: Context, val orderList: ArrayList<OrderItem>): RecyclerView.Adapter<OrderHistoryItemRecyclerAdapter.OrderHistoryItemViewHolder>() {

    class OrderHistoryItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtItemName: TextView = view.findViewById(R.id.txtCartItemName)
        val txtItemCost: TextView = view.findViewById(R.id.txtCartItemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_cart_single_row, parent, false)
        return OrderHistoryItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onBindViewHolder(holder: OrderHistoryItemViewHolder, position: Int) {
        val item = orderList[position]
        holder.txtItemName.text = item.itemName
        holder.txtItemCost.text = "₹ ${item.itemCost}"
    }

}