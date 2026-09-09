package com.chefmagic.airfryer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var sections: List<Section>
    private lateinit var adapter: RecipeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: View
    private lateinit var searchView: SearchView
    private lateinit var filterChipRow: ChipGroup
    private lateinit var recentSection: View
    private lateinit var recentChipRow: ChipGroup

    private val activeFilters = mutableSetOf<String>()
    private val filterOptions = listOf(
        "Vegetarian", "Non-Veg", "Airfryer", "Chef Magic", "High-Protein", "Quick (<20 min)"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        sections = RecipeRepository.loadSections(context)
        recyclerView = view.findViewById(R.id.searchRecyclerView)
        emptyText = view.findViewById(R.id.searchEmptyText)
        searchView = view.findViewById(R.id.fragmentSearchView)
        filterChipRow = view.findViewById(R.id.filterChipRow)
        recentSection = view.findViewById(R.id.recentSearchesSection)
        recentChipRow = view.findViewById(R.id.recentSearchesChipRow)

        adapter = RecipeAdapter(onClick = { recipe ->
            val i = Intent(context, RecipeDetailActivity::class.java)
            i.putExtra(RecipeDetailActivity.EXTRA_TITLE, recipe.title)
            i.putExtra(RecipeDetailActivity.EXTRA_FILE, recipe.file)
            startActivity(i)
        })
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.visibility = View.GONE

        buildFilterChips()
        refreshRecentChips()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    RecentSearchesManager.addSearch(context, query)
                    refreshRecentChips()
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                runSearch(newText ?: "")
                return true
            }
        })

        view.findViewById<View>(R.id.clearRecentText).setOnClickListener {
            RecentSearchesManager.clear(context)
            refreshRecentChips()
        }
    }

    private fun buildFilterChips() {
        val context = requireContext()
        filterChipRow.removeAllViews()
        for (option in filterOptions) {
            val chip = Chip(context)
            chip.text = option
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) activeFilters.add(option) else activeFilters.remove(option)
                runSearch(searchView.query?.toString() ?: "")
            }
            filterChipRow.addView(chip)
        }
    }

    private fun refreshRecentChips() {
        val context = requireContext()
        val recent = RecentSearchesManager.getRecent(context)
        recentChipRow.removeAllViews()
        recentSection.visibility = if (recent.isEmpty()) View.GONE else View.VISIBLE

        for (term in recent) {
            val chip = Chip(context)
            chip.text = term
            chip.isCheckable = false
            chip.setOnClickListener {
                searchView.setQuery(term, true)
            }
            recentChipRow.addView(chip)
        }
    }

    private fun passesFilters(recipe: Recipe): Boolean {
        for (filter in activeFilters) {
            val ok = when (filter) {
                "Vegetarian" -> !recipe.nonveg
                "Non-Veg" -> recipe.nonveg
                "Airfryer" -> recipe.appliances.contains("Airfryer")
                "Chef Magic" -> recipe.appliances.contains("Chef Magic")
                "High-Protein" -> recipe.highProtein
                "Quick (<20 min)" -> recipe.totalDurationSeconds in 1..1200
                else -> true
            }
            if (!ok) return false
        }
        return true
    }

    private fun runSearch(rawQuery: String) {
        val query = rawQuery.trim().lowercase()
        val context = requireContext()

        val hasQuery = query.isNotEmpty()
        val hasFilters = activeFilters.isNotEmpty()

        if (!hasQuery && !hasFilters) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
            recentSection.visibility =
                if (RecentSearchesManager.getRecent(context).isEmpty()) View.GONE else View.VISIBLE
            return
        }

        recentSection.visibility = View.GONE

        val searchTerms = if (hasQuery) SynonymRepository.expand(context, query) else emptySet()

        fun matchesText(recipe: Recipe): Boolean {
            if (!hasQuery) return true
            return searchTerms.any { term ->
                recipe.title.lowercase().contains(term) || recipe.search.contains(term)
            }
        }

        val items = mutableListOf<ListItem>()
        for (section in sections) {
            val matching = section.recipes
                .filter { matchesText(it) && passesFilters(it) }
                .sortedBy { if (!hasQuery || it.title.lowercase().contains(query)) 0 else 1 }
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
            (emptyText as? android.widget.TextView)?.text =
                if (hasQuery) "No recipes found for \"$rawQuery\"" else "No recipes match these filters"
        }
    }
}
