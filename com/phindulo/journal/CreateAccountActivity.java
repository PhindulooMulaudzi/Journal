package com.phindulo.journal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.phindulo.journal.Util.JournalAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity implements View.OnClickListener {
    private Button createAccountButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;
    private EditText userNameEditText;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Create firestore connection
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = database.collection("Users");
    private final String TAG = "CreateAccountActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        createAccountButton = findViewById(R.id.create_account_button);
        progressBar = findViewById(R.id.create_acct_progressbar);
        emailEditText = findViewById(R.id.email_account);
        passwordEditText = findViewById(R.id.account_password);
        userNameEditText = findViewById(R.id.username_account);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
/*
                if (currentUser != null) {
                    //user already logged in
                } else {
                    //user not registered yet
                }*/
            }
        };

        createAccountButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_account_button:
                if (!TextUtils.isEmpty(passwordEditText.getText().toString())
                        && !TextUtils.isEmpty(emailEditText.getText().toString())
                        && !TextUtils.isEmpty(userNameEditText.getText().toString())) {

                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    String username = userNameEditText.getText().toString().trim();

                    createUserEmailAccount(email, password, username);
                } else {
                    Toast.makeText(this, "Empty fields not allowed!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void createUserEmailAccount(String email, String password, final String username) {
        if (!email.isEmpty() && !password.isEmpty() && !username.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Send user to add journal activity
                        currentUser = firebaseAuth.getCurrentUser();
                        assert currentUser != null;
                        final String currentUserID = currentUser.getUid();
                        //Create a user map to add to the user collection
                        Map<String, String> userObject = new HashMap<>();
                        userObject.put("userID", currentUserID);
                        userObject.put("username", username);
                        //Save to firestore database

                        collectionReference.add(userObject).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (Objects.requireNonNull(task.getResult()).exists()) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            String name = task.getResult().getString("username");
                                            String userID = task.getResult().getString("userID");

                                            JournalAPI journalAPI = JournalAPI.getJournalInstance();
                                            journalAPI.setUserID(userID);
                                            journalAPI.setUsername(name);

                                            Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                            intent.putExtra("username", name);
                                            intent.putExtra("userID", userID);
                                            startActivity(intent);

                                        } else {

                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: " + e.getMessage());
                            }
                        });

                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(CreateAccountActivity.this, "Failed to create user, something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "invalid", Toast.LENGTH_SHORT).show();
        }
    }
}
