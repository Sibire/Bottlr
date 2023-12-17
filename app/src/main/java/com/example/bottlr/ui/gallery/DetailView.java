package com.example.bottlr.ui.gallery;

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.bottlr.AddABottle;
import com.example.bottlr.Bottle;
import com.example.bottlr.R;
import java.io.File;
//endregion

public class DetailView extends Fragment {

    // Class fields for bottle details
    private TextView tvBottleName, tvDistillery, tvRating, tvBottleDetails, tvNotes, tvKeywords;
    private ImageView imageViewBottle;
    private String selectedBottleName;

    public DetailView() {
    }

    //region On Create Code
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.detailviewlayout, container, false);

        // Initialize views
        tvBottleName = root.findViewById(R.id.tvBottleName);
        tvDistillery = root.findViewById(R.id.tvDistillery);
        tvRating = root.findViewById(R.id.tvRating);
        tvBottleDetails = root.findViewById(R.id.tvBottleDetails);
        imageViewBottle = root.findViewById(R.id.imageViewBottle);
        tvNotes = root.findViewById(R.id.tvNotes);
        tvKeywords = root.findViewById(R.id.tvKeywords);

        // Initialize buttons
        ImageButton buyButton = root.findViewById(R.id.buyButton);
        ImageButton deleteButton = root.findViewById(R.id.deleteButton);
        ImageButton editButton = root.findViewById(R.id.editButton);

        // Load arguments on creation, for use with OnResume method after editing
        Bundle bundle = getArguments();
        if (bundle != null) {
            Bottle selectedBottle = bundle.getParcelable("selectedBottle");
            if (selectedBottle != null) {
                selectedBottleName = selectedBottle.getName();
                displayBottleDetails(selectedBottle);
            }
        }

        // Set button listeners
        buyButton.setOnClickListener(view -> handleBuyButton(selectedBottleName));
        deleteButton.setOnClickListener(view -> handleDeleteButton(selectedBottleName));
        editButton.setOnClickListener(view -> handleEditButton(selectedBottleName));

        return root;
    }

    //region On Resume Code
    @Override
    public void onResume() {
        super.onResume();
        Bottle updatedBottle = getDetailsFromName(selectedBottleName);
        if (updatedBottle != null) {
            displayBottleDetails(updatedBottle);
        }
    }

    // Fetches updated bottle details by name
    private Bottle getDetailsFromName(String bottleName) {
        File directory = getContext().getFilesDir();
        File[] files = directory.listFiles((dir, name) -> name.startsWith("bottle_") && name.endsWith(".txt"));
        for (File file : files) {
            Bottle bottle = parseBottle(file);
            if (bottle != null && bottle.getName().equals(bottleName)) {
                return bottle;
            }
        }
        return null;
    }

    // Code for loading in and displaying bottle information
    private void displayBottleDetails(Bottle bottle) {
        if (bottle != null) {
            tvBottleName.setText(bottle.getName());
            tvDistillery.setText(bottle.getDistillery());
            String rating = bottle.getRating() + " / 10";
            tvRating.setText(rating);
            String details = bottle.getType() + ", " + bottle.getRegion() + ", " + bottle.getAge() + " Year, " + bottle.getAbv() + "% ABV";
            tvBottleDetails.setText(details);
            tvNotes.setText(bottle.getNotes());
            String keywords = "Keywords:\n" + String.join(", ", bottle.getKeywords());
            tvKeywords.setText(keywords);

            if (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo")) {
                Glide.with(this)
                        .load(bottle.getPhotoUri())
                        .error(R.drawable.nodrinkimg)
                        .into(imageViewBottle);
            } else {
                imageViewBottle.setImageResource(R.drawable.nodrinkimg);
            }
        }
    }

    // Button handling methods
    private void handleBuyButton(String bottleName) {
        Bottle bottle = getDetailsFromName(bottleName);
        if (bottle != null) {
            String buyQuery = queryBuilder(bottle);
            String url = "https://www.google.com/search?tbm=shop&q=" + Uri.encode(buyQuery);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }

    private void handleDeleteButton(String bottleName) {
        Bottle bottle = getDetailsFromName(bottleName);
        if (bottle != null) {
            showDeleteConfirm(bottle);
        }
    }

    private void handleEditButton(String bottleName) {
        Bottle bottle = getDetailsFromName(bottleName);
        if (bottle != null) {
            Intent intent = new Intent(getContext(), AddABottle.class);
            intent.putExtra("bottle", bottle);
            startActivity(intent);
        }
    }

    // Confirmation Popup for Deletion
    private void showDeleteConfirm(final Bottle bottle) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Bottle")
                .setMessage("Are you sure you want to delete this bottle?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    deleteBottle(bottle);
                    // Refresh
                    getActivity().getSupportFragmentManager().popBackStack();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Deletion Code
    private void deleteBottle(Bottle bottle) {
        String axedBottle = bottle.getName();
        String filename = "bottle_" + bottle.getName() + ".txt";
        File file = new File(getContext().getFilesDir(), filename);
        if (file.exists()) {
            file.delete();
            Toast.makeText(getContext(), axedBottle + " Deleted.", Toast.LENGTH_SHORT).show();
            // Delete and Refresh, refresh handled by Gallery code.
        }
        // Error Handling
        else {
            Toast.makeText(getContext(), "Error deleting bottle.", Toast.LENGTH_SHORT).show();
        }
    }
}
