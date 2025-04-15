package com.example.healthline;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class homeFragment extends Fragment {
    private FirebaseAuth authen;
    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authen = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button buttonQueueUp = view.findViewById(R.id.buttonQueueUp);
        Button buttonQueueStatus = view.findViewById(R.id.buttonQueueStatus);

        buttonQueueUp.setOnClickListener(v -> startActivity(new Intent(getActivity(), queueUpActivity.class)));

        buttonQueueStatus.setOnClickListener(v -> {
            if (authen.getCurrentUser() != null) {
                checkUserQueue(authen.getCurrentUser().getUid());
            } else {
                Toast.makeText(getActivity(), "Please sign in first", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void checkUserQueue(String userId) {
        db.collection("userInformation").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists() && doc.contains("activeQueueId") && doc.contains("activeHospitalId")) {
                            startQueueStatusActivity(
                                    doc.getString("activeHospitalId"),
                                    doc.getString("activeQueueId")
                            );
                        } else {
                            Toast.makeText(getActivity(), "No active queue found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error checking queue", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startQueueStatusActivity(String hospitalId, String queueId) {
        startActivity(new Intent(getActivity(), queueStatusActivity.class)
                .putExtra("HOSPITAL_ID", hospitalId)
                .putExtra("QUEUE_ID", queueId));
    }
}