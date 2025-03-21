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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class registerActivity extends AppCompatActivity {

    private EditText inputLastName, inputFirstName, inputMiddleName, inputAddress, inputEmail, inputMobileNumber, inputPassword, inputConfirmPassword;
    private TextView errorLastName, errorFirstName, errorMiddleName, errorAddress, errorEmail, errorMobileNumber, errorPassword, errorConfirmPassword;
    private Button register, directLogin;
    private FirebaseFirestore db;
    private Map<String, Object> userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

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

                if(isValid){
                    userInfo = new HashMap<>();
                    userInfo.put("firstName", inputFirstName.getText().toString());
                    userInfo.put("lastName", inputLastName.getText().toString());
                    userInfo.put("middleName", inputMiddleName.getText().toString());
                    userInfo.put("houseAddress", inputAddress.getText().toString());
                    userInfo.put("emailAddress", inputEmail.getText().toString());
                    userInfo.put("password", inputPassword.getText().toString());
                    db.collection("users")
                            .add(userInfo)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });

                    Toast.makeText(registerActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(registerActivity.this, MainActivity.class));
                }

            }
        });

        directLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = "test@gmail.com";

                db.collection("users")
                        .whereEqualTo("emailAddress", email)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                startActivity(new Intent(registerActivity.this, loginActivity.class));
                            } else {
                                Toast.makeText(registerActivity.this, "Account not found. Please register.", Toast.LENGTH_SHORT).show();
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
