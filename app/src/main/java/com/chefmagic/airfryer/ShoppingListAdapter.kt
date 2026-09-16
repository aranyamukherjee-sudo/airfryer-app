package com.chefmagic.airfryer

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ShoppingListAdapter(
    private val items: List<ShoppingListItem>,
    private val onToggle: (ShoppingListItem) -> Unit
) : RecyclerView.Adapter<ShoppingListAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping_list_row, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], onToggle)
    }

    override fun getItemCount(): Int = items.size

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val checkbox: CheckBox = view.findViewById(R.id.shoppingItemCheckbox)
        private val nameText: TextView = view.findViewById(R.id.shoppingItemName)
        private val quantityText: TextView = view.findViewById(R.id.shoppingItemQuantity)

        fun bind(item: ShoppingListItem, onToggle: (ShoppingListItem) -> Unit) {
            nameText.text = item.name
            quantityText.text = item.quantity
            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = item.checked
            applyCheckedStyle(item.checked)

            checkbox.setOnCheckedChangeListener { _, _ ->
                onToggle(item)
            }
        }

        private fun applyCheckedStyle(checked: Boolean) {
            if (checked) {
                nameText.paintFlags = nameText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                nameText.alpha = 0.5f
                quantityText.alpha = 0.5f
            } else {
                nameText.paintFlags = nameText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                nameText.alpha = 1f
                quantityText.alpha = 1f
            }
        }
    }
}
