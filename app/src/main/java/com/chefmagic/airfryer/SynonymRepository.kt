package com.chefmagic.airfryer

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object SynonymRepository {

    private var cache: Map<String, List<String>>? = null

    fun load(context: Context): Map<String, List<String>> {
        cache?.let { return it }

        val json = context.assets.open("synonyms.json").use { input ->
            BufferedReader(InputStreamReader(input, "UTF-8")).readText()
        }

        val root = JSONObject(json)
        val map = mutableMapOf<String, List<String>>()
        val keys = root.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val arr = root.getJSONArray(key)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            map[key] = list
        }

        cache = map
        return map
    }

    /**
     * Given a raw (already-lowercased, trimmed) query, returns the set of terms
     * to search for: the query itself, plus any Hinglish/English equivalents.
     * Works for single words and short multi-word phrases (e.g. "hari mirch").
     */
    fun expand(context: Context, query: String): Set<String> {
        if (query.isEmpty()) return emptySet()
        val map = load(context)
        val terms = mutableSetOf(query)
        map[query]?.let { terms.addAll(it) }
        return terms
    }
}
