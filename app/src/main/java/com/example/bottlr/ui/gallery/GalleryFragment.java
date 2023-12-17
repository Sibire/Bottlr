package com.example.bottlr.ui.gallery;

//region Imports
import android.net.Uri;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
//endregion

public class GalleryFragment extends Fragment implements BottleAdapter.OnBottleListener {

    //region Initialization

    private RecyclerView recyclerView;
    private BottleAdapter adapter;

    //endregion

    //region On Create Code

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
    //endregion

    //region On Resume Reload

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Liquor Cabinet");
        reloadBottles();
    }

    //endregion

    //region Bottle Loading

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
    //endregion

    //region Parse Bottle from Save

    // Code for parsing bottle details from save files

    private Bottle parseBottle(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String name = readValueSafe(br);

            if (name.isEmpty()) {
                return null;
                // Make sure the bottle is a valid entry, this is a double-check alongside a similar
                // Save block to handle a problem with double-saving an empty bottle
                // Probably because there's both a bitmap and URI image method
                // Keep it in as a safeguard, but check if those two really are the culprits
                // Once the app is actually functional
            }

            // Else continue

            String distillery = readValueSafe(br);
            String type = readValueSafe(br);
            String abv = readValueSafe(br);
            String age = readValueSafe(br);
            String notes = readValueSafe(br);
            String region = readValueSafe(br);
            Set<String> keywords = new HashSet<>(Arrays.asList(readValueSafe(br).split(",")));
            String rating = readValueSafe(br);
            String photoUriString = readValueSafe(br);
            Uri photoUri = (!photoUriString.equals("No photo") && !photoUriString.isEmpty()) ? Uri.parse(photoUriString) : null;

            // Return bottle
            return new Bottle(name, distillery, type, abv, age, photoUri, notes, region, keywords, rating);

            // Exception handling
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    //endregion

    //region Safe Read

    // Safe value read.
    private String readValueSafe(BufferedReader br) throws IOException {
        String line = br.readLine();
        return (line != null && line.contains(": ")) ? line.split(": ", 2)[1] : "";
    }
    //endregion

    //region Bottle Select Code

    // Bottle Selection
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
                    .addToBackStack(null)  // Adds transaction to back stack
                    .commit();
        }
    }
    //endregion

}
