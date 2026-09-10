package com.chefmagic.airfryer

import android.content.Context
import org.json.JSONArray

object RecentlyViewedManager {
    private const val PREFS = "recently_viewed_prefs"
    private const val KEY = "recently_viewed_files"
    private const val MAX_ENTRIES = 15

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getRecentFiles(context: Context): List<String> {
        val raw = prefs(context).getString(KEY, null) ?: return emptyList()
        val arr = JSONArray(raw)
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) list.add(arr.getString(i))
        return list
    }

    fun recordView(context: Context, file: String) {
        val current = getRecentFiles(context).toMutableList()
        current.remove(file)
        current.add(0, file)
        while (current.size > MAX_ENTRIES) current.removeAt(current.lastIndex)
        prefs(context).edit().putString(KEY, JSONArray(current).toString()).apply()
    }
}
