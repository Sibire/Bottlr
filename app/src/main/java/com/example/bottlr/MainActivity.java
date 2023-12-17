package com.example.bottlr;

//region Imports
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import com.google.android.material.navigation.NavigationView;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bottlr.databinding.ActivityMainBinding;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
//endregion

// TODO: Get the detail view to close properly when backing out of it

public class MainActivity extends AppCompatActivity {

    // This code is just about entirely stock from the app template provided by Android Studio
    // Don't screw with it unless you absolutely need to
    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.bottlr.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddABottle.class);
            startActivity(intent);
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            boolean handled = NavigationUI.onNavDestinationSelected(menuItem, navController);
            if (handled) {
                // Clears back stack
                if (menuItem.getItemId() != R.id.nav_gallery) { // Don't clear the gallery
                    while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStackImmediate();
                    }
                }
                drawer.closeDrawers();
            }
            return handled;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    //region App-Wide Methods

    //region Safe Read

    // Safe value read.
    public static String readValueSafe(BufferedReader br) throws IOException {
        String line = br.readLine();
        return (line != null && line.contains(": ")) ? line.split(": ", 2)[1] : "";
    }

    //endregion

    //region Parse Bottle from Save

    // Code for parsing bottle details from save files

    public static Bottle parseBottle(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String name = readValueSafe(br);

            if (name.isEmpty()) {
                return null;
                // Make sure the bottle is a valid entry, this is a double-check alongside a similar
                // Save block to handle a problem with double-saving an empty bottle
                // Probably because there's both a bitmap and URI image method
                // Keep it in as a safeguard, but check if those two really are the culprits
                // Once the app is actually functional
            }

            // Else continue

            String distillery = readValueSafe(br);
            String type = readValueSafe(br);
            String abv = readValueSafe(br);
            String age = readValueSafe(br);
            String notes = readValueSafe(br);
            String region = readValueSafe(br);
            Set<String> keywords = new HashSet<>(Arrays.asList(readValueSafe(br).split(",")));
            String rating = readValueSafe(br);
            String photoUriString = readValueSafe(br);
            Uri photoUri = (!photoUriString.equals("No photo") && !photoUriString.isEmpty()) ? Uri.parse(photoUriString) : null;

            // Return bottle
            return new Bottle(name, distillery, type, abv, age, photoUri, notes, region, keywords, rating);

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

    // Handle all share functionalities here

    //region shareBottleInfo

    // Shares bottle information
    public void shareBottleInfo(Bottle bottle) {
        if (bottle != null) {
            String shareText = createShareText(bottle);
            Uri imageUri = (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo"))
                    ? cacheImage(bottle.getPhotoUri(), this)
                    : null;
            shareBottleContent(shareText, imageUri, this);
        }
    }

    //endregion

    //region cacheImage

    // Saves the bottle image to the cache for use in sharing bottle details.
    private static Uri cacheImage(Uri imageUri, Context context) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            // It's probably fine to use time for the cache file name, but keep this in mind
            // In case it causes issues later down the line
            String fileName = "cached_bottle_image_" + System.currentTimeMillis() + ".png";
            File cacheFile = new File(context.getFilesDir(), fileName);
            try (OutputStream outputStream = Files.newOutputStream(cacheFile.toPath()))
            {
                byte[] buffer = new byte[4096]; // Adjust buffer size if needed
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return FileProvider.getUriForFile(context, "com.example.bottlr.fileprovider", cacheFile);
        }
        // Error handling
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //endregion

    //region shareBottleContent

    // For sharing Bottle details
    private static void shareBottleContent(String text, @Nullable Uri imageUri, Context context) {
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
    private static String createShareText(Bottle bottle) {
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

    //endregion

    //endregion

    //endregion

}
