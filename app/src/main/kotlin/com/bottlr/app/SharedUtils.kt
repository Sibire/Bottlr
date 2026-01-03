package com.bottlr.app

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files

// Safely reads a value from a BufferedReader.
fun readValueSafe(br: BufferedReader): String {
    val line = br.readLine()
    return if (line != null && line.contains(": ")) line.split(": ", limit = 2)[1] else ""
}

// Parses a Bottle from a saved file.
fun parseBottle(file: File): Bottle? {
    return try {
        BufferedReader(FileReader(file)).use { br ->
            val name = readValueSafe(br)
            if (name.isEmpty()) return null

            val distillery = readValueSafe(br)
            val type = readValueSafe(br)
            val abv = readValueSafe(br)
            val age = readValueSafe(br)
            val notes = readValueSafe(br)
            val region = readValueSafe(br)
            val keywords = readValueSafe(br)
            val rating = readValueSafe(br)
            val photoUriString = readValueSafe(br)
            val photoUri = if (photoUriString.isNotBlank() && photoUriString != "No photo") Uri.parse(photoUriString) else null

            Bottle(name, distillery, type, abv, age, photoUri, notes, region, keywords, rating)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

// Parses a Cocktail from a saved file.
fun parseCocktail(file: File): Cocktail? {
    return try {
        BufferedReader(FileReader(file)).use { br ->
            val name = readValueSafe(br)
            if (name.isEmpty()) return null

            val base = readValueSafe(br)
            val mixer = readValueSafe(br)
            val juice = readValueSafe(br)
            val liqueur = readValueSafe(br)
            val garnish = readValueSafe(br)
            val extra = readValueSafe(br)
            val notes = readValueSafe(br)
            val keywords = readValueSafe(br)
            val rating = readValueSafe(br)
            val photoUriString = readValueSafe(br)
            val photoUri = if (photoUriString.isNotBlank() && photoUriString != "No photo") Uri.parse(photoUriString) else null

            Cocktail(name, base, mixer, juice, liqueur, garnish, extra, photoUri, notes, keywords, rating)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

// Builds a shopping query string for a Bottle.
fun queryBuilder(toBuy: Bottle): String {
    return buildString {
        toBuy.distillery.takeIf { !it.isNullOrEmpty() }?.let { append(" $it ") }
        append(toBuy.name)
        toBuy.age.takeIf { !it.isNullOrEmpty() }?.let { append(" $it Year") }
        toBuy.region.takeIf { !it.isNullOrEmpty() }?.let { append(" $it") }
        toBuy.type.takeIf { !it.isNullOrEmpty() }?.let { append(" $it") }
    }
}

// Shares information about a Bottle.
fun shareBottleInfo(bottle: Bottle?, context: Context) {
    bottle ?: return
    val shareText = createShareText(bottle)
    val imageUri = bottle.photoUri?.takeIf { it.toString().isNotBlank() && it.toString() != "No photo" }?.let { cacheImage(it, context) }
    shareBottleContent(shareText, imageUri, context)
}

// Shares information about a Cocktail.
fun shareCocktailInfo(cocktail: Cocktail?, context: Context) {
    cocktail ?: return
    val shareText = createShareTextCocktail(cocktail)
    val imageUri = cocktail.photoUri?.takeIf { it.toString().isNotBlank() && it.toString() != "No photo" }?.let { cacheImage(it, context) }
    shareBottleContent(shareText, imageUri, context) // Reusing the same function should be fine
}

// Caches an image for sharing.
fun cacheImage(imageUri: Uri, context: Context): Uri? {
    return try {
        context.contentResolver.openInputStream(imageUri).use { inputStream ->
            val fileName = "cached_bottle_image_${System.currentTimeMillis()}.png"
            val cacheFile = File(context.filesDir, fileName)
            Files.newOutputStream(cacheFile.toPath()).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            FileProvider.getUriForFile(context, "com.bottlr.app.fileprovider", cacheFile)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

// Shares bottle content (text and optional image).
fun shareBottleContent(text: String, imageUri: Uri?, context: Context) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"

        imageUri?.let {
            putExtra(Intent.EXTRA_STREAM, it)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Bottle Info"))
}

// Creates the text for sharing a Bottle.
fun createShareText(bottle: Bottle): String {
    return buildString {
        append("Here's what I'm drinking:\n\n")
        append(bottle.name)
        bottle.distillery.takeIf { !it.isNullOrEmpty() }?.let { append(", by $it") }
        append("\n")

        val hasAge = !bottle.age.isNullOrEmpty()
        val hasRegion = !bottle.region.isNullOrEmpty()
        val hasType = !bottle.type.isNullOrEmpty()

        if (hasAge || hasRegion || hasType) {
            if (hasAge) append("${bottle.age}-Year ")
            if (hasRegion) append("${bottle.region} ")
            if (hasType) append(bottle.type)
            append("\n")
        }

        bottle.notes.takeIf { !it.isNullOrEmpty() }?.let { append("\nMy Thoughts:\n$it") }
    }
}

// Creates the text for sharing a Cocktail.
fun createShareTextCocktail(cocktail: Cocktail): String {
    return "I'm drinking a ${cocktail.name}.\n\nThanks Bottlr!"
}

// Shows a confirmation dialog for deleting a Bottle.
fun showBottleDeleteConfirm(bottle: Bottle, context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Delete Bottle")
        .setMessage("Are you sure you want to delete this bottle?")
        .setPositiveButton(android.R.string.yes) { _, _ -> deleteBottle(bottle, context) }
        .setNegativeButton(android.R.string.no, null)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show()
}

// Shows a confirmation dialog for deleting a Cocktail.
fun showDeleteConfirmCocktail(cocktail: Cocktail, context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Delete Cocktail")
        .setMessage("Are you sure you want to delete this cocktail?")
        .setPositiveButton(android.R.string.yes) { _, _ -> deleteCocktail(cocktail, context) }
        .setNegativeButton(android.R.string.no, null)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show()
}

// Deletes a Bottle file.
fun deleteBottle(bottle: Bottle, context: Context) {
    val filename = "bottle_${bottle.name}.txt"
    val file = File(context.filesDir, filename)
    if (file.delete()) {
        Toast.makeText(context, "Bottle Deleted.", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Failed To Delete Bottle.", Toast.LENGTH_SHORT).show()
    }
}

// Deletes a Cocktail file.
fun deleteCocktail(cocktail: Cocktail, context: Context) {
    val filename = "cocktail_${cocktail.name}.txt"
    val file = File(context.filesDir, filename)
    if (file.delete()) {
        Toast.makeText(context, "Cocktail Deleted.", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Failed To Delete Cocktail.", Toast.LENGTH_SHORT).show()
    }
}

// Loads all Bottles from storage.
fun loadBottles(context: Context): MutableList<Bottle> {
    val bottles = mutableListOf<Bottle>()
    val directory = context.filesDir
    directory.listFiles()?.forEach { file ->
        if (file.isFile && file.name.startsWith("bottle_")) {
            parseBottle(file)?.let { bottles.add(it) }
        }
    }
    return bottles
}

// Loads all Cocktails from storage.
fun loadCocktails(context: Context): MutableList<Cocktail> {
    val cocktails = mutableListOf<Cocktail>()
    val directory = context.filesDir
    directory.listFiles()?.forEach { file ->
        if (file.isFile && file.name.startsWith("cocktail_")) {
            parseCocktail(file)?.let { cocktails.add(it) }
        }
    }
    return cocktails
}

// Loads all Locations from storage.
fun loadLocations(context: Context): MutableList<Location> {
    val locations = mutableListOf<Location>()
    val directory = context.filesDir
    directory.listFiles()?.forEach { file ->
        if (file.isFile && file.name.startsWith("location_")) {
            parseLocation(file)?.let {
                locations.add(it)
                Log.d("SharedUtils", "Location with name ${it.name} loaded successfully.")
            }
        }
    }
    return locations
}

// Parses a Location from a saved file.
fun parseLocation(file: File): Location? {
    return try {
        BufferedReader(FileReader(file)).use { br ->
            val timeDateAdded = readValueSafe(br)
            if (timeDateAdded.isEmpty()) return null

            val gpsCoordinates = readValueSafe(br)
            val name = readValueSafe(br)

            Log.d("SharedUtils", "Location with name $name parsed successfully.")
            Location(timeDateAdded, gpsCoordinates, name)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

// Saves an image to the device's gallery.
fun saveImageToGallery(context: Context, bottle: Bottle) {
    try {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, bottle.photoUri)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${bottle.name}_BottlrSavedImage.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        imageUri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        }

        Toast.makeText(context, "Saved As ${bottle.name}_BottlrSavedImage.jpg", Toast.LENGTH_SHORT).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Failed To Save Image", Toast.LENGTH_SHORT).show()
    }
}

fun saveImageToGalleryCocktail(context: Context, cocktail: Cocktail) {
    try {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, cocktail.photoUri)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${cocktail.name}_BottlrSavedImage.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        imageUri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        }

        Toast.makeText(context, "Saved As ${cocktail.name}_BottlrSavedImage.jpg", Toast.LENGTH_SHORT).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Failed To Save Image", Toast.LENGTH_SHORT).show()
    }
}

// Shows a dialog with options for a Location.
fun showLocationDialog(location: Location, context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Location Options")
        .setItems(arrayOf("View", "Delete", "Close")) { dialog, which ->
            when (which) {
                0 -> { // View
                    val url = "https://www.google.com/maps?tbm=map&q=${Uri.encode(location.gpsCoordinates)}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
                1 -> showLocationDeleteConfirm(location, context) // Delete
                2 -> dialog.dismiss() // Close
            }
        }
        .create()
        .show()
}

// Shows a confirmation dialog for deleting a Location.
fun showLocationDeleteConfirm(location: Location, context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Delete Location")
        .setMessage("Are you sure you want to delete this location?")
        .setPositiveButton(android.R.string.yes) { _, _ -> deleteLocation(location, context) }
        .setNegativeButton(android.R.string.no, null)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show()
}

// Deletes a Location file.
fun deleteLocation(location: Location, context: Context) {
    val filename = "location_${location.name}.txt"
    val file = File(context.filesDir, filename)
    if (file.delete()) {
        Toast.makeText(context, "Location Deleted.", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Failed To Delete Location.", Toast.LENGTH_SHORT).show()
    }
}
