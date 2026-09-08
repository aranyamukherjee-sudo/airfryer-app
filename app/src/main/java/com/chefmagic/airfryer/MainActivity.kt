package com.chefmagic.airfryer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var tabLayout: TabLayout
    private lateinit var emptyText: TextView
    private lateinit var adapter: RecipeAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var sections: List<Section> = emptyList()
    private var showingFavorites = false
    private var currentQuery: String = ""
    private var currentItems: List<ListItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)
        tabLayout = findViewById(R.id.tabLayout)
        emptyText = findViewById(R.id.emptyText)

        adapter = RecipeAdapter { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra(RecipeDetailActivity.EXTRA_TITLE, recipe.title)
            intent.putExtra(RecipeDetailActivity.EXTRA_FILE, recipe.file)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        layoutManager = recyclerView.layoutManager as LinearLayoutManager
        recyclerView.adapter = adapter

        sections = RecipeRepository.loadSections(this)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText ?: ""
                refreshList()
                return true
            }
        })

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                showingFavorites = tab?.position == 1
                refreshList()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        refreshList()
    }

    override fun onResume() {
        super.onResume()
        // Favorites may have changed in the detail screen
        if (showingFavorites) refreshList()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_contents) {
            showContentsDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showContentsDialog() {
        if (sections.isEmpty()) return
        val names = sections.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Contents")
            .setItems(names) { _, which ->
                jumpToSection(sections[which].name)
            }
            .show()
    }

    private fun jumpToSection(sectionName: String) {
        // Contents only makes sense against the full, unfiltered "All Recipes" list.
        showingFavorites = false
        tabLayout.getTabAt(0)?.select()
        if (currentQuery.isNotEmpty()) {
            currentQuery = ""
            searchView.setQuery("", false)
            searchView.clearFocus()
        }
        refreshList()

        val position = currentItems.indexOfFirst {
            it is ListItem.Header && it.name == sectionName
        }
        if (position >= 0) {
            layoutManager.scrollToPositionWithOffset(position, 0)
        }
    }

    private fun refreshList() {
        val query = currentQuery.trim().lowercase()

        val items = mutableListOf<ListItem>()

        if (showingFavorites) {
            val favFiles = FavoritesManager.favoriteFiles(this)
            val favRecipes = sections.flatMap { it.recipes }
                .filter { it.file in favFiles }
                .filter { query.isEmpty() || it.title.lowercase().contains(query) || it.search.contains(query) }
                .sortedBy { it.title }
            favRecipes.forEach { items.add(ListItem.RecipeRow(it)) }
        } else {
            for (section in sections) {
                val matching = section.recipes
                    .filter {
                        query.isEmpty() || it.title.lowercase().contains(query) || it.search.contains(query)
                    }
                    .sortedBy { if (query.isEmpty() || it.title.lowercase().contains(query)) 0 else 1 }
                if (matching.isNotEmpty()) {
                    items.add(ListItem.Header(section.name))
                    matching.forEach { items.add(ListItem.RecipeRow(it)) }
                }
            }
        }

        currentItems = items
        adapter.submitList(items)
        emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }
}
