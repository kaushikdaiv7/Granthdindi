package com.example.granthdindi.controller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Admin;
import com.example.granthdindi.view.AddAdminDialog;
import com.example.granthdindi.view.FirestoreAdminsAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class Admins extends AppCompatActivity implements AddAdminDialog.AddAdminDialogListener, FirestoreAdminsAdapter.OnAdminClickListener {

    BottomNavigationView bottomNavigationView;
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    Admin admin;
    ProgressDialog progress;
    Toolbar toolbar;
    FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference adminsCollection = db.collection("Admins");
    private FirestoreAdminsAdapter adapter;

    public static final String fileName = "adminLogin";
    public static final String adminPhone = "phoneNo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admins);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.admins);
        recyclerView = findViewById(R.id.recycler_view_admins);
        floatingActionButton = findViewById(R.id.btn_add_admin);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);
        String phone = sharedPreferences.getString(adminPhone, "");

        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        Query query = adminsCollection;

        FirestoreRecyclerOptions<Admin> options = new FirestoreRecyclerOptions.Builder<Admin>()
                .setQuery(query,Admin.class)
                .build();

        adapter = new FirestoreAdminsAdapter(options, getApplicationContext(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddAlertBox();
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;

                switch (item.getItemId()){
                    case R.id.sellers:
                        intent = new Intent(getApplicationContext(), AdminHomePage.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        return true;

                    case R.id.inventory:
                        intent = new Intent(getApplicationContext(), Inventory.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        return true;

                    case R.id.admins:
                        return true;
                }
                return false;
            }
        });
    }

    private void showAddAlertBox() {
        AddAdminDialog addAdminDialog = new AddAdminDialog();
        addAdminDialog.show(getSupportFragmentManager(), "Add Admin Dialog");
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();         //when app is running it will update the data
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();         //when app is not running the it does not update the data
    }

    public void deleteAdmin(final String id){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete this admin")
                .setCancelable(false)
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progress = new ProgressDialog(Admins.this);
                        progress.setTitle( "Deleting Book..");
                        progress.setMessage("Please wait..");
                        progress.setCancelable(false);
                        progress.show();
                        db.collection("Admins").document(String.valueOf(id)).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (progress != null && progress.isShowing()) {
                                            progress.dismiss();
                                        }
                                        Toast.makeText(Admins.this, "Admin deleted..", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        if (progress != null && progress.isShowing()) {
                                            progress.dismiss();
                                        }
                                        Toast.makeText(Admins.this, "Failed..Try again..", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        alert.setCanceledOnTouchOutside(true);
    }

    @Override
    public void addAdmin(final String name, final Long contact, final String password) {

        progress = new ProgressDialog(Admins.this);
        progress.setTitle( "Adding Admin data");
        progress.setMessage("Please wait");
        progress.setCancelable(false);
        progress.show();

        final Map<String, Object> admin = new HashMap<>();
        admin.put("name", name);
        admin.put("password", password);
        admin.put("contact", contact);
        DocumentReference docRef = db.collection("Admins").document(String.valueOf(contact));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (progress != null && progress.isShowing()) {
                            progress.dismiss();
                        }
                        Toast.makeText(Admins.this, "Admin already exists..", Toast.LENGTH_LONG).show();
                    }
                    else {
                        db.collection("Admins").document(String.valueOf(contact)).set(admin)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (progress != null && progress.isShowing()) {
                                            progress.dismiss();
                                        }
                                        Toast.makeText(Admins.this, "Admin added successfully..", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        if (progress != null && progress.isShowing()) {
                                            progress.dismiss();
                                        }
                                        Toast.makeText(Admins.this, "Failed..try again..", Toast.LENGTH_LONG).show();
                                    }

                                });
                    }
                }
                else {
                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }
                    Toast.makeText(Admins.this, "Failed..try again..", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(Admins.this, "Failed..Check your internet connection and try again..", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onOptionClick(final String id, View view) {
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(view.getContext(),view);
        popupMenu.inflate(R.menu.menu_admin_options);
        popupMenu.setOnMenuItemClickListener(new android.widget.PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.delete_item:
                        deleteAdmin(String.valueOf(id));
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_signout,menu);

        return true;
    }

    private void signOut() {
        mAuth.signOut();
        sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        finish();
        Intent intent = new Intent(Admins.this, MainActivity.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
