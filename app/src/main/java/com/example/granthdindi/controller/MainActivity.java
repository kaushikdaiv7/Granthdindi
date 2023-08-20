package com.example.granthdindi.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Seller;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    EditText editTextPhone, editTextOtp, editTextFirstName, editTextLastName, editTextLocation;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String otp;
    SharedPreferences sharedPreferences;
    public static final String fileName = "adminLogin";
    public static final String adminPhone = "phoneNo";
    public static ArrayList<String> books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        editTextOtp = findViewById(R.id.editTextOtp);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextLocation = findViewById(R.id.editTextLocation);
        db = FirebaseFirestore.getInstance();
        books = new ArrayList<String>();

        checkSignInUser();
        books = getBooks();

        findViewById(R.id.buttonGetOTP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextOtp.requestFocus();
                sendOTP();
            }
        });

        findViewById(R.id.buttonSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyOTP();
            }
        });

        findViewById(R.id.textViewAdminSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminSignIn.class);
                startActivity(intent);
            }
        });

    }

    private ArrayList<String> getBooks() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference booksRef = db.collection("Books");

        booksRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        books.add(document.getString("englishName"));
                        books.add(document.getString("marathiName"));
                    }
                }
            }
        });
        return books;
    }

    private void checkSignInUser() {

        if(FirebaseAuth.getInstance().getCurrentUser()!= null){
            Intent intent = new Intent(MainActivity.this, SellerHomePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);
        if(sharedPreferences.contains(adminPhone)){
            Intent intent = new Intent(MainActivity.this, AdminHomePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    private void verifyOTP(){

        String type_otp = editTextOtp.getText().toString();
        PhoneAuthCredential credential;
        try{
            credential = PhoneAuthProvider.getCredential(otp, type_otp);
        }catch (IllegalArgumentException e){
            editTextOtp.setError("Enter valid OTP");
            editTextOtp.requestFocus();
            return;
        }
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            addSeller();
                            Intent intent = new Intent(MainActivity.this, SellerHomePage.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(getApplicationContext(), "Invalid OTP", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void addSeller() {

        final Long phone = Long.parseLong(editTextPhone.getText().toString());
        final String name = editTextFirstName.getText().toString() +" "+ editTextLastName.getText().toString();
        final String location = editTextLocation.getText().toString();
        final int cash = 0;

        final Seller seller = new Seller(name, phone, location,  cash);

        DocumentReference docRef = db.collection("Sellers").document(String.valueOf(phone));

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if(!(document.exists())){
                        String id = String.valueOf(seller.getContact());
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("name", seller.getName());
                        doc.put("contact", seller.getContact());
                        doc.put("location", seller.getLocation());
                        doc.put("cash", seller.getCash());
                        db.collection("Sellers").document(id).set(doc)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } else {
                    //Toast.makeText(getApplicationContext(), task.getException(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendOTP(){

        String phoneNumber = editTextPhone.getText().toString();

        if(editTextFirstName.getText().toString().length()==0){
            editTextFirstName.setError("Enter first name");
            editTextFirstName.requestFocus();
            return;
        }
        if(editTextLastName.getText().toString().length()==0){
            editTextLastName.setError("Enter last name");
            editTextLastName.requestFocus();
            return;
        }
        if(editTextLocation.getText().toString().length()==0){
            editTextLocation.setError("Enter location");
            editTextLocation.requestFocus();
            return;
        }
        if(phoneNumber.isEmpty()){
            editTextPhone.setError("Enter Phone no.");
            editTextPhone.requestFocus();
            return;
        }
        if(phoneNumber.length() != 10){
            editTextPhone.setError("Enter a 10 digit phone no.");
            editTextPhone.requestFocus();
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                TaskExecutors.MAIN_THREAD,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
//            addSeller();
//            Intent intent = new Intent(SellerSignIn.this, SellerHomePage.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Toast.makeText(getApplicationContext(), "OTP Sent", Toast.LENGTH_LONG).show();
            otp = s;
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            Toast.makeText(getApplicationContext(), "OTP cannot be sent..Please try again!", Toast.LENGTH_LONG).show();
        }
    };
}
