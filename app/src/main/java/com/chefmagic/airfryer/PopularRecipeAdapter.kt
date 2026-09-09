package com.chefmagic.airfryer

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PopularRecipeAdapter(
    private val recipes: List<Recipe>,
    private val onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<PopularRecipeAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_popular_recipe, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(recipes[position], onClick)
    }

    override fun getItemCount(): Int = recipes.size

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val image: ImageView = view.findViewById(R.id.popularImage)
        private val title: TextView = view.findViewById(R.id.popularTitle)
        private val subtitle: TextView = view.findViewById(R.id.popularSubtitle)

        fun bind(recipe: Recipe, onClick: (Recipe) -> Unit) {
            title.text = recipe.title
            subtitle.text = recipe.section
            if (recipe.image != null) {
                val uri = Uri.parse("file:///android_asset/${recipe.image}")
                Glide.with(itemView.context).load(uri).centerCrop().into(image)
            } else {
                image.setImageResource(R.drawable.ic_placeholder)
            }
            itemView.setOnClickListener { onClick(recipe) }
        }
    }
}
