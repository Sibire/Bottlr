package com.example.bottlr.ui.gallery;

//region Imports
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
//endregion

public class GalleryFragment extends Fragment implements BottleAdapter.OnBottleListener {

    //region Initialization

    private BottleAdapter adapter;

    //endregion

    //region On Create Code

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
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
