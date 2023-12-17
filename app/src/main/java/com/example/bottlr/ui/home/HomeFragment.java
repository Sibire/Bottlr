package com.example.bottlr.ui.home;

//region Imports
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.bottlr.R;
import com.example.bottlr.Bottle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
//endregion

public class HomeFragment extends Fragment {

    //region Initialization
    private TextView tvBottleName, tvDistillery, tvBottleDetails, tvNotes, tvRegion, tvRating, tvKeywords;
    private ImageView imageViewBottle;

    //endregion

    //region On Create Code

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the views
        tvBottleName = root.findViewById(R.id.tvBottleName);
        tvDistillery = root.findViewById(R.id.tvDistillery);
        tvBottleDetails = root.findViewById(R.id.tvBottleDetails);
        imageViewBottle = root.findViewById(R.id.imageViewBottle);
        tvNotes = root.findViewById(R.id.tvNotes);
        tvRating = root.findViewById(R.id.tvRating);
        tvKeywords = root.findViewById(R.id.tvKeywords);

        updateRecentBottleView();

        return root;
    }

    //endregion

    //region On Resume Reload
    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Last bottle");
        updateRecentBottleView();
    }
    //endregion

    //region Update Recent

    private void updateRecentBottleView() {
        Bottle recentBottle = getMostRecentBottle();
        if (recentBottle != null) {
            tvBottleName.setText(recentBottle.getName());
            tvDistillery.setText(recentBottle.getDistillery());
            String rating = recentBottle.getRating() + " / 10";
            tvRating.setText(rating);
            String details = recentBottle.getType() + ", " + recentBottle.getRegion() + ", " + recentBottle.getAge() + " Year, " + recentBottle.getAbv() + "% ABV";
            tvBottleDetails.setText(details);

            // Get image
            if (recentBottle.getPhotoUri() != null && !recentBottle.getPhotoUri().toString().equals("No photo")) {
                Glide.with(this) // Use 'getContext()' if 'this' is not appropriate
                        .load(recentBottle.getPhotoUri())
                        .error(R.drawable.nodrinkimg) // Default image in case of error
                        .into(imageViewBottle);
            }

            // Handle no image entries
            else {
                imageViewBottle.setImageResource(R.drawable.nodrinkimg);
            }
            tvNotes.setText(recentBottle.getNotes());
            String keywords = "Keywords:\n" + String.join(", ", recentBottle.getKeywords());
            tvKeywords.setText(keywords);
        }
    }

    //endregion

    //region Get Most Recent
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
    //endregion

    //region Parse Bottle from Save

    // Code for parsing bottle details from save files

    private Bottle parseBottle(File file) {
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

    //region Safe Read

    // Safe value read.
    private String readValueSafe(BufferedReader br) throws IOException {
        String line = br.readLine();
        return (line != null && line.contains(": ")) ? line.split(": ", 2)[1] : "";
    }
    //endregion

}
