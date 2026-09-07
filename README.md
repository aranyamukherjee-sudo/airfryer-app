# Airfryer ChefMagic — Recipe Book App

A native Android app for the *Airfryer & Wonderchef Chef Magic Recipe Book*, generated from your epub.

## What's inside

- **314 recipes**, grouped into **22 sections** (Snacks, Curries, Baking & Sweets, Homemade Basics & Dairy, etc.), exactly matching the current order of your epub — including the dough/pastry recipes now living under Homemade Basics & Dairy, and the corrected instant-yeast temperature/time notes.
- Every recipe's photo, ingredients, and numbered Chef Magic steps, styled to match the book's look (Lora + Poppins fonts, warm color palette).
- **Search** — live filter by recipe title.
- **Favorites** — tap the star on any recipe to save it; see them all under the *Favorites* tab.
- Works fully **offline** — all text, photos and fonts are bundled into the app (no internet needed after install).

## Project structure

```
AirfryerApp/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/chefmagic/airfryer/   ← Kotlin source
│       ├── res/                           ← layouts, colors, icons
│       └── assets/
│           ├── recipes.json               ← index of sections/recipes
│           ├── style.css                  ← shared recipe stylesheet
│           ├── fonts/                     ← Lora & Poppins .ttf
│           ├── images/                    ← 314 recipe photos
│           └── recipes/                   ← 314 standalone HTML recipe pages
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## How to build it

1. Install **Android Studio** (Koala or newer) if you don't have it: https://developer.android.com/studio
2. Unzip this project, then in Android Studio choose **File → Open** and select the `AirfryerApp` folder.
3. Android Studio will detect there's no Gradle wrapper jar bundled (it's a binary file, left out of this hand-built package) and will offer to generate it automatically — click **OK / Use Gradle wrapper**. This step needs an internet connection once, to download Gradle and the app's dependencies (AndroidX, Material Components, Glide) from Google's and Maven's servers.
4. Let Gradle sync finish, then click **Run ▶** with a device or emulator connected (minimum Android 7.0 / API 24).

That's it — no other setup needed. If Android Studio doesn't offer to create the wrapper automatically, you can do it from a terminal (with Gradle installed) by running `gradle wrapper --gradle-version 8.7` inside the `AirfryerApp` folder.

## Notes on the recipe photos

The 9 photos you supplied earlier (dough, marble cake, plain cake, puff pastry, etc.) are included, attached to their matching recipes, exactly as placed in the epub. The other 305 photos come from the epub's existing image set.

## Extending it later

- To add a new recipe: add a photo to `assets/images/`, an HTML file to `assets/recipes/` (copy the structure of an existing one), and an entry to `assets/recipes.json`.
- To restyle the look of a recipe page: edit `assets/style.css` — no code changes needed, it's plain CSS loaded by the in-app WebView.
- To change app colors/branding: edit `res/values/colors.xml`, `res/values/themes.xml`, and `res/drawable/ic_launcher.xml`.
