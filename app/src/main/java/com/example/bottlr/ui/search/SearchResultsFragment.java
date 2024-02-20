package com.example.bottlr.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bottlr.R;
import com.example.bottlr.ui.RecyclerView.BottleAdapter;
import com.example.bottlr.Bottle;
import com.example.bottlr.ui.gallery.DetailViewActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// TODO: This would almost certainly look better as a full-screen activity instead of a fragment below the query

public class SearchResultsFragment extends Fragment {
    private BottleAdapter searchResultsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);

        RecyclerView searchResultsRecyclerView = view.findViewById(R.id.search_results_recyclerview);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsAdapter = new BottleAdapter(new ArrayList<>(), this::onBottleClick);
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        // Divider for clarity
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        searchResultsRecyclerView.addItemDecoration(itemDecoration);

        // Get the filteredList from the arguments
        assert getArguments() != null;
        List<Bottle> filteredList = (List<Bottle>) getArguments().getSerializable("filteredList");

        // Update the searchResultsAdapter with the filteredList
        updateSearchResults(filteredList);

        return view;
    }

    private void updateSearchResults(List<Bottle> filteredList) {
        searchResultsAdapter.setBottles(filteredList);
        searchResultsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("Search Results");
        reloadBottles();
    }

    private void reloadBottles() {
        List<Bottle> bottles = loadResultBottles();
        if (searchResultsAdapter != null) {
            searchResultsAdapter.setBottles(bottles);
            searchResultsAdapter.notifyDataSetChanged();
        }
    }

    private List<Bottle> loadResultBottles() {
        // Get the filtered list of bottles from the arguments
        assert getArguments() != null;
        return getArguments().getParcelableArrayList("filteredBottles");
    }

    public void onBottleClick(int position) {
        Bottle selectedBottle = searchResultsAdapter.getBottle(position);
        if (selectedBottle != null) {
            Intent intent = new Intent(getActivity(), DetailViewActivity.class);
            intent.putExtra("selectedBottle", selectedBottle);
            startActivity(intent);
        }
    }
}