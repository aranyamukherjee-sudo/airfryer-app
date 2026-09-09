package com.chefmagic.airfryer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var adapter: RecipeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: View
    private lateinit var chipGroup: ChipGroup

    private val ALL_FAVORITES = "All Favorites"
    private var selectedFilter: String = ALL_FAVORITES

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        recyclerView = view.findViewById(R.id.favoritesRecyclerView)
        emptyText = view.findViewById(R.id.favoritesEmptyText)
        chipGroup = view.findViewById(R.id.collectionsChipRow)

        adapter = RecipeAdapter(
            onClick = { recipe ->
                val i = Intent(context, RecipeDetailActivity::class.java)
                i.putExtra(RecipeDetailActivity.EXTRA_TITLE, recipe.title)
                i.putExtra(RecipeDetailActivity.EXTRA_FILE, recipe.file)
                startActivity(i)
            },
            onLongClick = { recipe -> showAddToCollectionDialog(recipe) }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) refresh()
    }

    private fun refresh() {
        rebuildChips()
        rebuildList()
    }

    private fun rebuildChips() {
        val context = requireContext()
        chipGroup.removeAllViews()

        val allChip = Chip(context)
        allChip.text = ALL_FAVORITES
        allChip.isCheckable = true
        allChip.isChecked = selectedFilter == ALL_FAVORITES
        allChip.setOnClickListener {
            selectedFilter = ALL_FAVORITES
            rebuildList()
        }
        chipGroup.addView(allChip)

        for (name in CollectionsManager.allCollectionNames(context)) {
            val chip = Chip(context)
            chip.text = name
            chip.isCheckable = true
            chip.isChecked = selectedFilter == name
            chip.setOnClickListener {
                selectedFilter = name
                rebuildList()
            }
            chipGroup.addView(chip)
        }

        val addChip = Chip(context)
        addChip.text = "+ New"
        addChip.isCheckable = false
        addChip.setOnClickListener { showCreateCollectionDialog() }
        chipGroup.addView(addChip)
    }

    private fun rebuildList() {
        val context = requireContext()
        val sections = RecipeRepository.loadSections(context)
        val favFiles = FavoritesManager.favoriteFiles(context)

        val relevantFiles = if (selectedFilter == ALL_FAVORITES) {
            favFiles
        } else {
            CollectionsManager.recipesInCollection(context, selectedFilter)
        }

        val recipes = sections.flatMap { it.recipes }
            .filter { it.file in relevantFiles }
            .sortedBy { it.title }

        adapter.submitList(recipes.map { ListItem.RecipeRow(it) })

        val hasResults = recipes.isNotEmpty()
        recyclerView.visibility = if (hasResults) View.VISIBLE else View.GONE
        emptyText.visibility = if (hasResults) View.GONE else View.VISIBLE
    }

    private fun showAddToCollectionDialog(recipe: Recipe) {
        val context = requireContext()

        if (!FavoritesManager.isFavorite(context, recipe.file)) {
            android.widget.Toast.makeText(
                context, "Favorite this recipe first, then add it to a collection", android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

        val names = CollectionsManager.allCollectionNames(context)
        if (names.isEmpty()) {
            showCreateCollectionDialog()
            return
        }

        val checked = BooleanArray(names.size) { i ->
            CollectionsManager.isInCollection(context, names[i], recipe.file)
        }

        AlertDialog.Builder(context)
            .setTitle("Add \"${recipe.title}\" to")
            .setMultiChoiceItems(names.toTypedArray(), checked) { _, which, isChecked ->
                CollectionsManager.toggleRecipeInCollection(context, names[which], recipe.file)
            }
            .setPositiveButton("Done") { _, _ -> rebuildList() }
            .show()
    }

    private fun showCreateCollectionDialog() {
        val context = requireContext()
        val input = EditText(context)
        input.hint = "Collection name"

        AlertDialog.Builder(context)
            .setTitle("New collection")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    CollectionsManager.createCollection(context, name)
                    selectedFilter = name
                    refresh()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
