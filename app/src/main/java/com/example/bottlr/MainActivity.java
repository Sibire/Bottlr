package com.example.bottlr;

import static com.example.bottlr.SharedUtils.queryBuilder;
import static com.example.bottlr.SharedUtils.saveImageToGallery;
import static com.example.bottlr.SharedUtils.shareBottleInfo;
import static com.example.bottlr.SharedUtils.showDeleteConfirm;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.bumptech.glide.Glide;
import com.example.bottlr.ui.gallery.DetailViewActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

import java.util.List;

// TODO: Get the detail view to close properly when backing out of it

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AppBarConfiguration mAppBarConfiguration;
    public List<Bottle> bottles;
    public List<Bottle> allBottles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);

        // AppCheck Code
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());
    }

    @Override //Used for on click section in layout button attribute to switch layouts.
    public void onClick(View view) //add button with an else-if statement
    {
        int id = view.getId();
        if (id == R.id.menu_icon) { //menu navigation button
            //animate
            animateObject(R.id.nav_window, 0f, 0f, 700);
        } else if (id == R.id.exit_nav_button) { //exit nav menu
            //animate
            animateObject(R.id.nav_window, 0f, -0.9f, 300);
        } else if (id == R.id.menu_home_button) { //nav home screen click
            setContentView(R.layout.homescreen);
        } else if (id == R.id.menu_liquorcab_button) { //nav liquor cab screen click
            setContentView(R.layout.fragment_gallery);
            GenerateLiquorRecycler();
        } else if (id == R.id.menu_search_button) { //nav search screen click
            setContentView(R.layout.bottlelabel);
        } else if (id == R.id.menu_settings_button) { //nav settings screen click
            setContentView(R.layout.bottlelabel);
        } else {
            //home screen? or error text?
        }
    }

    /*public void onBottleClick(int position) {
        setContentView(R.layout.detail_view_activity);
    }*/

    ImageButton backButton;
    ImageButton deleteButton;
    ImageButton shareButton;
    ImageButton buyButton;
    ImageButton editButton;
    ImageButton saveImageButton;

    void GenerateLiquorRecycler() {
        // Set Recycler
        RecyclerView LiquorCabinetRecycler = findViewById(R.id.liquorRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LiquorCabinetRecycler.setLayoutManager(layoutManager);

        //List<Bottle> bottles = SharedUtils.loadBottles(this);
        // Line divider to keep things nice and neat
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        LiquorCabinetRecycler.addItemDecoration(dividerItemDecoration);

        // Bottle listing
        BottleAdapter liquorAdapter;
        liquorAdapter = new BottleAdapter(bottles, allBottles, new BottleAdapter.OnBottleCheckListener() {
            @Override
            public void onButtonClick(String string) { detailedView(string); }
        });
        LiquorCabinetRecycler.setAdapter(liquorAdapter);
        liquorAdapter.notifyDataSetChanged();
    }

    public void detailedView(String string) {
        setContentView(R.layout.detail_view_activity);

        /*// Find the views
        ImageView bottleImage = findViewById(R.id.detailImageView);
        bottleImage.setScaleType(ImageView.ScaleType.FIT_CENTER); // Set the scale type of the ImageView so it displays properly
        // view Initialization
        TextView bottleName = findViewById(R.id.tvBottleName);
        TextView bottleDistillery = findViewById(R.id.tvDistillery);
        TextView bottleRating = findViewById(R.id.tvRating);
        TextView bottleDetails = findViewById(R.id.tvBottleDetails);
        TextView bottleNotes = findViewById(R.id.tvNotes);
        TextView bottleKeywords = findViewById(R.id.tvKeywords);
        deleteButton = findViewById(R.id.deleteButton);
        shareButton = findViewById(R.id.shareButton);
        buyButton = findViewById(R.id.buyButton);
        editButton = findViewById(R.id.editButton);
        backButton = findViewById(R.id.backButton);
        saveImageButton = findViewById(R.id.saveImageButton);

        // Get the bottle from the intent
        Bottle bottle = getIntent().getParcelableExtra("selectedBottle");

        // Set the bottle details to the views

        // Glide
        assert bottle != null;
        if (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo")) {
            Glide.with(this)
                    .load(bottle.getPhotoUri())
                    .error(R.drawable.nodrinkimg) // Default image in case of error
                    .into(bottleImage);
        }
        // Image not working debugging code
        Log.d("DetailViewActivity", "Image URI: " + bottle.getPhotoUri());
        // Other fields
        bottleName.setText(bottle.getName());
        bottleDistillery.setText(bottle.getDistillery());
        String rating = bottle.getRating() + " / 10";
        bottleRating.setText(rating);
        String details = bottle.getType() + ", " + bottle.getRegion() + ", " + bottle.getAge() + " Year, " + bottle.getAbv() + "% ABV";
        bottleDetails.setText(details);
        bottleNotes.setText(bottle.getNotes());
        String keywords = "Keywords:\n" + String.join(", ", bottle.getKeywords());
        bottleKeywords.setText(keywords);

        // Delete button listener
        deleteButton.setOnClickListener(v -> showDeleteConfirm(bottle, this));

        // Buy button listener
        buyButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.google.com/search?tbm=shop&q=" + Uri.encode(queryBuilder(bottle))));
            startActivity(intent);
        });

        // Share button listener
        shareButton.setOnClickListener(view -> shareBottleInfo(bottle, this));

        // Edit button listener
        editButton.setOnClickListener(view -> {
            // Call AddABottle to reuse existing assets
            Intent intent = new Intent(DetailViewActivity.this, AddABottle.class);
            intent.putExtra("bottle", bottle);
            startActivity(intent);
        });

        // Back button listener
        backButton.setOnClickListener(v -> finish());

        // Save image button listener
        saveImageButton.setOnClickListener(v -> {
            if (bottle.getPhotoUri() != null) {
                // Save the image to the user's gallery
                saveImageToGallery(DetailViewActivity.this, bottle);
            }
        });*/
    }

    private void animateObject(int id, float start, float finish, int time) { //horizontal constraint animation
        ConstraintLayout navMenu = findViewById(id);
        ObjectAnimator animator = ObjectAnimator.ofFloat(navMenu, "translationX", start, finish * navMenu.getWidth());
        animator.setDuration(time);
        animator.start();
    }

 /*   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }*/

//endregion

    // region Settings Button
 /*   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
    // endregion

}
