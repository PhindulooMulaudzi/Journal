package com.phindulo.journal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.phindulo.journal.UI.JournalRecyclerAdapter;
import com.phindulo.journal.Util.JournalAPI;
import com.phindulo.journal.model.Journal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JournalListActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private List<Journal> journalList;
    private RecyclerView recyclerView;
    private JournalRecyclerAdapter journalRecyclerAdapter;

    //Connection to Firestore
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = database.collection("Journal");

    private TextView noJournalEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        noJournalEntry = findViewById(R.id.list_no_thoughts);
        recyclerView = findViewById(R.id.recylcer_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        journalList = new ArrayList<>();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                if (user != null && firebaseAuth != null) {
                    startActivity(new Intent(JournalListActivity.this, PostJournalActivity.class));
                }
                break;

            case R.id.action_signout:
                if (user != null && firebaseAuth != null) {
                    firebaseAuth.signOut();
                    startActivity(new Intent(JournalListActivity.this, MainActivity.class));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        collectionReference.whereEqualTo("userID", JournalAPI.getJournalInstance().
                getUserID()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot journals : queryDocumentSnapshots) {
                        Journal journal = journals.toObject(Journal.class);
                        journalList.add(journal);
                    }
                    //Invoke recycler view
                    journalRecyclerAdapter = new JournalRecyclerAdapter(JournalListActivity.this, journalList);
                    recyclerView.setAdapter(journalRecyclerAdapter);
                    journalRecyclerAdapter.notifyDataSetChanged();
                } else {
                    noJournalEntry.setVisibility(View.VISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.d("Trivia", "onFailure: ");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
