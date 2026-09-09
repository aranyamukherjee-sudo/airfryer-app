package com.chefmagic.airfryer

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        val sections = RecipeRepository.loadSections(context)
        val totalRecipes = sections.sumOf { it.recipes.size }
        val totalCategories = sections.size
        val totalFavorites = FavoritesManager.favoriteFiles(context).size

        bindStat(view.findViewById(R.id.statFavorites), totalFavorites.toString(), "Favorites")
        bindStat(view.findViewById(R.id.statRecipes), totalRecipes.toString(), "Recipes")
        bindStat(view.findViewById(R.id.statCategories), totalCategories.toString(), "Categories")
    }

    override fun onResume() {
        super.onResume()
        if (view != null) onViewCreated(requireView(), null)
    }

    private fun bindStat(cardRoot: View, value: String, label: String) {
        cardRoot.findViewById<TextView>(R.id.statValue).text = value
        cardRoot.findViewById<TextView>(R.id.statLabel).text = label
    }
}
