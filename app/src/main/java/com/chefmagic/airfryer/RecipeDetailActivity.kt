package com.chefmagic.airfryer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class RecipeDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_FILE = "extra_file"
    }

    private lateinit var webView: WebView
    private lateinit var toolbar: Toolbar
    private var recipeFile: String = ""
    private var recipeTitle: String = ""
    private var favoriteMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        recipeTitle = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.app_name)
        recipeFile = intent.getStringExtra(EXTRA_FILE) ?: return

        toolbar = findViewById(R.id.detailToolbar)
        toolbar.title = recipeTitle
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = false
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.allowFileAccess = true

        webView.loadUrl("file:///android_asset/$recipeFile")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        favoriteMenuItem = menu?.findItem(R.id.action_favorite)
        updateFavoriteIcon()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_favorite) {
            FavoritesManager.toggleFavorite(this, recipeFile)
            updateFavoriteIcon()
            return true
        }
        if (item.itemId == R.id.action_share) {
            shareRecipe()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareRecipe() {
        val shareText = "Check out \"$recipeTitle\" on ${getString(R.string.app_name)} 🍽"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, recipeTitle)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share recipe"))
    }

    private fun updateFavoriteIcon() {
        val isFav = FavoritesManager.isFavorite(this, recipeFile)
        favoriteMenuItem?.setIcon(
            if (isFav) R.drawable.ic_star_filled else R.drawable.ic_star_outline
        )
    }
}
