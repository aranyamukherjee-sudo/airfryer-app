package com.chefmagic.airfryer

data class Step(
    val text: String,
    val durationSeconds: Int?
)

data class Ingredient(
    val raw: String,
    val qty: Double?,
    val qty2: Double?,
    val unit: String?,
    val name: String,
    val scalable: Boolean
)

data class Recipe(
    val title: String,
    val nonveg: Boolean,
    val image: String?,
    val file: String,
    val section: String,
    val search: String,
    val steps: List<Step> = emptyList(),
    val appliances: List<String> = emptyList(),
    val totalDurationSeconds: Int = 0,
    val highProtein: Boolean = false,
    val ingredients: List<Ingredient> = emptyList(),
    val originalServings: Int = 2,
    val tip: String? = null
)

data class Section(
    val name: String,
    val recipes: List<Recipe>
)

// Items shown in the RecyclerView: either a section header or a recipe row.
sealed class ListItem {
    data class Header(val name: String) : ListItem()
    data class RecipeRow(val recipe: Recipe) : ListItem()
}
