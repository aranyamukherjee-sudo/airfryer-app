package com.chefmagic.airfryer

import android.content.Context
import org.json.JSONArray

object RecentSearchesManager {
    private const val PREFS = "recent_searches_prefs"
    private const val KEY = "recent_searches"
    private const val MAX_ENTRIES = 8

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getRecent(context: Context): List<String> {
        val raw = prefs(context).getString(KEY, null) ?: return emptyList()
        val arr = JSONArray(raw)
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) list.add(arr.getString(i))
        return list
    }

    fun addSearch(context: Context, term: String) {
        val trimmed = term.trim()
        if (trimmed.length < 2) return

        val current = getRecent(context).toMutableList()
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        current.add(0, trimmed)
        while (current.size > MAX_ENTRIES) current.removeAt(current.lastIndex)

        prefs(context).edit().putString(KEY, JSONArray(current).toString()).apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY).apply()
    }
}
