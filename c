[1mdiff --git a/app/src/main/java/com/chefmagic/airfryer/RecipeDetailActivity.kt b/app/src/main/java/com/chefmagic/airfryer/RecipeDetailActivity.kt[m
[1mindex 2ae53aa..c64faf2 100644[m
[1m--- a/app/src/main/java/com/chefmagic/airfryer/RecipeDetailActivity.kt[m
[1m+++ b/app/src/main/java/com/chefmagic/airfryer/RecipeDetailActivity.kt[m
[36m@@ -5,6 +5,7 @@[m [mimport android.net.Uri[m
 import android.os.Bundle[m
 import android.view.LayoutInflater[m
 import android.view.View[m
[32m+[m[32mimport android.widget.CheckBox[m
 import android.widget.ImageButton[m
 import android.widget.ImageView[m
 import android.widget.LinearLayout[m
[36m@@ -32,6 +33,7 @@[m [mclass RecipeDetailActivity : AppCompatActivity() {[m
     private lateinit var servingsCountText: TextView[m
     private lateinit var ingredientsContainer: LinearLayout[m
     private val ingredientRowViews = mutableListOf<Pair<Ingredient, TextView>>()[m
[32m+[m[32m    private val selectedIngredientIndices = mutableSetOf<Int>()[m
     private lateinit var favoriteButton: ImageButton[m
 [m
     override fun onCreate(savedInstanceState: Bundle?) {[m
[36m@@ -101,7 +103,16 @@[m [mclass RecipeDetailActivity : AppCompatActivity() {[m
 [m
     private fun addIngredientsToShoppingList() {[m
         val r = recipe ?: return[m
[31m-        val items = r.ingredients.map { ingredient ->[m
[32m+[m
[32m+[m[32m        if (selectedIngredientIndices.isEmpty()) {[m
[32m+[m[32m            android.widget.Toast.makeText([m
[32m+[m[32m                this, "Select at least one ingredient first", android.widget.Toast.LENGTH_SHORT[m
[32m+[m[32m            ).show()[m
[32m+[m[32m            return[m
[32m+[m[32m        }[m
[32m+[m
[32m+[m[32m        val items = selectedIngredientIndices.sorted().map { index ->[m
[32m+[m[32m            val ingredient = r.ingredients[index][m
             val quantity = IngredientScaler.scaledQuantityOnly(ingredient, currentServings, r.originalServings)[m
             val name = IngredientScaler.displayName(ingredient)[m
             val category = IngredientScaler.categoryName(ingredient)[m
[36m@@ -258,10 +269,12 @@[m [mclass RecipeDetailActivity : AppCompatActivity() {[m
         ingredientsContainer = findViewById(R.id.ingredientsContainer)[m
         ingredientsContainer.removeAllViews()[m
         ingredientRowViews.clear()[m
[32m+[m[32m        selectedIngredientIndices.clear()[m
 [m
         val inflater = LayoutInflater.from(this)[m
[31m-        for (ingredient in r.ingredients) {[m
[32m+[m[32m        for ((index, ingredient) in r.ingredients.withIndex()) {[m
             val row = inflater.inflate(R.layout.item_ingredient_row, ingredientsContainer, false)[m
[32m+[m[32m            val checkbox: CheckBox = row.findViewById(R.id.ingredientCheckbox)[m
             val nameView: TextView = row.findViewById(R.id.ingredientText)[m
             val qtyView: TextView = row.findViewById(R.id.ingredientQuantity)[m
             val swatch: View = row.findViewById(R.id.ingredientSwatch)[m
[36m@@ -271,6 +284,11 @@[m [mclass RecipeDetailActivity : AppCompatActivity() {[m
                 android.graphics.Color.parseColor(IngredientScaler.categoryColor(ingredient))[m
             )[m
 [m
[32m+[m[32m            checkbox.isChecked = false[m
[32m+[m[32m            checkbox.setOnCheckedChangeListener { _, isChecked ->[m
[32m+[m[32m                if (isChecked) selectedIngredientIndices.add(index) else selectedIngredientIndices.remove(index)[m
[32m+[m[32m            }[m
[32m+[m
             ingredientsContainer.addView(row)[m
             ingredientRowViews.add(ingredient to qtyView)[m
         }[m
[1mdiff --git a/app/src/main/java/com/chefmagic/airfryer/ShoppingListActivity.kt b/app/src/main/java/com/chefmagic/airfryer/ShoppingListActivity.kt[m
[1mindex 3b80ffb..a9f63b1 100644[m
[1m--- a/app/src/main/java/com/chefmagic/airfryer/ShoppingListActivity.kt[m
[1m+++ b/app/src/main/java/com/chefmagic/airfryer/ShoppingListActivity.kt[m
[36m@@ -6,6 +6,7 @@[m [mimport android.widget.ImageButton[m
 import android.widget.TextView[m
 import androidx.appcompat.app.AlertDialog[m
 import androidx.appcompat.app.AppCompatActivity[m
[32m+[m[32mimport androidx.recyclerview.widget.ItemTouchHelper[m
 import androidx.recyclerview.widget.LinearLayoutManager[m
 import androidx.recyclerview.widget.RecyclerView[m
 import com.google.android.material.chip.Chip[m
[36m@@ -29,10 +30,29 @@[m [mclass ShoppingListActivity : AppCompatActivity() {[m
         emptyText = findViewById(R.id.shoppingEmptyText)[m
         chipGroup = findViewById(R.id.shoppingCategoryChipRow)[m
         recyclerView.layoutManager = LinearLayoutManager(this)[m
[32m+[m[32m        attachSwipeToDelete()[m
 [m
         refresh()[m
     }[m
 [m
[32m+[m[32m    private fun attachSwipeToDelete() {[m
[32m+[m[32m        val callback = object : ItemTouchHelper.SimpleCallback([m
[32m+[m[32m            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT[m
[32m+[m[32m        ) {[m
[32m+[m[32m            override fun onMove([m
[32m+[m[32m                rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder[m
[32m+[m[32m            ): Boolean = false[m
[32m+[m
[32m+[m[32m            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {[m
[32m+[m[32m                val adapter = recyclerView.adapter as? ShoppingListAdapter ?: return[m
[32m+[m[32m                val item = adapter.getItem(viewHolder.bindingAdapterPosition)[m
[32m+[m[32m                ShoppingListManager.removeItem(this@ShoppingListActivity, item.id)[m
[32m+[m[32m                refresh()[m
[32m+[m[32m            }[m
[32m+[m[32m        }[m
[32m+[m[32m        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)[m
[32m+[m[32m    }[m
[32m+[m
     override fun onResume() {[m
         super.onResume()[m
         refresh()[m
[1mdiff --git a/app/src/main/java/com/chefmagic/airfryer/ShoppingListAdapter.kt b/app/src/main/java/com/chefmagic/airfryer/ShoppingListAdapter.kt[m
[1mindex 13e8624..4bdabd1 100644[m
[1m--- a/app/src/main/java/com/chefmagic/airfryer/ShoppingListAdapter.kt[m
[1m+++ b/app/src/main/java/com/chefmagic/airfryer/ShoppingListAdapter.kt[m
[36m@@ -24,6 +24,8 @@[m [mclass ShoppingListAdapter([m
 [m
     override fun getItemCount(): Int = items.size[m
 [m
[32m+[m[32m    fun getItem(position: Int): ShoppingListItem = items[position][m
[32m+[m
     class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {[m
         private val checkbox: CheckBox = view.findViewById(R.id.shoppingItemCheckbox)[m
         private val nameText: TextView = view.findViewById(R.id.shoppingItemName)[m
[1mdiff --git a/app/src/main/res/layout/item_ingredient_row.xml b/app/src/main/res/layout/item_ingredient_row.xml[m
[1mindex 6e21aa3..429bd05 100644[m
[1m--- a/app/src/main/res/layout/item_ingredient_row.xml[m
[1m+++ b/app/src/main/res/layout/item_ingredient_row.xml[m
[36m@@ -7,6 +7,13 @@[m
     android:paddingTop="9dp"[m
     android:paddingBottom="9dp">[m
 [m
[32m+[m[32m    <CheckBox[m
[32m+[m[32m        android:id="@+id/ingredientCheckbox"[m
[32m+[m[32m        android:layout_width="wrap_content"[m
[32m+[m[32m        android:layout_height="wrap_content"[m
[32m+[m[32m        android:layout_marginEnd="10dp"[m
[32m+[m[32m        android:checked="false" />[m
[32m+[m
     <View[m
         android:id="@+id/ingredientSwatch"[m
         android:layout_width="14dp"[m
