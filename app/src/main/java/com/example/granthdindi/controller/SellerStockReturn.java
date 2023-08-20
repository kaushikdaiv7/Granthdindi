package com.example.granthdindi.controller;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Book;
import com.example.granthdindi.view.FirestoreBooksAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SellerStockReturn extends AppCompatActivity {

    TextView textViewSellerName, textViewSellerPhone, textViewSellerLocation, textViewSellerCash;
    String name, phone, location, cash;
    int invStock;
    FirebaseFirestore firebaseFirestore;
    BottomNavigationView bottomNavigationView;
    RecyclerView recyclerViewBookReturn;
    FirestoreRecyclerAdapter adapterReturn;
    FloatingActionButton floatingActionButton;
    CollectionReference booksReturnRef, sellerRef;
    Toolbar toolbar;
    ProgressDialog progress;
    HashMap<String , Long> booksReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_stock_return);

        booksReturn = new HashMap<>();

        firebaseFirestore = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigation_stock);

        floatingActionButton = findViewById(R.id.btn_verified_return);

        bottomNavigationView.setSelectedItemId(R.id.stock_return);

        recyclerViewBookReturn = findViewById(R.id.recycler_view_stock_return);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerViewBookReturn.addItemDecoration(new DividerItemDecoration(recyclerViewBookReturn.getContext(), DividerItemDecoration.VERTICAL));

        progress = new ProgressDialog(SellerStockReturn.this);
        progress.setTitle("Updating Inventory..");
        progress.setMessage("Please wait..");

        textViewSellerName = findViewById(R.id.tv_seller_name);
        textViewSellerPhone = findViewById(R.id.tv_seller_phone);
        textViewSellerLocation = findViewById(R.id.tv_seller_loc);
        textViewSellerCash = findViewById(R.id.tv_seller_cash);

        name = getIntent().getStringExtra("sellerName");
        phone = getIntent().getStringExtra("sellerPhone");
        location = getIntent().getStringExtra("sellerLocation");
        cash = getIntent().getStringExtra("sellerCash");

        Query query = firebaseFirestore.collection("Sellers").document(phone).collection("StockReturn").orderBy("price");

        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        adapterReturn = new FirestoreBooksAdapter(options, getApplicationContext());

        recyclerViewBookReturn.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBookReturn.setAdapter(adapterReturn);

        textViewSellerName.setText(name);
        textViewSellerPhone.setText(phone);
        textViewSellerLocation.setText("Location: "+location);
        textViewSellerCash.setText("Cash in hand: ₹ "+cash);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;

                switch (item.getItemId()) {
                    case R.id.stock_alloted:
                        intent = new Intent(getApplicationContext(), SellerStockAlloted.class);
                        intent.putExtra("sellerName", name);
                        intent.putExtra("sellerPhone", phone);
                        intent.putExtra("sellerLocation", location);
                        intent.putExtra("sellerCash", cash);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.stock_return:
                        return true;

                    case R.id.stock_sold:
                        intent = new Intent(getApplicationContext(), SellerStockSold.class);
                        intent.putExtra("sellerName", name);
                        intent.putExtra("sellerPhone", phone);
                        intent.putExtra("sellerLocation", location);
                        intent.putExtra("sellerCash", cash);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }

    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SellerStockReturn.this);

        builder.setTitle("Are you sure?");

        builder.setMessage("Have you recieved all the books and cash?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progress.show();
                getReturnData();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getReturnData() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        booksReturnRef = firebaseFirestore.collection("Sellers").document(phone).collection("StockReturn");
        sellerRef = firebaseFirestore.collection("Sellers");

        booksReturnRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        booksReturn.put(document.getString("englishName"), (Long) document.get("stocks"));
                    }
                    if(booksReturn.isEmpty()){
                        updateCash();
                    }else{
                        updateInventory(booksReturn, cash);
                    }

                } else {
                    Toast.makeText(SellerStockReturn.this, "Failed to get return data..Check your internet connection..", Toast.LENGTH_SHORT).show();
                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }
                    return;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SellerStockReturn.this, "Failed to get return data..Check your internet connection..", Toast.LENGTH_SHORT).show();
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                return;
            }
        });

    }

    private void updateInventory(HashMap<String, Long> booksReturn, String cash) {

        Iterator<String> itr = booksReturn.keySet().iterator();

        for (final HashMap.Entry<String, Long> set : booksReturn.entrySet()) {

            firebaseFirestore.collection("Books").document(set.getKey()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    int stocks;
                    Book book = snapshot.toObject(Book.class);
                    invStock = book.getStocks();
                    stocks = (int) (invStock + set.getValue());

                    firebaseFirestore.collection("Books").document(set.getKey()).update("stocks", stocks).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            updateCash();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SellerStockReturn.this, "Failed to update inventory..Check your internet connection..", Toast.LENGTH_SHORT).show();
                            if (progress != null && progress.isShowing()) {
                                progress.dismiss();
                            }
                            return;
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SellerStockReturn.this, "Failed to get inventory stock..Check your internet connection..", Toast.LENGTH_SHORT).show();
                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }
                    return;
                }
            });
        }

    }

    private void updateCash() {
        firebaseFirestore.collection("Sellers").document(phone).update("cash", 0).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                deleteSeller();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(SellerStockReturn.this, "Failed to update sellers cash..Check your internet connection..", Toast.LENGTH_SHORT).show();
                return;
            }
        });
    }

    private void deleteSeller() {

        firebaseFirestore.collection("Sellers").document(phone).collection("StockAlloted").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                WriteBatch batch = FirebaseFirestore.getInstance().batch();
                List<DocumentSnapshot> documentSnapshotList;
                documentSnapshotList = queryDocumentSnapshots.getDocuments();

                for(DocumentSnapshot snapshot : documentSnapshotList){
                    batch.delete(snapshot.getReference());
                }

                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (progress != null && progress.isShowing()) {
                            progress.dismiss();
                        }
                        Toast.makeText(SellerStockReturn.this, "Failed to delete seller data..Check your internet connection..", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(SellerStockReturn.this, "Failed..Check your internet connection..", Toast.LENGTH_SHORT).show();
                return;
            }
        });

        firebaseFirestore.collection("Sellers").document(phone).collection("StockReturn").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                WriteBatch batch = FirebaseFirestore.getInstance().batch();
                List<DocumentSnapshot> documentSnapshotList;
                documentSnapshotList = queryDocumentSnapshots.getDocuments();

                for(DocumentSnapshot snapshot : documentSnapshotList){
                    batch.delete(snapshot.getReference());
                }

                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (progress != null && progress.isShowing()) {
                            progress.dismiss();
                        }
                        Toast.makeText(SellerStockReturn.this, "Failed to delete seller data..Check your internet connection..", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(SellerStockReturn.this, "Failed..Check your internet connection..", Toast.LENGTH_SHORT).show();
                return;
            }
        });

        firebaseFirestore.collection("Sellers").document(phone).collection("StockSold").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                WriteBatch batch = FirebaseFirestore.getInstance().batch();
                List<DocumentSnapshot> documentSnapshotList;
                documentSnapshotList = queryDocumentSnapshots.getDocuments();

                for(DocumentSnapshot snapshot : documentSnapshotList){
                    batch.delete(snapshot.getReference());
                }

                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (progress != null && progress.isShowing()) {
                            progress.dismiss();
                        }
                        Toast.makeText(SellerStockReturn.this, "Failed to delete seller data..Check your internet connection..", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(SellerStockReturn.this, "Failed..Check your internet connection..", Toast.LENGTH_SHORT).show();
                return;
            }
        });

        firebaseFirestore.collection("Sellers").document(phone).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(SellerStockReturn.this, "Updated Successfully..", Toast.LENGTH_SHORT).show();
                return;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(SellerStockReturn.this, "Failed to delete seller data..", Toast.LENGTH_SHORT).show();
                return;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapterReturn.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterReturn.stopListening();
    }
}
