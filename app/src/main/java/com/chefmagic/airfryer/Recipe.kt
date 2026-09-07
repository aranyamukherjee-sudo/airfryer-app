package com.chefmagic.airfryer

data class Recipe(
    val title: String,
    val nonveg: Boolean,
    val image: String?,
    val file: String,
    val section: String
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
