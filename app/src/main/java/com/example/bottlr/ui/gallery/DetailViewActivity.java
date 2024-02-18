package com.example.bottlr.ui.gallery;

import static com.example.bottlr.SharedUtils.queryBuilder;
import static com.example.bottlr.SharedUtils.shareBottleInfo;
import static com.example.bottlr.SharedUtils.showDeleteConfirm;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bottlr.AddABottle;
import com.example.bottlr.Bottle;
import com.example.bottlr.R;

// TODO: I just did some testing on a physical device. While the app functions as intended at the moment in the emulator,
//  using it on a physical device causes two distinct and functionality disabling bugs:
//  When a user loads the detail view, or views the recent bottle from the home page,
//  the text fields of the bottle details are not being populated.
//  Additionally, if no image has been saved, the default image will display for the gallery and search result preview,
//  but not in the homefragment or detailviewactivity

public class DetailViewActivity extends AppCompatActivity {

    // Button initialization

    ImageButton backButton;
    ImageButton deleteButton;
    ImageButton shareButton;
    ImageButton buyButton;
    ImageButton editButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.detail_view_activity);

        // Find the views
        ImageView bottleImage = findViewById(R.id.detailImageView);
        bottleImage.setScaleType(ImageView.ScaleType.FIT_CENTER); // Set the scale type of the ImageView so it displays properly
        // view Initialization
        TextView bottleName = findViewById(R.id.tvBottleName);
        TextView bottleDistillery = findViewById(R.id.tvDistillery);
        TextView bottleRating = findViewById(R.id.tvRating);
        TextView bottleDetails = findViewById(R.id.tvBottleDetails);
        TextView bottleNotes = findViewById(R.id.tvNotes);
        TextView bottleKeywords = findViewById(R.id.tvKeywords);
        deleteButton = findViewById(R.id.deleteButton);
        shareButton = findViewById(R.id.shareButton);
        buyButton = findViewById(R.id.buyButton);
        editButton = findViewById(R.id.editButton);
        backButton = findViewById(R.id.backButton);

        // Get the bottle from the intent
        Bottle bottle = getIntent().getParcelableExtra("selectedBottle");

        // Set the bottle details to the views

        // Glide
        if (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo")) {
            Glide.with(this)
                    .load(bottle.getPhotoUri())
                    .error(R.drawable.nodrinkimg) // Default image in case of error
                    .into(bottleImage);
        }
        // Image not working debugging code
        Log.d("DetailViewActivity", "Image URI: " + bottle.getPhotoUri());
        // Other fields
        bottleName.setText(bottle.getName());
        bottleDistillery.setText(bottle.getDistillery());
        String rating = bottle.getRating() + " / 10";
        bottleRating.setText(rating);
        String details = bottle.getType() + ", " + bottle.getRegion() + ", " + bottle.getAge() + " Year, " + bottle.getAbv() + "% ABV";
        bottleDetails.setText(details);
        bottleNotes.setText(bottle.getNotes());
        String keywords = "Keywords:\n" + String.join(", ", bottle.getKeywords());
        bottleKeywords.setText(keywords);

        // Delete button listener
        deleteButton.setOnClickListener(v -> showDeleteConfirm(bottle, this));

        // Buy button listener
        buyButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.google.com/search?tbm=shop&q=" + Uri.encode(queryBuilder(bottle))));
            startActivity(intent);
        });

        // Share button listener
        shareButton.setOnClickListener(view -> shareBottleInfo(bottle, this));

        // Edit button listener
        editButton.setOnClickListener(view -> {
                // Call AddABottle to reuse existing assets
            Intent intent = new Intent(DetailViewActivity.this, AddABottle.class);
            intent.putExtra("bottle", bottle);
            startActivity(intent);
        });

        // Back button listener
        backButton.setOnClickListener(v -> finish());
    }
}