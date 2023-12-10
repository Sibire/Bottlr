package com.example.bottlr.ui.gallery;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bottlr.R;
import com.example.bottlr.ui.RecyclerView.BottleAdapter;
import com.example.bottlr.Bottle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // Adding the divider line
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

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
            String name = readValue(br);
            String distillery = readValue(br);
            String type = readValue(br);
            String abv = readValue(br);
            String age = readValue(br);
            String notes = readValue(br);
            String photoUriString = readValue(br);

            // Validation: Check if any essential field is empty or invalid
            if (name.isEmpty()) {
                return null; // Skip this bottle as it's incomplete or invalid
            }

            Uri photoUri = photoUriString.equals("No photo") ? null : Uri.parse(photoUriString);
            return new Bottle(name, distillery, type, abv, age, photoUri, notes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String readValue(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line != null && line.contains(": ")) {
            String[] parts = line.split(": ", 2);
            if (parts.length > 1) {
                return parts[1];
            }
        }
        return "";
    }
}
