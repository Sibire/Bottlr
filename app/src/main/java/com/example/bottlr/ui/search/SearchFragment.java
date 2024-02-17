package com.example.bottlr.ui.search;

import static com.example.bottlr.SharedUtils.parseBottle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bottlr.Bottle;
import com.example.bottlr.R;
import com.example.bottlr.ui.RecyclerView.BottleAdapter;
import com.example.bottlr.ui.gallery.DetailViewActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchFragment extends Fragment {

    private EditText nameField, distilleryField, typeField, abvField, ageField, notesField, regionField, ratingField, keywordsField;
    private BottleAdapter searchResultsAdapter;
    Button searchButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        nameField = view.findViewById(R.id.search_name);
        distilleryField = view.findViewById(R.id.search_distillery);
        typeField = view.findViewById(R.id.search_type);
        abvField = view.findViewById(R.id.search_abv);
        ageField = view.findViewById(R.id.search_age);
        notesField = view.findViewById(R.id.search_notes);
        regionField = view.findViewById(R.id.search_region);
        ratingField = view.findViewById(R.id.search_rating);
        keywordsField = view.findViewById(R.id.search_keywords);
        searchButton = view.findViewById(R.id.search_button);


        RecyclerView searchResultsRecyclerView = view.findViewById(R.id.search_results_recyclerview);
        // Set the LayoutManager
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize your adapter and set it to the RecyclerView
        searchResultsAdapter = new BottleAdapter(new ArrayList<>(), this::searchedBottleSelected);
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        searchButton.setOnClickListener(v -> {
            performSearch();
            Log.d("SearchFragment", "Search button clicked");
        });
        return view;
    }

    private void performSearch() {
        Log.d("SearchFragment", "performSearch() called");
        // Get search criteria from user input
        String name = nameField.getText().toString().toLowerCase();
        String distillery = distilleryField.getText().toString().toLowerCase();
        String type = typeField.getText().toString().toLowerCase();
        String abv = abvField.getText().toString().toLowerCase();
        String age = ageField.getText().toString().toLowerCase();
        String notes = notesField.getText().toString().toLowerCase();
        String region = regionField.getText().toString().toLowerCase();
        String rating = ratingField.getText().toString().toLowerCase();
        String keywordsInput = keywordsField.getText().toString().toLowerCase();

        Set<String> searchKeywords = new HashSet<>(Arrays.asList(keywordsInput.split("\\s*,\\s*")));

        // getBottlesToSearch() should retrieve the full list of Bottle objects
        List<Bottle> allBottles = getBottlesToSearch();
        Log.d("SearchFragment", "All bottles: " + allBottles);

        // Filter the list based on search criteria
        List<Bottle> filteredList = allBottles.stream()
                .filter(bottle -> bottle.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(bottle -> bottle.getDistillery().toLowerCase().contains(distillery.toLowerCase()))
                .filter(bottle -> bottle.getType().toLowerCase().contains(type.toLowerCase()))
                .filter(bottle -> bottle.getAbv().toLowerCase().contains(abv.toLowerCase()))
                .filter(bottle -> bottle.getAge().toLowerCase().contains(age.toLowerCase()))
                .filter(bottle -> bottle.getNotes().toLowerCase().contains(notes.toLowerCase()))
                .filter(bottle -> bottle.getRegion().toLowerCase().contains(region.toLowerCase()))
                .filter(bottle -> bottle.getRating().toLowerCase().contains(rating.toLowerCase()))
                .filter(bottle -> bottle.getKeywords().stream().anyMatch(keyword -> searchKeywords.contains(keyword.toLowerCase())))
                .collect(Collectors.toList());
        Log.d("SearchFragment", "Filtered list: " + filteredList);
        updateSearchResults(filteredList);
    }

    private void updateSearchResults(List<Bottle> filteredList) {
        searchResultsAdapter.setBottles(filteredList);
        searchResultsAdapter.notifyDataSetChanged();
    }

    // Get the list of bottles to search
    private List<Bottle> getBottlesToSearch() {
        List<Bottle> bottles = new ArrayList<>();
        File directory = getContext().getFilesDir();
        File[] files = ((File) directory).listFiles();

        for (File file : files) {
            if (file.isFile() && file.getName().startsWith("bottle_")) {
                Bottle bottle = parseBottle(file);
                if (bottle != null) {
                    // Make sure the bottle actually exists
                    bottles.add(bottle);
                }
            }
        }
        Log.d("SearchFragment", "Bottles to search: " + bottles);
        return bottles;
    }
    // Handle bottle selection
    public void searchedBottleSelected(int position) {
        Bottle selectedBottle = searchResultsAdapter.getBottle(position);
        if (selectedBottle != null) {
            Intent intent = new Intent(getActivity(), DetailViewActivity.class);
            intent.putExtra("selectedBottle", selectedBottle);
            startActivity(intent);
        }
    }
}
