package com.chefmagic.airfryer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)

        if (savedInstanceState == null) {
            showFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_categories -> CategoriesFragment()
                R.id.nav_search -> SearchFragment()
                R.id.nav_favorites -> FavoritesFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            showFragment(fragment)
            true
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /** Lets fragments (e.g. Home's "See All") switch tabs programmatically. */
    fun selectBottomNavTab(itemId: Int) {
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = itemId
    }
}
