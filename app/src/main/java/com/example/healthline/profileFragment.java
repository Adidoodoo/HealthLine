package com.example.healthline;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class profileFragment extends Fragment {

    private TextView nameDisplay, emailDisplay, mobNumDisplay;
    private Button logoutButton;
    private FirebaseFirestore db;
    private FirebaseAuth authen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        logoutButton = view.findViewById(R.id.buttonLogout);
        nameDisplay = view.findViewById(R.id.textFullName);
        emailDisplay = view.findViewById(R.id.textEmail);
        mobNumDisplay = view.findViewById(R.id.textMobileNumber);

        db = FirebaseFirestore.getInstance();
        authen = FirebaseAuth.getInstance();

        FirebaseUser currentUser = authen.getCurrentUser();
        if (currentUser != null) {
            fetchUserData(currentUser.getUid());
        } else {
            Toast.makeText(getActivity(), "Not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }

        logoutButton.setOnClickListener(v -> {
            authen.signOut();
            Toast.makeText(getActivity(), "Logout Successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        });

        return view;
    }

    private void fetchUserData(String userId) {
        db.collection("userInformation")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            String middleName = document.getString("middleName");
                            String mobile = document.getString("mobileNumber");
                            String email = document.getString("email");

                            firstName = firstName != null ? firstName : "";
                            lastName = lastName != null ? lastName : "";
                            middleName = middleName != null ? " " + middleName : "";
                            mobile = mobile != null ? mobile : "Not provided";

                            String fullName = lastName + ", " + firstName + middleName;
                            nameDisplay.setText(fullName);
                            emailDisplay.setText(email != null ? email : "Email not found");
                            mobNumDisplay.setText(mobile);
                        } else {
                            nameDisplay.setText("User data not found");
                            Toast.makeText(getActivity(),
                                    "Please complete your profile",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        nameDisplay.setText("Error loading data");
                        Toast.makeText(getActivity(),
                                "Failed to load profile: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}