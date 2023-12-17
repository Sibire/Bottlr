package com.example.bottlr;

//region Imports
import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
//endregion

@SuppressWarnings("deprecation")
// Ignore deprecation issues
public class AddABottle extends AppCompatActivity {

    //region Permissions
    // Relevant permissions code
    private static final int PERMISSIONS_REQUEST_CODE = 200;
    private static final int CAMERA_REQUEST_CODE = 201;
    private static final int GALLERY_REQUEST_CODE = 202;
    //endregion

    //region Fields
    // Field initialization
    private EditText bottleNameField, distillerField, spiritTypeField, abvField, ageField, tastingNotesField, regionField, keywordsField, ratingField;

    // Buttons
    private Button addPhotoButton, saveButton, cancelButton;

    // Gallery storage URI
    private Uri photoUri;

    // Camera storage URI
    private Uri cameraImageUri;
    //endregion

    //region OnCreate

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        addPhotoButton = findViewById(R.id.addPhotoButton);
        addPhotoButton.setOnClickListener(view -> {
            if (checkPermissions()) {
                chooseImageSource();
            } else {
                requestPermissions();
            }
        });

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            saveEntryToFile();
            finish(); // Close add bottle and return to prior window. Works with OnResume refresh code.
        });
    }
    //endregion

    //region Permissions Code
    // Permission checking
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Requesting permissions where needed
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
    }

    // Notifying user that permissions are required to use features
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImageSource();
            } else {
                Toast.makeText(this, "Camera and Gallery permissions are required to use this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //endregion

    //region Choosing Image

    // Method for choosing images for a bottle
    // Pops up after ONLY AFTER checking and/or requesting (and getting) permission
    private void chooseImageSource() {
        CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");

        builder.setItems(options, (dialog, which) -> {
            if ("Take Photo".equals(options[which])) {
                launchCameraIntent();
            } else if ("Choose From Gallery".equals(options[which])) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);
            } else if ("Cancel".equals(options[which])) {
                dialog.dismiss();
            }
        });
        // Build the option popup
        builder.show();
    }
    //endregion

    //region Camera Code

    // Launching the camera using the code originally implemented in main
    // Make sure to erase all permission and camera code from main and move it here
    // Main no longer has a use for that, adding it to the button there was just for testing
    private void launchCameraIntent() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From The Camera");
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            ImageView imagePreview = findViewById(R.id.imagePreview);

            if (requestCode == GALLERY_REQUEST_CODE) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    photoUri = saveImageToLocal(imageUri); // Save image to user's gallery as a backup for loss of app memory
                    imagePreview.setImageURI(photoUri);
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                // Use the cameraImageUri directly
                photoUri = cameraImageUri;
                imagePreview.setImageURI(cameraImageUri);
            }
        }
    }
    //endregion

    //region Image Saving

    // Image save code using Bitmap
    private Uri saveImageToLocal(Bitmap bitmap) {
        try {
            String fileName = "bottle_" + System.currentTimeMillis() + ".png";
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return Uri.fromFile(getFileStreamPath(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    // Image save code using URI
    private Uri saveImageToLocal(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            return saveImageToLocal(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    //endregion

    //region Bottle Saving

    // Saves bottle to a file
    private void saveEntryToFile() {

        String name = bottleNameField.getText().toString();

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

        String filename = "bottle_" + System.currentTimeMillis() + ".txt";
        String fileContents = "Name: " + name + "\n" +
                "Distiller: " + distillery + "\n" +
                "Type: " + type + "\n" +
                "ABV: " + abv + "\n" +
                "Age: " + age + "\n" +
                "Notes: " + notes + "\n" +
                "Region: " + region + "\n" +
                "Keywords: " + keywordsBuilder.toString() + "\n" +
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
    //endregion

}
