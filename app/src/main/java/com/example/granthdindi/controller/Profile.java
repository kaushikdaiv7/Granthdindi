package com.example.granthdindi.controller;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Book;
import com.example.granthdindi.model.Seller;
import com.example.granthdindi.view.FirestoreBooksSoldAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class Profile extends AppCompatActivity {

    TextView textViewSellerName, textViewSellerPhone, textViewSellerLocation, textViewSellerCash;
    String phone;
    FirebaseFirestore firebaseFirestore;
    RecyclerView recyclerViewSellers;
    FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseFirestore = FirebaseFirestore.getInstance();

        recyclerViewSellers = findViewById(R.id.recycler_view_stock_sold);

        recyclerViewSellers.addItemDecoration(new DividerItemDecoration(recyclerViewSellers.getContext(), DividerItemDecoration.VERTICAL));


        textViewSellerName = findViewById(R.id.tv_seller_name);
        textViewSellerPhone = findViewById(R.id.tv_seller_phone);
        textViewSellerLocation = findViewById(R.id.tv_seller_loc);
        textViewSellerCash = findViewById(R.id.tv_seller_cash);

        phone = SellerHomePage.userContact;

        getUserData(phone);

        Query query = firebaseFirestore.collection("Sellers").document(phone).collection("StockSold").orderBy("price");

        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        adapter = new FirestoreBooksSoldAdapter(options, getApplicationContext());
        recyclerViewSellers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSellers.setAdapter(adapter);

    }

    private void getUserData(String phone) {

        firebaseFirestore.collection("Sellers").document(phone).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if(snapshot!=null){
                    textViewSellerName.setText(snapshot.getString("name"));
                    textViewSellerPhone.setText(String.valueOf(snapshot.getLong("contact")));
                    textViewSellerLocation.setText("Location: "+snapshot.getString("location"));
                    textViewSellerCash.setText("Cash in hand: ₹ "+String.valueOf(snapshot.getLong("cash")));
                } else {
                    Toast.makeText(Profile.this, "Failed to get profile data. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Profile.this, "Failed to get profile data. Try again.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
