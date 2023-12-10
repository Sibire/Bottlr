package com.example.bottlr.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bottlr.R;
import com.example.bottlr.ui.RecyclerView.BottleAdapter;
import com.example.bottlr.Bottle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private BottleAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Bottle> bottles = loadBottles();
        adapter = new BottleAdapter(bottles);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadBottles();
    }

    private void reloadBottles() {
        List<Bottle> bottles = loadBottles();
        if (adapter != null) {
            adapter.setBottles(bottles);
            adapter.notifyDataSetChanged();
        }
    }

    private List<Bottle> loadBottles() {
        List<Bottle> bottles = new ArrayList<>();
        File directory = getContext().getFilesDir();
        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.isFile() && file.getName().startsWith("bottle_")) {
                Bottle bottle = parseBottle(file);
                if (bottle != null) {
                    bottles.add(bottle);
                }
            }
        }
        return bottles;
    }

    private Bottle parseBottle(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String name = safeSplit(br.readLine());
            String distillery = safeSplit(br.readLine());
            String type = safeSplit(br.readLine());
            String abv = safeSplit(br.readLine());
            String age = safeSplit(br.readLine());
            String notes = safeSplit(br.readLine()); // Added for tasting notes
            String photoUriString = safeSplit(br.readLine());
            URI uri = URI.create(photoUriString.equals("No photo") ? null : photoUriString);

            return new Bottle(name, distillery, type, abv, age, uri, notes); // Updated constructor call
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
        return null;
    }

    private String safeSplit(String line) {
        String[] parts = line.split(": ");
        return parts.length > 1 ? parts[1] : "";
    }

}
