package com.chefmagic.airfryer

import android.content.Context

object FavoritesManager {
    private const val PREFS = "favorites_prefs"
    private const val KEY = "favorite_files"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isFavorite(context: Context, file: String): Boolean =
        prefs(context).getStringSet(KEY, emptySet())?.contains(file) == true

    fun toggleFavorite(context: Context, file: String): Boolean {
        val p = prefs(context)
        val current = HashSet(p.getStringSet(KEY, emptySet()) ?: emptySet())
        val nowFavorite: Boolean
        if (current.contains(file)) {
            current.remove(file)
            nowFavorite = false
        } else {
            current.add(file)
            nowFavorite = true
        }
        p.edit().putStringSet(KEY, current).apply()
        return nowFavorite
    }

    fun favoriteFiles(context: Context): Set<String> =
        prefs(context).getStringSet(KEY, emptySet()) ?: emptySet()
}
