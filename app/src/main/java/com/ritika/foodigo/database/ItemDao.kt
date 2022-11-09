package com.ritika.foodigo.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ItemDao {

    @Insert
    fun insertItem(itemEntity: ItemEntity)

    @Delete
    fun deleteItem(itemEntity: ItemEntity)

    @Query("SELECT * FROM items")
    fun getAllItems(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE item_id = :itemId")
    fun getItemById(itemId: String): ItemEntity

    @Query("DELETE FROM items")
    fun emptyCart()

}