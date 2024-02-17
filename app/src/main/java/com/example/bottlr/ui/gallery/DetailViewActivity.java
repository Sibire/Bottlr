package com.example.bottlr.ui.gallery;

import static com.example.bottlr.MainActivity.cacheImage;
import static com.example.bottlr.MainActivity.createShareText;
import static com.example.bottlr.MainActivity.queryBuilder;
import static com.example.bottlr.MainActivity.shareBottleContent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bottlr.AddABottle;
import com.example.bottlr.Bottle;
import com.example.bottlr.MainActivity;
import com.example.bottlr.R;
import java.io.File;

public class DetailViewActivity extends AppCompatActivity {

    // view Initialization
    private TextView bottleName, bottleDistillery, bottleDetails, bottleNotes, bottleRating, bottleKeywords;
    private ImageView bottleImage;

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
        bottleImage = findViewById(R.id.detailImageView);
        bottleImage.setScaleType(ImageView.ScaleType.FIT_CENTER); // Set the scale type of the ImageView so it displays properly
        bottleName = findViewById(R.id.tvBottleName);
        bottleDistillery = findViewById(R.id.tvDistillery);
        bottleRating = findViewById(R.id.tvRating);
        bottleDetails = findViewById(R.id.tvBottleDetails);
        bottleNotes = findViewById(R.id.tvNotes);
        bottleKeywords = findViewById(R.id.tvKeywords);
        deleteButton = findViewById(R.id.deleteButton);
        shareButton = findViewById(R.id.shareButton);
        buyButton = findViewById(R.id.buyButton);
        editButton = findViewById(R.id.editButton);

        // Get the bottle from the intent
        Bottle bottle = getIntent().getParcelableExtra("selectedBottle");

        // Set the bottle details to the views

        // Glide
        if (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo")) {
            Glide.with(this) // Use 'getContext()' if 'this' is not appropriate
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
        deleteButton.setOnClickListener(v -> showDeleteConfirm(bottle));

        // Buy button listener
        buyButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.google.com/search?tbm=shop&q=" + Uri.encode(queryBuilder(bottle))));
            startActivity(intent);
        });

        // Share button listener
        shareButton.setOnClickListener(view -> {
                    MainActivity.shareBottleInfo(bottle);
                });

        // Edit button listener
        editButton.setOnClickListener(view -> {
                // Call AddABottle to reuse existing assets
            Intent intent = new Intent(DetailViewActivity.this, AddABottle.class); // Adjust to use in an Activity
            intent.putExtra("bottle", bottle);
            startActivity(intent);
        });
    }

    //region Deletion Handling

    // Confirmation Popup
    private void showDeleteConfirm(final Bottle bottle) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Bottle")
                .setMessage("Are you sure you want to delete this bottle?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteBottle(bottle))
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Deletion Code
    private void deleteBottle(Bottle bottle) {
        String filename = "bottle_" + bottle.getName() + ".txt";
        File file = new File(getFilesDir(), filename);
        boolean deleted = file.delete();
        if (deleted) {
            Toast.makeText(this, "Bottle deleted", Toast.LENGTH_SHORT).show();
            finish(); // Go back to the gallery view
        } else {
            Toast.makeText(this, "Failed to delete bottle", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion
}