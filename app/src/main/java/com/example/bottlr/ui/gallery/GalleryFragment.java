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
import com.example.bottlr.Bottle; // Ensure this is the correct path to your Bottle class

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
            String name = br.readLine().split(": ")[1];
            String distiller = br.readLine().split(": ")[1];
            String type = br.readLine().split(": ")[1];
            String abv = br.readLine().split(": ")[1];
            String age = br.readLine().split(": ")[1];
            String photoUriString = br.readLine().split(": ")[1];
            URI uri = URI.create(photoUriString.equals("No photo") ? null : photoUriString);

            return new Bottle(name, distiller, type, abv, age, uri);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
        return null;
    }
}
