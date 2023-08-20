package com.example.granthdindi.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Book;
import com.example.granthdindi.view.FirestoreBooksAllocatedAdapter;
import com.example.granthdindi.view.UpdateDialogBookAllocated;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class SellerStockAlloted extends AppCompatActivity implements FirestoreBooksAllocatedAdapter.OnBookItemClick, PopupMenu.OnMenuItemClickListener, UpdateDialogBookAllocated.UpdateDialogBookAllocatedListener {

    TextView textViewSellerName, textViewSellerPhone, textViewSellerLocation, textViewSellerCash;
    String name, phone, location, cash;
    FirebaseFirestore firebaseFirestore;
    BottomNavigationView bottomNavigationView;
    RecyclerView recyclerViewBooksAlloted;
    FirestoreRecyclerAdapter adapter;
    FloatingActionButton floatingActionButton;
    Toolbar toolbar;
    Book book;
    int bookReturnQuantity;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_stock_alloted);

        firebaseFirestore = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigation_stock);

        bottomNavigationView.setSelectedItemId(R.id.stock_alloted);

        recyclerViewBooksAlloted = findViewById(R.id.recycler_view_stock_alloted);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerViewBooksAlloted.addItemDecoration(new DividerItemDecoration(recyclerViewBooksAlloted.getContext(), DividerItemDecoration.VERTICAL));

        floatingActionButton = findViewById(R.id.btn_add_stock);

        textViewSellerName = findViewById(R.id.tv_seller_name);
        textViewSellerPhone = findViewById(R.id.tv_seller_phone);
        textViewSellerLocation = findViewById(R.id.tv_seller_loc);
        textViewSellerCash = findViewById(R.id.tv_seller_cash);

        name = getIntent().getStringExtra("sellerName");
        phone = getIntent().getStringExtra("sellerPhone");
        location = getIntent().getStringExtra("sellerLocation");
        cash = getIntent().getStringExtra("sellerCash");

        textViewSellerName.setText(name);
        textViewSellerPhone.setText(phone);
        textViewSellerLocation.setText("Location: "+location);
        textViewSellerCash.setText("Cash in hand: ₹ "+cash);

        Query query = firebaseFirestore.collection("Sellers").document(phone).collection("StockAlloted").orderBy("price");

        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        adapter = new FirestoreBooksAllocatedAdapter(options, getApplicationContext(), this);

        recyclerViewBooksAlloted.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBooksAlloted.setAdapter(adapter);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddNewStock.class);
                intent.putExtra("sellerName", name);
                intent.putExtra("sellerPhone", phone);
                intent.putExtra("sellerLocation", location);
                intent.putExtra("sellerCash", cash);
                startActivity(intent);
            }
        });


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;

                switch (item.getItemId()){
                    case R.id.stock_alloted:
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
                        intent = new Intent(getApplicationContext(), SellerStockSold.class);
                        intent.putExtra("sellerName", name);
                        intent.putExtra("sellerPhone", phone);
                        intent.putExtra("sellerLocation", location);
                        intent.putExtra("sellerCash", cash);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0,0);
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

    @Override
    public void onBookClick(Book book, View view) {
        this.book = book;
        showPopupMenu(view);
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.menu_stock_alloted_options);
        popupMenu.setGravity(Gravity.END);
        popupMenu.setGravity(Gravity.RIGHT);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int flag;
        switch (item.getItemId()) {
            case R.id.edit_item:
                flag = 1;
                showAlertBox(flag);
                //fetchBookData(book.getEnglishName(), , flag);
                return true;

            case R.id.delete_item:
                flag = 0;
                showAlertBox(flag);
                return true;

            default:
                return false;
        }

    }

    private void showAlertBox(final int flag) {
        if(flag == 1) {
            UpdateDialogBookAllocated updateDialogBookAllocated = new UpdateDialogBookAllocated(book);
            updateDialogBookAllocated.show(getSupportFragmentManager(), "update dialogbook allocated");
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(SellerStockAlloted.this);

            builder.setTitle("Deleting "+book.getEnglishName());

            builder.setMessage("Are you sure?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fetchBookData(book.getEnglishName(), 0, 0, flag);
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialogDelete = builder.create();

            dialogDelete.show();
        }
    }


    private void fetchBookReturn(final int bookStock, final int quantity){
        Query query1 = firebaseFirestore.collection("Sellers").document(phone).collection("StockReturn").whereEqualTo("englishName", book.getEnglishName());
        query1.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> books = queryDocumentSnapshots.getDocuments();
                if(books.size()>0){
                    final Book bookReturn1 = books.get(0).toObject(Book.class);
                    if(bookReturn1!=null){
                        bookReturnQuantity = bookReturn1.getStocks();
                        fetchBookData(book.getEnglishName(), bookStock, quantity, 1);

                    } else{
                        Toast.makeText(getApplicationContext(), "Failed to fetch book from book return inventory..Try again 1!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to fetch book from book return inventory..Try again 2!", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed..Check your internet connection and try again!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchBookData(String bookName, final int bookStock, final int quantity, final int flag) {
        String str = bookName.trim().replaceAll(" ", "");
        Query query = firebaseFirestore.collection("Books").whereEqualTo("englishName", book.getEnglishName());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> books = queryDocumentSnapshots.getDocuments();
                if(books.size()>0){
                    final Book bookInv = books.get(0).toObject(Book.class);
                    if(bookInv!=null){
                        int invStock = bookInv.getStocks();
                        if(invStock >= bookStock){

                            if(flag == 1){
                                progress = new ProgressDialog(SellerStockAlloted.this);
                                progress.setTitle( "Updating Book..");
                                progress.setMessage("Please wait..");
                                progress.setCancelable(false);
                                progress.show();
                                editBook(invStock, bookStock, quantity);
                            }else{
                                progress = new ProgressDialog(SellerStockAlloted.this);
                                progress.setTitle( "Deleting Book..");
                                progress.setMessage("Please wait..");
                                progress.setCancelable(false);
                                progress.show();
                                fetchReturnBook(book.getEnglishName(), invStock);
                            }
                        } else {
                            if (progress != null && progress.isShowing()) {
                                progress.dismiss();
                            }
                            SellerStockAlloted.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SellerStockAlloted.this,
                                            "Stock not available in inventory", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    } else {
                        if (progress != null && progress.isShowing()) {
                            progress.dismiss();
                        }
                        Toast.makeText(getApplicationContext(), "Failed to fetch book from inventory..Try again!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }
                    Toast.makeText(getApplicationContext(), "Failed to fetch book from inventory..Try again!", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Failed..Check your internet connection and try again!", Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void fetchReturnBook(String englishName, final int invStock) {
        Query query = firebaseFirestore.collection("Sellers").document(phone)
                .collection("StockReturn").whereEqualTo("englishName", englishName);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> books = queryDocumentSnapshots.getDocuments();
                if(books.size()>0){
                    final Book bookReturn = books.get(0).toObject(Book.class);
                    if(bookReturn!=null){
                        int returnStock = bookReturn.getStocks();
                        deleteBook(returnStock, invStock);
                    } else {
                        if (progress != null && progress.isShowing()) {
                            progress.dismiss();
                        }
                        Toast.makeText(getApplicationContext(), "Failed to fetch book from inventory..Try again!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }
                    Toast.makeText(getApplicationContext(), "Failed to fetch book from inventory..Try again!", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Failed..Check your internet connection and try again!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void editBook(int invStock, int bookStock, int quantity) {

        int finalStock = invStock - bookStock;

        int finalBookReturn = bookReturnQuantity + (quantity - book.getStocks());

        WriteBatch batch = firebaseFirestore.batch();

        DocumentReference bookAlloted = firebaseFirestore.collection("Sellers").document(phone)
                .collection("StockAlloted").document(book.getEnglishName());

        DocumentReference bookReturn = firebaseFirestore.collection("Sellers").document(phone)
                .collection("StockReturn").document(book.getEnglishName());

        DocumentReference bookInv = firebaseFirestore.collection("Books").document(book.getEnglishName());

        batch.update(bookAlloted,"stocks", quantity);
        batch.update(bookReturn,"stocks", finalBookReturn);
        batch.update(bookInv,"stocks", finalStock);

        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Book quantity updated successfully..", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Failed..Check internet connection and try again..", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void deleteBook(int returnStock, int invStock) {

        int finalStock = invStock + returnStock;

        WriteBatch batch = firebaseFirestore.batch();

        DocumentReference bookAlloted = firebaseFirestore.collection("Sellers").document(phone)
                                                            .collection("StockAlloted").document(book.getEnglishName());

        DocumentReference bookReturn = firebaseFirestore.collection("Sellers").document(phone)
                                                        .collection("StockReturn").document(book.getEnglishName());

        DocumentReference bookInv = firebaseFirestore.collection("Books").document(book.getEnglishName());

        batch.delete(bookAlloted);
        batch.delete(bookReturn);
        batch.update(bookInv,"stocks", finalStock);

        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Book deleted successfully..", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Failed..Check internet connection and try again..", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void update(int quantity) {
        int bookStock = quantity - book.getStocks();
        fetchBookReturn(bookStock, quantity);
    }
}
