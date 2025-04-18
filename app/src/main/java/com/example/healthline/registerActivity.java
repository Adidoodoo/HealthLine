package com.example.healthline;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class registerActivity extends AppCompatActivity {

    private NestedScrollView view;
    private EditText inputLastName, inputFirstName, inputMiddleName, inputAddress, inputEmail, inputMobileNumber, inputPassword, inputConfirmPassword;
    private TextView errorLastName, errorFirstName, errorMiddleName, errorAddress, errorEmail, errorMobileNumber, errorPassword, errorConfirmPassword;
    private Button register, directLogin;
    private FirebaseFirestore db;
    private FirebaseAuth authen;
    private Map<String, Object> userInfo;
    private Map<String, Object> loginInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        view = findViewById(R.id.main);

        inputLastName = findViewById(R.id.inputLastName);
        inputFirstName = findViewById(R.id.inputFirstName);
        inputMiddleName = findViewById(R.id.inputMiddleName);
        inputAddress = findViewById(R.id.inputAddress);
        inputEmail = findViewById(R.id.inputEmail);
        inputMobileNumber = findViewById(R.id.inputMobileNumber);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);

        errorFirstName = findViewById(R.id.errorFirstName);
        errorLastName = findViewById(R.id.errorLastName);
        errorMiddleName = findViewById(R.id.errorMiddleName);
        errorAddress = findViewById(R.id.errorAddress);
        errorEmail = findViewById(R.id.errorEmail);
        errorMobileNumber = findViewById(R.id.errorMobileNumber);
        errorPassword = findViewById(R.id.errorPassword);
        errorConfirmPassword = findViewById(R.id.errorConfirmPassword);

        register = findViewById(R.id.buttonRegisterAccount);
        directLogin = findViewById(R.id.buttonLoginRedirect);

        db = FirebaseFirestore.getInstance();
        authen = FirebaseAuth.getInstance();


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isValid = true;
                errorLastName.setText("");
                errorFirstName.setText("");
                errorMiddleName.setText("");
                errorAddress.setText("");
                errorEmail.setText("");
                errorPassword.setText("");
                errorConfirmPassword.setText("");
                errorMobileNumber.setText("");

                if (inputFirstName.getText().toString().trim().isEmpty()) {
                    errorFirstName.setText(getString(R.string.error_blank_field));
                    isValid = false;
                }

                if (inputLastName.getText().toString().trim().isEmpty()) {
                    errorLastName.setText(getString(R.string.error_blank_field));
                    isValid = false;
                }

                if (inputMiddleName.getText().toString().trim().isEmpty()) {
                    errorMiddleName.setText(getString(R.string.error_blank_field));
                    isValid = false;
                }

                if (inputAddress.getText().toString().trim().isEmpty()) {
                    errorAddress.setText(getString(R.string.error_blank_field));
                    isValid = false;
                }

                if (inputEmail.getText().toString().trim().isEmpty()) {
                    errorEmail.setText(R.string.error_blank_field);
                    isValid = false;
                } else if (!inputEmail.getText().toString().trim().endsWith("@gmail.com")) {
                    errorEmail.setText(R.string.error_invalid_email);
                    isValid = false;
                }

                if (inputMobileNumber.getText().toString().trim().isEmpty()) {
                    errorMobileNumber.setText(getString(R.string.error_blank_field));
                    isValid = false;
                } else if (!inputMobileNumber.getText().toString().trim().startsWith("09") || inputMobileNumber.getText().toString().trim().length() != 11) {
                    errorMobileNumber.setText(getString(R.string.error_invalid_mobile));
                    isValid = false;
                }

                if (inputPassword.getText().toString().trim().isEmpty()) {
                    errorPassword.setText(getString(R.string.error_blank_field));
                    isValid = false;
                } else if (inputPassword.getText().toString().length() < 6) {
                    errorPassword.setText(getString(R.string.error_password_length));
                    isValid = false;
                }

                if (inputConfirmPassword.getText().toString().trim().isEmpty()) {
                    errorConfirmPassword.setText(getString(R.string.error_blank_field));
                    isValid = false;
                } else if (!inputConfirmPassword.getText().toString().equals(inputPassword.getText().toString())) {
                    errorConfirmPassword.setText(getString(R.string.error_password_mismatch));
                    isValid = false;
                }

                if (isValid) {
                    authenUser();
                }
            }


            public void authenUser() {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                authen.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = authen.getCurrentUser();
                                if (user != null) {
                                    registerUser(user.getUid(), email);
                                }
                            } else {
                                Toast.makeText(registerActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "fail", task.getException());
                            }
                        });
            }


            private void registerUser(String uid, String email) {
                userInfo = new HashMap<>();
                loginInfo = new HashMap<>();
                userInfo.put("firstName", inputFirstName.getText().toString().trim());
                userInfo.put("lastName", inputLastName.getText().toString().trim());
                userInfo.put("middleName", inputMiddleName.getText().toString().trim());
                userInfo.put("address", inputAddress.getText().toString().trim());
                userInfo.put("email", email);
                userInfo.put("mobileNumber", inputMobileNumber.getText().toString().trim());
                userInfo.put("userId", uid);


                loginInfo.put("email", inputEmail.getText().toString());
                loginInfo.put("password", inputPassword.getText().toString());
                loginInfo.put("mobileNumber", inputMobileNumber.getText().toString());
                loginInfo.put("userId", uid);

                db.collection("userInformation").document(uid)
                        .set(userInfo)
                        .addOnSuccessListener(view -> {
                            Log.d(TAG, "User information saved");

                            db.collection("loginInformation").document(uid)
                                    .set(loginInfo)
                                    .addOnSuccessListener(view2 -> {
                                        Log.d(TAG, "Login information saved");
                                        completeRegistration();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error saving login information", e);
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error saving user information", e);
                            Toast.makeText(registerActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show();
                            authen.getCurrentUser().delete();
                        });
            }
            private void completeRegistration() {
                Toast.makeText(registerActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                authen.signOut();
                startActivity(new Intent(registerActivity.this, loginActivity.class));
                finish();
            }
        });

        directLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("loginInformation")
                        .whereGreaterThanOrEqualTo("email", "a@gmail.com")
                        .whereGreaterThanOrEqualTo("email", "0@gmail.com")
                        .whereLessThanOrEqualTo("email", "z@gmail.com")
                        .whereGreaterThanOrEqualTo("email", "9@gmail.com")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && !task.getResult().isEmpty()) {
                                    startActivity(new Intent(registerActivity.this, loginActivity.class));
                                } else {
                                    Toast.makeText(registerActivity.this,"No accounts found. Please register.",Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(registerActivity.this,"Error checking accounts",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
