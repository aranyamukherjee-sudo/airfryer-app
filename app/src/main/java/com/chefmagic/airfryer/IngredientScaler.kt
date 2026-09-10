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
}
