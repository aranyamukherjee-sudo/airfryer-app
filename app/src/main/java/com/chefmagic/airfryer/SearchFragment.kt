package com.chefmagic.airfryer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var sections: List<Section>
    private lateinit var adapter: RecipeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        sections = RecipeRepository.loadSections(context)
        recyclerView = view.findViewById(R.id.searchRecyclerView)
        emptyText = view.findViewById(R.id.searchEmptyText)

        adapter = RecipeAdapter { recipe ->
            val i = Intent(context, RecipeDetailActivity::class.java)
            i.putExtra(RecipeDetailActivity.EXTRA_TITLE, recipe.title)
            i.putExtra(RecipeDetailActivity.EXTRA_FILE, recipe.file)
            startActivity(i)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.visibility = View.GONE

        val searchView: SearchView = view.findViewById(R.id.fragmentSearchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                runSearch(newText ?: "")
                return true
            }
        })
    }

    private fun runSearch(rawQuery: String) {
        val query = rawQuery.trim().lowercase()
        val context = requireContext()

        if (query.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
            return
        }

        val searchTerms = SynonymRepository.expand(context, query)

        fun matches(recipe: Recipe): Boolean {
            return searchTerms.any { term ->
                recipe.title.lowercase().contains(term) || recipe.search.contains(term)
            }
        }

        val items = mutableListOf<ListItem>()
        for (section in sections) {
            val matching = section.recipes
                .filter { matches(it) }
                .sortedBy { if (it.title.lowercase().contains(query)) 0 else 1 }
            if (matching.isNotEmpty()) {
                items.add(ListItem.Header(section.name))
                matching.forEach { items.add(ListItem.RecipeRow(it)) }
            }
        }

        adapter.submitList(items)
        val hasResults = items.isNotEmpty()
        recyclerView.visibility = if (hasResults) View.VISIBLE else View.GONE
        emptyText.visibility = if (hasResults) View.GONE else View.VISIBLE
        if (!hasResults) {
            (emptyText as? android.widget.TextView)?.text = "No recipes found for \"$rawQuery\""
        }
    }
}
