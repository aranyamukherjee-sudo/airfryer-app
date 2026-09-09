package com.chefmagic.airfryer

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CategoryGridAdapter(
    private val sections: List<Section>,
    private val onClick: (Section) -> Unit
) : RecyclerView.Adapter<CategoryGridAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_card, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(sections[position], onClick)
    }

    override fun getItemCount(): Int = sections.size

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val image: ImageView = view.findViewById(R.id.gridCategoryImage)
        private val label: TextView = view.findViewById(R.id.gridCategoryLabel)

        fun bind(section: Section, onClick: (Section) -> Unit) {
            label.text = "${section.name} (${section.recipes.size})"
            val firstImage = section.recipes.firstOrNull { it.image != null }?.image
            if (firstImage != null) {
                val uri = Uri.parse("file:///android_asset/$firstImage")
                Glide.with(itemView.context).load(uri).centerCrop().into(image)
            } else {
                image.setImageDrawable(null)
            }
            itemView.setOnClickListener { onClick(section) }
        }
    }
}
