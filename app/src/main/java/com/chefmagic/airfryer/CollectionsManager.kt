package com.chefmagic.airfryer

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object CollectionsManager {
    private const val PREFS = "collections_prefs"
    private const val KEY = "collections_json"

    private val defaultCollections = listOf(
        "Quick & Easy", "Weekend Cooking", "Healthy", "Party Snacks", "Family Favorites"
    )

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun readAll(context: Context): LinkedHashMap<String, MutableSet<String>> {
        val raw = prefs(context).getString(KEY, null)
        val map = LinkedHashMap<String, MutableSet<String>>()

        if (raw == null) {
            // First run: seed the suggested collections, all empty.
            defaultCollections.forEach { map[it] = mutableSetOf() }
            return map
        }

        val obj = JSONObject(raw)
        val keys = obj.keys()
        while (keys.hasNext()) {
            val name = keys.next()
            val arr = obj.getJSONArray(name)
            val files = mutableSetOf<String>()
            for (i in 0 until arr.length()) files.add(arr.getString(i))
            map[name] = files
        }
        return map
    }

    private fun writeAll(context: Context, map: Map<String, Set<String>>) {
        val obj = JSONObject()
        for ((name, files) in map) {
            obj.put(name, JSONArray(files.toList()))
        }
        prefs(context).edit().putString(KEY, obj.toString()).apply()
    }

    fun allCollectionNames(context: Context): List<String> = readAll(context).keys.toList()

    fun recipesInCollection(context: Context, name: String): Set<String> =
        readAll(context)[name] ?: emptySet()

    fun isInCollection(context: Context, name: String, file: String): Boolean =
        readAll(context)[name]?.contains(file) == true

    fun createCollection(context: Context, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val all = readAll(context)
        if (!all.containsKey(trimmed)) {
            all[trimmed] = mutableSetOf()
            writeAll(context, all)
        }
    }

    fun toggleRecipeInCollection(context: Context, collectionName: String, file: String) {
        val all = readAll(context)
        val set = all.getOrPut(collectionName) { mutableSetOf() }
        if (set.contains(file)) set.remove(file) else set.add(file)
        writeAll(context, all)
    }

    fun deleteCollection(context: Context, name: String) {
        val all = readAll(context)
        all.remove(name)
        writeAll(context, all)
    }
}
