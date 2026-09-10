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

                val steps = mutableListOf<Step>()
                val stepsArray = rObj.optJSONArray("steps")
                if (stepsArray != null) {
                    for (k in 0 until stepsArray.length()) {
                        val stepObj = stepsArray.getJSONObject(k)
                        val duration = if (stepObj.isNull("duration_seconds")) null
                            else stepObj.optInt("duration_seconds")
                        steps.add(Step(text = stepObj.getString("text"), durationSeconds = duration))
                    }
                }

                val appliances = mutableListOf<String>()
                val appliancesArray = rObj.optJSONArray("appliances")
                if (appliancesArray != null) {
                    for (k in 0 until appliancesArray.length()) {
                        appliances.add(appliancesArray.getString(k))
                    }
                }

                val ingredients = mutableListOf<Ingredient>()
                val ingredientsArray = rObj.optJSONArray("ingredients")
                if (ingredientsArray != null) {
                    for (k in 0 until ingredientsArray.length()) {
                        val iObj = ingredientsArray.getJSONObject(k)
                        ingredients.add(
                            Ingredient(
                                raw = iObj.getString("raw"),
                                qty = if (iObj.isNull("qty")) null else iObj.optDouble("qty"),
                                qty2 = if (iObj.isNull("qty2")) null else iObj.optDouble("qty2"),
                                unit = if (iObj.isNull("unit")) null else iObj.getString("unit"),
                                name = iObj.getString("name"),
                                scalable = iObj.optBoolean("scalable", false)
                            )
                        )
                    }
                }

                recipes.add(
                    Recipe(
                        title = rObj.getString("title"),
                        nonveg = rObj.optBoolean("nonveg", false),
                        image = if (rObj.isNull("image")) null else rObj.getString("image"),
                        file = rObj.getString("file"),
                        section = name,
                        search = rObj.optString("search", ""),
                        steps = steps,
                        appliances = appliances,
                        totalDurationSeconds = rObj.optInt("total_duration_seconds", 0),
                        highProtein = rObj.optBoolean("high_protein", false),
                        ingredients = ingredients,
                        originalServings = rObj.optInt("original_servings", 2),
                        tip = if (rObj.isNull("tip")) null else rObj.optString("tip", null),
                        kcalPerServing = if (rObj.isNull("kcal_per_serving")) null else rObj.optInt("kcal_per_serving"),
                        servingsConfident = rObj.optBoolean("servings_confident", true)
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
