package com.example.healthline;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class queueStatusActivity extends AppCompatActivity {

    private TextView textHospital, textDepartment, textDoctor;
    private TextView textQueueNumber;
    private Button buttonOnTheWay, removeQueue;
    private FirebaseFirestore db;
    private FirebaseAuth authen;
    private String hospitalId, queueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_status);

        textHospital = findViewById(R.id.textHospital);
        textDepartment = findViewById(R.id.textDepartment);
        textDoctor = findViewById(R.id.textDoctor);
        textQueueNumber = findViewById(R.id.textQueueNumber);
        buttonOnTheWay = findViewById(R.id.buttonOnTheWay);
        removeQueue = findViewById(R.id.removeQueue);

        db = FirebaseFirestore.getInstance();
        authen = FirebaseAuth.getInstance();

        hospitalId = getIntent().getStringExtra("HOSPITAL_ID");
        queueId = getIntent().getStringExtra("QUEUE_ID");

        if (hospitalId == null || queueId == null) {
            Toast.makeText(this, "Invalid queue reference", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        buttonOnTheWay.setOnClickListener(v -> updateStatus("on_the_way"));
        removeQueue.setOnClickListener(v -> removeQueue());

        loadQueueData();
    }

    private void loadQueueData() {
        db.collection("hospitalQueues")
                .document(hospitalId)
                .collection("queues")
                .document(queueId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        textHospital.setText(documentSnapshot.getString("hospitalName"));
                        textDepartment.setText(documentSnapshot.getString("departmentName"));
                        textDoctor.setText(documentSnapshot.getString("doctorName"));
                        textQueueNumber.setText(String.valueOf(documentSnapshot.getLong("queueNumber")));
                    } else {
                        Toast.makeText(this, "Queue not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading queue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateStatus(String newStatus) {
        db.collection("hospitalQueues")
                .document(hospitalId)
                .collection("queues")
                .document(queueId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    updateGlobalQueueStatus(newStatus);
                    Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateGlobalQueueStatus(String newStatus) {
        FirebaseUser user = authen.getCurrentUser();
        if (user != null) {
            db.collection("userInformation").document(user.getUid())
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            String globalQueueId = userDoc.getString("activeGlobalQueueId");
                            if (globalQueueId != null) {
                                db.collection("queues").document(globalQueueId)
                                        .update("status", newStatus);
                            }
                        }
                    });
        }
    }

    private void removeQueue() {
        FirebaseUser user = authen.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference hospitalQueueRef = db.collection("hospitalQueues")
                .document(hospitalId)
                .collection("queues")
                .document(queueId);

        db.collection("userInformation").document(user.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String globalQueueId = userDoc.getString("activeGlobalQueueId");

                        db.collection("hospitalQueues")
                                .document(hospitalId)
                                .collection("queues")
                                .document(queueId)
                                .get()
                                .addOnSuccessListener(queueDoc -> {
                                    if (queueDoc.exists()) {
                                        String departmentName = queueDoc.getString("departmentName");

                                        db.runTransaction(transaction -> {
                                            DocumentReference deptRef = db.collection("hospitals")
                                                    .document(hospitalId)
                                                    .collection("departments")
                                                    .document(departmentName);
                                            transaction.update(deptRef, "currentQueue", FieldValue.increment(-1));

                                            transaction.delete(hospitalQueueRef);
                                            if (globalQueueId != null) {
                                                transaction.delete(db.collection("queues").document(globalQueueId));
                                            }

                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("activeQueueId", FieldValue.delete());
                                            updates.put("activeHospitalId", FieldValue.delete());
                                            updates.put("activeGlobalQueueId", FieldValue.delete());
                                            transaction.update(db.collection("userInformation").document(user.getUid()), updates);

                                            return null;
                                        }).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(this, "Queue removed", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Removal failed: " + task.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                    }
                });
    }
}