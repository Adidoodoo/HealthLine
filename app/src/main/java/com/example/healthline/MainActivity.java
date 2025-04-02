package com.example.healthline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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


    /*
    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser user = authen.getCurrentUser();
        if(user != null){
            startActivity(new Intent(MainActivity.this, homeActivity.class));
            finish();
        }
    }

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        authen = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.buttonLogin);
        registerButton = findViewById(R.id.buttonRegister);

        loginButton.setOnClickListener(view -> {
                String email = "@gmail.com".trim();
                db.collection("loginInformation")
                        .whereGreaterThanOrEqualTo("email", "a@gmail.com")
                        .whereLessThanOrEqualTo("email", "z@gmail.com")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                startActivity(new Intent(MainActivity.this, loginActivity.class));
                            } else {
                                Toast.makeText(MainActivity.this, "Account not found. Please register.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, registerActivity.class));
                            }
                        });
        });



        registerButton.setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, registerActivity.class));
        });
    }
}
