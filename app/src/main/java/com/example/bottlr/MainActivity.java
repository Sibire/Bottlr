package com.example.bottlr;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DividerItemDecoration;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

import java.util.List;

// TODO: Get the detail view to close properly when backing out of it

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AppBarConfiguration mAppBarConfiguration;
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

    /*void GenerateLiquorRecycler() {
        // Set Recycler
        RecyclerView LiquorCabinetRecycler = findViewById(R.id.liquorRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LiquorCabinetRecycler.setLayoutManager(layoutManager);
        List<Bottle> bottles = SharedUtils.loadBottles(this);

        // Line divider to keep things nice and neat
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        LiquorCabinetRecycler.addItemDecoration(dividerItemDecoration);

        // Bottle listing
        BottleAdapter liquorAdapter = new BottleAdapter(bottles, this);
        LiquorCabinetRecycler.setAdapter(liquorAdapter);
        liquorAdapter.notifyDataSetChanged();
    }*/

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
