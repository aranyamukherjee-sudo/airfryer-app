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

    /**
     * Returns a practical shopping-list name without changing the recipe ingredient display.
     *
     * Uses the structured ingredient name and removes common preparation/serving
     * instructions while preserving meaningful descriptors such as "red chilli powder",
     * "chicken breast", "low-fat paneer", etc.
     */
    fun shoppingListName(ingredient: Ingredient): String {
        var name = ingredient.name.trim()

        // Remove serving/use instructions from the end.
        name = name.replace(
            Regex(
                """,?\s*(?:to taste|to garnish|for garnish|for serving|to serve|for frying|for cooking|for tempering|to finish|for greasing|for coating|for the stuffing|for a smoky finish|as needed|divided|optional)(?:\s*[,;].*)?(?:\s*\([^)]*\))?\s*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove parenthesized "optional" markers while preserving meaningful
        // descriptors such as "(sev)", "(haldi)", or "(plain flour)".
        name = name.replace(
            Regex("""\s*\(\s*optional\s*\)""", RegexOption.IGNORE_CASE),
            ""
        ).trim()

        // Remove leading size descriptors.
        name = name.replace(
            Regex(
                """^(?:a\s+few|few|a\s+small|small|a\s+medium|medium|a\s+large|large|extra-large|extra large)\s+""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove preparation words wherever they occur, but keep the
        // ingredient that follows them.
        // Examples:
        // "chopped onion" -> "onion"
        // "lemon & chopped coriander" -> "lemon & coriander"
        // "Farsan (sev), chopped onion, lemon & pav" -> "Farsan (sev), onion, lemon & pav"
        name = name.replace(
            Regex(
                """\b(?:finely|roughly|very finely|thinly|thickly|lightly)\s+(?=(?:chopped|cubed|diced|minced|sliced|slit|grated|beaten|mashed|crushed|pureed|shredded|julienned|boiled|peeled)\b)""",
                RegexOption.IGNORE_CASE
            ),
            ""
        )

        name = name.replace(
            Regex(
                """\b(?:chopped|cubed|diced|minced|sliced|slit|grated|beaten|mashed|crushed|pureed|shredded|julienned|boiled|peeled)\s+(?=[A-Za-z])""",
                RegexOption.IGNORE_CASE
            ),
            ""
        )

        // Remove preparation words that remain at the end.
        // Examples: "onion, chopped" -> "onion".
        name = name.replace(
            Regex(
                """,?\s*(?:finely|roughly|very finely|thinly|thickly|lightly)?\s*(?:chopped|cubed|diced|minced|sliced|slit|grated|beaten|mashed|crushed|pureed|shredded|julienned|boiled|peeled)\s*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove complete trailing preparation chains.
        // Examples:
        // "potatoes, boiled & mashed" -> "potatoes"
        // "arbi, boiled, peeled and halved" -> "arbi"
        // "chicken, minced or finely chopped" -> "chicken"
        // "baby potatoes, peeled and pricked" -> "baby potatoes"
        // "bell peppers, tops cut & seeded" -> "bell peppers"
        name = name.replace(
            Regex(
                """,?\s*(?:(?:finely|roughly|very finely|thinly|thickly|lightly)\s+)?(?:chopped|cubed|diced|minced|sliced|slit|grated|beaten|mashed|crushed|pureed|shredded|julienned|boiled|peeled|halved|quartered|trimmed|deveined|seeded|cored|pricked|tops\s+cut)(?:\s*(?:,|and|or|&)\s*(?:(?:finely|roughly|very finely|thinly|thickly|lightly)\s+)?(?:chopped|cubed|diced|minced|sliced|slit|grated|beaten|mashed|crushed|pureed|shredded|julienned|boiled|peeled|halved|quartered|trimmed|deveined|seeded|cored|pricked|tops\s+cut))*\s*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove trailing adjustment/purpose notes.
        // Examples:
        // "red chilli flakes (adjust to taste)" -> "red chilli flakes"
        // "ginger (adrak), julienned (plus extra to garnish)" -> preparation cleanup can finish the name
        name = name.replace(
            Regex(
                """\s*\(\s*(?:adjust\s+to\s+taste|plus\s+extra\s+to\s+garnish|plus\s+extra\s+for\s+garnish)[^)]*\)""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove parenthesized usage/purpose instructions.
        // Keep meaningful ingredient translations/descriptors.
        name = name.replace(
            Regex(
                """\s*\(\s*(?:for|to)\s+(?:frying|cooking|tempering|serving|garnish|coating|greasing|dredging|dusting|skewering|the crust|the stuffing|the batter|the slurry|the filling|a smoky finish|extra crispness|colour|color)[^)]*\)""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove a preparation word left behind before a dangling connector.
        // Example: "potatoes, boiled &" -> "potatoes".
        name = name.replace(
            Regex(
                """,?\s*(?:chopped|cubed|diced|minced|sliced|slit|grated|beaten|mashed|crushed|pureed|shredded|julienned|boiled|peeled|halved|quartered|trimmed|deveined|seeded|cored|pricked|tops\s+cut)\s*(?:&|and|or)\s*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove dangling connectors left after preparation cleanup.
        // Examples: "potatoes, boiled &" -> "potatoes",
        //           "eggs, boiled and" -> "eggs".
        name = name.replace(
            Regex(
                """,?\s*(?:&|and|or)\s*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove preparation words that became exposed after connector cleanup.
        name = name.replace(
            Regex(
                """,?\s*(?:finely|roughly|very finely|thinly|thickly|lightly)?\s*(?:chopped|cubed|diced|minced|sliced|slit|grated|beaten|mashed|crushed|pureed|shredded|julienned|boiled|peeled|halved|quartered|trimmed|deveined|seeded|cored|pricked|tops\s+cut)\s*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove a second dangling connector if cleanup exposed one.
        name = name.replace(
            Regex(
                """,?\s*(?:&|and|or)\s*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove trailing "cut into..." preparation instructions.
        name = name.replace(
            Regex(
                """,?\s*(?:cut into|cut in|sliced into|peeled and cut into)\s+[^,;&]+(?=\s*(?:,|&|$))""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove common "for X" clauses that remain after cleanup.
        name = name.replace(
            Regex(
                """,?\s+(?:for|plus extra)\s+(?:both methods|the crust|coating|greasing|skewering|a smoky finish|serving|garnish|the stuffing|cooking|frying|tempering|the batter|the slurry|the filling|to serve).*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Clean punctuation and repeated whitespace without altering
        // meaningful descriptors, alternatives, or compound ingredients.
        name = name
            .replace(Regex("""\s{2,}"""), " ")
            .replace(Regex("""\s*,\s*,"""), ",")
            .replace(Regex("""\s*&\s*"""), " & ")
            .trim(' ', ',', '-', ':')

        return name.replaceFirstChar { it.uppercase() }
    }

    /**
     * Returns true for ingredients that are normally not useful as
     * standalone shopping-list items.
     *
     * Word-boundary matching avoids excluding real ingredients such as
     * coconut water or water chestnuts.
     */
    fun isShoppingListExcluded(ingredient: Ingredient): Boolean {
        val name = shoppingListName(ingredient).lowercase().trim()

        return name == "water" ||
            name == "ice" ||
            name == "ice cubes" ||
            name == "ice-cold water" ||
            name.matches(Regex("""water\s+(?:as needed|for|to|for the)\b.*"""))
    }

    private data class CategoryGroup(val keywords: List<String>, val color: String, val displayName: String)

    private val CATEGORY_GROUPS = listOf(
        CategoryGroup(listOf("paneer", "curd", "yogurt", "dahi", "milk", "cream", "cheese", "khoya", "malai"), "#F3E4C8", "Dairy"),
        CategoryGroup(listOf("ghee", "butter", "oil", "vinegar", "sauce", "water", "honey"), "#F6D77A", "Pantry"),
        CategoryGroup(listOf("chicken", "mutton", "fish", "prawn", "egg", "soya", "tofu", "dal", "lentil", "chana", "rajma", "moong"), "#D9A279", "Protein"),
        CategoryGroup(listOf("onion", "tomato", "potato", "capsicum", "bell pepper", "carrot", "peas", "cauliflower", "spinach", "mushroom",
               "cabbage", "beans", "okra", "bhindi", "brinjal", "cucumber", "gourd", "pumpkin", "chilli", "chili"), "#A8C79A", "Vegetables"),
        CategoryGroup(listOf("chilli powder", "chili powder", "turmeric", "garam masala", "cumin", "coriander", "salt", "pepper", "masala",
               "cardamom", "cinnamon", "dalchini", "clove", "spice"), "#E29B7D", "Spices"),
        CategoryGroup(listOf("flour", "maida", "besan", "sooji", "rava", "rice", "atta", "cornflour", "breadcrumbs"), "#E8DCC4", "Pantry"),
        CategoryGroup(listOf("cashew", "almond", "raisin", "pistachio", "walnut", "dates", "coconut"), "#C9A66B", "Pantry"),
        CategoryGroup(listOf("sugar", "jaggery", "gud", "gur", "chocolate"), "#E8B4A8", "Pantry")
    )

    /** True if `keyword` occurs in `text` starting at a word boundary (string start or a
     *  non-letter before it). This stops short keywords like "oil" from matching inside an
     *  unrelated word such as "boiled" or "foil", while still allowing suffixed matches like
     *  "potato" inside "potatoes". */
    private fun keywordMatches(text: String, keyword: String): Boolean {
        var startIndex = text.indexOf(keyword)
        while (startIndex != -1) {
            val precedingChar = if (startIndex == 0) null else text[startIndex - 1]
            if (precedingChar == null || !precedingChar.isLetter()) return true
            startIndex = text.indexOf(keyword, startIndex + 1)
        }
        return false
    }

    /** Finds the best category group for an ingredient name: the group owning the longest
     *  matching keyword wins, so a more specific keyword (e.g. "chilli powder") beats a
     *  shorter, more general one (e.g. "chilli") regardless of group order. */
    private fun matchGroup(nameLower: String): CategoryGroup? {
        var bestGroup: CategoryGroup? = null
        var bestLength = -1
        for (group in CATEGORY_GROUPS) {
            for (keyword in group.keywords) {
                if (keyword.length > bestLength && keywordMatches(nameLower, keyword)) {
                    bestGroup = group
                    bestLength = keyword.length
                }
            }
        }
        return bestGroup
    }

    /** Returns a soft category color for the ingredient's swatch, based on keyword matching. */
    fun categoryColor(ingredient: Ingredient): String {
        return matchGroup(ingredient.name.lowercase())?.color ?: "#D8D0C0"
    }

    /** Returns a shopping-list category name for the ingredient, based on the same keyword groups. */
    fun categoryName(ingredient: Ingredient): String {
        return matchGroup(ingredient.name.lowercase())?.displayName ?: "Other"
    }
}
