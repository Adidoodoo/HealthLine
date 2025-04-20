package com.example.healthline;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class profileFragment extends Fragment {

    private TextView nameDisplay, emailDisplay, mobNumDisplay;
    private Button logoutButton, deleteAccountButton;
    private FirebaseFirestore db;
    private FirebaseAuth authen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        logoutButton = view.findViewById(R.id.buttonLogout);
        deleteAccountButton = view.findViewById(R.id.buttonDeleteAccount);
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
            showLogoutDialog();
        });

        deleteAccountButton.setOnClickListener(v -> {
            showDeleteAccountDialog();
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

                            middleName = middleName != null ? " " + middleName : "";


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

    private void deleteAccount(FirebaseUser user) {
        db.collection("userInformation").document(user.getUid())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void a) {
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            db.collection("loginInformation").document(user.getUid())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void b) {
                                                            Toast.makeText(getActivity(),
                                                                    "Account deleted successfully",
                                                                    Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(getActivity(), MainActivity.class));
                                                            getActivity().finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(getActivity(),
                                                                    "Failed to delete user data: " + e.getMessage(),
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(getActivity(),
                                                    "Failed to delete account: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(),
                                "Failed to delete user data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.logout_warning);
        dialog.setCancelable(true);

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            showLogoutProgress();
        });

        dialog.show();
    }

    private void showLogoutProgress() {
        Dialog progressDialog = new Dialog(requireContext());
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.logging_out_progressbar);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
            performLogout();
        }, 1500);
    }

    private void performLogout(){
        authen.signOut();
        Toast.makeText(getActivity(), "Logout Successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }

    private void showDeleteAccountDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.delete_account_warning);
        dialog.setCancelable(true);

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteAccountProgress();
        });

        dialog.show();
    }

    private void showDeleteAccountProgress() {
        Dialog progressDialog = new Dialog(requireContext());
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.deleting_account_progressbar);
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseUser currentUser = authen.getCurrentUser();
        if (currentUser != null) {
            deleteAccount(currentUser);
        }
    }
}