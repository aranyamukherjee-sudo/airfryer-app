package com.chefmagic.airfryer

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

data class CollectionInfo(
    val name: String,
    val recipeCount: Int,
    val thumbnailImage: String?
)

class CollectionAdapter(
    private val collections: List<CollectionInfo>,
    private val onClick: (CollectionInfo) -> Unit
) : RecyclerView.Adapter<CollectionAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_card, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(collections[position], onClick)
    }

    override fun getItemCount(): Int = collections.size

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val image: ImageView = view.findViewById(R.id.gridCategoryImage)
        private val label: TextView = view.findViewById(R.id.gridCategoryLabel)

        fun bind(collection: CollectionInfo, onClick: (CollectionInfo) -> Unit) {
            val recipeWord = if (collection.recipeCount == 1) "recipe" else "recipes"
            label.text = "${collection.name}\n${collection.recipeCount} $recipeWord"
            if (collection.thumbnailImage != null) {
                val uri = Uri.parse("file:///android_asset/${collection.thumbnailImage}")
                Glide.with(itemView.context).load(uri).centerCrop().into(image)
            } else {
                image.setImageDrawable(null)
            }
            itemView.setOnClickListener { onClick(collection) }
        }
    }
}
