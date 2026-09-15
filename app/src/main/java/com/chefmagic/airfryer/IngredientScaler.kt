package com.chefmagic.airfryer

import kotlin.math.abs
import kotlin.math.floor

object IngredientScaler {

    private val FRACTION_GLYPHS = listOf(
        0.0 to "",
        1.0 / 8 to "⅛",
        1.0 / 4 to "¼",
        1.0 / 3 to "⅓",
        3.0 / 8 to "⅜",
        1.0 / 2 to "½",
        5.0 / 8 to "⅝",
        2.0 / 3 to "⅔",
        3.0 / 4 to "¾",
        7.0 / 8 to "⅞",
        1.0 to ""
    )

    /** Formats a decimal quantity as a kitchen-friendly whole + fraction string, e.g. 1.5 -> "1½". */
    fun formatQuantity(value: Double): String {
        if (value <= 0.0) return "0"

        val whole = floor(value).toInt()
        val frac = value - whole

        val nearest = FRACTION_GLYPHS.minByOrNull { abs(it.first - frac) }!!

        return when {
            nearest.first == 0.0 -> if (whole == 0) "0" else whole.toString()
            nearest.first == 1.0 -> (whole + 1).toString()
            whole == 0 -> nearest.second
            else -> "$whole${nearest.second}"
        }
    }

    /** Returns the display text for an ingredient at the given selected/original serving ratio. */
    fun scaledText(ingredient: Ingredient, selectedServings: Int, originalServings: Int): String {
        if (!ingredient.scalable || ingredient.qty == null) return ingredient.raw

        val factor = selectedServings.toDouble() / originalServings.toDouble()
        val scaledQty = ingredient.qty * factor
        val qtyStr = formatQuantity(scaledQty)
        val unitPart = if (ingredient.unit != null) " ${ingredient.unit}" else ""

        return if (ingredient.qty2 != null) {
            val qty2Str = formatQuantity(ingredient.qty2 * factor)
            "$qtyStr–$qty2Str$unitPart ${ingredient.name}".trim()
        } else {
            "$qtyStr$unitPart ${ingredient.name}".trim()
        }
    }

    /** Returns just the quantity+unit portion (for right-aligned display), or "" if not scalable. */
    fun scaledQuantityOnly(ingredient: Ingredient, selectedServings: Int, originalServings: Int): String {
        if (!ingredient.scalable || ingredient.qty == null) return ""

        val factor = selectedServings.toDouble() / originalServings.toDouble()
        val qtyStr = formatQuantity(ingredient.qty * factor)
        val unitPart = if (ingredient.unit != null) " ${ingredient.unit}" else ""

        return if (ingredient.qty2 != null) {
            val qty2Str = formatQuantity(ingredient.qty2 * factor)
            "$qtyStr–$qty2Str$unitPart"
        } else {
            "$qtyStr$unitPart"
        }
    }

    /** Returns the left-side display name: raw text if non-scalable, else just the ingredient name. */
    fun displayName(ingredient: Ingredient): String {
        return if (!ingredient.scalable || ingredient.qty == null) ingredient.raw else ingredient.name
    }

    private data class CategoryGroup(val keywords: List<String>, val color: String, val displayName: String)

    private val CATEGORY_GROUPS = listOf(
        CategoryGroup(listOf("paneer", "curd", "yogurt", "dahi", "milk", "cream", "cheese", "khoya", "malai"), "#F3E4C8", "Dairy"),
        CategoryGroup(listOf("ghee", "butter", "oil", "vinegar", "sauce", "water", "honey"), "#F6D77A", "Pantry"),
        CategoryGroup(listOf("chicken", "mutton", "fish", "prawn", "egg", "soya", "tofu", "dal", "lentil", "chana", "rajma", "moong"), "#D9A279", "Protein"),
        CategoryGroup(listOf("onion", "tomato", "potato", "capsicum", "carrot", "peas", "cauliflower", "spinach", "mushroom",
               "cabbage", "beans", "okra", "bhindi", "brinjal", "cucumber", "gourd", "pumpkin", "chilli", "chili"), "#A8C79A", "Vegetables"),
        CategoryGroup(listOf("chilli powder", "turmeric", "garam masala", "cumin", "coriander", "salt", "pepper", "masala",
               "cardamom", "cinnamon", "clove", "spice"), "#E29B7D", "Spices"),
        CategoryGroup(listOf("flour", "maida", "besan", "sooji", "rava", "rice", "atta", "cornflour", "breadcrumbs"), "#E8DCC4", "Pantry"),
        CategoryGroup(listOf("cashew", "almond", "raisin", "pistachio", "walnut", "dates", "coconut"), "#C9A66B", "Pantry"),
        CategoryGroup(listOf("sugar", "jaggery", "gud", "gur", "chocolate"), "#E8B4A8", "Pantry")
    )

    /** Returns a soft category color for the ingredient's swatch, based on simple keyword matching. */
    fun categoryColor(ingredient: Ingredient): String {
        val nameLower = ingredient.name.lowercase()
        for (group in CATEGORY_GROUPS) {
            if (group.keywords.any { nameLower.contains(it) }) return group.color
        }
        return "#D8D0C0"
    }

    /** Returns a shopping-list category name for the ingredient, based on the same keyword groups. */
    fun categoryName(ingredient: Ingredient): String {
        val nameLower = ingredient.name.lowercase()
        for (group in CATEGORY_GROUPS) {
            if (group.keywords.any { nameLower.contains(it) }) return group.displayName
        }
        return "Other"
    }
}
