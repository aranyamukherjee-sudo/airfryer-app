package com.chefmagic.airfryer

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ShoppingListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: View
    private lateinit var chipGroup: ChipGroup
    private var activeCategory: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        findViewById<ImageButton>(R.id.shoppingBackButton).setOnClickListener { finish() }
        findViewById<TextView>(R.id.shoppingClearAllText).setOnClickListener { confirmClearAll() }

        recyclerView = findViewById(R.id.shoppingListRecyclerView)
        emptyText = findViewById(R.id.shoppingEmptyText)
        chipGroup = findViewById(R.id.shoppingCategoryChipRow)
        recyclerView.layoutManager = LinearLayoutManager(this)
        attachSwipeToDelete()

        refresh()
    }

    private fun attachSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = recyclerView.adapter as? ShoppingListAdapter ?: return
                val item = adapter.getItem(viewHolder.bindingAdapterPosition)
                ShoppingListManager.removeItem(this@ShoppingListActivity, item.id)
                refresh()
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val allItems = ShoppingListManager.getAll(this)
        rebuildCategoryChips(allItems)

        val filtered = if (activeCategory == "All") allItems
            else allItems.filter { it.category == activeCategory }

        // Unchecked items first, then checked ones at the bottom
        val sorted = filtered.sortedBy { it.checked }

        recyclerView.adapter = ShoppingListAdapter(sorted) { item ->
            ShoppingListManager.toggleChecked(this, item.id)
            refresh()
        }

        findViewById<TextView>(R.id.shoppingCountText).text =
            "${allItems.size} item" + if (allItems.size == 1) "" else "s"

        val hasItems = allItems.isNotEmpty()
        val hasFilteredItems = filtered.isNotEmpty()
        recyclerView.visibility = if (hasFilteredItems) View.VISIBLE else View.GONE
        emptyText.visibility = if (!hasItems) View.VISIBLE else View.GONE
    }

    private fun rebuildCategoryChips(allItems: List<ShoppingListItem>) {
        val categories = listOf("All") + allItems.map { it.category }.distinct().sorted()
        if (activeCategory != "All" && activeCategory !in categories) activeCategory = "All"

        chipGroup.removeAllViews()
        for (category in categories) {
            val chip = Chip(this)
            chip.text = category
            chip.isCheckable = true
            chip.isChecked = category == activeCategory
            chip.setOnClickListener {
                activeCategory = category
                refresh()
            }
            chipGroup.addView(chip)
        }
    }

    private fun confirmClearAll() {
        if (ShoppingListManager.getAll(this).isEmpty()) return
        AlertDialog.Builder(this)
            .setTitle("Clear shopping list?")
            .setMessage("This removes all items from your shopping list.")
            .setPositiveButton("Clear all") { _, _ ->
                ShoppingListManager.clearAll(this)
                refresh()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
