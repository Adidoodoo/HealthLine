package com.example.healthline;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
    private EditText inputLastName, inputFirstName, inputMiddleName, inputAddress, inputEmail,
            inputMobileNumber, inputPassword, inputConfirmPassword;
    private TextView errorLastName, errorFirstName, errorMiddleName, errorAddress, errorEmail,
            errorMobileNumber, errorPassword, errorConfirmPassword;
    private Button register, directLogin;
    private FirebaseFirestore db;
    private FirebaseAuth authen;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        view = findViewById(R.id.main);

        initializeViews();
        db = FirebaseFirestore.getInstance();
        authen = FirebaseAuth.getInstance();

        register.setOnClickListener(view -> {
            if (validateInputs()) {
                showLoadingDialog();
                register.setEnabled(false);
                directLogin.setEnabled(false);
                authenUser();
            }
        });

        directLogin.setOnClickListener(view -> navigateToLogin());

        setupWindowInsets();
    }

    private void initializeViews() {
        inputLastName = findViewById(R.id.inputLastName);
        inputFirstName = findViewById(R.id.inputFirstName);
        inputMiddleName = findViewById(R.id.inputMiddleName);
        inputAddress = findViewById(R.id.inputAddress);
        inputEmail = findViewById(R.id.inputEmail);
        inputMobileNumber = findViewById(R.id.inputMobileNumber);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);

        errorLastName = findViewById(R.id.errorLastName);
        errorFirstName = findViewById(R.id.errorFirstName);
        errorMiddleName = findViewById(R.id.errorMiddleName);
        errorAddress = findViewById(R.id.errorAddress);
        errorEmail = findViewById(R.id.errorEmail);
        errorMobileNumber = findViewById(R.id.errorMobileNumber);
        errorPassword = findViewById(R.id.errorPassword);
        errorConfirmPassword = findViewById(R.id.errorConfirmPassword);

        register = findViewById(R.id.buttonRegisterAccount);
        directLogin = findViewById(R.id.buttonLoginRedirect);
    }

    private boolean validateInputs() {
        boolean isValid = true;
        clearErrorMessages();

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
        } else if (!inputMobileNumber.getText().toString().trim().startsWith("09") ||
                inputMobileNumber.getText().toString().trim().length() != 11) {
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

        return isValid;
    }

    private void clearErrorMessages() {
        errorLastName.setText("");
        errorFirstName.setText("");
        errorMiddleName.setText("");
        errorAddress.setText("");
        errorEmail.setText("");
        errorMobileNumber.setText("");
        errorPassword.setText("");
        errorConfirmPassword.setText("");
    }

    private void showLoadingDialog() {
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void authenUser() {
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
                        hideLoadingDialog();
                        enableButtons();
                        Toast.makeText(registerActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.w("REGISTRATION", "Failed", task.getException());
                    }
                });
    }

    private void registerUser(String uid, String email) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("firstName", inputFirstName.getText().toString().trim());
        userInfo.put("lastName", inputLastName.getText().toString().trim());
        userInfo.put("middleName", inputMiddleName.getText().toString().trim());
        userInfo.put("address", inputAddress.getText().toString().trim());
        userInfo.put("email", email);
        userInfo.put("mobileNumber", inputMobileNumber.getText().toString().trim());
        userInfo.put("userId", uid);

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("email", email);
        loginInfo.put("password", inputPassword.getText().toString());
        loginInfo.put("mobileNumber", inputMobileNumber.getText().toString());
        loginInfo.put("userId", uid);

        db.collection("userInformation").document(uid)
                .set(userInfo)
                .addOnSuccessListener(view -> {
                    db.collection("loginInformation").document(uid)
                            .set(loginInfo)
                            .addOnSuccessListener(view2 -> {
                                hideLoadingDialog();
                                completeRegistration();
                            })
                            .addOnFailureListener(e -> {
                                handleRegistrationFailure(uid, e);
                            });
                })
                .addOnFailureListener(e -> {
                    handleRegistrationFailure(uid, e);
                });
    }

    private void handleRegistrationFailure(String uid, Exception e) {
        hideLoadingDialog();
        enableButtons();
        Log.w("REGISTRATION", "Error saving data", e);
        Toast.makeText(this, "Error saving user data", Toast.LENGTH_SHORT).show();

        FirebaseUser user = authen.getCurrentUser();
        if (user != null) {
            user.delete();
        }
    }

    private void completeRegistration() {
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
        authen.signOut();
        startActivity(new Intent(this, loginActivity.class));
        finish();
    }

    private void enableButtons() {
        register.setEnabled(true);
        directLogin.setEnabled(true);
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, loginActivity.class));
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}