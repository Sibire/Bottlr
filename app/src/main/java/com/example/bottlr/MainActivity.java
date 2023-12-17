package com.example.bottlr;

//region Imports


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
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

    //endregion

}
