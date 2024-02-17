package com.example.bottlr.ui.gallery;

// THIS FRAGMENT IS CURRENTLY DEPRECATED
// AT SOME POINT I MAY BRING IT BACK
// WE'RE SAYING FUCK IT WE BALL TO USING AN ACTIVITY

// IF I HAVE TO KEEP DEBUGGING THIS I'M DROPPING OUT AND BECOMING A WELDER


//region Imports
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
import com.example.bottlr.MainActivity;
import com.example.bottlr.R;
import java.io.File;
//endregion

public class DetailView extends Fragment {

    //region On Create Code
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.detail_view_activity, container, false);

        // Shopping button initialization
        ImageButton buyButton = root.findViewById(R.id.buyButton);

        // Delete button initialization
        ImageButton deleteButton = root.findViewById(R.id.deleteButton);

        // Edit button initialization
        ImageButton editButton = root.findViewById(R.id.editButton);

        // Share button initialization
        ImageButton shareButton = root.findViewById(R.id.shareButton);

        //region Load Bottle Details

        // Initializing bottle views using cannibalized code from HomeFragment
        TextView tvBottleName = root.findViewById(R.id.tvBottleName);
        TextView tvDistillery = root.findViewById(R.id.tvDistillery);
        TextView tvRating = root.findViewById(R.id.tvRating);
        TextView tvBottleDetails = root.findViewById(R.id.tvBottleDetails);
        ImageView imageViewBottle = root.findViewById(R.id.imageViewBottle);
        TextView tvNotes = root.findViewById(R.id.tvNotes);
        TextView tvKeywords = root.findViewById(R.id.tvKeywords);

        // Main bottle load code

        Bundle bundle = getArguments();
        if (bundle != null) {
            Bottle selectedBottle = bundle.getParcelable("selectedBottle");
            if (selectedBottle != null) {

                // More HomeFragment cannibalized code
                tvBottleName.setText(selectedBottle.getName());
                tvDistillery.setText(selectedBottle.getDistillery());
                String rating = selectedBottle.getRating() + " / 10";
                tvRating.setText(rating);
                String details = selectedBottle.getType() + ", " + selectedBottle.getRegion() + ", " + selectedBottle.getAge() + " Year, " + selectedBottle.getAbv() + "% ABV";
                tvBottleDetails.setText(details);
                tvNotes.setText(selectedBottle.getNotes());
                String keywords = "Keywords:\n" + String.join(", ", selectedBottle.getKeywords());
                tvKeywords.setText(keywords);

                // Loading images using the Glide Library
                if (selectedBottle.getPhotoUri() != null && !selectedBottle.getPhotoUri().toString().equals("No photo")) {
                    Glide.with(this)
                            .load(selectedBottle.getPhotoUri())
                            .error(R.drawable.nodrinkimg) // Default image in case something goes wrong
                            .into(imageViewBottle);
                } else {
                    imageViewBottle.setImageResource(R.drawable.nodrinkimg);
                }
            }

            // End main bottle load code


            //region Delete Button

            // Delete button listener
            deleteButton.setOnClickListener(view -> showDeleteConfirm(selectedBottle));

            //endregion

            //region Edit Button

            // Edit button listener

            editButton.setOnClickListener(view -> {
                if (selectedBottle != null) {
                    // Call AddABottle to reuse existing assets
                    Intent intent = new Intent(getContext(), AddABottle.class);
                    intent.putExtra("bottle", selectedBottle);
                    startActivity(intent);
                }
            });

            //endregion

            //region Shopping Query

            // Buy button listener
            buyButton.setOnClickListener(view -> {
                if (selectedBottle != null) {
                    // Creates a search query from pertinent bottle details
                    String buyQuery = queryBuilder(selectedBottle);
                    String url = "https://www.google.com/search?tbm=shop&q=" + Uri.encode(buyQuery); // Shopping focused query
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));

                    // Searches using query
                    startActivity(intent);
                }
            });

            //endregion

            //region Share Button

            // Share button listener
            // Janky as hell because I think I broke some import cleanliness
            // But if I import MainActivity directly it seems to work

            shareButton.setOnClickListener(view -> {
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    Bottle bottleToShare = bundle.getParcelable("selectedBottle");
                    if (bottleToShare != null) {
                        shBottleInfo(bottleToShare);
                    }
                }
            });

            //endregion

        }
        return root;
    }

    //endregion

    //region Deletion Handling

    // Confirmation Popup
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

    //endregion

}

