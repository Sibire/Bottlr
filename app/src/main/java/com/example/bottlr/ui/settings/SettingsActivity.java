package com.example.bottlr.ui.settings;

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
import java.util.List;
import java.util.Objects;

// TODO: Automate cloud storage

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
                Log.d("SettingsActivity", "Google Sign In failed", e);
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
                        Toast.makeText(SettingsActivity.this, "Signed In Successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Navigate back to the settings window
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d("SettingsActivity", "Sign in failed");
                        Toast.makeText(SettingsActivity.this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
                        finish(); // Navigate back to the settings window
                    }
                });
    }
    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> {
                    // Update your UI here
                    Toast.makeText(SettingsActivity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Navigate back to the settings window
                });
    }
    private void uploadBottlesToCloud() {
        // Get a reference to the Firebase Storage instance
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Use the loadBottles() method to get all the bottles
        List<Bottle> bottleList = SharedUtils.loadBottles(this);

        // Loop through the bottles in the bottle list
        for (Bottle bottle : bottleList) {
            // Get the Uri for the bottle image
            Uri file = bottle.getPhotoUri();

            // Get the file name from the Uri
            String fileName = file.getLastPathSegment();

            // Create a storage reference
            StorageReference userStorageRef = storage.getReference()
                    .child("users")
                    .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                    .child("bottles")
                    .child(fileName);

            // Check if the file already exists
            userStorageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // File already exists, do not upload
                Log.d("SettingsActivity", "File already exists for bottle: " + fileName);
            }).addOnFailureListener(e -> {
                // File does not exist, upload the file
                userStorageRef.putFile(file)
                        .addOnSuccessListener(taskSnapshot -> {
                            // Handle successful uploads
                            Log.d("SettingsActivity", "Upload successful for bottle: " + fileName);
                        })
                        .addOnFailureListener(uploadException -> {
                            // Handle failed uploads
                            Log.d("SettingsActivity", "Upload failed for bottle: " + fileName, uploadException);
                        });
            });
        }
    }

    private void syncBottlesFromCloud() {
        // Code to sync bottles from cloud
    }

    private void eraseCloudStorage() {
        // Code to erase cloud storage
    }
}