package com.chefmagic.airfryer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var adapter: RecipeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        recyclerView = view.findViewById(R.id.favoritesRecyclerView)
        emptyText = view.findViewById(R.id.favoritesEmptyText)

        adapter = RecipeAdapter { recipe ->
            val i = Intent(context, RecipeDetailActivity::class.java)
            i.putExtra(RecipeDetailActivity.EXTRA_TITLE, recipe.title)
            i.putExtra(RecipeDetailActivity.EXTRA_FILE, recipe.file)
            startActivity(i)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        refresh()
    }

    override fun onResume() {
        super.onResume()
        // Favorites may have changed while viewing a recipe detail screen.
        if (::adapter.isInitialized) refresh()
    }

    private fun refresh() {
        val context = requireContext()
        val sections = RecipeRepository.loadSections(context)
        val favFiles = FavoritesManager.favoriteFiles(context)

        val favRecipes = sections.flatMap { it.recipes }
            .filter { it.file in favFiles }
            .sortedBy { it.title }

        adapter.submitList(favRecipes.map { ListItem.RecipeRow(it) })

        val hasResults = favRecipes.isNotEmpty()
        recyclerView.visibility = if (hasResults) View.VISIBLE else View.GONE
        emptyText.visibility = if (hasResults) View.GONE else View.VISIBLE
    }
}
