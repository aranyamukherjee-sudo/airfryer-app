package com.chefmagic.airfryer

import android.graphics.Paint
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ShoppingListAdapter(
    private val items: List<ShoppingListItem>,
    private val onToggle: (ShoppingListItem) -> Unit,
    private val onQuantityChanged: (ShoppingListItem, String) -> Unit
) : RecyclerView.Adapter<ShoppingListAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping_list_row, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], onToggle, onQuantityChanged)
    }

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int): ShoppingListItem = items[position]

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val checkbox: CheckBox = view.findViewById(R.id.shoppingItemCheckbox)
        private val nameText: TextView = view.findViewById(R.id.shoppingItemName)
        private val quantityText: EditText = view.findViewById(R.id.shoppingItemQuantity)

        fun bind(
            item: ShoppingListItem,
            onToggle: (ShoppingListItem) -> Unit,
            onQuantityChanged: (ShoppingListItem, String) -> Unit
        ) {
            quantityText.setOnFocusChangeListener(null)
            quantityText.setOnEditorActionListener(null)

            nameText.text = item.name
            quantityText.setText(item.quantity)

            quantityText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    saveQuantity(item, onQuantityChanged)
                }
            }

            quantityText.setOnEditorActionListener { _, actionId, event ->
                val doneByKey = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                    event.action == KeyEvent.ACTION_DOWN
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE || doneByKey) {
                    quantityText.clearFocus()
                    true
                } else {
                    false
                }
            }

            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = item.checked
            applyCheckedStyle(item.checked)

            checkbox.setOnCheckedChangeListener { _, _ ->
                onToggle(item)
            }
        }

        private fun saveQuantity(
            item: ShoppingListItem,
            onQuantityChanged: (ShoppingListItem, String) -> Unit
        ) {
            val quantity = quantityText.text.toString().trim()
            if (quantity != item.quantity) {
                onQuantityChanged(item, quantity)
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
