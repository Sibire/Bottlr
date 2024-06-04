package com.example.bottlr;

import static com.example.bottlr.SharedUtils.queryBuilder;
import static com.example.bottlr.SharedUtils.saveImageToGallery;
import static com.example.bottlr.SharedUtils.shareBottleInfo;
import static com.example.bottlr.SharedUtils.showDeleteConfirm;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// TODO: Get the detail view to close properly when backing out of it

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private AppBarConfiguration mAppBarConfiguration;
    private List<Bottle> bottles;
    private List<Bottle> allBottles;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    // Relevant permissions code
    private static final int CAMERA_REQUEST_CODE = 201;
    private static final int GALLERY_REQUEST_CODE = 202;
    private EditText bottleNameField, distillerField, spiritTypeField, abvField, ageField, tastingNotesField, regionField, keywordsField, ratingField;
    ImageButton backButton;
    // Gallery storage URI
    private Uri photoUri;
    // Camera storage URI
    private Uri cameraImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);

        // AppCheck Code
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());

        //initialize bottle storage
        bottles = new ArrayList<>();
        allBottles = new ArrayList<>();
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
            setContentView(R.layout.fragment_search);
        } else if (id == R.id.menu_settings_button) { //settings area
            settings();
        } else if (id == R.id.fab) { //add bottle
            addBottle();
        } else if (id == R.id.addPhotoButton) { //add photo button
            if (checkCameraPermission()) { chooseImageSource(); } else { requestCameraPermission(); }
        } else if (id == R.id.saveButton) { //save bottle button
            saveEntryToFile();
            setContentView(R.layout.homescreen); //TODO: Make previous screen
        } else {
            //home screen? or error text?
        }
    }

    public void settings() {
        setContentView(R.layout.activity_settings);
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.bottlr_web_client_id)) // Keep this updated as needed
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
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
    }

    public void addBottle() {
        setContentView(R.layout.addbottlewindow);

        // Setting up the add bottle window congruent to .xml
        bottleNameField = findViewById(R.id.bottleNameField);
        distillerField = findViewById(R.id.distillerField);
        spiritTypeField = findViewById(R.id.spiritTypeField);
        abvField = findViewById(R.id.abvField);
        ageField = findViewById(R.id.ageField);
        tastingNotesField = findViewById(R.id.tastingNotesField);
        regionField = findViewById(R.id.regionField);
        keywordsField = findViewById(R.id.keywordsField);
        ratingField = findViewById(R.id.ratingField);
        backButton = findViewById(R.id.backButton);

        // Check for existing data (Critical for Edit function)
        // Adjust header text if editing
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (getIntent().hasExtra("bottle")) {
            Bottle bottleToEdit = getIntent().getParcelableExtra("bottle");
            toolbar.setTitle("Edit Bottle");
            popFields(bottleToEdit);
        } else {
            toolbar.setTitle("Add A Bottle");
        }
    }
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImageSource();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }

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

    // Launching the camera using the code originally implemented in main
    // Make sure to erase all permission and camera code from main and move it here
    // Main no longer has a use for that, adding it to the button there was just for testing
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
            fos.write(buffer, 0, bytesRead);
        }
        is.close();
        fos.close();
        return Uri.fromFile(new File(getFilesDir(), filename));
    }

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

        Set<String> keywords = new HashSet<>(Arrays.asList(keywordsField.getText().toString().split(",")));

        // Keyword String Constructor
        StringBuilder keywordsBuilder = new StringBuilder();
        for (String keyword : keywords) {
            if (keywordsBuilder.length() > 0) {
                keywordsBuilder.append(", ");
            }
            keywordsBuilder.append(keyword.trim());
        }

        String filename = "bottle_" + bottleNameField.getText().toString() + ".txt";
        String fileContents = "Name: " + name + "\n" +
                "Distiller: " + distillery + "\n" +
                "Type: " + type + "\n" +
                "ABV: " + abv + "\n" +
                "Age: " + age + "\n" +
                "Notes: " + notes + "\n" +
                "Region: " + region + "\n" +
                "Keywords: " + keywordsBuilder + "\n" +
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
            keywordsField.setText(bottle.getKeywords() != null && !bottle.getKeywords().isEmpty() ? String.join(", ", bottle.getKeywords()) : "No Keywords Saved");
            ratingField.setText(bottle.getRating() != null && !bottle.getRating().isEmpty() ? bottle.getRating() : "No Rating ( / 10) Saved");

            if (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo")) {
                photoUri = Uri.parse(bottle.getPhotoUri().toString());
                ImageView imagePreview = findViewById(R.id.imagePreview);
                imagePreview.setImageURI(photoUri);
            }
        }
    }

    @Override //TODO: Fix back button without activities or fragment tracking
    public void onBackPressed() {
        super.onBackPressed();
        /*Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);*/
        setContentView(R.layout.homescreen);
    }

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
                    // Update your UI here
                    updateSignedInUserTextView();
                    Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                });
    }
    @SuppressLint("SetTextI18n")
    private void updateSignedInUserTextView() {
        TextView signedInUserTextView = findViewById(R.id.signed_in_user);
        if (mAuth.getCurrentUser() != null) {
            signedInUserTextView.setText(mAuth.getCurrentUser().getEmail());
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
        bottles = SharedUtils.loadBottles(this);
        liquorAdapter = new BottleAdapter(bottles, allBottles, new BottleAdapter.OnBottleCheckListener() {
            @Override
            public void onButtonClick(String string) { detailedView(string); }
        });
        LiquorCabinetRecycler.setAdapter(liquorAdapter);
        liquorAdapter.notifyDataSetChanged();
    }
    ImageButton backButton2, deleteButton, shareButton, buyButton, editButton, saveImageButton;
    public void detailedView(String string) { //TODO: populate fields
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
        backButton2 = findViewById(R.id.backButton);
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
        backButton2.setOnClickListener(v -> finish());

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

    // TODO: Implement Automatic Save/Load
//    private void uploadSingleBottleToCloud(Bottle bottle) {
//        if (mAuth.getCurrentUser() == null) {
//            // Sign-In Check
//            return;
//        }
//        // Get a reference to the Firebase Storage instance
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//
//        // Get the file name for the bottle data
//        String dataFileName = "bottle_" + bottle.getName() + ".txt";
//
//        // Create a reference for the bottle data file
//        StorageReference dataFileRef = storage.getReference()
//                .child("users")
//                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
//                .child("bottles")
//                .child(dataFileName);
//
//        // Upload the bottle data file
//        dataFileRef.putFile(Uri.fromFile(new File(getFilesDir(), dataFileName)))
//                .addOnSuccessListener(taskSnapshot -> {
//                    // Handle successful uploads
//                    Log.d("SettingsActivity", "Upload successful for bottle data: " + dataFileName);
//                })
//                .addOnFailureListener(uploadException -> {
//                    // Handle failed uploads
//                    Log.d("SettingsActivity", "Upload failed for bottle data: " + dataFileName, uploadException);
//                });
//
//        // Get the Uri for the bottle image
//        Uri imageUri = bottle.getPhotoUri();
//
//        // Check if the Uri is not null
//        // This can happen if no image is uploaded for the bottle, thus using default image
//        if (imageUri != null) {
//            try {
//                InputStream stream = getContentResolver().openInputStream(imageUri);
//
//                // Get the file name from the Uri
//                String imageName = imageUri.getLastPathSegment();
//
//                // Create a storage reference for the image
//                assert imageName != null;
//                StorageReference imageFileRef = storage.getReference()
//                        .child("users")
//                        .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
//                        .child("bottles")
//                        .child(imageName);
//
//                // Upload the image file
//                assert stream != null;
//                imageFileRef.putStream(stream)
//                        .addOnSuccessListener(taskSnapshot -> {
//                            // Handle successful uploads
//                            Log.d("SettingsActivity", "Upload successful for bottle image: " + imageName);
//                        })
//                        .addOnFailureListener(uploadException -> {
//                            // Handle failed uploads
//                            Log.d("SettingsActivity", "Upload failed for bottle image: " + imageName, uploadException);
//                        });
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        } else {
//            // Handle the case where the Uri is null
//            Log.d("SettingsActivity", "No photo URI for bottle: " + bottle.getName());
//        }
//    }
}
