package com.chefmagic.airfryer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout

class RecipeDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_FILE = "extra_file"
    }

    private var recipeFile: String = ""
    private var recipeTitle: String = ""
    private var recipe: Recipe? = null
    private var currentServings: Int = 2

    private lateinit var servingsCountText: TextView
    private lateinit var ingredientsContainer: LinearLayout
    private val ingredientRowViews = mutableListOf<Pair<Ingredient, TextView>>()
    private lateinit var favoriteButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        recipeTitle = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.app_name)
        recipeFile = intent.getStringExtra(EXTRA_FILE) ?: return

        recipe = RecipeRepository.loadSections(this)
            .flatMap { it.recipes }
            .firstOrNull { it.file == recipeFile }

        val r = recipe ?: return
        currentServings = r.originalServings.coerceIn(1, 20)

        RecentlyViewedManager.recordView(this, recipeFile)

        bindHero(r)
        bindOverlayButtons()
        bindChips(r)
        bindServingSelector(r)
        updateNutritionText()
        updateAirfryerTip()
        bindIngredients(r)
        bindSteps(r)
        bindTip(r)
        bindTabs()

        findViewById<MaterialButton>(R.id.startCookingButton).setOnClickListener {
            val i = Intent(this, CookingModeActivity::class.java)
            i.putExtra(CookingModeActivity.EXTRA_TITLE, recipeTitle)
            i.putExtra(CookingModeActivity.EXTRA_FILE, recipeFile)
            i.putExtra(CookingModeActivity.EXTRA_SERVINGS, currentServings)
            startActivity(i)
        }
    }

    private fun bindHero(r: Recipe) {
        findViewById<TextView>(R.id.detailTitle).text = r.title
        val heroImage: ImageView = findViewById(R.id.detailHeroImage)
        if (r.image != null) {
            val uri = Uri.parse("file:///android_asset/${r.image}")
            Glide.with(this).load(uri).centerCrop().into(heroImage)
        } else {
            heroImage.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun bindOverlayButtons() {
        findViewById<ImageButton>(R.id.detailBackButton).setOnClickListener { finish() }

        favoriteButton = findViewById(R.id.detailFavoriteButton)
        updateFavoriteIcon()
        favoriteButton.setOnClickListener {
            FavoritesManager.toggleFavorite(this, recipeFile)
            updateFavoriteIcon()
        }

        findViewById<ImageButton>(R.id.detailShareButton).setOnClickListener { shareRecipe() }
    }

    private fun updateFavoriteIcon() {
        val isFav = FavoritesManager.isFavorite(this, recipeFile)
        favoriteButton.setImageResource(
            if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )
    }

    private fun shareRecipe() {
        val shareText = "Check out \"$recipeTitle\" on ${getString(R.string.app_name)} 🍽"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, recipeTitle)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share recipe"))
    }

    private fun bindChips(r: Recipe) {
        val chipGroup: ChipGroup = findViewById(R.id.detailChipsRow)
        chipGroup.removeAllViews()
        for (appliance in r.appliances) {
            val chip = Chip(this)
            chip.text = appliance
            chip.isClickable = false
            chip.isCheckable = false
            chipGroup.addView(chip)
        }
        val dietChip = Chip(this)
        dietChip.text = if (r.nonveg) "Non-Veg" else "Vegetarian"
        dietChip.isClickable = false
        dietChip.isCheckable = false
        chipGroup.addView(dietChip)
    }

    private fun bindServingSelector(r: Recipe) {
        servingsCountText = findViewById(R.id.servingsCountText)
        val minusButton: ImageButton = findViewById(R.id.servingsMinusButton)
        val plusButton: ImageButton = findViewById(R.id.servingsPlusButton)
        val resetText: TextView = findViewById(R.id.servingsResetText)

        updateServingsDisplay()

        minusButton.setOnClickListener {
            if (currentServings > 1) {
                currentServings -= 1
                onServingsChanged()
            }
        }
        plusButton.setOnClickListener {
            if (currentServings < 20) {
                currentServings += 1
                onServingsChanged()
            }
        }
        resetText.setOnClickListener {
            currentServings = r.originalServings.coerceIn(1, 20)
            onServingsChanged()
        }
    }

    private fun onServingsChanged() {
        updateServingsDisplay()
        refreshIngredientTexts()
        updateNutritionText()
        updateAirfryerTip()

        servingsCountText.animate().cancel()
        servingsCountText.scaleX = 1.25f
        servingsCountText.scaleY = 1.25f
        servingsCountText.animate().scaleX(1f).scaleY(1f).setDuration(160).start()
    }

    private fun updateAirfryerTip() {
        val r = recipe ?: return
        val tipCard: CardView = findViewById(R.id.airfryerTipCard)
        val isAirfryer = r.appliances.contains("Airfryer")
        val significantlyLarger = currentServings >= r.originalServings * 2 && currentServings > r.originalServings
        tipCard.visibility = if (isAirfryer && significantlyLarger) View.VISIBLE else View.GONE
    }

    private fun updateNutritionText() {
        val r = recipe ?: return
        val nutritionRow: View = findViewById(R.id.nutritionRow)
        val fallbackText: TextView = findViewById(R.id.nutritionText)
        val perServing = r.kcalPerServing

        if (perServing == null) {
            nutritionRow.visibility = View.GONE
            fallbackText.visibility = View.VISIBLE
            fallbackText.text = "Calories unavailable for this recipe"
            return
        }

        nutritionRow.visibility = View.VISIBLE
        fallbackText.visibility = View.GONE

        val total = perServing * currentServings
        findViewById<TextView>(R.id.kcalPerServingText).text = "≈$perServing kcal\nper serving"
        findViewById<TextView>(R.id.kcalTotalText).text = "≈$total kcal\ntotal"
    }

    private fun updateServingsDisplay() {
        servingsCountText.text = currentServings.toString()
        servingsCountText.contentDescription = "Current serving size: $currentServings"
        findViewById<ImageButton>(R.id.servingsMinusButton).isEnabled = currentServings > 1
        findViewById<ImageButton>(R.id.servingsMinusButton).alpha = if (currentServings > 1) 1f else 0.35f
        findViewById<ImageButton>(R.id.servingsPlusButton).isEnabled = currentServings < 20
        findViewById<ImageButton>(R.id.servingsPlusButton).alpha = if (currentServings < 20) 1f else 0.35f
    }

    private fun bindIngredients(r: Recipe) {
        ingredientsContainer = findViewById(R.id.ingredientsContainer)
        ingredientsContainer.removeAllViews()
        ingredientRowViews.clear()

        val inflater = LayoutInflater.from(this)
        for (ingredient in r.ingredients) {
            val row = inflater.inflate(R.layout.item_ingredient_row, ingredientsContainer, false)
            val textView: TextView = row.findViewById(R.id.ingredientText)
            ingredientsContainer.addView(row)
            ingredientRowViews.add(ingredient to textView)
        }
        refreshIngredientTexts()
    }

    private fun refreshIngredientTexts() {
        val r = recipe ?: return
        for ((ingredient, textView) in ingredientRowViews) {
            textView.text = IngredientScaler.scaledText(ingredient, currentServings, r.originalServings)
        }
    }

    private fun bindSteps(r: Recipe) {
        val container: LinearLayout = findViewById(R.id.stepsContainer)
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)
        r.steps.forEachIndexed { index, step ->
            val row = inflater.inflate(R.layout.item_step_row, container, false)
            row.findViewById<TextView>(R.id.stepNumberText).text = (index + 1).toString()
            row.findViewById<TextView>(R.id.stepTextRow).text = step.text
            container.addView(row)
        }
    }

    private fun bindTip(r: Recipe) {
        if (r.tip != null) {
            findViewById<CardView>(R.id.tipCard).visibility = View.VISIBLE
            findViewById<TextView>(R.id.tipText).text = r.tip
        } else {
            findViewById<TextView>(R.id.noTipText).visibility = View.VISIBLE
        }
    }

    /** Real tab switching: only one of Ingredients / Method / Tips is visible at a time. */
    private fun bindTabs() {
        val tabLayout: TabLayout = findViewById(R.id.detailTabLayout)
        val stepsContainer: LinearLayout = findViewById(R.id.stepsContainer)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showTab(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        showTab(0)
    }

    private fun showTab(position: Int) {
        ingredientsContainer.visibility = if (position == 0) View.VISIBLE else View.GONE
        findViewById<LinearLayout>(R.id.stepsContainer).visibility = if (position == 1) View.VISIBLE else View.GONE

        val showTips = position == 2
        val hasTip = recipe?.tip != null
        findViewById<CardView>(R.id.tipCard).visibility = if (showTips && hasTip) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.noTipText).visibility = if (showTips && !hasTip) View.VISIBLE else View.GONE
    }
}
