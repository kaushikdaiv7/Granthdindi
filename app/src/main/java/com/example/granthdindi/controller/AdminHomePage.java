package com.example.granthdindi.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Seller;
import com.example.granthdindi.view.FirestoreSellersAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class AdminHomePage extends AppCompatActivity implements FirestoreSellersAdapter.OnSellerItemClick {

    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    BottomNavigationView bottomNavigationView;
    FirebaseFirestore firebaseFirestore;
    RecyclerView recyclerViewSellers;
    Toolbar toolbar;
    FirestoreSellersAdapter adapter;
    public static final String fileName = "adminLogin";
    public static final String adminPhone = "phoneNo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home_page);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.sellers);
        mAuth = FirebaseAuth.getInstance();

        sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);

        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerViewSellers = findViewById(R.id.recycler_view_sellers);

        recyclerViewSellers.addItemDecoration(new DividerItemDecoration(recyclerViewSellers.getContext(), DividerItemDecoration.VERTICAL));

        Query query = firebaseFirestore.collection("Sellers");

        FirestoreRecyclerOptions<Seller> options = new FirestoreRecyclerOptions.Builder<Seller>()
                .setQuery(query, Seller.class)
                .build();


        adapter = new FirestoreSellersAdapter(options, this);
        recyclerViewSellers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSellers.setAdapter(adapter);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;

                switch (item.getItemId()){
                    case R.id.sellers:
                        return true;

                    case R.id.inventory:
                        intent = new Intent(getApplicationContext(), Inventory.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        return true;

                    case R.id.admins:
                        intent = new Intent(getApplicationContext(), Admins.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        return true;
                }
                return false;
            }
        });

    }


    private void signOut() {
        mAuth.signOut();
        sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        finish();
        Intent intent = new Intent(AdminHomePage.this, MainActivity.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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

    @Override
    public void onSellerClick(Seller seller) {
        Intent intent = new Intent(AdminHomePage.this, SellerStockAlloted.class);
        intent.putExtra("sellerName", seller.getName());
        intent.putExtra("sellerPhone", String.valueOf(seller.getContact()));
        intent.putExtra("sellerLocation", seller.getLocation());
        intent.putExtra("sellerCash", String.valueOf(seller.getCash()));
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
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
}
