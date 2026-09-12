package com.chefmagic.airfryer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class PopularRecipesActivity : AppCompatActivity() {

    private lateinit var allRecipes: List<Recipe>
    private lateinit var adapter: RecipeAdapter
    private var activeFilter: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popular_recipes)

        allRecipes = RecipeRepository.loadSections(this).flatMap { it.recipes }

        findViewById<android.widget.ImageButton>(R.id.popularBackButton).setOnClickListener { finish() }
        findViewById<android.widget.ImageButton>(R.id.popularSearchButton).setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.putExtra(MainActivity.EXTRA_OPEN_TAB, R.id.nav_search)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        val recyclerView: RecyclerView = findViewById(R.id.popularListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecipeAdapter(onClick = { recipe ->
            val i = Intent(this, RecipeDetailActivity::class.java)
            i.putExtra(RecipeDetailActivity.EXTRA_TITLE, recipe.title)
            i.putExtra(RecipeDetailActivity.EXTRA_FILE, recipe.file)
            startActivity(i)
        })
        recyclerView.adapter = adapter

        buildFilterChips()
        refreshList()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) refreshList()
    }

    private fun buildFilterChips() {
        val chipGroup: ChipGroup = findViewById(R.id.popularFilterChipRow)
        val options = listOf("All", "Air Fryer", "Chef Magic", "Vegetarian")

        for (option in options) {
            val chip = Chip(this)
            chip.text = option
            chip.isCheckable = true
            chip.isChecked = option == activeFilter
            chip.setOnClickListener {
                activeFilter = option
                refreshList()
            }
            chipGroup.addView(chip)
        }
    }

    private fun refreshList() {
        val filtered = allRecipes.filter { recipe ->
            when (activeFilter) {
                "Air Fryer" -> recipe.appliances.contains("Airfryer")
                "Chef Magic" -> recipe.appliances.contains("Chef Magic")
                "Vegetarian" -> !recipe.nonveg
                else -> true
            }
        }
        adapter.submitList(filtered.map { ListItem.RecipeRow(it) })
    }
}
