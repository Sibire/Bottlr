/*
package com.example.bottlr.ui.gallery;

import android.content.Intent;
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
import com.example.bottlr.SharedUtils;
import com.example.bottlr.BottleAdapter;
import com.example.bottlr.Bottle;
import java.util.List;

public class GalleryFragment extends Fragment implements BottleAdapter.OnBottleListener {

    //region Initialization

    private BottleAdapter adapter;

    //endregion

    //region On Create Code

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.liquorRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // Line divider to keep things nice and neat
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Bottle listing
        List<Bottle> bottles = SharedUtils.loadBottles(requireContext());
        adapter = new BottleAdapter(bottles, this);
        recyclerView.setAdapter(adapter);

        return root;
    }
    //endregion

    //region On Resume Reload

   */
/* @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("Liquor Cabinet");
        reloadBottles();
    }*//*


    //endregion

    //region Bottle Loading

    // Reload bottle method
    private void reloadBottles() {
        List<Bottle> bottles = SharedUtils.loadBottles(requireContext());
        if (adapter != null) {
            adapter.setBottles(bottles);
            adapter.notifyDataSetChanged();
        }
    }

    //region Bottle Select Code

    // Bottle Selection
    @Override
    public void onBottleClick(int position) {
        Bottle selectedBottle = adapter.getBottle(position);
        if (selectedBottle != null) {
            Intent intent = new Intent(getActivity(), DetailViewActivity.class);
            intent.putExtra("selectedBottle", selectedBottle);
            startActivity(intent);
        }
    }
    //endregion

}
*/
