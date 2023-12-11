package com.example.bottlr.ui.home;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.bottlr.R;
import com.example.bottlr.Bottle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class HomeFragment extends Fragment {

    private TextView tvBottleName, tvDistillery, tvBottleDetails, tvNotes;
    private ImageView imageViewBottle;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the views
        tvBottleName = root.findViewById(R.id.tvBottleName);
        tvDistillery = root.findViewById(R.id.tvDistillery);
        tvBottleDetails = root.findViewById(R.id.tvBottleDetails);
        imageViewBottle = root.findViewById(R.id.imageViewBottle);
        tvNotes = root.findViewById(R.id.tvNotes);

        updateRecentBottleView();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRecentBottleView(); // Refreshes the view when fragment is resumed
    }

    private void updateRecentBottleView() {
        Bottle recentBottle = getMostRecentBottle();
        if (recentBottle != null) {
            tvBottleName.setText(recentBottle.getName());
            tvDistillery.setText(recentBottle.getDistillery());
            String details = recentBottle.getType() + ", " + recentBottle.getAge() + " Year, " + recentBottle.getAbv() + "% ABV";
            tvBottleDetails.setText(details);
            if (recentBottle.getPhotoUri() != null && !recentBottle.getPhotoUri().toString().equals("No photo")) {
                Glide.with(this) // Use 'getContext()' if 'this' is not appropriate
                        .load(recentBottle.getPhotoUri())
                        .error(R.drawable.nodrinkimg) // default image in case of error
                        .into(imageViewBottle);
            } else {
                imageViewBottle.setImageResource(R.drawable.nodrinkimg);
            }
            tvNotes.setText(recentBottle.getNotes());
        }
    }

    private Bottle getMostRecentBottle() {
        File directory = getContext().getFilesDir();
        File[] files = directory.listFiles((dir, name) -> name.startsWith("bottle_") && name.endsWith(".txt"));

        // Sort out deprecation issues
        // Low priority issue

        Bottle mostRecentBottle = null;
        long lastModifiedTime = Long.MIN_VALUE;

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.lastModified() > lastModifiedTime) {
                    Bottle bottle = parseBottle(file);
                    if (bottle != null) {
                        mostRecentBottle = bottle;
                        lastModifiedTime = file.lastModified();
                    }
                }
            }
        }

        return mostRecentBottle;
    }

    // Parsing bottle
    private Bottle parseBottle(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String name = readValueSafe(br);
            String distillery = readValueSafe(br);
            String type = readValueSafe(br);
            String abv = readValueSafe(br);
            String age = readValueSafe(br);
            String notes = readValueSafe(br);
            String photoUriString = readValueSafe(br);

            Uri photoUri = (!photoUriString.equals("No photo") && !photoUriString.isEmpty()) ? Uri.parse(photoUriString) : null;

            return new Bottle(name, distillery, type, abv, age, photoUri, notes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    // Safe value read.
    // Can probably sync this with the updated method in DetailView but make sure
    // The app has basic functionality, first. Don't break it until you can afford it.
    private String readValueSafe(BufferedReader br) throws IOException {
        String line = br.readLine();
        return (line != null && line.contains(": ")) ? line.split(": ", 2)[1] : "";
    }
}
