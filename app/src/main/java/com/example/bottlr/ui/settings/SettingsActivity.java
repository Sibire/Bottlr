package com.example.bottlr.ui.settings;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.firebase.auth.FirebaseAuth;
import com.example.bottlr.R;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        Button loginButton = findViewById(R.id.login_Button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(true)
                        .setServerClientId(SERVER_CLIENT_ID)
                        .build();
            }
        });
    }
}