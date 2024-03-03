package com.example.bottlr.ui.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import com.example.bottlr.SharedUtils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.example.bottlr.Bottle;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.bottlr.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

// TODO: Automate cloud storage
// TODO: Toast Notifications for Upload, Sync, Delete

public class SettingsActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                        Toast.makeText(SettingsActivity.this, "Signed In Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d("SettingsActivity", "Sign in failed");
                        Toast.makeText(SettingsActivity.this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void signOut() {
        if (mAuth.getCurrentUser() == null) {
            // Sign-In Check
            Toast.makeText(SettingsActivity.this, "You're Already Signed Out", Toast.LENGTH_SHORT).show();
            return;
        }
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> {
                    // Update your UI here
                    updateSignedInUserTextView();
                    Toast.makeText(SettingsActivity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(SettingsActivity.this, "Sign-In Required For This Feature", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(SettingsActivity.this, "Bottles Uploaded", Toast.LENGTH_SHORT).show(); // TODO: Make this only show if it's successful.
    }

    private void syncBottlesFromCloud() {
        // TODO: Maybe do a double-save of bottle images to the user's gallery like I do with AddABottle? Or maybe remove that now since there's cloud storage.
        if (mAuth.getCurrentUser() == null) {
            // Sign-In Check
            Toast.makeText(SettingsActivity.this, "Sign-In Required For This Feature", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SettingsActivity.this, "Bottles Downloaded", Toast.LENGTH_SHORT).show(); // TODO: Make this only show if it's successful.
                })
                .addOnFailureListener(e -> {
                    // Handle errors in listing files
                    Log.d("SettingsActivity", "Failed to list files in Firebase Storage", e);
                });
    }

    private void eraseCloudStorage() {
        if (mAuth.getCurrentUser() == null) {
            // Sign-In Check
            Toast.makeText(SettingsActivity.this, "Sign-In Required For This Feature", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SettingsActivity.this, "Cloud Storage Cleared", Toast.LENGTH_SHORT).show(); // TODO: Make this only show if it's successful.
                })
                .setNegativeButton(android.R.string.no, null).show();
    }
}