package com.chefmagic.airfryer

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecipeAdapter(
    private val onLongClick: (Recipe) -> Unit = {},
    private val onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ListItem>()

    private val TYPE_HEADER = 0
    private val TYPE_RECIPE = 1

    fun submitList(newItems: List<ListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.RecipeRow -> TYPE_RECIPE
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderVH(inflater.inflate(R.layout.item_header, parent, false))
        } else {
            RecipeVH(inflater.inflate(R.layout.item_recipe, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> (holder as HeaderVH).bind(item.name)
            is ListItem.RecipeRow -> (holder as RecipeVH).bind(item.recipe, onClick, onLongClick)
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        private val text: TextView = view.findViewById(R.id.headerText)
        fun bind(name: String) {
            text.text = name
        }
    }

    class RecipeVH(view: View) : RecyclerView.ViewHolder(view) {
        private val image: ImageView = view.findViewById(R.id.recipeImage)
        private val title: TextView = view.findViewById(R.id.recipeTitle)
        private val section: TextView = view.findViewById(R.id.recipeSection)
        private val heartButton: ImageButton = view.findViewById(R.id.favoriteHeartButton)

        fun bind(recipe: Recipe, onClick: (Recipe) -> Unit, onLongClick: (Recipe) -> Unit) {
            title.text = recipe.title
            section.text = buildSubtitle(recipe)

            bindHeart(recipe)

            if (recipe.image != null) {
                image.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                image.setPadding(0, 0, 0, 0)
                image.setBackgroundColor(android.graphics.Color.parseColor("#EDE6D6"))
                val uri = Uri.parse("file:///android_asset/${recipe.image}")
                Glide.with(itemView.context)
                    .load(uri)
                    .centerCrop()
                    .into(image)
            } else {
                val pad = (12 * itemView.resources.displayMetrics.density).toInt()
                image.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                image.setPadding(pad, pad, pad, pad)
                image.setBackgroundColor(android.graphics.Color.parseColor("#EDE6D6"))
                image.setImageResource(R.drawable.ic_placeholder)
            }

            itemView.setOnClickListener { onClick(recipe) }
            itemView.setOnLongClickListener { onLongClick(recipe); true }
        }

        private fun buildSubtitle(recipe: Recipe): String {
            val parts = mutableListOf<String>()
            if (recipe.totalDurationSeconds > 0) {
                parts.add("${recipe.totalDurationSeconds / 60} min")
            }
            if (recipe.kcalPerServing != null) {
                parts.add("≈${recipe.kcalPerServing} kcal")
            }
            return if (parts.isNotEmpty()) parts.joinToString("  ·  ") else recipe.section
        }

        private fun bindHeart(recipe: Recipe) {
            val context = itemView.context
            fun refreshIcon() {
                val isFav = FavoritesManager.isFavorite(context, recipe.file)
                heartButton.setImageResource(
                    if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                )
            }
            refreshIcon()
            heartButton.setOnClickListener {
                FavoritesManager.toggleFavorite(context, recipe.file)
                refreshIcon()
            }
        }
    }
}
