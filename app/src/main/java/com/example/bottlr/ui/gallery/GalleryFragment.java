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

public class GalleryFragment extends Fragment implements BottleAdapter.OnBottleListener {
    private RecyclerView recyclerView;
    private BottleAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        recyclerView = root.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // Line divider to keep things nice and neat
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Bottle listing
        List<Bottle> bottles = loadBottles();
        adapter = new BottleAdapter(bottles, this);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    // Reloads gallery after adding a bottle or any other return to the window.
    public void onResume() {
        super.onResume();
        reloadBottles();
    }

    // Reload bottle method
    private void reloadBottles() {
        List<Bottle> bottles = loadBottles();
        if (adapter != null) {
            adapter.setBottles(bottles);
            adapter.notifyDataSetChanged();
        }
    }

    // Initial bottle load
    private List<Bottle> loadBottles() {
        List<Bottle> bottles = new ArrayList<>();
        File directory = getContext().getFilesDir();
        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.isFile() && file.getName().startsWith("bottle_")) {
                Bottle bottle = parseBottle(file);
                if (bottle != null) {
                    // Make sure the bottle actually exists
                    bottles.add(bottle);
                }
            }
        }
        return bottles;
    }

    // Code for parsing bottle details from save files
    private Bottle parseBottle(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String name = readValue(br);
            String distillery = readValue(br);
            String type = readValue(br);
            String abv = readValue(br);
            String age = readValue(br);
            String notes = readValue(br);
            String photoUriString = readValue(br);

            if (name.isEmpty()) {
                return null;
                // Make sure the bottle is a valid entry, this is a double-check alongside a similar
                // Save block to handle a problem with double-saving an empty bottle
                // Probably because there's both a bitmap and URI image method
                // Keep it in as a safeguard, but check if those two really are the culprits
                // Once the app is actually functional
            }
            // Code for handling bottles with no image
            Uri photoUri = photoUriString.equals("No photo") ? null : Uri.parse(photoUriString);
            return new Bottle(name, distillery, type, abv, age, photoUri, notes);
        }
        // Exception handling
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Code for safely splitting lines and reading values without crashing
    // or throwing some other error like initially faced with gallery implementation
    private String readValue(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line != null && line.contains(": ")) {
            String[] parts = line.split(": ", 2);
            return parts.length > 1 ? parts[1] : "";
        }
        return "";
    }

    // Bottle Selection Code
    @Override
    public void onBottleClick(int position) {
        Bottle selectedBottle = adapter.getBottle(position);
        if (selectedBottle != null) {
            DetailView bottleDetail = new DetailView();
            Bundle bundle = new Bundle();
            bundle.putParcelable("selectedBottle", selectedBottle);
            bottleDetail.setArguments(bundle);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, bottleDetail)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
