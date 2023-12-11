package com.example.bottlr.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.bottlr.Bottle;
import com.example.bottlr.R;

public class DetailView extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.detailviewlayout, container, false);

        // Initialize the views (similar to how you did in HomeFragment)
        TextView tvBottleName = root.findViewById(R.id.tvBottleName);
        TextView tvDistillery = root.findViewById(R.id.tvDistillery);
        TextView tvBottleDetails = root.findViewById(R.id.tvBottleDetails);
        ImageView imageViewBottle = root.findViewById(R.id.imageViewBottle);
        TextView tvNotes = root.findViewById(R.id.tvNotes);

        Bundle bundle = getArguments();
        if (bundle != null) {
            Bottle selectedBottle = bundle.getParcelable("selectedBottle");
            if (selectedBottle != null) {
                // Set text views similar to how you did in HomeFragment
                tvBottleName.setText(selectedBottle.getName());
                tvDistillery.setText(selectedBottle.getDistillery());
                String details = selectedBottle.getType() + ", " + selectedBottle.getAge() + " Year, " + selectedBottle.getAbv() + "% ABV";
                tvBottleDetails.setText(details);
                tvNotes.setText(selectedBottle.getNotes());

                // Use Glide to load the bottle image
                if (selectedBottle.getPhotoUri() != null && !selectedBottle.getPhotoUri().toString().equals("No photo")) {
                    Glide.with(this)
                            .load(selectedBottle.getPhotoUri())
                            .error(R.drawable.nodrinkimg) // Use a default image in case of an error
                            .into(imageViewBottle);
                } else {
                    imageViewBottle.setImageResource(R.drawable.nodrinkimg);
                }
            }
        }

        return root;
    }
}
