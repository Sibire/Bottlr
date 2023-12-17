package com.example.bottlr.ui.home;

//region Imports
import static com.example.bottlr.MainActivity.parseBottle;
import static com.example.bottlr.MainActivity.queryBuilder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.bottlr.R;
import com.example.bottlr.Bottle;
import java.io.File;
//endregion

public class HomeFragment extends Fragment {

    //region Initialization
    private TextView tvBottleName, tvDistillery, tvBottleDetails, tvNotes, tvRating, tvKeywords;
    private ImageView imageViewBottle;

    //endregion

    //region On Create Code

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        tvBottleName = root.findViewById(R.id.tvBottleName);
        tvDistillery = root.findViewById(R.id.tvDistillery);
        tvBottleDetails = root.findViewById(R.id.tvBottleDetails);
        imageViewBottle = root.findViewById(R.id.imageViewBottle);
        tvNotes = root.findViewById(R.id.tvNotes);
        tvRating = root.findViewById(R.id.tvRating);
        tvKeywords = root.findViewById(R.id.tvKeywords);

        // Shopping button initialization
        ImageButton buyButton = root.findViewById(R.id.buyButton);

        // Delete button initialization
        ImageButton deleteButton = root.findViewById(R.id.deleteButton);

        // Call update
        updateRecentBottleView();

        //region Shopping Query

        // Buy button listener
        buyButton.setOnClickListener(v -> {
            Bottle recentBottle = getMostRecentBottle();
            if (recentBottle != null) {
                String query = queryBuilder(recentBottle);
                String url = "https://www.google.com/search?tbm=shop&q=" + Uri.encode(query);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        // Delete button listener
        deleteButton.setOnClickListener(v -> {
            Bottle recentBottle = getMostRecentBottle();
            if (recentBottle != null) {
                showDeleteConfirm(recentBottle);
            }
        });


        //endregion

        return root;
    }

    //endregion

    //region On Resume Reload
    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Last bottle");
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

    //Delete Confirmation Dialog
    private void showDeleteConfirm(final Bottle bottle) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Bottle")
                .setMessage("Are you sure you want to delete this bottle?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteBottle(bottle))
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //Delete Bottle
    private void deleteBottle(Bottle bottle) {
        String axedBottle = bottle.getName();
        String filename = "bottle_" + bottle.getName() + ".txt";
        File file = new File(getContext().getFilesDir(), filename);
        if (file.exists()) {
            file.delete();
            Toast.makeText(getContext(), axedBottle + " Deleted.", Toast.LENGTH_SHORT).show();
            updateRecentBottleView();
            // Delete and Refresh
        }
        // Error Handling
        else {
            Toast.makeText(getContext(), "Error deleting bottle.", Toast.LENGTH_SHORT).show();
        }
    }

}
