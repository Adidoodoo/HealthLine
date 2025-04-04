package com.example.healthline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Button loginButton, registerButton;
    private FirebaseFirestore db;
    private FirebaseAuth authen;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = authen.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(MainActivity.this, homeActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        authen = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.buttonLogin);
        registerButton = findViewById(R.id.buttonRegister);

        loginButton.setOnClickListener(view -> {
            db.collection("loginInformation")
                    .whereGreaterThanOrEqualTo("email", "a@gmail.com")
                    .whereGreaterThanOrEqualTo("email", "0@gmail.com")
                    .whereLessThanOrEqualTo("email", "z@gmail.com")
                    .whereGreaterThanOrEqualTo("email", "9@gmail.com")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                startActivity(new Intent(MainActivity.this, loginActivity.class));
                            } else {
                                Toast.makeText(MainActivity.this,"No accounts found. Please register.",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, registerActivity.class));
                            }
                        } else {
                            Toast.makeText(MainActivity.this,"Error checking accounts",Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        registerButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, registerActivity.class));
        });
    }
}