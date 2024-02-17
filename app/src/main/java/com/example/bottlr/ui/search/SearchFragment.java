package com.example.bottlr.ui.search;

import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchFragment extends Fragment {

    private EditText nameField, distilleryField, typeField, abvField, ageField, notesField, regionField, ratingField, keywordsField;
    private Button searchButton;
    private RecyclerView searchResultsRecyclerView;
    private BottleAdapter searchResultsAdapter;

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


        searchResultsRecyclerView = view.findViewById(R.id.search_results_recyclerview);
        // Set the LayoutManager
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize your adapter and set it to the RecyclerView
        searchResultsAdapter = new BottleAdapter(new ArrayList<>(), this::onBottleSelected);
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        searchButton.setOnClickListener(v -> performSearch());
        return view;
    }

    private void performSearch() {
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

        // Assuming getBottles() retrieves your full list of Bottle objects
        List<Bottle> allBottles = getBottles();

        // Filter the list based on search criteria
        List<Bottle> filteredList = allBottles.stream()
                .filter(bottle -> bottle.getName().toLowerCase().contains(name))
                .filter(bottle -> bottle.getDistillery().toLowerCase().contains(distillery))
                .filter(bottle -> bottle.getType().toLowerCase().contains(type))
                .filter(bottle -> bottle.getAbv().toLowerCase().contains(abv))
                .filter(bottle -> bottle.getAge().toLowerCase().contains(age))
                .filter(bottle -> bottle.getNotes().toLowerCase().contains(notes))
                .filter(bottle -> bottle.getRegion().toLowerCase().contains(region))
                .filter(bottle -> bottle.getRating().toLowerCase().contains(rating))
                .filter(bottle -> bottle.getKeywords().stream().anyMatch(keyword -> searchKeywords.contains(keyword.toLowerCase())))
                .collect(Collectors.toList());
        updateSearchResults(filteredList);
    }

    private void updateSearchResults(List<Bottle> filteredList) {
        searchResultsAdapter.setBottles(filteredList);
        searchResultsAdapter.notifyDataSetChanged();
    }

    private List<Bottle> getBottles() {
        // Implement this method to return your actual list of bottles
        return new ArrayList<>();
    }

    // Example method to handle bottle selection from the search results
    private void onBottleSelected(int position) {
        // Handle the bottle selection
    }
}
