package com.example.bottlr;

import static com.example.bottlr.SharedUtils.parseBottle;
import static com.example.bottlr.SharedUtils.queryBuilder;
import static com.example.bottlr.SharedUtils.saveImageToGallery;
import static com.example.bottlr.SharedUtils.shareBottleInfo;
import static com.example.bottlr.SharedUtils.showDeleteConfirm;
import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int CAMERA_REQUEST_CODE = 201, GALLERY_REQUEST_CODE = 202, LOCATION_REQUEST_CODE = 203;
    private EditText bottleNameField, distillerField, spiritTypeField, abvField,
            ageField, tastingNotesField, regionField, keywordsField, ratingField,
            nameField, distilleryField, typeField, notesField;
    private Uri photoUri, cameraImageUri;
    private BottleAdapter searchResultsAdapter;
    private int editor, lastLayout; //0 = no edits, 1 = bottle editor, 2 = setting access
    private String currentBottle;
    private LocationAdapter locationAdapter;

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
    }
    //endregion

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
            setContentView(R.layout.fragment_gallery);
            GenerateLiquorRecycler();
            lastLayout = R.layout.fragment_gallery;
        } else if (id == R.id.menu_search_button) { //nav search screen click
            setContentView(R.layout.fragment_search);
            search();
        } else if (id == R.id.search_button) { //search activate button
            performSearch();
            KeyboardVanish(view);
        } else if (id == R.id.menu_settings_button) { //settings area
            editor = 2;
            setContentView(R.layout.activity_settings);
            lastLayout = R.layout.activity_settings;
            settings();
        } else if (id == R.id.fab) { //add bottle
            editor = 0;
            addBottle();
        } else if (id == R.id.addPhotoButton) { //add photo button
            if (checkCameraPermission()) { chooseImageSource(); } else { requestCameraPermission(); }
            KeyboardVanish(view);
        } else if (id == R.id.saveButton) { //save bottle button
            saveEntryToFile();
            customBackButton();
        } else if (id == R.id.homescreen) { //fragment home
            homeScreen();
        } else if (id == R.id.deleteButton) { //delete bottle //TODO: screen changes before deletion selection
            showDeleteConfirm(getMostRecentBottle(), this);
            setContentView(R.layout.fragment_gallery);
            GenerateLiquorRecycler();
        } else if (id == R.id.shareButton) { //share bottle
            shareBottleInfo(getMostRecentBottle(), this);
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
        } else if (id == R.id.saveImageButton) { // Save the image to the user's gallery
            Bottle recentBottle = getMostRecentBottle();
            if (recentBottle != null && recentBottle.getPhotoUri() != null) {
                saveImageToGallery(this, recentBottle);}
        } else if (id == R.id.backButton) { //back button bottle
            customBackButton();
        } else if (id == R.id.sign_in_button_home) { //sign in home button
            SignInChecker(id);
        } else if (id == R.id.nfcButton) { //nfc button info
            nfcShare();
        } else if (id == R.id.search_liquor_button) { //search same screen liquor cabinet
            FrameLayout filterFrame = findViewById(R.id.liquorSearchFrame);
            filterFrame.setVisibility(View.VISIBLE);
        } else if (id == R.id.search_button_filterClick) { //search same screen liquor cabinet button
            filterSearch();
            KeyboardVanish(view);
        } else if (id == R.id.closefilterButton) { //search filter close
            FrameLayout filterFrame = findViewById(R.id.liquorSearchFrame);
            filterFrame.setVisibility(View.GONE);
            KeyboardVanish(view);
        }
        else if (id == R.id.menu_locations_button) { //nav locations screen click
            setContentView(R.layout.locations_page);
            GenerateLocationRecycler();
            lastLayout = R.layout.locations_page;
        }
        else if (id == R.id.addLocationButton) { // add location
            Log.d("MainActivity", "Add Location Button Clicked");
            addNewLocation();
        }
        else if (id == R.id.upload_locations_Button) { // add location
            Log.d("MainActivity", "Upload Locations Button Clicked");
            uploadLocationsToCloud();
        }
        else if (id == R.id.download_locations_Button) { // add location
            Log.d("MainActivity", "Download Locations Button Clicked");
            syncLocationsFromCloud();
        }
            else {
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
                if (checker != null) {
                    TextView tbottleName = findViewById(R.id.tvBottleName);
                    tbottleName.setText(currentBottle);
                    ImageView bottleImage = findViewById(R.id.detailImageView);
                    if(checker.getPhotoUri() == null && !bottleImage.toString().equals("No photo")) {
                        bottleImage.setImageResource(R.drawable.nodrinkimg);
                    } else {
                        bottleImage.setImageURI(checker.getPhotoUri());
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
    void GenerateLiquorRecycler() {
        // Set Recycler
        RecyclerView LiquorCabinetRecycler = findViewById(R.id.liquorRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LiquorCabinetRecycler.setLayoutManager(layoutManager);
        // Line divider to keep things nice and neat
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        LiquorCabinetRecycler.addItemDecoration(dividerItemDecoration);
        // Bottle listing
        BottleAdapter liquorAdapter;
        bottles = SharedUtils.loadBottles(this);
        liquorAdapter = new BottleAdapter(bottles, allBottles, this::detailedView);
        LiquorCabinetRecycler.setAdapter(liquorAdapter);
        liquorAdapter.notifyDataSetChanged();
    }
    void GenerateLocationRecycler() {
        // Set Recycler
        RecyclerView LocationRecycler = findViewById(R.id.LocationAdapter);
        if (LocationRecycler == null) {
            Log.d("MainActivity", "LocationRecycler is null");
            return;
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LocationRecycler.setLayoutManager(layoutManager);
        // Line divider to keep things nice and neat
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        LocationRecycler.addItemDecoration(dividerItemDecoration);
        // Locations listing
        locationAdapter = new LocationAdapter(allLocations); // Initialize locationAdapter
        LocationRecycler.setAdapter(locationAdapter);
        locationAdapter.notifyDataSetChanged();
        Log.d("MainActivity", "LocationRecycler and locationAdapter initialized");
        // TODO: Get this to display properly
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
            uploadButton.setOnClickListener(v -> uploadBottlesToCloud());
            Button syncButton = findViewById(R.id.sync_Button);
            syncButton.setOnClickListener(v -> syncBottlesFromCloud());
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
        List<Bottle> bottleList = SharedUtils.loadBottles(this);
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

        FirebaseStorage storage = FirebaseStorage.getInstance();
        Log.d("MainActivity", "Location Upload: Stepped Past getInstance()");
        for (Location location : allLocations) {
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
                    Toast.makeText(this, "Cloud Storage Cleared", Toast.LENGTH_SHORT).show(); // TODO: Make this only show if it's successful.
                })
                .setNegativeButton(android.R.string.no, null).show();
    }
    //endregion

    //region Add Bottle
    public void addBottle() {
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
            Bottle bottleToEdit = getMostRecentBottle();
            toolbar.setTitle("Edit Bottle");
            popFields(bottleToEdit);
            editor = 0;
        } else {
            toolbar.setTitle("Add A Bottle");
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
        String bottleName = bottleNameField.getText().toString();
        if (bottleName.isEmpty()) {
            Toast.makeText(this, "Bottle Name Required To Add Image", Toast.LENGTH_SHORT).show();
            return;
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
        String bottleName = bottleNameField.getText().toString() + "_BottlrCameraImage";
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
        String bottleName = bottleNameField.getText().toString();
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
            distillerField.setText(bottle.getDistillery() != null && !bottle.getDistillery().isEmpty() ? bottle.getDistillery() : "No Distiller Saved");
            spiritTypeField.setText(bottle.getType() != null && !bottle.getType().isEmpty() ? bottle.getType() : "No Type Saved");
            abvField.setText(bottle.getAbv() != null && !bottle.getAbv().isEmpty() ? bottle.getAbv() : "No ABV (%) Saved");
            ageField.setText(bottle.getAge() != null && !bottle.getAge().isEmpty() ? bottle.getAge() : "No Age (Years) Saved");
            tastingNotesField.setText(bottle.getNotes() != null && !bottle.getNotes().isEmpty() ? bottle.getNotes() : "No Notes Saved");
            regionField.setText(bottle.getRegion() != null && !bottle.getRegion().isEmpty() ? bottle.getRegion() : "No Data Saved");
            keywordsField.setText(bottle.getKeywords() != null && !bottle.getKeywords().isEmpty() ? bottle.getKeywords() : "No Keywords Saved");
            ratingField.setText(bottle.getRating() != null && !bottle.getRating().isEmpty() ? bottle.getRating() : "No Rating ( / 10) Saved");
            if (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo")) {
                photoUri = Uri.parse(bottle.getPhotoUri().toString());
                ImageView imagePreview = findViewById(R.id.imagePreview);
                imagePreview.setImageURI(photoUri);
            }
        }
    }
    //endregion

    //region Search Code
    public void search() {
        nameField = findViewById(R.id.search_name);
        distilleryField = findViewById(R.id.search_distillery);
        typeField = findViewById(R.id.search_type);
        abvField = findViewById(R.id.search_abv);
        ageField = findViewById(R.id.search_age);
        notesField = findViewById(R.id.search_notes);
        regionField = findViewById(R.id.search_region);
        ratingField = findViewById(R.id.search_rating);
        keywordsField = findViewById(R.id.search_keywords);
        RecyclerView searchResultsRecyclerView = findViewById(R.id.search_results_recyclerview);
        // Set LayoutManager
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize adapter and set it to the RecyclerView
        searchResultsAdapter = new BottleAdapter(bottles, allBottles, this::detailedView);
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);
    }

    private void performSearch() {
        Log.d("SearchFragment", "performSearch() called");
        // Get search criteria from user input
        String name = nameField.getText().toString().toLowerCase();
        String distillery = distilleryField.getText().toString().toLowerCase();
        String type = typeField.getText().toString().toLowerCase();
        String abv = abvField.getText().toString().toLowerCase();
        String age = ageField.getText().toString().toLowerCase();
        String notes = notesField.getText().toString().toLowerCase();
        String region = regionField.getText().toString().toLowerCase();
        String rating = ratingField.getText().toString().toLowerCase();
        String keywords = keywordsField.getText().toString().toLowerCase();

        // getBottlesToSearch() should retrieve the full list of Bottle objects
        List<Bottle> allBottles = SharedUtils.loadBottles(this);
        Log.d("SearchFragment", "All bottles: " + allBottles);

        // Filter the list based on search criteria
        List<Bottle> filteredList = allBottles.stream()
                .filter(bottle -> name.isEmpty() || (bottle.getName() != null && bottle.getName().trim().toLowerCase().contains(name.trim().toLowerCase())))
                .filter(bottle -> distillery.isEmpty() || (bottle.getDistillery() != null && bottle.getDistillery().trim().toLowerCase().contains(distillery.trim().toLowerCase())))
                .filter(bottle -> type.isEmpty() || (bottle.getType() != null && bottle.getType().trim().toLowerCase().contains(type.trim().toLowerCase())))
                .filter(bottle -> abv.isEmpty() || (bottle.getAbv() != null && bottle.getAbv().trim().toLowerCase().contains(abv.trim().toLowerCase())))
                .filter(bottle -> age.isEmpty() || (bottle.getAge() != null && bottle.getAge().trim().toLowerCase().contains(age.trim().toLowerCase())))
                .filter(bottle -> notes.isEmpty() || (bottle.getNotes() != null && bottle.getNotes().trim().toLowerCase().contains(notes.trim().toLowerCase())))
                .filter(bottle -> region.isEmpty() || (bottle.getRegion() != null && bottle.getRegion().trim().toLowerCase().contains(region.trim().toLowerCase())))
                .filter(bottle -> rating.isEmpty() || (bottle.getRating() != null && bottle.getRating().trim().toLowerCase().contains(rating.trim().toLowerCase())))
                .filter(bottle -> keywords.isEmpty() || (bottle.getKeywords() != null && bottle.getKeywords().trim().toLowerCase().contains(keywords.trim().toLowerCase())))
                .collect(Collectors.toList());
        Log.d("SearchFragment", "Filtered list: " + filteredList);
        // Check if the filtered list is empty and display a toast message if it is
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
        }
        // Keep this outside any if/else so that it clears the search results if there are none
        updateSearchResults(filteredList);
    }
    @SuppressLint("NotifyDataSetChanged")
    private void updateSearchResults(List<Bottle> filteredList) {
        searchResultsAdapter.setBottles(filteredList);
        searchResultsAdapter.notifyDataSetChanged();
    }

    public void filterSearch() {
        //hide filter
        FrameLayout filterFrame = findViewById(R.id.liquorSearchFrame);
        filterFrame.setVisibility(View.GONE);
        //hide regular cabinet
        RecyclerView liquor = findViewById(R.id.liquorRecycler);
        liquor.setVisibility(View.GONE);
        //show filtered results
        RecyclerView searchFilter = findViewById(R.id.search_results_recyclerview);
        searchFilter.setVisibility(View.VISIBLE);
        search();
        performSearch();
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
        locationAdapter.notifyDataSetChanged();
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
