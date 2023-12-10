package com.example.bottlr;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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

@SuppressWarnings("deprecation")
public class AddABottle extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 200;
    private static final int CAMERA_REQUEST_CODE = 201;
    private static final int GALLERY_REQUEST_CODE = 202;

    private EditText bottleNameField, distillerField, spiritTypeField, abvField, ageField, tastingNotesField;
    private Button addPhotoButton, saveButton;
    private Uri photoUri; // For storing the local photo's Uri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addbottlewindow);

        bottleNameField = findViewById(R.id.bottleNameField);
        distillerField = findViewById(R.id.distillerField);
        spiritTypeField = findViewById(R.id.spiritTypeField);
        abvField = findViewById(R.id.abvField);
        ageField = findViewById(R.id.ageField);
        tastingNotesField = findViewById(R.id.tastingNotesField);

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
            finish(); // Closes this activity and returns to the previous one
        });
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImageSource();
            } else {
                Toast.makeText(this, "Camera and Gallery permissions are required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void chooseImageSource() {
        CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");

        builder.setItems(options, (dialog, which) -> {
            if ("Take Photo".equals(options[which])) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, CAMERA_REQUEST_CODE);
            } else if ("Choose from Gallery".equals(options[which])) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);
            } else if ("Cancel".equals(options[which])) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            ImageView imagePreview = findViewById(R.id.imagePreview);

            if (requestCode == CAMERA_REQUEST_CODE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                photoUri = saveImageToLocal(imageBitmap);
                imagePreview.setImageBitmap(imageBitmap);
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                Uri originalUri = data.getData();
                if (originalUri != null) {
                    photoUri = saveImageToLocal(originalUri);
                    imagePreview.setImageURI(photoUri);
                }
            }
        }
    }

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

    private void saveEntryToFile() {
        String filename = "bottle_" + System.currentTimeMillis() + ".txt";
        String fileContents = "Name: " + bottleNameField.getText().toString() + "\n" +
                "Distiller: " + distillerField.getText().toString() + "\n" +
                "Type: " + spiritTypeField.getText().toString() + "\n" +
                "ABV: " + abvField.getText().toString() + "\n" +
                "Age: " + ageField.getText().toString() + "\n" +
                "Notes: " + tastingNotesField.getText().toString() + "\n" +
                "Photo: " + (photoUri != null ? photoUri.toString() : "No photo");

        try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
            fos.write(fileContents.getBytes());
            Toast.makeText(this, "Saved to " + getFilesDir() + "/" + filename, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
        }
    }
}
