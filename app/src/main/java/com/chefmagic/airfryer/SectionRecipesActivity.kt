package com.chefmagic.airfryer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SectionRecipesActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SECTION_NAME = "extra_section_name"
        const val EXTRA_COLLECTION_NAME = "extra_collection_name"
    }

    private lateinit var adapter: RecipeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: View
    private var recipes: List<Recipe> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_recipes)

        val sectionName = intent.getStringExtra(EXTRA_SECTION_NAME)
        val collectionName = intent.getStringExtra(EXTRA_COLLECTION_NAME)
        val screenTitle = sectionName ?: collectionName ?: return

        val toolbar: Toolbar = findViewById(R.id.sectionToolbar)
        toolbar.title = screenTitle
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.sectionRecyclerView)
        emptyText = findViewById(R.id.sectionEmptyText)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RecipeAdapter(
            onLongClick = { recipe -> CollectionDialogHelper.showAddToCollectionDialog(this, recipe) },
            onClick = { recipe ->
                val i = Intent(this, RecipeDetailActivity::class.java)
                i.putExtra(RecipeDetailActivity.EXTRA_TITLE, recipe.title)
                i.putExtra(RecipeDetailActivity.EXTRA_FILE, recipe.file)
                startActivity(i)
            }
        )
        recyclerView.adapter = adapter

        val allRecipes = RecipeRepository.loadSections(this).flatMap { it.recipes }
        recipes = if (sectionName != null) {
            allRecipes.filter { it.section == sectionName }
        } else {
            val fileSet = CollectionsManager.recipesInCollection(this, collectionName!!)
            allRecipes.filter { it.file in fileSet }
        }
        refresh()
    }

    override fun onResume() {
        super.onResume()
        // Re-bind so favorite heart icons reflect any change made elsewhere (e.g. Recipe
        // Detail) while this screen was paused. The recipe list itself is not re-queried —
        // only this screen's own favorite-state display is refreshed.
        if (::adapter.isInitialized) refresh()
    }

    private fun refresh() {
        adapter.submitList(recipes.map { ListItem.RecipeRow(it) })

        val hasResults = recipes.isNotEmpty()
        recyclerView.visibility = if (hasResults) View.VISIBLE else View.GONE
        emptyText.visibility = if (hasResults) View.GONE else View.VISIBLE
    }
}
