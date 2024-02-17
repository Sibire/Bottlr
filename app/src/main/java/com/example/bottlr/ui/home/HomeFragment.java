package com.example.bottlr.ui.home;

import static com.example.bottlr.SharedUtils.parseBottle;
import static com.example.bottlr.SharedUtils.queryBuilder;
import static com.example.bottlr.SharedUtils.shareBottleInfo;
import static com.example.bottlr.SharedUtils.showDeleteConfirm;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.bottlr.AddABottle;
import com.example.bottlr.MainActivity;
import com.example.bottlr.R;
import com.example.bottlr.Bottle;
import java.io.File;

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
        imageViewBottle.setScaleType(ImageView.ScaleType.FIT_CENTER); // Set the scale type of the ImageView so it displays properly
        tvNotes = root.findViewById(R.id.tvNotes);
        tvRating = root.findViewById(R.id.tvRating);
        tvKeywords = root.findViewById(R.id.tvKeywords);

        // Shopping button initialization
        ImageButton buyButton = root.findViewById(R.id.buyButton);

        // Delete button initialization
        ImageButton deleteButton = root.findViewById(R.id.deleteButton);

        // Edit button initialization
        ImageButton editButton = root.findViewById(R.id.editButton);

        // Share button initialization
        ImageButton shareButton = root.findViewById(R.id.shareButton);

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
                showDeleteConfirm(recentBottle, getContext());
            }
        });

        // Edit button listener

        editButton.setOnClickListener(view -> {
            if (getMostRecentBottle() != null) {
                // Call AddABottle to reuse existing assets
                Intent intent = new Intent(getContext(), AddABottle.class);
                intent.putExtra("bottle", getMostRecentBottle());
                startActivity(intent);
            }
        });

        // Share button listener


        shareButton.setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                Bottle bottleToShare = getMostRecentBottle();
                if (bottleToShare != null) {
                    shareBottleInfo(bottleToShare, getContext());
                }
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

    //Updates recent bottle for OnResume call

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
}
