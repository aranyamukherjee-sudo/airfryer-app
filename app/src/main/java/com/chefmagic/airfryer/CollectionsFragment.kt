package com.chefmagic.airfryer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CollectionsFragment : Fragment(R.layout.fragment_collections) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.collectionsGridRecyclerView)
        emptyText = view.findViewById(R.id.collectionsEmptyText)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        view.findViewById<View>(R.id.addCollectionButton).setOnClickListener {
            showCreateCollectionDialog()
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val context = requireContext()
        val allRecipes = RecipeRepository.loadSections(context).flatMap { it.recipes }
        val names = CollectionsManager.allCollectionNames(context)

        val collectionInfos = names.map { name ->
            val fileSet = CollectionsManager.recipesInCollection(context, name)
            val recipesInCollection = allRecipes.filter { it.file in fileSet }
            CollectionInfo(
                name = name,
                recipeCount = recipesInCollection.size,
                thumbnailImage = recipesInCollection.firstOrNull { it.image != null }?.image
            )
        }

        recyclerView.adapter = CollectionAdapter(collectionInfos) { collection ->
            val i = Intent(context, SectionRecipesActivity::class.java)
            i.putExtra(SectionRecipesActivity.EXTRA_COLLECTION_NAME, collection.name)
            startActivity(i)
        }

        val hasCollections = collectionInfos.isNotEmpty()
        recyclerView.visibility = if (hasCollections) View.VISIBLE else View.GONE
        emptyText.visibility = if (hasCollections) View.GONE else View.VISIBLE
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
                    refresh()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
