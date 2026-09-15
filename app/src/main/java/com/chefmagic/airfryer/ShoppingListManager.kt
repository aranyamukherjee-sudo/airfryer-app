package com.chefmagic.airfryer

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ShoppingListItem(
    val id: String,
    val name: String,
    val quantity: String,
    val category: String,
    val checked: Boolean
)

object ShoppingListManager {
    private const val PREFS = "shopping_list_prefs"
    private const val KEY = "shopping_list_items"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getAll(context: Context): List<ShoppingListItem> {
        val raw = prefs(context).getString(KEY, null) ?: return emptyList()
        val arr = JSONArray(raw)
        val list = mutableListOf<ShoppingListItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                ShoppingListItem(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    quantity = obj.optString("quantity", ""),
                    category = obj.optString("category", "Other"),
                    checked = obj.optBoolean("checked", false)
                )
            )
        }
        return list
    }

    private fun saveAll(context: Context, items: List<ShoppingListItem>) {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("quantity", item.quantity)
            obj.put("category", item.category)
            obj.put("checked", item.checked)
            arr.put(obj)
        }
        prefs(context).edit().putString(KEY, arr.toString()).apply()
    }

    fun addItems(context: Context, newItems: List<Triple<String, String, String>>) {
        // Triple = (name, quantity, category)
        val current = getAll(context).toMutableList()
        for ((name, quantity, category) in newItems) {
            current.add(ShoppingListItem(UUID.randomUUID().toString(), name, quantity, category, false))
        }
        saveAll(context, current)
    }

    fun toggleChecked(context: Context, id: String) {
        val current = getAll(context).map {
            if (it.id == id) it.copy(checked = !it.checked) else it
        }
        saveAll(context, current)
    }

    fun removeItem(context: Context, id: String) {
        val current = getAll(context).filter { it.id != id }
        saveAll(context, current)
    }

    fun clearAll(context: Context) {
        saveAll(context, emptyList())
    }
}
