package com.chefmagic.airfryer

import android.content.Context
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

/**
 * Shared "add recipe to a collection" dialog flow. Extracted from FavoritesFragment so it can
 * also be used from SectionRecipesActivity (and anywhere else) without duplicating the dialog
 * logic. Intentionally does NOT check FavoritesManager.isFavorite() — that gate only ever made
 * sense on the Favorites screen (where every listed recipe is already a favorite by definition,
 * so it was a no-op there), and must not block this flow from a general recipe list.
 */
object CollectionDialogHelper {

    fun showAddToCollectionDialog(context: Context, recipe: Recipe) {
        val names = CollectionsManager.allCollectionNames(context)
        if (names.isEmpty()) {
            showCreateCollectionDialog(context, recipe)
            return
        }

        val checked = BooleanArray(names.size) { i ->
            CollectionsManager.isInCollection(context, names[i], recipe.file)
        }

        AlertDialog.Builder(context)
            .setTitle("Add \"${recipe.title}\" to")
            .setMultiChoiceItems(names.toTypedArray(), checked) { _, which, _ ->
                CollectionsManager.toggleRecipeInCollection(context, names[which], recipe.file)
            }
            .setPositiveButton("Done", null)
            .show()
    }

    private fun showCreateCollectionDialog(context: Context, recipe: Recipe) {
        val input = EditText(context)
        input.hint = "Collection name"

        AlertDialog.Builder(context)
            .setTitle("New collection")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    CollectionsManager.createCollection(context, name)
                    CollectionsManager.toggleRecipeInCollection(context, name, recipe.file)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
