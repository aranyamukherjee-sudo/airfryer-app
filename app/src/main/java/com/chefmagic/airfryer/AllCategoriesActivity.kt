package com.chefmagic.airfryer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AllCategoriesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_categories)

        findViewById<android.widget.ImageButton>(R.id.allCategoriesBackButton).setOnClickListener { finish() }

        val sections = RecipeRepository.loadSections(this)
        val recyclerView: RecyclerView = findViewById(R.id.allCategoriesGridRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = CategoryGridAdapter(sections) { section ->
            val i = Intent(this, SectionRecipesActivity::class.java)
            i.putExtra(SectionRecipesActivity.EXTRA_SECTION_NAME, section.name)
            startActivity(i)
        }
    }
}
