package com.example.healthline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class loginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button login, directRegister;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        login = findViewById(R.id.buttonLogin);
        directRegister = findViewById(R.id.buttonRegisterRedirect);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        directRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(loginActivity.this, registerActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(loginActivity.this, "Email or Password cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    checkUserCredentials(email, password);
                }
            }
        });
    }

    private void checkUserCredentials(String email, String password) {
        db.collection("users")
                .whereEqualTo("emailAddress", email)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documentSnapshots = task.getResult();
                        if (documentSnapshots != null && !documentSnapshots.isEmpty()) {
                            Toast.makeText(loginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(loginActivity.this, homeActivity.class));
                        } else {
                            Toast.makeText(loginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(loginActivity.this, "Error checking credentials", Toast.LENGTH_SHORT).show();
                    }
                });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
