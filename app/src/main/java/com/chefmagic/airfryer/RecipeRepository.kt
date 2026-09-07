package com.chefmagic.airfryer

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object RecipeRepository {

    private var cachedSections: List<Section>? = null

    fun loadSections(context: Context): List<Section> {
        cachedSections?.let { return it }

        val json = context.assets.open("recipes.json").use { input ->
            BufferedReader(InputStreamReader(input, "UTF-8")).readText()
        }

        val root = JSONObject(json)
        val sectionsArray = root.getJSONArray("sections")
        val sections = mutableListOf<Section>()

        for (i in 0 until sectionsArray.length()) {
            val sObj = sectionsArray.getJSONObject(i)
            val name = sObj.getString("name")
            val recipesArray = sObj.getJSONArray("recipes")
            val recipes = mutableListOf<Recipe>()
            for (j in 0 until recipesArray.length()) {
                val rObj = recipesArray.getJSONObject(j)
                recipes.add(
                    Recipe(
                        title = rObj.getString("title"),
                        nonveg = rObj.optBoolean("nonveg", false),
                        image = if (rObj.isNull("image")) null else rObj.getString("image"),
                        file = rObj.getString("file"),
                        section = name
                    )
                )
            }
            sections.add(Section(name, recipes))
        }

        cachedSections = sections
        return sections
    }

    fun allRecipes(context: Context): List<Recipe> =
        loadSections(context).flatMap { it.recipes }
}
