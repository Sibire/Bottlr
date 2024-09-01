package com.example.bottlr;

import static com.example.bottlr.SharedUtils.loadBottles;
import static com.example.bottlr.SharedUtils.loadCocktails;
import static com.example.bottlr.SharedUtils.loadLocations;
import static com.example.bottlr.SharedUtils.parseBottle;
import static com.example.bottlr.SharedUtils.parseCocktail;
import static com.example.bottlr.SharedUtils.parseLocation;
import static com.example.bottlr.SharedUtils.queryBuilder;
import static com.example.bottlr.SharedUtils.saveImageToGallery;
import static com.example.bottlr.SharedUtils.saveImageToGalleryCocktail;
import static com.example.bottlr.SharedUtils.shareBottleInfo;
import static com.example.bottlr.SharedUtils.showBottleDeleteConfirm;
import static com.example.bottlr.SharedUtils.shareCocktailInfo;
import static com.example.bottlr.SharedUtils.showDeleteConfirmCocktail;
import static com.example.bottlr.SharedUtils.showLocationDialog;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //region Initializations
    private List<Bottle> bottles, allBottles;
    private List<Location> locations, allLocations;
    private List<Cocktail> cocktails, allCocktails;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int CAMERA_REQUEST_CODE = 201, GALLERY_REQUEST_CODE = 202, LOCATION_REQUEST_CODE = 203;
    private EditText bottleNameField, distillerField, spiritTypeField, abvField,
            ageField, tastingNotesField, regionField, keywordsField, ratingField, cocktailNameField, baseField,
            mixerField, juiceField, liqueurField, garnishField, extraField;
    private Uri photoUri, cameraImageUri;
    private BottleAdapter searchResultsAdapter;
    private CocktailAdapter searchResultsAdapter2;
    private int editor, lastLayout; //0 = no edits, 1 = bottle editor, 2 = setting access, 3 = locations
    private boolean drinkFlag; //true = bottle, false = cocktail
    private String currentBottle, currentLocation;

    //endregion

    //region onCreate Code
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeScreen();

        // AppCheck Code
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());

        //initialize bottle storage
        bottles = new ArrayList<>();
        allBottles = new ArrayList<>();

        //initialize location storage
        locations = new ArrayList<>();
        allLocations = new ArrayList<>();
        cocktails = new ArrayList<>();
        allCocktails = new ArrayList<>();
        }
    //endregion

    // TODO: Move this to onClick
    public void onFeedbackButtonClick(View view) {
        String url = "https://forms.gle/f9bkcs2JjTuyDywn9";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    //region onClick Code
    @Override //Used for on click section in layout button attribute to switch layouts.
    public void onClick(View view) //add button with an else-if statement
    {
        int id = view.getId();
        if (id == R.id.menu_icon) { //menu navigation button, animate
            animateObject(R.id.nav_window, 0f, 700);
            KeyboardVanish(view);
        } else if (id == R.id.exit_nav_button) { //exit nav menu, animate
            animateObject(R.id.nav_window, -1f, 300);
        } else if (id == R.id.menu_home_button) { //nav home screen click
            homeScreen();
        } else if (id == R.id.menu_liquorcab_button) { //nav liquor cab screen click
            drinkFlag = true;
            setContentView(R.layout.fragment_gallery);
            GenerateLiquorRecycler();
            lastLayout = R.layout.fragment_gallery;
        } else if (id == R.id.menu_cocktail_button) { //nav cocktail screen click
            drinkFlag = false;
            editor = 0;
            setContentView(R.layout.fragment_gallery);
            FloatingActionButton addButton = findViewById(R.id.fab);
            Drawable drawable = getResources().getDrawable(R.drawable.cocktailadd, null);
            addButton.setForeground(drawable);
            GenerateLiquorRecycler();
            lastLayout = R.layout.fragment_gallery;
        } else if (id == R.id.menu_search_button) { //nav search screen click
            showSearchPopup();
        } else if (id == R.id.menu_settings_button) { //settings area
            editor = 2;
            setContentView(R.layout.activity_settings);
            lastLayout = R.layout.activity_settings;
            settings();
        } else if (id == R.id.fab) { //add bottle
            if(editor == 3) {
                Log.d("MainActivity", "Add Location Button Clicked");
                addNewLocation();
            } else {
                editor = 0;
                addBottle();
            }
        } else if (id == R.id.addPhotoButton) { //add photo button bottle
            drinkFlag = true;
            if (checkCameraPermission()) { chooseImageSource(); } else { requestCameraPermission(); }
            KeyboardVanish(view);
        } else if (id == R.id.addPhotoButtonCocktail) { //add photo button cocktail
            drinkFlag = false;
            editor = 0;
            addBottle();
        } else if (id == R.id.addPhotoButton) { //add photo button
            if (checkCameraPermission()) { chooseImageSource(); } else { requestCameraPermission(); }
            KeyboardVanish(view);
        } else if (id == R.id.saveButton) { //save bottle button
            drinkFlag = true;
            saveEntryToFile();
            customBackButton();
        } else if (id == R.id.saveButtonCocktail) { //save cocktail button
            drinkFlag = false;
            saveEntryToFile();
            customBackButton();
        } else if (id == R.id.homescreen) { //fragment home
            homeScreen();
        } else if (id == R.id.deleteButton) { //delete bottle //TODO: screen changes before deletion selection
            if (drinkFlag) { showBottleDeleteConfirm(getMostRecentBottle(), this); }
            else { showDeleteConfirmCocktail(getMostRecentCocktail(), this); }
            setContentView(R.layout.fragment_gallery);
            GenerateLiquorRecycler();
        } else if (id == R.id.shareButton) { //share bottle
            if(drinkFlag) {
                shareBottleInfo(getMostRecentBottle(), this);
            } else {
                shareCocktailInfo(getMostRecentCocktail(), this);
            }
        } else if (id == R.id.buyButton) { //buy bottle
            String url = "https://www.google.com/search?tbm=shop&q=" + Uri.encode(queryBuilder(getMostRecentBottle()));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } else if (id == R.id.editButton) { //edit Bottle
            editor = 1;
            addBottle();
        } else if (id == R.id.cloud_sync_button_home) { //sync cloud button
            KeyboardVanish(view);
            settings();
            uploadBottlesToCloud();
            syncBottlesFromCloud();
            uploadLocationsToCloud();
            syncLocationsFromCloud();
        } else if (id == R.id.saveImageButton) { // Save the image to the user's gallery
            if (drinkFlag) {
                Bottle recentBottle = getMostRecentBottle();
                if (recentBottle != null && recentBottle.getPhotoUri() != null) {
                    saveImageToGallery(this, recentBottle);}
            } else {
                Cocktail recentcocktail = getMostRecentCocktail();
                if (recentcocktail != null && recentcocktail.getPhotoUri() != null) {
                    saveImageToGalleryCocktail(this, recentcocktail);}
            }
        /*} else if (id == R.id.backButton) { //back button bottle
            customBackButton();*/
        } else if (id == R.id.sign_in_button_home) { //sign in home button
            SignInChecker(id);
        /*} else if (id == R.id.nfcButton) { //nfc button info
            nfcShare();*/
        }
        else if (id == R.id.menu_locations_button) { //nav locations screen click
            //setContentView(R.layout.locations_page);
            editor = 3;
            setContentView(R.layout.fragment_gallery);
            FloatingActionButton addButton = findViewById(R.id.fab);
            Drawable drawable = getResources().getDrawable(R.drawable.locationadd, null);
            addButton.setForeground(drawable);
            GenerateLocationRecycler();
        }else if (id == R.id.switchButton) { //switch add bottle type
            if (drinkFlag) {
                drinkFlag = false;
                addBottle();
            } else {
                drinkFlag = true;
                addBottle();
            }
        } else {
            Toast.makeText(this, "Button Not Working", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    //region onBackPressed Code
    @Override
    public void onBackPressed() { //Resets App, i.e. goes to home screen.
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void customBackButton() {
        setContentView(lastLayout);
        if (lastLayout == R.layout.fragment_gallery) {
            setContentView(R.layout.fragment_gallery);
            GenerateLiquorRecycler();
        } else if (lastLayout == R.layout.homescreen) {
            homeScreen();
        }
    }
    //endregion

    //region Home Screen
    @SuppressLint("SetTextI18n")
    public void homeScreen() {
        setContentView(R.layout.homescreen);
        SignInChecker(R.layout.homescreen);
        lastLayout = R.layout.homescreen;
        drinkFlag = true;
    }

    private Bottle getMostRecentBottle() {
        File directory = this.getFilesDir();
        File[] files = directory.listFiles((dir, name) -> name.startsWith("bottle_") && name.endsWith(".txt"));
        Bottle mostRecentBottle = null;
        if (files != null) {
            for (File file : files) {
                Bottle bottle = parseBottle(file);
                assert bottle != null;
                if (currentBottle.equals(bottle.getName())) {
                    mostRecentBottle = bottle;
                }
            }
        }
        return mostRecentBottle;
    }

    private Cocktail getMostRecentCocktail() {
        File directory = this.getFilesDir();
        File[] files = directory.listFiles((dir, name) -> name.startsWith("cocktail_") && name.endsWith(".txt"));
        Cocktail mostRecentCocktail = null;
        if (files != null) {
            for (File file : files) {
                Cocktail cocktail = parseCocktail(file);
                assert cocktail != null;
                if (currentBottle.equals(cocktail.getName())) {
                    mostRecentCocktail = cocktail;
                }
            }
        }
        return mostRecentCocktail;
    }

    private Location getMostRecentLocation() {
        File directory = this.getFilesDir();
        File[] files = directory.listFiles((dir, name) -> name.startsWith("location_") && name.endsWith(".txt"));
        Location mostRecentLocation = null;
        if (files != null) {
            for (File file : files) {
                Location location = parseLocation(file);
                assert location != null;
                if (currentLocation.equals(location.getTimeDateAdded())) {
                    mostRecentLocation = location;
                }
            }
        }
        return mostRecentLocation;
    }

    @SuppressLint("SetTextI18n")
    public void SignInChecker(int layout) {
        Button signIn = findViewById(R.id.sign_in_button_home);
        FrameLayout recentBottle = findViewById(R.id.home_last_bottle);
        settings();
        if(mAuth.getCurrentUser() != null) {
            signIn.setVisibility(View.GONE);
            recentBottle.setVisibility(View.VISIBLE);

            //last bottle viewed displayed
            SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
            String storedData = sharedPreferences.getString("CurrentBottle", "defaultValue");
            if (!storedData.isEmpty()) {
                currentBottle = storedData;
                Bottle checker = getMostRecentBottle();
                Cocktail checker2 = getMostRecentCocktail();
                if (checker != null) {
                    TextView tbottleName = findViewById(R.id.tvBottleName);
                    tbottleName.setText(currentBottle);
                    ImageView bottleImage = findViewById(R.id.detailImageView);
                    if(checker.getPhotoUri() == null && !bottleImage.toString().equals("No photo")) {
                        bottleImage.setImageResource(R.drawable.nodrinkimg);
                    } else {
                        bottleImage.setImageURI(checker.getPhotoUri());
                    }
                } else if (checker2 != null) {
                    TextView tbottleName = findViewById(R.id.tvBottleName);
                    tbottleName.setText(currentBottle);
                    ImageView bottleImage = findViewById(R.id.detailImageView);
                    if(checker2.getPhotoUri() == null && !bottleImage.toString().equals("No photo")) {
                        bottleImage.setImageResource(R.drawable.nodrinkimg);
                    } else {
                        bottleImage.setImageURI(checker2.getPhotoUri());
                    }
                } else {
                    TextView tbottleName = findViewById(R.id.tvBottleName);
                    tbottleName.setText("No Bottle Viewed");
                }
            } else {
                TextView tbottleName = findViewById(R.id.tvBottleName);
                tbottleName.setText("No Bottle Viewed");
            }
        } else {
            if(layout != R.layout.homescreen) {
                signIn();
            }
        }
    }
    //endregion

    //region Recycler Code
    @SuppressLint("NotifyDataSetChanged")
    // Update the method call in GenerateLiquorRecycler
    void GenerateLiquorRecycler() {
        // Set Recycler
        RecyclerView LiquorCabinetRecycler = findViewById(R.id.liquorRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LiquorCabinetRecycler.setLayoutManager(layoutManager);

        // Bottle listing
        if (drinkFlag) {
            BottleAdapter liquorAdapter;
            bottles = loadBottles(this);
            liquorAdapter = new BottleAdapter(bottles, allBottles, this::detailedView);
            LiquorCabinetRecycler.setAdapter(liquorAdapter);
            liquorAdapter.notifyDataSetChanged();
        } else {
            CocktailAdapter liquorAdapter;
            cocktails = loadCocktails(this);
            liquorAdapter = new CocktailAdapter(cocktails, allCocktails, this::detailedViewCocktail);
            LiquorCabinetRecycler.setAdapter(liquorAdapter);
            liquorAdapter.notifyDataSetChanged();
        }
    }
    void GenerateLocationRecycler() {
        // Set Recycler
        RecyclerView LocationRecycler = findViewById(R.id.liquorRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LocationRecycler.setLayoutManager(layoutManager);
        // Line divider to keep things nice and neat
        /*DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        LocationRecycler.addItemDecoration(dividerItemDecoration);*/
        // Locations listing
        locations = loadLocations(this);
        LocationAdapter locationAdapter;
        locationAdapter = new LocationAdapter(locations, allLocations, this::detailedLocationView); // Initialize locationAdapter
        LocationRecycler.setAdapter(locationAdapter);
        locationAdapter.notifyDataSetChanged();
    }
    //endregion

    //region animateObject Code
    private void animateObject(int id, float finish, int time) { //horizontal constraint animation
        ConstraintLayout navMenu = findViewById(id);
        ObjectAnimator animator = ObjectAnimator.ofFloat(navMenu, "translationX", (float) 0.0, finish * navMenu.getWidth());
        animator.setDuration(time);
        animator.start();
    }
    //endregion

    // region Permissions Handling

    // Camera
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    // Location, Fine
    private boolean checkFineLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CAMERA_REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permissions are granted, continue
                } else {
                    // Camera permissions are denied, show a message to the user
                    Toast.makeText(this, "Camera permission is required to use this feature.", Toast.LENGTH_SHORT).show();
                }
                break;
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permissions are granted, you can perform your location related task here
                    createNewLocation();
                } else {
                    // Location permissions are denied, show a message to the user
                    Toast.makeText(this, "Location permission is required to use this feature.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    //endregion

    //region Bottle Detail View

    public void detailedView(String bottleName, String bottleId, String bottleDistillery, String bottleType, String bottleABV, String bottleAge,
                             Uri bottlePhoto, String bottleNotes, String bottleRegion, String bottleRating, String bottleKeywords) {
        setContentView(R.layout.description_screen);

        //fill empty data
        if(bottleName.isEmpty()) { bottleName = "Name"; }
        if(bottleDistillery.isEmpty()) { bottleDistillery = "No Distillery"; }
        if(bottleType.isEmpty()) { bottleType = "No Type"; }
        if(bottleABV.isEmpty()) { bottleABV = "N/A"; }
        if(bottleAge.isEmpty()) { bottleAge = "No"; }
        if(bottleNotes.isEmpty()) { bottleNotes = "No Notes"; }
        if(bottleRegion.isEmpty()) { bottleRegion = "No Region"; }
        if(bottleRating.isEmpty()) { bottleRating = "No Rating"; }
        if(bottleKeywords.isEmpty()) { bottleKeywords = "None"; }

        // Find the views
        ImageView bottleImage = findViewById(R.id.detailImageView);
        bottleImage.setScaleType(ImageView.ScaleType.FIT_CENTER); // Set the scale type of the ImageView so it displays properly

        TextView tbottleName = findViewById(R.id.tvBottleName);
        TextView tbottleDistillery = findViewById(R.id.tvDistillery);
        TextView tbottleRating = findViewById(R.id.tvRating);
        TextView tbottleDetails = findViewById(R.id.tvBottleDetails);
        TextView tbottleNotes = findViewById(R.id.tvNotes);
        TextView tbottleKeywords = findViewById(R.id.tvKeywords);

        //add data to layout
        String details = bottleType + ", " + bottleRegion + ", " + bottleAge + " Year, " + bottleABV + "% ABV";
        tbottleName.setText(bottleName);
        tbottleDistillery.setText(bottleDistillery);
        String rating = bottleRating + " / 10";
        tbottleRating.setText(rating);
        tbottleDetails.setText(details);
        tbottleNotes.setText(bottleNotes);

        String keywords = "Keywords:\n" + bottleKeywords;
        tbottleKeywords.setText(keywords);
        if(bottlePhoto == null && !bottleImage.toString().equals("No photo")) {
            bottleImage.setImageResource(R.drawable.nodrinkimg);
        } else {
            bottleImage.setImageURI(bottlePhoto);
        }
        currentBottle = bottleName;
        //Store last viewed info as user preference for restart
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CurrentBottle", currentBottle);
        editor.apply();
    }

    public void detailedViewCocktail(Cocktail cocktail) {
        String cocktailName = cocktail.getName();
        String cocktailBase = cocktail.getBase();
        String cocktailMixer = cocktail.getMixer();
        String cocktailJuice = cocktail.getJuice();
        String cocktailLiqueur = cocktail.getLiqueur();
        String cocktailGarnish = cocktail.getGarnish();
        String cocktailExtra = cocktail.getExtra();
        Uri cocktailPhoto = cocktail.getPhotoUri();
        String cocktailNotes = cocktail.getNotes();
        String cocktailRating = cocktail.getRating();
        String cocktailKeywords = cocktail.getKeywords();
        setContentView(R.layout.description_cocktails);

        //fill empty data
        if(cocktailName.isEmpty()) { cocktailName = "Name"; }
        if(cocktailBase.isEmpty()) { cocktailBase = "N/A"; }
        if(cocktailMixer.isEmpty()) { cocktailMixer = ""; }
        if(cocktailJuice.isEmpty()) { cocktailJuice = ""; }
        if(cocktailLiqueur.isEmpty()) { cocktailLiqueur = ""; }
        if(cocktailGarnish.isEmpty()) { cocktailGarnish = ""; }
        if(cocktailExtra.isEmpty()) { cocktailExtra = ""; }
        if(cocktailNotes.isEmpty()) { cocktailNotes = "No Notes"; }
        if(cocktailRating.isEmpty()) { cocktailRating = "No Rating"; }
        if(cocktailKeywords.isEmpty()) { cocktailKeywords = "None"; }

        // Find the views
        ImageView bottleImage = findViewById(R.id.detailImageView);
        bottleImage.setScaleType(ImageView.ScaleType.FIT_CENTER); // Set the scale type of the ImageView so it displays properly
        TextView tcocktailName = findViewById(R.id.cvCocktailName);
        TextView tcocktailbase = findViewById(R.id.cvBase);
        TextView tcocktailrating = findViewById(R.id.cvRating);
        TextView tcocktaildetails = findViewById(R.id.cvCocktailDetails);
        TextView tcocktailnotes = findViewById(R.id.cvNotes);
        TextView tcocktailKeywords = findViewById(R.id.cvKeywords);

        //add data to layout
        String details = "Additives: " + cocktailMixer + " " + cocktailJuice + " " + cocktailLiqueur + " " + cocktailGarnish + " " + cocktailExtra;
        tcocktailName.setText(cocktailName);
        tcocktailbase.setText(cocktailBase);
        String rating = cocktailRating + " / 10";
        tcocktailrating.setText(rating);
        tcocktaildetails.setText(details);
        tcocktailnotes.setText(cocktailNotes);

        String keywords = "Keywords:\n" + cocktailKeywords;
        tcocktailKeywords.setText(keywords);
        if(cocktailPhoto == null && !bottleImage.toString().equals("No photo")) {
            bottleImage.setImageResource(R.drawable.nodrinkimg);
        } else {
            bottleImage.setImageURI(cocktailPhoto);
        }
        currentBottle = cocktailName;
        //Store last viewed info as user preference for restart
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CurrentBottle", currentBottle);
        editor.apply();
    }

    public void detailedLocationView(String name, String coordinates, String date) {
        //Toast.makeText(this, "Action Complete", Toast.LENGTH_SHORT).show();
        currentLocation = date;
        Location location = getMostRecentLocation();
        showLocationDialog(location, this);
    }
    //endregion

    //region NFC info

    public void nfcShare() {
        //if adding bottle via NFC

        //if sharing bottle info via NFC

    }

    //endregion

    //region Settings
    @SuppressLint("SetTextI18n")
    public void settings() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.bottlr_web_client_id)) // Keep this updated as needed
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        if(editor == 2) {
            // Update signed in user TextView
            TextView signedInUserTextView = findViewById(R.id.signed_in_user);
            if (mAuth.getCurrentUser() != null) {
                signedInUserTextView.setText(mAuth.getCurrentUser().getEmail());
            } else {
                signedInUserTextView.setText("Not Signed In");
            }
            Button loginButton = findViewById(R.id.login_Button);
            loginButton.setOnClickListener(v -> signIn());
            Button logoutButton = findViewById(R.id.logout_Button);
            logoutButton.setOnClickListener(v -> signOut());
            Button uploadButton = findViewById(R.id.upload_Button);
            uploadButton.setOnClickListener(v -> uploadData());
            Button syncButton = findViewById(R.id.sync_Button);
            syncButton.setOnClickListener(v -> downloadData());
            Button eraseButton = findViewById(R.id.erase_Button);
            eraseButton.setOnClickListener(v -> eraseCloudStorage());
            editor = 0;
        }
    }
    //endregion

    //region Cloud Storage, Login, Firebase
    private void signIn() {
        Log.d("SettingsActivity", "signIn method called");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("SettingsActivity", "onActivityResult called");
        // Result returned by GoogleSignInClient.getSignInIntent
        if (requestCode == RC_SIGN_IN) {
            Log.d("SettingsActivity", "Result received from sign-in intent");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, notification handled in firebaseAuthWithGoogle
                Log.d("SettingsActivity", "Google Sign In Failed", e);
            }
        }
        ImageView imagePreview = findViewById(R.id.imagePreview);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                Uri selectedImageUri = data.getData();
                try {
                    // Copy the image and get the URI of the copied image
                    photoUri = copyImageToAppDir(selectedImageUri);
                    imagePreview.setImageURI(photoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed To Copy Image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                try {
                    // Copy the image taken by the camera and get the URI of the copied image
                    photoUri = copyImageToAppDir(cameraImageUri);
                    imagePreview.setImageURI(photoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed To Copy Image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        Log.d("SettingsActivity", "firebaseAuthWithGoogle method called");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SettingsActivity", "Sign in successful");
                        updateSignedInUserTextView();
                        Toast.makeText(this, "Signed In Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d("SettingsActivity", "Sign in failed");
                        Toast.makeText(this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void signOut() {
        if (mAuth.getCurrentUser() == null) {
            // Sign-In Check
            Toast.makeText(this, "You're Already Signed Out", Toast.LENGTH_SHORT).show();
            return;
        }
        // Firebase sign out
        mAuth.signOut();
        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> {
                    // Update UI
                    updateSignedInUserTextView();
                    Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                });
    }
    @SuppressLint("SetTextI18n")
    private void updateSignedInUserTextView() {
        TextView signedInUserTextView = findViewById(R.id.signed_in_user);
        if (mAuth.getCurrentUser() != null) {
            signedInUserTextView.setText(mAuth.getCurrentUser().getDisplayName());
            if(lastLayout != R.layout.activity_settings) {
                SignInChecker(lastLayout);
            }
        } else {
            signedInUserTextView.setText("Not Signed In");
        }
    }
    private void uploadBottlesToCloud() {
        if (mAuth.getCurrentUser() == null) {
            // Sign-In Check
            Toast.makeText(this, "Sign-In Required For This Feature", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get a reference to the Firebase Storage instance
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Use the loadBottles() method to get all the bottles
        List<Bottle> bottleList = loadBottles(this);
        // Loop through the bottles in the bottle list
        for (Bottle bottle : bottleList) {
            // Get the file name for the bottle data
            String dataFileName = "bottle_" + bottle.getName() + ".txt";
            // Create a reference for the bottle data file
            StorageReference dataFileRef = storage.getReference()
                    .child("users")
                    .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                    .child("bottles")
                    .child(dataFileName);
            // Upload the bottle data file
            dataFileRef.putFile(Uri.fromFile(new File(getFilesDir(), dataFileName)))
                    .addOnSuccessListener(taskSnapshot -> {
                        // Handle successful uploads
                        Log.d("SettingsActivity", "Upload successful for bottle data: " + dataFileName);
                    })
                    .addOnFailureListener(uploadException -> {
                        // Handle failed uploads
                        Log.d("SettingsActivity", "Upload failed for bottle data: " + dataFileName, uploadException);
                    });
            // Get the Uri for the bottle image
            Uri imageUri = bottle.getPhotoUri();
            // Check if the Uri is not null
            // This can happen if no image is uploaded for the bottle, thus using default image
            if (imageUri != null) {
                try {
                    InputStream stream = getContentResolver().openInputStream(imageUri);
                    // Get the file name from the Uri
                    String imageName = imageUri.getLastPathSegment();
                    // Create a storage reference for the image
                    assert imageName != null;
                    StorageReference imageFileRef = storage.getReference()
                            .child("users")
                            .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                            .child("bottles")
                            .child(imageName);
                    // Upload the image file
                    assert stream != null;
                    imageFileRef.putStream(stream)
                            .addOnSuccessListener(taskSnapshot -> {
                                // Handle successful uploads
                                Log.d("SettingsActivity", "Upload successful for bottle image: " + imageName);
                            })
                            .addOnFailureListener(uploadException -> {
                                // Handle failed uploads
                                Log.d("SettingsActivity", "Upload failed for bottle image: " + imageName, uploadException);
                            });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                // Handle the case where the Uri is null
                Log.d("SettingsActivity", "No photo URI for bottle: " + bottle.getName());
            }
        }
        //copy of bottle, but for cocktails
        List<Cocktail> cocktailList = loadCocktails(this);
        for (Cocktail cocktail : cocktailList) {
            String dataFileName = "cocktail_" + cocktail.getName() + ".txt";
            StorageReference dataFileRef = storage.getReference()
                    .child("users")
                    .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                    .child("cocktails")
                    .child(dataFileName);
            dataFileRef.putFile(Uri.fromFile(new File(getFilesDir(), dataFileName)))
                    .addOnSuccessListener(taskSnapshot -> Log.d("SettingsActivity", "Upload successful for cocktail data: " + dataFileName))
                    .addOnFailureListener(uploadException -> Log.d("SettingsActivity", "Upload failed for cocktail data: " + dataFileName, uploadException));
            Uri imageUri = cocktail.getPhotoUri();
            if (imageUri != null) {
                try {
                    InputStream stream = getContentResolver().openInputStream(imageUri);
                    String imageName = imageUri.getLastPathSegment();
                    assert imageName != null;
                    StorageReference imageFileRef = storage.getReference()
                            .child("users")
                            .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                            .child("cocktails")
                            .child(imageName);
                    assert stream != null;
                    imageFileRef.putStream(stream)
                            .addOnSuccessListener(taskSnapshot -> Log.d("SettingsActivity", "Upload successful for cocktail image: " + imageName))
                            .addOnFailureListener(uploadException -> Log.d("SettingsActivity", "Upload failed for cocktail image: " + imageName, uploadException));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("SettingsActivity", "No photo URI for cocktail: " + cocktail.getName());
            }
        }
        Toast.makeText(this, "Bottles Uploaded", Toast.LENGTH_SHORT).show(); // TODO: Make this only show if it's successful.
    }
    private void syncBottlesFromCloud() {
        // TODO: Maybe do a double-save of bottle images to the user's gallery like I do with AddABottle? Or maybe remove that now since there's cloud storage.
        if (mAuth.getCurrentUser() == null) {
            // Sign-In Check
            Toast.makeText(this, "Sign-In Required For This Feature", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get a reference to the Firebase Storage instance
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a reference to the user's bottles directory in Firebase Storage
        StorageReference userStorageRef = storage.getReference()
                .child("users")
                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .child("bottles");
        StorageReference userStorageRef2 = storage.getReference()
                .child("users")
                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .child("cocktails");

        // List all the files in the user's bottles directory in Firebase Storage
        userStorageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference fileRef : listResult.getItems()) {
                        // Get the file name
                        String fileName = fileRef.getName();

                        // Check if a file with the same name exists in the local storage
                        File localFile = new File(getFilesDir(), fileName);
                        if (!localFile.exists()) {
                            // If a file with the same name does not exist in the local storage, download the file from Firebase Storage
                            fileRef.getFile(localFile)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        // Handle successful downloads
                                        Log.d("SettingsActivity", "Download successful for bottle: " + fileName);
                                    })
                                    .addOnFailureListener(downloadException -> {
                                        // Handle failed downloads
                                        if (downloadException instanceof com.google.firebase.storage.StorageException
                                                && ((com.google.firebase.storage.StorageException) downloadException).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                                            Log.d("SettingsActivity", "File does not exist in Firebase Storage: " + fileName);
                                        } else {
                                            Log.d("SettingsActivity", "Download failed for bottle: " + fileName, downloadException);
                                        }
                                    });
                        }
                    }
                    //Toast.makeText(this, "Bottles Downloaded", Toast.LENGTH_SHORT).show(); // TODO: Make this only show if it's successful.
                })
                .addOnFailureListener(e -> {
                    // Handle errors in listing files
                    Log.d("SettingsActivity", "Failed to list files in Firebase Storage", e);
                });
        userStorageRef2.listAll() //copy of bottle, but for cocktails
                .addOnSuccessListener(listResult -> {
                    for (StorageReference fileRef : listResult.getItems()) {
                        String fileName = fileRef.getName();
                        File localFile = new File(getFilesDir(), fileName);
                        if (!localFile.exists()) {
                            fileRef.getFile(localFile)
                                    .addOnSuccessListener(taskSnapshot -> Log.d("SettingsActivity", "Download successful for cocktail: " + fileName))
                                    .addOnFailureListener(downloadException -> {
                                        if (downloadException instanceof com.google.firebase.storage.StorageException
                                                && ((com.google.firebase.storage.StorageException) downloadException).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                                            Log.d("SettingsActivity", "File does not exist in Firebase Storage: " + fileName);
                                        } else {
                                            Log.d("SettingsActivity", "Download failed for cocktail: " + fileName, downloadException);
                                        }
                                    });
                        }
                    }
                    Toast.makeText(this, "Bottles Downloaded", Toast.LENGTH_SHORT).show(); // TODO: Make this only show if it's successful.
                })
                .addOnFailureListener(e -> {
                    // Handle errors in listing files
                    Log.d("SettingsActivity", "Failed to list files in Firebase Storage", e);
                });
    }

    private void uploadLocationsToCloud() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Sign-In Required For This Feature", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Location Upload: Not Signed In");
            return;
        }
        List<Location> locationList = loadLocations(this);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        Log.d("MainActivity", "Location Upload: Stepped Past getInstance()");
        for (Location location : locationList) {
            String dataFileName = "location_" + location.getName() + ".txt";
            Log.d("MainActivity", "Location Queued for Upload: " + dataFileName);
            File localFile = new File(getFilesDir(), dataFileName);

            if (!localFile.exists()) {
                Log.d("MainActivity", "File does not exist: " + dataFileName);
                continue;
            }

            StorageReference dataFileRef = storage.getReference()
                    .child("users")
                    .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                    .child("locations")
                    .child(dataFileName);

            dataFileRef.putFile(Uri.fromFile(localFile))
                    .addOnSuccessListener(taskSnapshot -> Log.d("MainActivity", "Upload successful for location data: " + dataFileName))
                    .addOnFailureListener(uploadException -> Log.d("MainActivity", "Upload failed for location data: " + dataFileName, uploadException));
        }
        Toast.makeText(this, "Locations Uploaded", Toast.LENGTH_SHORT).show();
    }

    private void syncLocationsFromCloud() {
        if (mAuth.getCurrentUser() == null) {
            // Sign-In Check
            Toast.makeText(this, "Sign-In Required For This Feature", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get a reference to the Firebase Storage instance
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a reference to the user's locations directory in Firebase Storage
        StorageReference userStorageRef = storage.getReference()
                .child("users")
                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .child("locations");

        // List all the files in the user's locations directory in Firebase Storage
        userStorageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference fileRef : listResult.getItems()) {
                        // Get the file name
                        String fileName = fileRef.getName();

                        // Check if a file with the same name exists in the local storage
                        File localFile = new File(getFilesDir(), fileName);
                        if (!localFile.exists()) {
                            // If a file with the same name does not exist in the local storage, download the file from Firebase Storage
                            fileRef.getFile(localFile)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        // Handle successful downloads
                                        Log.d("MainActivity", "Download successful for location: " + fileName);
                                    })
                                    .addOnFailureListener(downloadException -> {
                                        // Handle failed downloads
                                        if (downloadException instanceof com.google.firebase.storage.StorageException
                                                && ((com.google.firebase.storage.StorageException) downloadException).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                                            Log.d("MainActivity", "File does not exist in Firebase Storage: " + fileName);
                                        } else {
                                            Log.d("MainActivity", "Download failed for location: " + fileName, downloadException);
                                        }
                                    });
                        }
                    }
                    Toast.makeText(this, "Locations Downloaded", Toast.LENGTH_SHORT).show(); // TODO: Make this only show if it's successful.
                })
                .addOnFailureListener(e -> {
                    // Handle errors in listing files
                    Log.d("MainActivity", "Failed to list files in Firebase Storage", e);
                });
    }

    private void eraseCloudStorage() {
        if (mAuth.getCurrentUser() == null) {
            // Sign-In Check
            Toast.makeText(this, "Sign-In Required For This Feature", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Cloud Storage Confirmation")
                .setMessage("Are you sure you want to delete all Bottle files from cloud storage?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    // Get a reference to the Firebase Storage instance
                    FirebaseStorage storage = FirebaseStorage.getInstance();

                    // Create a reference to the user's bottles directory in Firebase Storage
                    StorageReference userStorageRef = storage.getReference()
                            .child("users")
                            .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                            .child("bottles");
                    StorageReference userStorageRef2 = storage.getReference()
                            .child("users")
                            .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                            .child("cocktails");
                    // List all the files in the user's bottles directory in Firebase Storage
                    userStorageRef.listAll()
                            .addOnSuccessListener(listResult -> {
                                for (StorageReference fileRef : listResult.getItems()) {
                                    // Delete the file from Firebase Storage
                                    fileRef.delete()
                                            .addOnSuccessListener(aVoid -> {
                                                // Handle successful deletions
                                                Log.d("SettingsActivity", "Delete successful for file: " + fileRef.getName());
                                            })
                                            .addOnFailureListener(deleteException -> {
                                                // Handle failed deletions
                                                Log.d("SettingsActivity", "Delete failed for file: " + fileRef.getName(), deleteException);
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Handle errors in listing files
                                Log.d("SettingsActivity", "Failed to list files in Firebase Storage", e);
                            });
                    userStorageRef2.listAll()
                            .addOnSuccessListener(listResult -> {
                                for (StorageReference fileRef : listResult.getItems()) {
                                    // Delete the file from Firebase Storage
                                    fileRef.delete()
                                            .addOnSuccessListener(aVoid -> {
                                                // Handle successful deletions
                                                Log.d("SettingsActivity", "Delete successful for file: " + fileRef.getName());
                                            })
                                            .addOnFailureListener(deleteException -> {
                                                // Handle failed deletions
                                                Log.d("SettingsActivity", "Delete failed for file: " + fileRef.getName(), deleteException);
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Handle errors in listing files
                                Log.d("SettingsActivity", "Failed to list files in Firebase Storage", e);
                            });
                    Toast.makeText(this, "Cloud Storage Cleared", Toast.LENGTH_SHORT).show(); // TODO: Make this only show if it's successful.
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void uploadData(){
        uploadBottlesToCloud();
        uploadLocationsToCloud();
    }
    private void downloadData(){
        syncBottlesFromCloud();
        syncLocationsFromCloud();
    }

    //endregion

    //region Add Bottle
    public void addBottle() {
        if(drinkFlag) {
            setContentView(R.layout.addbottlewindow);
            bottleNameField = findViewById(R.id.bottleNameField);
            distillerField = findViewById(R.id.distillerField);
            spiritTypeField = findViewById(R.id.spiritTypeField);
            abvField = findViewById(R.id.abvField);
            ageField = findViewById(R.id.ageField);
            tastingNotesField = findViewById(R.id.tastingNotesField);
            regionField = findViewById(R.id.regionField);
            keywordsField = findViewById(R.id.keywordsField);
            ratingField = findViewById(R.id.ratingField);
            // Adjust header text if editing
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (editor == 1) {
                ImageButton switcher = findViewById(R.id.switchButton);
                switcher.setVisibility(View.GONE);
                Bottle bottleToEdit = getMostRecentBottle();
                toolbar.setTitle("Edit Bottle");
                popFields(bottleToEdit);
                editor = 0;
            } else {
                toolbar.setTitle("Add A Bottle");
            }
        } else{
            setContentView(R.layout.addcocktailwindow);
            cocktailNameField = findViewById(R.id.cocktailNameField);
            baseField = findViewById(R.id.baseField);
            mixerField = findViewById(R.id.mixerField);
            juiceField = findViewById(R.id.juiceField);
            liqueurField = findViewById(R.id.liqueurField);
            garnishField = findViewById(R.id.garnishField);
            extraField = findViewById(R.id.extraField);
            tastingNotesField = findViewById(R.id.tastingNotesField);
            keywordsField = findViewById(R.id.keywordsField);
            ratingField = findViewById(R.id.ratingField);
            // Adjust header text if editing
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (editor == 1) {
                ImageButton switcher = findViewById(R.id.switchButton);
                switcher.setVisibility(View.GONE);
                Cocktail cocktailToEdit = getMostRecentCocktail();
                toolbar.setTitle("Edit Cocktail");
                popFieldsCocktail(cocktailToEdit);
                editor = 0;
            } else {
                toolbar.setTitle("Add A Cocktail");
            }
        }
    }

    void KeyboardVanish(View view) {
        //if keyboard doesn't go away
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        view.clearFocus();
    }
    //endregion

    //region Camera & Image Code

    // See dedicated permissions region for old permission code

    // Method for choosing images for a bottle
    // Pops up after ONLY AFTER checking and/or requesting (and getting) permission
    private void chooseImageSource() {
        if(drinkFlag) {
            String bottleName = bottleNameField.getText().toString();
            if (bottleName.isEmpty()) {
                Toast.makeText(this, "Bottle Name Required To Add Image", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            String cocktailName = cocktailNameField.getText().toString();
            if (cocktailName.isEmpty()) {
                Toast.makeText(this, "Cocktail Name Required To Add Image", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, which) -> {
            if (options[which].equals("Take Photo")) {
                launchCameraIntent();
            } else if (options[which].equals("Choose From Gallery")) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);
            } else if (options[which].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    // Launching camera
    private void launchCameraIntent() {
        String bottleName;
        if (drinkFlag) {
            bottleName = bottleNameField.getText().toString() + "_BottlrCameraImage";
        } else {
            bottleName = cocktailNameField.getText().toString() + "_BottlrCameraImage";
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, bottleName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Taken in the Bottlr App using the Camera");
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    // TODO: This might be removable
    // Copies uploaded files into the app directory so they're not subject to the same URI issues they were before.
    // Not needed for photos taken with the camera, but this is related to how that saves to the user's gallery, too.
    private Uri copyImageToAppDir(Uri imageUri) throws IOException {
        InputStream is = getContentResolver().openInputStream(imageUri);
        String bottleName;
        if (drinkFlag) {
            bottleName = bottleNameField.getText().toString();
        } else {
            bottleName = cocktailNameField.getText().toString();
        }
        String filename = bottleName + "_BottlrImage.jpg";
        FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while (true) {
            assert is != null;
            if ((bytesRead = is.read(buffer)) == -1) break;
            //assert is != null;
            fos.write(buffer, 0, bytesRead);
        }
        is.close();
        fos.close();
        return Uri.fromFile(new File(getFilesDir(), filename));
    }
//endregion

    //region Bottle Saving
    // Saves bottle to a file
    private void saveEntryToFile() {
        if(drinkFlag) {
            String name = bottleNameField.getText().toString();

            // TODO: Copy fixed code which turned this into a bool to keep it open if a failed save happens
            // Check if the bottle has a name
            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return; // Do not proceed with saving if there's no name
            }

            String distillery = distillerField.getText().toString();
            String type = spiritTypeField.getText().toString();
            String abv = abvField.getText().toString();
            String age = ageField.getText().toString();
            String notes = tastingNotesField.getText().toString();
            String photoPath = (photoUri != null ? photoUri.toString() : "No photo");
            Log.d("AddABottle", "Image URI: " + photoUri);
            String region = regionField.getText().toString();
            String rating = ratingField.getText().toString();
            String keywords = keywordsField.getText().toString();

            String filename = "bottle_" + bottleNameField.getText().toString() + ".txt";
            String fileContents = "Name: " + name + "\n" +
                    "Distiller: " + distillery + "\n" +
                    "Type: " + type + "\n" +
                    "ABV: " + abv + "\n" +
                    "Age: " + age + "\n" +
                    "Notes: " + notes + "\n" +
                    "Region: " + region + "\n" +
                    "Keywords: " + keywords + "\n" +
                    "Rating: " + rating + "\n" +
                    "Photo: " + photoPath;

            try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
                fos.write(fileContents.getBytes());
                Toast.makeText(this, "Saved to " + getFilesDir() + "/" + filename, Toast.LENGTH_LONG).show();
            }

            // Exception handling
            catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
            }
        } else { //cocktail inclusion
            String name = cocktailNameField.getText().toString();

            // TODO: Copy fixed code which turned this into a bool to keep it open if a failed save happens
            // Check if the cocktail has a name
            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return; // Do not proceed with saving if there's no name
            }

            String base = baseField.getText().toString();
            String mixer = mixerField.getText().toString();
            String juice = juiceField.getText().toString();
            String liqueur = liqueurField.getText().toString();
            String garnish = garnishField.getText().toString();
            String extra = extraField.getText().toString();
            String notes = tastingNotesField.getText().toString();
            String photoPath = (photoUri != null ? photoUri.toString() : "No photo");
            Log.d("AddACocktail", "Image URI: " + photoUri);
            String rating = ratingField.getText().toString();
            String keywords = keywordsField.getText().toString();

            String filename = "cocktail_" + cocktailNameField.getText().toString() + ".txt";
            String fileContents = "Name: " + name + "\n" +
                    "Base: " + base + "\n" +
                    "Mixer: " + mixer + "\n" +
                    "Juice: " + juice + "\n" +
                    "Liqueur: " + liqueur + "\n" +
                    "Garnish: " + garnish + "\n" +
                    "Extra: " + extra + "\n" +
                    "Notes: " + notes + "\n" +
                    "Keywords: " + keywords + "\n" +
                    "Rating: " + rating + "\n" +
                    "Photo: " + photoPath;

            try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
                fos.write(fileContents.getBytes());
                Toast.makeText(this, "Saved to " + getFilesDir() + "/" + filename, Toast.LENGTH_LONG).show();
            }

            // Exception handling
            catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
            }
        }

        // Automatic Upload (If Applicable)
        // TODO: This
        // uploadBottleToCloud(bottle);
    }
    //endregion

    //region Bottle Editing Field Population
    // Populates fields with existing data if used as an edit class
    private void popFields(Bottle bottle) {
        if (bottle != null) {
            // Imports any existing data, but marks empty fields.
            bottleNameField.setText(bottle.getName() != null && !bottle.getName().isEmpty() ? bottle.getName() : "No Name Saved");
            distillerField.setText(bottle.getDistillery() != null && !bottle.getDistillery().isEmpty() ? bottle.getDistillery() : "");
            spiritTypeField.setText(bottle.getType() != null && !bottle.getType().isEmpty() ? bottle.getType() : "");
            abvField.setText(bottle.getAbv() != null && !bottle.getAbv().isEmpty() ? bottle.getAbv() : "");
            ageField.setText(bottle.getAge() != null && !bottle.getAge().isEmpty() ? bottle.getAge() : "");
            tastingNotesField.setText(bottle.getNotes() != null && !bottle.getNotes().isEmpty() ? bottle.getNotes() : "No Notes Saved");
            regionField.setText(bottle.getRegion() != null && !bottle.getRegion().isEmpty() ? bottle.getRegion() : "");
            keywordsField.setText(bottle.getKeywords() != null && !bottle.getKeywords().isEmpty() ? bottle.getKeywords() : "No Keywords Saved");
            ratingField.setText(bottle.getRating() != null && !bottle.getRating().isEmpty() ? bottle.getRating() : "No Rating");
            if (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo")) {
                photoUri = Uri.parse(bottle.getPhotoUri().toString());
                ImageView imagePreview = findViewById(R.id.imagePreview);
                imagePreview.setImageURI(photoUri);
            }
        }
    }

    private void popFieldsCocktail(Cocktail cocktail) {
        if (cocktail != null) {
            // Imports any existing data, but marks empty fields.
            cocktailNameField.setText(cocktail.getName() != null && !cocktail.getName().isEmpty() ? cocktail.getName() : "No Name Saved");
            baseField.setText(cocktail.getBase() != null && !cocktail.getBase().isEmpty() ? cocktail.getBase() : "No Base Saved");
            mixerField.setText(cocktail.getMixer() != null && !cocktail.getMixer().isEmpty() ? cocktail.getMixer() : "");
            juiceField.setText(cocktail.getJuice() != null && !cocktail.getJuice().isEmpty() ? cocktail.getJuice() : "");
            liqueurField.setText(cocktail.getLiqueur() != null && !cocktail.getLiqueur().isEmpty() ? cocktail.getLiqueur() : "");
            garnishField.setText(cocktail.getGarnish() != null && !cocktail.getGarnish().isEmpty() ? cocktail.getGarnish() : "");
            extraField.setText(cocktail.getExtra() != null && !cocktail.getExtra().isEmpty() ? cocktail.getExtra() : "");
            tastingNotesField.setText(cocktail.getNotes() != null && !cocktail.getNotes().isEmpty() ? cocktail.getNotes() : "No Notes Saved");
            keywordsField.setText(cocktail.getKeywords() != null && !cocktail.getKeywords().isEmpty() ? cocktail.getKeywords() : "");
            ratingField.setText(cocktail.getRating() != null && !cocktail.getRating().isEmpty() ? cocktail.getRating() : "No Rating");
            if (cocktail.getPhotoUri() != null && !cocktail.getPhotoUri().toString().equals("No photo")) {
                photoUri = Uri.parse(cocktail.getPhotoUri().toString());
                ImageView imagePreview = findViewById(R.id.imagePreview);
                imagePreview.setImageURI(photoUri);
            }
        }
    }
    //endregion

    //region New Search Code

    private boolean searchingCocktails = false;

    private void showSearchPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Search Type")
                .setItems(new String[]{"Bottle Search", "Cocktail Search", "Cancel"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Bottle Search
                            searchingCocktails = false;
                            goToSearchView();
                            break;
                        case 1: // Cocktail Search
                            searchingCocktails = true;
                            goToSearchView();
                            break;
                        case 2: // Cancel
                            dialog.dismiss();
                            break;
                    }
                });
        builder.create().show();
    }

    private void goToSearchView() {
        setContentView(R.layout.search_view);

        TextView header = findViewById(R.id.search_header);
        Spinner fieldSelectorSpinner = findViewById(R.id.field_selector_spinner);
        EditText queryField = findViewById(R.id.search_query);
        Button searchButton = findViewById(R.id.search_button);
        RecyclerView searchResultsRecyclerView = findViewById(R.id.search_results_recyclerview);

        // Load bottles before setting up the search view
        allBottles = loadBottles(this);
        allCocktails = loadCocktails(this);

        if (!searchingCocktails) {
            header.setText("Bottle Search");
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.bottle_fields, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fieldSelectorSpinner.setAdapter(adapter);
        } else {
            header.setText("Cocktail Search");
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.cocktail_fields, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fieldSelectorSpinner.setAdapter(adapter);
        }

        searchButton.setOnClickListener(v -> {
            String selectedField = fieldSelectorSpinner.getSelectedItem().toString();
            String query = queryField.getText().toString();
            Log.d("Search", "Search button clicked with field: " + selectedField + " and query: " + query);
            if (!selectedField.isEmpty() && !query.isEmpty()) {
                if (searchingCocktails) {
                    performCocktailSearch(selectedField, query);
                } else {
                    performBottleSearch(selectedField, query);
                }
            } else {
                Toast.makeText(this, "Please select a field and enter a query", Toast.LENGTH_SHORT).show();
            }
        });

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (searchingCocktails) {
            searchResultsAdapter2 = new CocktailAdapter(new ArrayList<>(), allCocktails, this::detailedViewCocktail);
            searchResultsRecyclerView.setAdapter(searchResultsAdapter2);
        } else {
            searchResultsAdapter = new BottleAdapter(new ArrayList<>(), allBottles, this::detailedView);
            searchResultsRecyclerView.setAdapter(searchResultsAdapter);
        }
    }

    private void performBottleSearch(String selectedField, String query) {
        List<Bottle> filteredBottles = new ArrayList<>();
        query = query.toLowerCase();
        Log.d("Search", "Performing bottle search for field: " + selectedField + " with query: " + query);
        Log.d("Search", "Total bottles available: " + allBottles.size());

        for (Bottle bottle : allBottles) {
            switch (selectedField) {
                case "Name":
                    if (bottle.getName().toLowerCase().contains(query)) {
                        filteredBottles.add(bottle);
                        Log.d("Search", "Bottle matched: " + bottle.getName());
                    }
                    break;
                case "Distillery":
                    if (bottle.getDistillery().toLowerCase().contains(query)) {
                        filteredBottles.add(bottle);
                    }
                    break;
                case "Type":
                    if (bottle.getType().toLowerCase().contains(query)) {
                        filteredBottles.add(bottle);
                    }
                    break;
                case "ABV":
                    if (bottle.getAbv().toLowerCase().contains(query)) {
                        filteredBottles.add(bottle);
                    }
                    break;
                case "Age":
                    if (bottle.getAge().toLowerCase().contains(query)) {
                        filteredBottles.add(bottle);
                    }
                    break;
                case "Notes":
                    if (bottle.getNotes().toLowerCase().contains(query)) {
                        filteredBottles.add(bottle);
                    }
                    break;
                case "Region":
                    if (bottle.getRegion().toLowerCase().contains(query)) {
                        filteredBottles.add(bottle);
                    }
                    break;
                case "Rating":
                    if (bottle.getRating().toLowerCase().contains(query)) {
                        filteredBottles.add(bottle);
                    }
                    break;
                case "Keywords":
                    if (bottle.getKeywords().toLowerCase().contains(query)) {
                        filteredBottles.add(bottle);
                    }
                    break;
            }
        }
        Log.d("Search", "Bottle search results: " + filteredBottles.size() + " items found");
        searchResultsAdapter.updateData(filteredBottles);
    }

    private void performCocktailSearch(String selectedField, String query) {
        List<Cocktail> filteredCocktails = new ArrayList<>();
        query = query.toLowerCase();

        for (Cocktail cocktail : allCocktails) {
            switch (selectedField) {
                case "Name":
                    if (cocktail.getName().toLowerCase().contains(query)) {
                        filteredCocktails.add(cocktail);
                    }
                    break;
                case "Base":
                    if (cocktail.getBase().toLowerCase().contains(query)) {
                        filteredCocktails.add(cocktail);
                    }
                    break;
                case "Mixer":
                    if (cocktail.getMixer().toLowerCase().contains(query)) {
                        filteredCocktails.add(cocktail);
                    }
                    break;
                case "Juice":
                    if (cocktail.getJuice().toLowerCase().contains(query)) {
                        filteredCocktails.add(cocktail);
                    }
                    break;
                case "Liqueur":
                    if (cocktail.getLiqueur().toLowerCase().contains(query)) {
                        filteredCocktails.add(cocktail);
                    }
                    break;
                case "Garnish":
                    if (cocktail.getGarnish().toLowerCase().contains(query)) {
                        filteredCocktails.add(cocktail);
                    }
                    break;
                case "Extra":
                    if (cocktail.getExtra().toLowerCase().contains(query)) {
                        filteredCocktails.add(cocktail);
                    }
                    break;
                case "Notes":
                    if (cocktail.getNotes().toLowerCase().contains(query)) {
                        filteredCocktails.add(cocktail);
                    }
                    break;
                case "Rating":
                    if (cocktail.getRating().toLowerCase().contains(query)) {
                        filteredCocktails.add(cocktail);
                    }
                    break;
                case "Keywords":
                    if (cocktail.getKeywords().toLowerCase().contains(query)) {
                        filteredCocktails.add(cocktail);
                    }
                    break;
            }
        }

        searchResultsAdapter2.updateData(filteredCocktails);
    }
    //endregion

    //region Location Code
    public void addNewLocation() {
        // Check if location permissions are granted
        if (!checkFineLocationPermission()) {
            requestFineLocationPermission();
            Log.d("MainActivity", "Location permission requested.");
        } else {
            Log.d("MainActivity", "createNewLocation() called.");
            createNewLocation();
        }
    }

    private void createNewLocation() {
        // Create a new Location object
        Location newLocation = new Location(this);
        newLocation.setName(newLocation.getLocationTimeStamp());
        newLocation.setGpsCoordinates(newLocation.getLocationCoordinates());
        newLocation.setTimeDateAdded(newLocation.getLocationTimeStamp());

        // Add the new Location object to the locationList array
        allLocations.add(newLocation);
        Log.d("MainActivity", newLocation + "added to locationList.");

        // Save the new Location object to a file
        saveLocationToFile(newLocation);

        // Notify the adapter that the data set has changed
        //locationAdapter.notifyDataSetChanged();
    }

    // Refined saveLocationToFile method
    private void saveLocationToFile(Location location) {
        String name = location.getName();
        String coords = location.getGpsCoordinates();
        String timestamp = location.getTimeDateAdded();

        String filename = "location_" + name + ".txt";
        String fileContents = "Name: " + name + "\n" +
                "Coordinates: " + coords + "\n" +
                "Timestamp: " + timestamp + "\n";

        try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
            fos.write(fileContents.getBytes());
            Toast.makeText(this, "Saved to " + getFilesDir() + "/" + filename, Toast.LENGTH_LONG).show();
            Log.d("MainActivity", filename + "saved.");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Error saving location.");
        }
    }


    //endregion

}
