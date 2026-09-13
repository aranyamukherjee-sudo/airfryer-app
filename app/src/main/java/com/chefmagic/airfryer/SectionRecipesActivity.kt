package com.chefmagic.airfryer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SectionRecipesActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SECTION_NAME = "extra_section_name"
        const val EXTRA_COLLECTION_NAME = "extra_collection_name"
    }

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

        val recyclerView: RecyclerView = findViewById(R.id.sectionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = RecipeAdapter { recipe ->
            val i = Intent(this, RecipeDetailActivity::class.java)
            i.putExtra(RecipeDetailActivity.EXTRA_TITLE, recipe.title)
            i.putExtra(RecipeDetailActivity.EXTRA_FILE, recipe.file)
            startActivity(i)
        }
        recyclerView.adapter = adapter

        val allRecipes = RecipeRepository.loadSections(this).flatMap { it.recipes }
        val recipes = if (sectionName != null) {
            allRecipes.filter { it.section == sectionName }
        } else {
            val fileSet = CollectionsManager.recipesInCollection(this, collectionName!!)
            allRecipes.filter { it.file in fileSet }
        }
        adapter.submitList(recipes.map { ListItem.RecipeRow(it) })
    }
}
