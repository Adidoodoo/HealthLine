package com.example.healthline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Button loginButton, registerButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        loginButton = findViewById(R.id.buttonLogin);
        registerButton = findViewById(R.id.buttonRegister);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("users")
                        .whereGreaterThanOrEqualTo("emailAddress", "a@gmail.com")
                        .whereLessThanOrEqualTo("emailAddress", "z@gmail.com")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                startActivity(new Intent(MainActivity.this, loginActivity.class));
                            } else {
                                Toast.makeText(MainActivity.this, "No account found. Please register.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, registerActivity.class));
                            }
                        });
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, registerActivity.class));
            }
        });
    }
}
