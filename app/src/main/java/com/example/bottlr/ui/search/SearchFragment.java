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
import android.widget.Toast;

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
import java.util.List;
import java.util.stream.Collectors;

// TODO: Re-Import when keyword searches are working.
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;

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

        // TODO: Add back in when keyword searching is fixed
        //String keywordsInput = keywordsField.getText().toString().toLowerCase();
        //Set<String> searchKeywords = new HashSet<>(Arrays.asList(keywordsInput.split("\\s*,\\s*")));

        // getBottlesToSearch() should retrieve the full list of Bottle objects
        List<Bottle> allBottles = getBottlesToSearch();
        Log.d("SearchFragment", "All bottles: " + allBottles);

        // Filter the list based on search criteria
        List<Bottle> filteredList = allBottles.stream()
                .filter(bottle -> name.isEmpty() || (bottle.getName() != null && bottle.getName().trim().toLowerCase().contains(name.trim().toLowerCase())))
                .filter(bottle -> distillery.isEmpty() || (bottle.getDistillery() != null && bottle.getDistillery().trim().toLowerCase().contains(distillery.trim().toLowerCase())))
                .filter(bottle -> type.isEmpty() || (bottle.getType() != null && bottle.getType().trim().toLowerCase().contains(type.trim().toLowerCase())))
                .filter(bottle -> abv.isEmpty() || (bottle.getAbv() != null && bottle.getAbv().trim().toLowerCase().contains(abv.trim().toLowerCase())))
                .filter(bottle -> age.isEmpty() || (bottle.getAge() != null && bottle.getAge().trim().toLowerCase().contains(age.trim().toLowerCase())))
                .filter(bottle -> notes.isEmpty() || (bottle.getNotes() != null && bottle.getNotes().trim().toLowerCase().contains(notes.trim().toLowerCase())))
                .filter(bottle -> region.isEmpty() || (bottle.getRegion() != null && bottle.getRegion().trim().toLowerCase().contains(region.trim().toLowerCase())))
                .filter(bottle -> rating.isEmpty() || (bottle.getRating() != null && bottle.getRating().trim().toLowerCase().contains(rating.trim().toLowerCase())))

                // Those all seem to work, problem seems to be keyword searching
                // TODO: Fix and re-enable keyword searching
                //.filter(bottle -> searchKeywords.isEmpty() || (bottle.getKeywords() != null && searchKeywords.stream().allMatch(keyword -> bottle.getKeywords().contains(keyword.trim().toLowerCase()))))

                .collect(Collectors.toList());
        Log.d("SearchFragment", "Filtered list: " + filteredList);
        // Check if the filtered list is empty and display a toast message if it is
        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
        }
        // Keep this outside any if/else so that it clears the search results if there are none
            updateSearchResults(filteredList);
    }

    private void updateSearchResults(List<Bottle> filteredList) {
        searchResultsAdapter.setBottles(filteredList);
        searchResultsAdapter.notifyDataSetChanged();
    }

    // Get the list of bottles to search
    private List<Bottle> getBottlesToSearch() {
        List<Bottle> bottles = new ArrayList<>();
        File directory = requireContext().getFilesDir();
        File[] files = directory.listFiles();

        assert files != null;
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
