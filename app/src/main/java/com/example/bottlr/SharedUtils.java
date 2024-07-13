package com.example.bottlr;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class SharedUtils {

    //region App-Wide Methods

    //region Safe Read

    // Safe value read.
    public static String readValueSafe(BufferedReader br) throws IOException {
        String line = br.readLine();
        return (line != null && line.contains(": ")) ? line.split(": ", 2)[1] : "";
    }

    //endregion

    //region Parse Bottle from Save
    public static Bottle parseBottle(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String name = readValueSafe(br);

            if (name.isEmpty()) {
                return null;
                // Make sure the bottle is a valid entry
            }
            String distillery = readValueSafe(br);
            String type = readValueSafe(br);
            String abv = readValueSafe(br);
            String age = readValueSafe(br);
            String notes = readValueSafe(br);
            String region = readValueSafe(br);
            String keywords = readValueSafe(br);
            String rating = readValueSafe(br);
            String photoUriString = readValueSafe(br);
            Uri photoUri = (!photoUriString.equals("No photo") && !photoUriString.isEmpty()) ? Uri.parse(photoUriString) : null;

            return new Bottle(name, distillery, type, abv, age, photoUri, notes, region, keywords, rating);

            // Exception handling
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Cocktail parseCocktail(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String name = readValueSafe(br);

            if (name.isEmpty()) {
                return null;
                // Make sure the bottle is a valid entry
            }
            String base = readValueSafe(br);
            String mixer = readValueSafe(br);
            String juice = readValueSafe(br);
            String liqueur = readValueSafe(br);
            String garnish = readValueSafe(br);
            String extra = readValueSafe(br);
            String notes = readValueSafe(br);
            String keywords = readValueSafe(br);
            String rating = readValueSafe(br);
            String photoUriString = readValueSafe(br);
            Uri photoUri = (!photoUriString.equals("No photo") && !photoUriString.isEmpty()) ? Uri.parse(photoUriString) : null;

            return new Cocktail(name, base, mixer, juice, liqueur, garnish, extra, photoUri, notes, keywords, rating);

            // Exception handling
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //endregion

    //region Shopping Query Builder

    // Search query string builder
    public static String queryBuilder(Bottle toBuy) {
        StringBuilder queryBuilder = new StringBuilder();

        // Append other fields only if they are not empty
        if (toBuy.getDistillery() != null && !toBuy.getDistillery().isEmpty()) {
            queryBuilder.append(" ").append(toBuy.getDistillery()).append(" ");
        }

        // Always include name
        queryBuilder.append(toBuy.getName());

        if (toBuy.getAge() != null && !toBuy.getAge().isEmpty()) {
            queryBuilder.append(" ").append(toBuy.getAge()).append(" Year");
        }
        if (toBuy.getRegion() != null && !toBuy.getRegion().isEmpty()) {
            queryBuilder.append(" ").append(toBuy.getRegion());
        }
        if (toBuy.getType() != null && !toBuy.getType().isEmpty()) {
            queryBuilder.append(" ").append(toBuy.getType());
        }
        return queryBuilder.toString();
    }

    //endregion

    //region Share Functionalities

    //region shareBottleInfo

    // Shares bottle information
    public static void shareBottleInfo(Bottle bottle, Context context) {
        if (bottle != null) {
            String shareText = createShareText(bottle);
            Uri imageUri = (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo"))
                    ? cacheImage(bottle.getPhotoUri(), context)
                    : null;
            shareBottleContent(shareText, imageUri, context);
        }
    }

    public static void shareCocktailInfo(Cocktail cocktail, Context context) {
        if (cocktail != null) {
            String shareText = createShareTextCocktail(cocktail);
            Uri imageUri = (cocktail.getPhotoUri() != null && !cocktail.getPhotoUri().toString().equals("No photo"))
                    ? cacheImage(cocktail.getPhotoUri(), context)
                    : null;
            shareBottleContent(shareText, imageUri, context); //should still work
        }
    }

    //endregion

    //region cacheImage

    // Saves the bottle image to the cache for use in sharing bottle details.
    public static Uri cacheImage(Uri imageUri, Context context) {
        try {
            // Changed to use try-with-resources to automatically close the InputStream
            try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
                String fileName = "cached_bottle_image_" + System.currentTimeMillis() + ".png";
                File cacheFile = new File(context.getFilesDir(), fileName);
                try (OutputStream outputStream = Files.newOutputStream(cacheFile.toPath())) {
                    byte[] buffer = new byte[4096]; // Adjust this buffer size if needed
                    int bytesRead;
                    while (true) {
                        assert inputStream != null;
                        if ((bytesRead = inputStream.read(buffer)) == -1) break;
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                return FileProvider.getUriForFile(context, "com.example.bottlr.fileprovider", cacheFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //endregion

    //region shareBottleContent

    // For sharing Bottle details
    public static void shareBottleContent(String text, @Nullable Uri imageUri, Context context) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setType("text/plain");

        if (imageUri != null) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Bottle Info"));
    }

    //endregion

    //region Bottle ShareText Builder

    // Constructs a share post using pertinent bottle details, if available.
    public static String createShareText(Bottle bottle) {
        StringBuilder shareText = new StringBuilder("Here's what I'm drinking:\n\n");

        // Always include the bottle name, since it's always there
        shareText.append(bottle.getName());
        // Include the distillery statement if one exists
        if (bottle.getDistillery() != null && !bottle.getDistillery().isEmpty()) {
            shareText.append(", by ").append(bottle.getDistillery());
        }
        shareText.append("\n");

        // Check for Age, Region, and Type fields
        boolean hasAge = bottle.getAge() != null && !bottle.getAge().isEmpty();
        boolean hasRegion = bottle.getRegion() != null && !bottle.getRegion().isEmpty();
        boolean hasType = bottle.getType() != null && !bottle.getType().isEmpty();

        if (hasAge || hasRegion || hasType) {
            // Get to work constructing the post with available data
            if (hasAge) {
                // Add age statement, if applicable
                shareText.append(bottle.getAge()).append("-Year ");
            }
            if (hasRegion) {
                // Add region, if applicable
                shareText.append(bottle.getRegion()).append(" ");
            }
            if (hasType) {
                // Add type, if applicable
                shareText.append(bottle.getType());
            }
            shareText.append("\n");
        }

        // Adding notes, if applicable
        if (bottle.getNotes() != null && !bottle.getNotes().isEmpty()) {
            shareText.append("\nMy Thoughts:\n").append(bottle.getNotes());
        }

        // Finalize string. Should look like this:
        //
        // Here's what I'm drinking:
        //
        // The Peat Seat, by Speynnigan's Wake
        // 5-Year Speyside Scotch
        //
        // My Thoughts:
        // Peaty, smoky, nice notes of caramel.
        //
        // Any missing details should be omitted in formatting.

        return shareText.toString();
    }

    public static String createShareTextCocktail(Cocktail cocktail) { //copied from bottle
        StringBuilder shareText = new StringBuilder("I'm drinking a ");
        shareText.append(cocktail.getName());
        shareText.append(".\n\n");
        shareText.append("Thanks Bottlr!");

        // Finalize string. Should look like this:
        //
        // I'm drinking a Gin and Tonic.
        //
        // Thanks Bottlr!

        return shareText.toString();
    }

    //endregion

    //endregion

    //region Deletion Handling

    // Confirmation Popup
    public static void showDeleteConfirm(final Bottle bottle, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Bottle")
                .setMessage("Are you sure you want to delete this bottle?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteBottle(bottle, context))
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void showDeleteConfirmCocktail(final Cocktail cocktail, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Cocktail")
                .setMessage("Are you sure you want to delete this cocktail?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteBottle(cocktail, context))
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Deletion Code
    public static void deleteBottle(Bottle bottle, Context context) {
        String filename = "bottle_" + bottle.getName() + ".txt";
        File file = new File(context.getFilesDir(), filename);
        boolean deleted = file.delete();
        if (deleted) {
            Toast.makeText(context, "Bottle Deleted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed To Delete Bottle.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void deleteCocktail(Cocktail cocktail, Context context) {
        String filename = "cocktail_" + cocktail.getName() + ".txt";
        File file = new File(context.getFilesDir(), filename);
        boolean deleted = file.delete();
        if (deleted) {
            Toast.makeText(context, "Cocktail Deleted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed To Delete Cocktail.", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    //region Bottle List Loading
    public static List<Bottle> loadBottles(Context context) {
        List<Bottle> bottles = new ArrayList<>();
        File directory = context.getFilesDir();
        File[] files = directory.listFiles();

        assert files != null;
        for (File file : files) {
            if (file.isFile() && file.getName().startsWith("bottle_")) {
                Bottle bottle = parseBottle(file);
                if (bottle != null) {
                    bottles.add(bottle); // Add the bottle to the list
                }
            }
        }
        return bottles;
    }

    public static List<Cocktail> loadCocktails(Context context) {
        List<Cocktail> cocktails = new ArrayList<>();
        File directory = context.getFilesDir();
        File[] files = directory.listFiles();

        assert files != null;
        for (File file : files) {
            if (file.isFile() && file.getName().startsWith("cocktail_")) {
                Cocktail cocktail = parseCocktail(file);
                if (cocktail != null) {
                    cocktails.add(cocktail); // Add the bottle to the list
                }
            }
        }
        return cocktails;
    }
    //endregion

    //region Save Image to Gallery
    public static void saveImageToGallery(Context context, Bottle bottle) {
        // Convert the Uri to a Bitmap
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), bottle.getPhotoUri());

            // Prepare the values for the new image
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, bottle.getName() + "_BottlrSavedImage.jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            // Add the new image to the MediaStore.Images collection
            Uri imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (imageUri != null) {
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(imageUri)) {
                    assert outputStream != null;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Failed To Save Image", Toast.LENGTH_SHORT).show();
                }
            }
            // TODO: Get this notification working
            //else {
            //    Toast.makeText(context, "No Image To Save", Toast.LENGTH_SHORT).show();
            //}

            // Show a toast message
            Toast.makeText(context, "Saved As " + bottle.getName() + "_BottlrSavedImage.jpg", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed To Save Image", Toast.LENGTH_SHORT).show();
        }
    }
    public static void saveImageToGalleryCocktail(Context context, Cocktail cocktail) {
        // Convert the Uri to a Bitmap
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), cocktail.getPhotoUri());

            // Prepare the values for the new image
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, cocktail.getName() + "_BottlrSavedImage.jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            // Add the new image to the MediaStore.Images collection
            Uri imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (imageUri != null) {
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(imageUri)) {
                    assert outputStream != null;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Failed To Save Image", Toast.LENGTH_SHORT).show();
                }
            }
            // TODO: Get this notification working
            //else {
            //    Toast.makeText(context, "No Image To Save", Toast.LENGTH_SHORT).show();
            //}

            // Show a toast message
            Toast.makeText(context, "Saved As " + cocktail.getName() + "_BottlrSavedImage.jpg", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed To Save Image", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    //endregion
}