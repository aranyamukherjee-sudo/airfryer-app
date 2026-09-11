package com.chefmagic.airfryer

/**
 * Lightweight one-shot signal: Home's promo banners set this before switching to the
 * Search tab, and SearchFragment consumes (and clears) it on creation to pre-check a filter.
 */
object HomeToSearchBridge {
    var pendingFilter: String? = null
}
