package com.example.healthline;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class queueUpActivity extends AppCompatActivity {

    private Spinner hospitalSpinner, departmentSpinner;
    private EditText patientCommentsEditText;
    private Button startQueue, backToHome;
    private FrameLayout progressOverlay;
    private FirebaseFirestore db;
    private FirebaseAuth authen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_up);

        hospitalSpinner = findViewById(R.id.spinnerHospital);
        departmentSpinner = findViewById(R.id.spinnerDepartment);
        patientCommentsEditText = findViewById(R.id.editTextPatientComments);
        startQueue = findViewById(R.id.buttonGetQueue);
        backToHome = findViewById(R.id.buttonBack);
        progressOverlay = findViewById(R.id.progress_overlay);

        db = FirebaseFirestore.getInstance();
        authen = FirebaseAuth.getInstance();

        startQueue.setOnClickListener(v -> startQueueProcess());
        backToHome.setOnClickListener(v -> finish());

        loadHospitals();
    }

    private void startQueueProcess() {
        showLoading(true);
        FirebaseUser user = authen.getCurrentUser();
        if (user == null) {
            showLoading(false);
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String hospitalName = hospitalSpinner.getSelectedItem().toString();
        String departmentName = departmentSpinner.getSelectedItem().toString();
        String comments = patientCommentsEditText.getText().toString().trim();

        if (comments.isEmpty()) {
            comments = "No comments provided";
        }

        checkExistingQueue(userId, hospitalName, departmentName, comments);
    }

    private void checkExistingQueue(String userId, String hospitalName, String departmentName, String comments) {
        db.collection("userInformation").document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        showLoading(false);
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (userDoc.contains("activeQueueId")) {
                        verifyActiveQueue(userDoc.getString("activeQueueId"), userDoc, userId, hospitalName, departmentName, comments);
                    } else {
                        createNewQueue(userDoc, userId, hospitalName, departmentName, comments);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error checking queue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void verifyActiveQueue(String queueId, DocumentSnapshot userDoc, String userId, String hospitalName, String departmentName, String comments) {
        db.collection("hospitals")
                .whereEqualTo("name", hospitalName)
                .limit(1)
                .get()
                .addOnSuccessListener(hospitalQuery -> {
                    if (!hospitalQuery.isEmpty()) {
                        String hospitalId = hospitalQuery.getDocuments().get(0).getId();
                        db.collection("hospitalQueues")
                                .document(hospitalId)
                                .collection("queues")
                                .document(queueId)
                                .get()
                                .addOnSuccessListener(existingQueue -> {
                                    if (existingQueue.exists() && !"completed".equals(existingQueue.getString("status"))) {
                                        showLoading(false);
                                        Toast.makeText(this,
                                                "You already have queue #" + existingQueue.getLong("queueNumber"),
                                                Toast.LENGTH_LONG).show();
                                        redirectToQueueStatus();
                                    } else {
                                        createNewQueue(userDoc, userId, hospitalName, departmentName, comments);
                                    }
                                });
                    }
                });
    }

    private void createNewQueue(DocumentSnapshot userDoc, String userId, String hospitalName, String departmentName, String comments) {
        String fullName = userDoc.getString("lastName") + ", " + userDoc.getString("firstName") + " " + userDoc.getString("middleName");
        db.collection("hospitals")
                .whereEqualTo("name", hospitalName)
                .limit(1)
                .get()
                .addOnSuccessListener(hospitalQuery -> {
                    if (hospitalQuery.isEmpty()) {
                        showLoading(false);
                        Toast.makeText(this, "Hospital not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot hospitalDoc = hospitalQuery.getDocuments().get(0);
                    String hospitalId = hospitalDoc.getId();

                    db.collection("hospitals")
                            .document(hospitalId)
                            .collection("departments")
                            .whereEqualTo("departmentName", departmentName)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(deptQuery -> {
                                if (deptQuery.isEmpty()) {
                                    showLoading(false);
                                    Toast.makeText(this, "Department not found", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                DocumentSnapshot deptDoc = deptQuery.getDocuments().get(0);
                                processQueueCreation(deptDoc, hospitalId, userId, fullName, hospitalName, departmentName, comments);
                            });
                });
    }

    private void processQueueCreation(DocumentSnapshot deptDoc, String hospitalId, String userId, String fullName, String hospitalName, String departmentName, String comments) {
        String doctorName = deptDoc.getString("doctorName");
        long currentQueue = deptDoc.getLong("currentQueue") != null ? deptDoc.getLong("currentQueue") : 20;

        Map<String, Object> queueData = new HashMap<>();
        queueData.put("patientName", fullName);
        queueData.put("hospitalName", hospitalName);
        queueData.put("departmentName", departmentName);
        queueData.put("doctorName", doctorName);
        queueData.put("queueNumber", currentQueue + 20);
        queueData.put("status", "waiting");
        queueData.put("timestamp", FieldValue.serverTimestamp());
        queueData.put("userId", userId);
        queueData.put("patientComments", comments);

        db.runTransaction(transaction -> {
            transaction.update(deptDoc.getReference(), "currentQueue", currentQueue + 1);

            DocumentReference queueRef = db.collection("hospitalQueues").document(hospitalId)
                            .collection("departments").document(departmentName)
                            .collection("queues").document();

            transaction.set(queueRef, queueData);

            DocumentReference globalQueueRef = db.collection("queues").document();
            transaction.set(globalQueueRef, queueData);

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("activeQueueId", queueRef.getId());
            updateData.put("activeHospitalId", hospitalId);
            updateData.put("activeDepartmentName", departmentName);
            updateData.put("activeGlobalQueueId", globalQueueRef.getId());
            transaction.update(db.collection("userInformation").document(userId), updateData);

            return currentQueue + 1;
        }).addOnCompleteListener(task -> {
            showLoading(false);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Queue successful!", Toast.LENGTH_LONG).show();
                redirectToQueueStatus();
            } else {
                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToQueueStatus() {
        finish();
        Toast.makeText(this, "Queue created successfully!", Toast.LENGTH_LONG).show();
    }

    private void loadHospitals() {
        showLoading(true);
        db.collection("hospitals").get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        List<String> hospitals = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            hospitals.add(doc.getString("name"));
                        }
                        setupHospitalSpinner(hospitals);
                    } else {
                        Toast.makeText(this, "Failed to load hospitals", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupHospitalSpinner(List<String> hospitals) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hospitals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hospitalSpinner.setAdapter(adapter);

        hospitalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadDepartments(hospitals.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                departmentSpinner.setAdapter(null);
            }
        });
    }

    private void loadDepartments(String hospitalName) {
        showLoading(true);
        db.collection("hospitals")
                .whereEqualTo("name", hospitalName)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String hospitalId = task.getResult().getDocuments().get(0).getId();
                        fetchDepartmentData(hospitalId);
                    }
                });
    }

    private void fetchDepartmentData(String hospitalId) {
        db.collection("hospitals")
                .document(hospitalId)
                .collection("departments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> departments = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            departments.add(doc.getString("departmentName"));
                        }
                        setupDepartmentSpinner(departments);
                    }
                });
    }

    private void setupDepartmentSpinner(List<String> departments) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentSpinner.setAdapter(adapter);
    }

    private void showLoading(boolean show) {
        if (show) {
            Dialog progressDialog = new Dialog(this);
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressDialog.setContentView(R.layout.loading_progressbar);
            progressDialog.setCancelable(false);
            progressDialog.show();
            progressOverlay.setTag(progressDialog);
        } else {
            Dialog progressDialog = (Dialog) progressOverlay.getTag();
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
}