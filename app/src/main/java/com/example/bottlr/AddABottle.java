package com.example.bottlr;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AddABottle extends AppCompatActivity {

    //region Permissions
    // Relevant permissions code
    private static final int CAMERA_REQUEST_CODE = 201;
    private static final int GALLERY_REQUEST_CODE = 202;
    //endregion

    //region Fields
    // Field initialization
    private EditText bottleNameField, distillerField, spiritTypeField, abvField, ageField, tastingNotesField, regionField, keywordsField, ratingField;

    ImageButton backButton;

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
        backButton = findViewById(R.id.backButton);

        // Photo Button
        Button addPhotoButton = findViewById(R.id.addPhotoButton);
        addPhotoButton.setOnClickListener(view -> {
            if (checkCameraPermission()) {
                chooseImageSource();
            } else {
                requestCameraPermission();
            }
        });

        // Save Button
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            saveEntryToFile();
            finish(); // Close add bottle and return to prior window. Works with OnResume refresh code.
        });

        // Back button listener
        backButton.setOnClickListener(v -> finish());

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

    //endregion

    //region Permissions Code
    // Permission checking
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
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
    //endregion

    //region Choosing Image

    // Method for choosing images for a bottle
    // Pops up after ONLY AFTER checking and/or requesting (and getting) permission
    private void chooseImageSource() {
        String bottleName = bottleNameField.getText().toString();
        if (bottleName.isEmpty()) {
            Toast.makeText(this, "Bottle Name Required To Add Image", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
    //endregion

    //region Camera Code

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
    //endregion

    //region Field Population

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

    //endregion

}
