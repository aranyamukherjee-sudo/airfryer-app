package com.chefmagic.airfryer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoriesFragment : Fragment(R.layout.fragment_categories) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sections = RecipeRepository.loadSections(requireContext())
        val recyclerView: RecyclerView = view.findViewById(R.id.categoriesGridRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = CategoryGridAdapter(sections) { section ->
            val i = Intent(requireContext(), SectionRecipesActivity::class.java)
            i.putExtra(SectionRecipesActivity.EXTRA_SECTION_NAME, section.name)
            startActivity(i)
        }
    }
}
