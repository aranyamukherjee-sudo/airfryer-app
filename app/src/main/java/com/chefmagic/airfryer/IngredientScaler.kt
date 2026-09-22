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

        /*
         * Shopping-list normalization only.
         *
         * The recipe ingredient text is never changed. This function removes
         * preparation/serving instructions while preserving meaningful
         * ingredient descriptors and useful parenthetical information.
         */

        // Remove trailing serving/use instructions.
        name = name.replace(
            Regex(
                """,?\s*(?:to taste|to garnish|for garnish|for serving|to serve|for frying|for cooking|for tempering|to finish|for greasing|for coating|for the stuffing|for a smoky finish|as needed|divided|optional)(?:\s*[,;].*)?\s*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove trailing adjustment/purpose notes in parentheses.
        name = name.replace(
            Regex(
                """\s*\(\s*(?:adjust\s+to\s+taste|plus\s+extra\s+(?:to|for)\s+garnish)[^)]*\)""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove parenthesized usage/purpose notes, but keep ingredient
        // descriptors/translations such as "(sev)", "(haldi)", "(dhania)".
        name = name.replace(
            Regex(
                """\s*\(\s*(?:for|to)\s+(?:frying|cooking|tempering|serving|garnish|coating|greasing|dredging|dusting|skewering|the crust|the stuffing|the batter|the slurry|the filling|a smoky finish|extra crispness|colour|color)[^)]*\)""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove standalone optional markers.
        name = name.replace(
            Regex("""\s*\(\s*optional\s*\)""", RegexOption.IGNORE_CASE),
            ""
        ).trim()

        // Remove leading size/quantity-style descriptors that aren't part
        // of the ingredient identity.
        name = name.replace(
            Regex(
                """^(?:a\s+few|few|a\s+small|small|a\s+medium|medium|a\s+large|large|extra-large|extra\s+large)\s+""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove quantity/preparation fragments such as:
        // "garlic, 1 minced + 1 whole for rubbing" -> "garlic"
        name = name.replace(
            Regex(
                """\s*,?\s*\d+\s+(?:minced|chopped|diced|sliced|cubed|grated|crushed|peeled|whole)(?:\s*\+\s*\d+\s+(?:minced|chopped|diced|sliced|cubed|grated|crushed|peeled|whole))*\s*(?:for\s+[^,)]*)?""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove em-dash quantity/preparation fragments:
        // "onions — 1 sliced, 1 chopped" -> "onions"
        name = name.replace(
            Regex("""\s+—\s+\d+.*$"""),
            ""
        ).trim()

        // Preparation vocabulary used by the normalization rules.
        val prep =
            """(?:(?:finely|roughly|very\s+finely|thinly|thickly|lightly)\s+)?""" +
            """(?:chopped|cubed|diced|minced|sliced|slit|grated|beaten|mashed|""" +
            """crushed|pureed|shredded|julienned|boiled|peeled|pasted|halved|quartered|""" +
            """trimmed|deveined|seeded|cored|pricked|par-boiled|parboiled|cooked|""" +
            """tops\s+cut)"""

        val prepChain = "(?:roasted|$prep)"

        // Remove preparation words before an ingredient:
        // "chopped onion" -> "onion"
        // "minced/shredded cooked chicken" -> "chicken"
        name = name.replace(
            Regex(
                """^(?:(?:$prep)(?:\s*/\s*(?:$prep))*)\s+""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove complete preparation chains after a comma.
        // Examples:
        // "boneless chicken, finely shredded or minced" -> "boneless chicken"
        // "garlic (lehsun), finely grated or crushed" -> "garlic (lehsun)"
        // "eggs, boiled and peeled" -> "eggs"
        //
        // Treat connected preparation words as one preparation chain.
        name = name.replace(
            Regex(
                """,?\s*$prepChain(?:\s+(?:and|or)\s+$prepChain)+\s*(?=\(|$)""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Handle preparation words after a comma/connector when they precede
        // another ingredient:
        // "lemon & chopped coriander" -> "lemon & coriander"
        // "Farsan (sev), chopped onion, lemon & pav" -> "Farsan (sev), onion, lemon & pav"
        name = name.replace(
            Regex(
                """(?<=[,&])\s*(?:$prep)\s+(?!(?:and|or)\s+$prep\b)(?=[A-Za-z])""",
                RegexOption.IGNORE_CASE
            ),
            " "
        ).trim()

        // Remove preparation words after "and" when they describe
        // the following ingredient.
        // "butter and chopped coriander" -> "butter and coriander"
        // "sesame seeds and sliced spring onion" -> "sesame seeds and spring onion"
        name = name.replace(
            Regex(
                """(\band\s+)(?:$prep)\s+(?=[A-Za-z])""",
                RegexOption.IGNORE_CASE
            ),
            "$1"
        ).trim()

        // Remove trailing preparation chains:
        // "potatoes, boiled & mashed" -> "potatoes"
        // "arbi, boiled, peeled and halved" -> "arbi"
        // "bell peppers, tops cut & seeded" -> "bell peppers"
        name = name.replace(
            Regex(
                """,?\s*$prep(?:\s*(?:,|&|and|or)\s*$prep)*\s*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove "cut into..." preparation instructions.
        name = name.replace(
            Regex(
                """,?\s*(?:peeled\s+and\s+)?(?:cut\s+into|cut\s+in|sliced\s+into)\s+[^,;&()]+(?=\s*(?:,|&|$|\())""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove preparation chains immediately before a retained parenthetical.
        // This preserves useful information such as:
        // "prawns, peeled and deveined (tails on)" -> "prawns (tails on)"
        // "apples, thinly sliced (core removed)" -> "apples (core removed)"
        name = name.replace(
            Regex(
                """,?\s*$prep(?:\s+(?:and|or)\s+$prep)*\s+(?=\()""",
                RegexOption.IGNORE_CASE
            ),
            " "
        ).trim()

        // Remove trailing handling notes that are clearly preparation-only.
        name = name.replace(
            Regex(
                """,?\s*(?:greens\s+and\s+whites\s+separated|whites\s+and\s+greens\s+separated|core\s+removed|tops\s+removed)\s*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove a trailing cooking state/duration.
        name = name.replace(
            Regex(
                """,?\s*(?:par-boiled|parboiled|boiled|cooked)(?:\s+(?:for\s+)?\d+\s*(?:min|mins|minutes|sec|secs|seconds))?\s*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove dangling preparation connectors.
        name = name.replace(
            Regex(""",?\s*(?:&|and|or)\s*$""", RegexOption.IGNORE_CASE),
            ""
        ).trim()

        // Remove optional/purpose parentheticals that may have become exposed
        // after preparation cleanup.
        name = name.replace(
            Regex(
                """\s*\(\s*(?:optional|for\s+(?:skewering|serving|garnish|cooking|frying|tempering|coating|greasing)|to\s+(?:garnish|serve))[^)]*\)""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Final cleanup for preparation/quantity fragments that can remain
        // after the main normalization rules.

        // Remove leading preparation/state words.
        // Example: "minced/shredded cooked chicken" -> "chicken"
        name = name.replace(
            Regex(
                """^(?:(?:finely|roughly|very\s+finely|thinly|thickly|lightly)\s+)?(?:chopped|cubed|diced|minced|sliced|slit|grated|beaten|mashed|crushed|pureed|shredded|julienned|boiled|peeled|halved|quartered|trimmed|deveined|seeded|cored|pricked|par-boiled|parboiled|cooked)(?:\s*/\s*(?:chopped|cubed|diced|minced|sliced|slit|grated|beaten|mashed|crushed|pureed|shredded|julienned|boiled|peeled|cooked))*\s+""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove em-dash quantity/preparation suffixes.
        // Example: "onions — 1 sliced, 1 chopped" -> "onions"
        name = name.replace(
            Regex("""\s+—.*$"""),
            ""
        ).trim()

        // Remove trailing preparation + handling notes.
        // Example: "spring onions, chopped, greens and whites separated"
        // -> "spring onions"
        name = name.replace(
            Regex(
                """,?\s*(?:finely|roughly|very\s+finely|thinly|thickly|lightly)?\s*(?:chopped|cubed|diced|minced|sliced|slit|grated|beaten|mashed|crushed|pureed|shredded|julienned|boiled|peeled|halved|quartered|trimmed|deveined|seeded|cored|pricked|cooked)(?:\s*,\s*(?:greens\s+and\s+whites|whites\s+and\s+greens)\s+separated)?\s*$""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove preparation modifiers left before a retained parenthetical.
        // Example: "apples, thinly sliced (core removed)"
        // -> "apples (core removed)"
        name = name.replace(
            Regex(
                """,?\s*(?:finely|roughly|very\s+finely|thinly|thickly|lightly)\s+(?:sliced|chopped|cubed|diced|minced|grated|shredded|julienned|crushed|mashed|boiled|peeled|cooked)\s*(?=\()""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove any remaining standalone preparation modifier before a
        // retained parenthetical.
        // Example: "apples, thinly (core removed)" -> "apples (core removed)"
        name = name.replace(
            Regex(
                """,?\s*(?:finely|roughly|very\s+finely|thinly|thickly|lightly)\s*(?=\()""",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()

        // Remove commas immediately before retained parentheticals.
        // Example: "prawns, (tails on)" -> "prawns (tails on)"
        name = name.replace(
            Regex(""",\s*(?=\()"""),
            " "
        ).trim()

        // Remove an orphan opening parenthesis left by an optional/purpose note.
        name = name.replace(
            Regex("""\s*\($"""),
            ""
        ).trim()

        // Final parenthetical punctuation cleanup.
        // Example: "prawns, (tails on)" -> "prawns (tails on)"
        name = name.replace(
            Regex(""",\s*(?=\()"""),
            " "
        ).trim()

        // Normalize spacing/punctuation without changing meaningful
        // ingredient descriptors or parenthetical content.
        name = name
            .replace(Regex("""\s{2,}"""), " ")
            .replace(Regex("""\s*,\s*,"""), ",")
            .replace(Regex("""\s*&\s*"""), " & ")
            .replace(Regex("""\s*\(\s*"""), " (")
            .replace(Regex("""\s*\)"""), ")")
            .replace(Regex("""\(\s*\)"""), "")
            .trim(' ', ',', '-', ':')

        // Remove a connector accidentally left immediately before a parenthetical.
        name = name.replace(
            Regex("""\s+(?:&|and|or)\s*(?=\()""", RegexOption.IGNORE_CASE),
            " "
        ).trim()

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
