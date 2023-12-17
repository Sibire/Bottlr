package com.example.bottlr.ui.gallery;

//region Imports
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
//endregion

public class DetailView extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.detailviewlayout, container, false);

        // Initializing bottle views using cannibalized code from HomeFragment
        TextView tvBottleName = root.findViewById(R.id.tvBottleName);
        TextView tvDistillery = root.findViewById(R.id.tvDistillery);
        TextView tvRating = root.findViewById(R.id.tvRating);
        TextView tvBottleDetails = root.findViewById(R.id.tvBottleDetails);
        ImageView imageViewBottle = root.findViewById(R.id.imageViewBottle);
        TextView tvNotes = root.findViewById(R.id.tvNotes);
        TextView tvKeywords = root.findViewById(R.id.tvKeywords);

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
        }

        return root;
    }
}
