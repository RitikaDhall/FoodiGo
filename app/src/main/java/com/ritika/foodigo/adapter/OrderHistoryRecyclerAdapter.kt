package com.ritika.foodigo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ritika.foodigo.R
import com.ritika.foodigo.model.Order
import com.ritika.foodigo.model.OrderItem

class OrderHistoryRecyclerAdapter(val context: Context, val order: ArrayList<Order>) : RecyclerView.Adapter<OrderHistoryRecyclerAdapter.OrderHistoryViewHolder>() {

    class OrderHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtRestaurantName: TextView = view.findViewById(R.id.txtOHRestaurantName)
        val txtDate: TextView = view.findViewById(R.id.txtOHDate)
        val recyclerItems: RecyclerView = view.findViewById(R.id.recyclerItems)
        lateinit var recyclerAdapter: OrderHistoryItemRecyclerAdapter
        lateinit var layoutManager: RecyclerView.LayoutManager
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_order_history, parent, false)
        return OrderHistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return order.size
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {

        val item = order[position]

        holder.txtRestaurantName.text = item.restaurantName
        holder.txtDate.text = item.time

        val orderItemList = ArrayList<OrderItem>()
        for (i in 0 until item.food.length()) {
            val foodJson = item.food.getJSONObject(i)
            orderItemList.add(
                OrderItem(
                    foodJson.getString("food_item_id"),
                    foodJson.getString("name"),
                    foodJson.getString("cost")
                )
            )
        }
        holder.recyclerAdapter = OrderHistoryItemRecyclerAdapter(context, orderItemList)
        holder.layoutManager = LinearLayoutManager(context)
        holder.recyclerItems.adapter = holder.recyclerAdapter
        holder.recyclerItems.layoutManager = holder.layoutManager

    }
}