package com.chefmagic.airfryer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Calendar

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        val sections = RecipeRepository.loadSections(context)
        val allRecipes = sections.flatMap { it.recipes }
        val recipesWithImages = allRecipes.filter { it.image != null }

        // --- Hero / Featured recipe: stable pick for the day ---
        if (recipesWithImages.isNotEmpty()) {
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val featured = recipesWithImages[dayOfYear % recipesWithImages.size]

            view.findViewById<TextView>(R.id.heroTitle).text = featured.title
            view.findViewById<TextView>(R.id.heroSubtitle).text = "From ${featured.section}"

            val heroImage: ImageView = view.findViewById(R.id.heroImage)
            featured.image?.let {
                val uri = Uri.parse("file:///android_asset/$it")
                Glide.with(context).load(uri).centerCrop().into(heroImage)
            }

            view.findViewById<View>(R.id.heroCard).setOnClickListener {
                openRecipe(featured)
            }
            view.findViewById<View>(R.id.heroViewRecipeButton).setOnClickListener {
                openRecipe(featured)
            }
        }

        // --- Quick Categories: first 8 sections for a diverse spread ---
        val quickCategories = sections.take(8)
        val quickRecycler: RecyclerView = view.findViewById(R.id.quickCategoriesRecyclerView)
        quickRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        quickRecycler.adapter = QuickCategoryAdapter(quickCategories) { section ->
            openSection(section.name)
        }

        // --- Popular Recipes: first recipe from each of the first 15 sections, for variety ---
        val popular = sections.take(15).mapNotNull { it.recipes.firstOrNull() }
        val popularRecycler: RecyclerView = view.findViewById(R.id.popularRecyclerView)
        popularRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        popularRecycler.adapter = PopularRecipeAdapter(popular) { recipe ->
            openRecipe(recipe)
        }

        view.findViewById<TextView>(R.id.seeAllPopular).setOnClickListener {
            startActivity(Intent(context, PopularRecipesActivity::class.java))
        }

        view.findViewById<ImageButton>(R.id.homeSearchIcon).setOnClickListener {
            (activity as? MainActivity)?.selectBottomNavTab(R.id.nav_search)
        }

        view.findViewById<View>(R.id.airfryerBannerCard).setOnClickListener {
            HomeToSearchBridge.pendingFilter = "Airfryer"
            (activity as? MainActivity)?.selectBottomNavTab(R.id.nav_search)
        }

        refreshRecentlyViewed()
    }

    override fun onResume() {
        super.onResume()
        if (view != null) refreshRecentlyViewed()
    }

    private fun refreshRecentlyViewed() {
        val context = requireContext()
        val view = view ?: return

        val recentFiles = RecentlyViewedManager.getRecentFiles(context)
        val section = view.findViewById<View>(R.id.recentlyViewedSection)

        if (recentFiles.isEmpty()) {
            section.visibility = View.GONE
            return
        }

        val sections = RecipeRepository.loadSections(context)
        val allRecipes = sections.flatMap { it.recipes }
        val recipeByFile = allRecipes.associateBy { it.file }
        val recentRecipes = recentFiles.mapNotNull { recipeByFile[it] }

        if (recentRecipes.isEmpty()) {
            section.visibility = View.GONE
            return
        }

        section.visibility = View.VISIBLE
        val recycler: RecyclerView = view.findViewById(R.id.recentlyViewedRecyclerView)
        recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recycler.adapter = PopularRecipeAdapter(recentRecipes) { recipe -> openRecipe(recipe) }
    }

    private fun openRecipe(recipe: Recipe) {
        val i = Intent(requireContext(), RecipeDetailActivity::class.java)
        i.putExtra(RecipeDetailActivity.EXTRA_TITLE, recipe.title)
        i.putExtra(RecipeDetailActivity.EXTRA_FILE, recipe.file)
        startActivity(i)
    }

    private fun openSection(sectionName: String) {
        val i = Intent(requireContext(), SectionRecipesActivity::class.java)
        i.putExtra(SectionRecipesActivity.EXTRA_SECTION_NAME, sectionName)
        startActivity(i)
    }
}
