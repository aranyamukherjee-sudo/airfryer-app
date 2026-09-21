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

        adapter = RecipeAdapter(
            onClick = { recipe ->
                val i = Intent(context, RecipeDetailActivity::class.java)
                i.putExtra(RecipeDetailActivity.EXTRA_TITLE, recipe.title)
                i.putExtra(RecipeDetailActivity.EXTRA_FILE, recipe.file)
                startActivity(i)
            },
            onLongClick = { recipe -> CollectionDialogHelper.showAddToCollectionDialog(context, recipe) }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.favoritesSearchIcon).setOnClickListener {
            (activity as? MainActivity)?.selectBottomNavTab(R.id.nav_search)
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) refresh()
    }

    private fun refresh() {
        val context = requireContext()
        val sections = RecipeRepository.loadSections(context)
        val favFiles = FavoritesManager.favoriteFiles(context)

        val recipes = sections.flatMap { it.recipes }
            .filter { it.file in favFiles }
            .sortedBy { it.title }

        adapter.submitList(recipes.map { ListItem.RecipeRow(it) })

        val recipeWord = if (recipes.size == 1) "recipe" else "recipes"
        requireView().findViewById<android.widget.TextView>(R.id.favoritesCountText).text =
            "${recipes.size} $recipeWord"

        val hasResults = recipes.isNotEmpty()
        recyclerView.visibility = if (hasResults) View.VISIBLE else View.GONE
        emptyText.visibility = if (hasResults) View.GONE else View.VISIBLE
    }
}
