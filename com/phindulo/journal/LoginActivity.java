package com.phindulo.journal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.phindulo.journal.Util.JournalAPI;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button createAccountButton;
    private Button loginButton;
    private AutoCompleteTextView email;
    private EditText password;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //Connection to Firestore
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = database.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        progressBar = findViewById(R.id.login_progressbar);

        firebaseAuth = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.log_in_button);
        createAccountButton = findViewById(R.id.create_account_button_login);

        createAccountButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_account_button_login:
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
                break;
            case R.id.log_in_button:
                loginUserEmailPassword(email.getText().toString(), password.getText().toString());
                break;
        }
    }

    private void loginUserEmailPassword(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        if (!email.isEmpty() && !password.isEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBar.setVisibility(View.INVISIBLE);

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    assert user != null;
                    String currentUserID = user.getUid();

                    collectionReference.whereEqualTo("userID", currentUserID).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e== null && !queryDocumentSnapshots.isEmpty()) {
                                JournalAPI journalAPI = JournalAPI.getJournalInstance();
                                for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                                    journalAPI.setUsername(queryDocumentSnapshot.getString("username"));
                                    journalAPI.setUserID(queryDocumentSnapshot.getString("userID"));
                                }
                                startActivity(new Intent(LoginActivity.this, PostJournalActivity.class));
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, "Please enter a valid email and password", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Please enter a valid email and password", Toast.LENGTH_SHORT).show();
        }
    }
}
