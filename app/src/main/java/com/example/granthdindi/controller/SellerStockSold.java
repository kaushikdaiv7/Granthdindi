package com.example.granthdindi.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Book;
import com.example.granthdindi.view.FirestoreBooksSoldAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class SellerStockSold extends AppCompatActivity {

    TextView textViewSellerName, textViewSellerPhone, textViewSellerLocation, textViewSellerCash;
    String name, phone, location, cash;
    FirebaseFirestore firebaseFirestore;
    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;
    RecyclerView recyclerViewSellers;
    FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_stock_sold);

        firebaseFirestore = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigation_stock);

        bottomNavigationView.setSelectedItemId(R.id.stock_sold);

        recyclerViewSellers = findViewById(R.id.recycler_view_stock_sold);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerViewSellers.addItemDecoration(new DividerItemDecoration(recyclerViewSellers.getContext(), DividerItemDecoration.VERTICAL));


        textViewSellerName = findViewById(R.id.tv_seller_name);
        textViewSellerPhone = findViewById(R.id.tv_seller_phone);
        textViewSellerLocation = findViewById(R.id.tv_seller_loc);
        textViewSellerCash = findViewById(R.id.tv_seller_cash);

        name = getIntent().getStringExtra("sellerName");
        phone = getIntent().getStringExtra("sellerPhone");
        location = getIntent().getStringExtra("sellerLocation");
        cash = getIntent().getStringExtra("sellerCash");

        Query query = firebaseFirestore.collection("Sellers").document(phone).collection("StockSold").orderBy("price");

        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        adapter = new FirestoreBooksSoldAdapter(options, getApplicationContext());
        recyclerViewSellers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSellers.setAdapter(adapter);

        textViewSellerName.setText(name);
        textViewSellerPhone.setText(phone);
        textViewSellerLocation.setText("Location: "+location);
        textViewSellerCash.setText("Cash in hand: ₹ "+cash);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;

                switch (item.getItemId()){
                    case R.id.stock_alloted:
                        intent = new Intent(getApplicationContext(), SellerStockAlloted.class);
                        intent.putExtra("sellerName", name);
                        intent.putExtra("sellerPhone", phone);
                        intent.putExtra("sellerLocation", location);
                        intent.putExtra("sellerCash", cash);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.stock_return:
                        intent = new Intent(getApplicationContext(), SellerStockReturn.class);
                        intent.putExtra("sellerName", name);
                        intent.putExtra("sellerPhone", phone);
                        intent.putExtra("sellerLocation", location);
                        intent.putExtra("sellerCash", cash);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.stock_sold:
                        return true;
                }
                return false;
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
