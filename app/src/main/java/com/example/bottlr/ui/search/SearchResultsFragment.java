package com.example.bottlr.ui.search;

import static com.example.bottlr.MainActivity.parseBottle;
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
import com.example.bottlr.ui.gallery.DetailView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchResultsFragment extends Fragment implements BottleAdapter.OnBottleListener {

    private BottleAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        List<Bottle> bottles = loadResultBottles();
        adapter = new BottleAdapter(bottles, this);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Search Results");
        reloadBottles();
    }

    private void reloadBottles() {
        List<Bottle> bottles = loadResultBottles();
        if (adapter != null) {
            adapter.setBottles(bottles);
            adapter.notifyDataSetChanged();
        }
    }

    private List<Bottle> loadResultBottles() {
        // Get the filtered list of bottles from the arguments
        return getArguments().getParcelableArrayList("filteredBottles");
    }

    @Override
    public void onBottleClick(int position) {
        Bottle selectedBottle = adapter.getBottle(position);
        if (selectedBottle != null) {
            DetailView bottleDetail = new DetailView();
            Bundle bundle = new Bundle();
            bundle.putParcelable("selectedBottle", selectedBottle);
            bottleDetail.setArguments(bundle);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, bottleDetail)
                    .addToBackStack(null)
                    .commit();
        }
    }
}