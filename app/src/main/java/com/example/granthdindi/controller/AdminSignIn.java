package com.example.granthdindi.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.granthdindi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminSignIn extends AppCompatActivity {
    EditText editTextFirstName, editTextLastName, editTextPhone, editTextPassword;
    String password, firstName, lastName, name;
    Long phone;
    ProgressDialog progress;
    SharedPreferences sharedPreferences;
    FirebaseFirestore db;

    public static final String fileName = "adminLogin";
    public static final String adminPhone = "phoneNo";
    public static final String adminName = "name";
    public static final String adminPassword = "password";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sign_in);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhone = findViewById(R.id.editTextPhone);

        sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.buttonSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress = new ProgressDialog(AdminSignIn.this);
                progress.setTitle( "Verifying Admin data");
                progress.setMessage("Please wait");
                progress.setCancelable(false);
                progress.show();

                if(editTextFirstName.getText().toString().length()==0){
                    editTextFirstName.setError("Enter first name");
                    editTextFirstName.requestFocus();
                }
                if(editTextLastName.getText().toString().length()==0) {
                    editTextLastName.setError("Enter last name");
                    editTextLastName.requestFocus();
                }

                adminLogin();
            }
        });
    }


    private void adminLogin(){
        firstName = editTextFirstName.getText().toString().trim();
        lastName = editTextLastName.getText().toString().trim();
        name = firstName + lastName;
        phone = Long.parseLong(editTextPhone.getText().toString().trim());
        password = editTextPassword.getText().toString();
        verifyAdmin(String.valueOf(phone));
    }

    public void verifyAdmin(final String phone){

        DocumentReference docRef = db.collection("Admins").document(phone);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document!=null && document.exists()) {
                        if(password.equals("jvm888***")){
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(adminName, name);
                            editor.putString(adminPhone, phone);
                            editor.putString(adminPassword, password);
                            editor.apply();
                            if (progress != null && progress.isShowing()) {
                                progress.dismiss();
                            }
                            Intent intent = new Intent(AdminSignIn.this, AdminHomePage.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "SignIn Successful", Toast.LENGTH_LONG).show();
                        } else {
                            if (progress != null && progress.isShowing()) {
                                progress.dismiss();
                            }
                            Toast.makeText(getApplicationContext(), "Incorrect password. SignIn Failed.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        if (progress != null && progress.isShowing()) {
                            progress.dismiss();
                        }
                        Toast.makeText(getApplicationContext(), "You are not an admin. SignIn Failed.", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }
                    Toast.makeText(getApplicationContext(), "SignIn Failed. Check your internet connection and try again.", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getApplicationContext(), "SignIn Failed. Check your internet connection and try again.", Toast.LENGTH_LONG).show();
            }
        });

    }
}
