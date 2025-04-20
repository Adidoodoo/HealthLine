package com.example.healthline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class loginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button login, directRegister;
    private FirebaseAuth authen;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authen = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        login = findViewById(R.id.buttonLogin);
        directRegister = findViewById(R.id.buttonRegisterRedirect);

        directRegister.setOnClickListener(view -> {
            startActivity(new Intent(loginActivity.this, registerActivity.class));
        });

        login.setOnClickListener(view -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(loginActivity.this, "Email or Password cannot be empty",Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });
    }

    private void loginUser(String email, String password) {
        authen.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = authen.getCurrentUser();
                        if (user != null) {
                            db.collection("userInformation").document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful() && userTask.getResult().exists()) {
                                            db.collection("loginInformation").document(user.getUid())
                                                    .get()
                                                    .addOnCompleteListener(loginTask -> {
                                                        if (loginTask.isSuccessful() && loginTask.getResult().exists()) {
                                                            navigateToHome(email);
                                                        }else {
                                                            Toast.makeText(loginActivity.this, "Invalid credentials",Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    } else {
                        checkEmailExists(email);
                    }
                });
    }


    private void checkEmailExists(String email) {
        db.collection("loginInformation")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(loginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void navigateToHome(String email) {
        Intent intent = new Intent(loginActivity.this, homeActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}