package com.example.bottlr.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottlr.Bottle;
import com.example.bottlr.R;
import com.example.bottlr.ui.RecyclerView.BottleAdapter;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private BottleAdapter adapter;
    private List<Bottle> allBottles; // Using existing list of all bottles

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        SearchView searchView = view.findViewById(R.id.search_view);
        RecyclerView recyclerView = view.findViewById(R.id.search_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        allBottles = loadBottles();
        adapter = new BottleAdapter(allBottles, position -> {
            // Handle click event
        });

        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // No action on submit
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        return view;
    }

    private List<Bottle> loadBottles() {
        return new ArrayList<>();
    }
}
