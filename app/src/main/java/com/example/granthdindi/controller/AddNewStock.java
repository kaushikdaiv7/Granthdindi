package com.example.granthdindi.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Book;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNewStock extends AppCompatActivity {

    AutoCompleteTextView autoCompleteTextViewBook;
    ProgressDialog progress;
    NumberPicker numberPickerQuantity;
    Button buttonAdd, buttonCancel;
    FirebaseFirestore db;
    Toolbar toolbar;
    String name, phone, location, cash, bookName;
    CollectionReference booksRef;
    int bookStock;

    ArrayList<String> books = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_stock);

        autoCompleteTextViewBook = findViewById(R.id.atv_book_name);
        numberPickerQuantity = findViewById(R.id.np_quantity);
        buttonAdd = findViewById(R.id.btn_add);
        buttonCancel = findViewById(R.id.btn_cancel);
        db = FirebaseFirestore.getInstance();

        books = MainActivity.books;

        booksRef = db.collection("Books");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = new ProgressDialog(AddNewStock.this);
        progress.setTitle("Adding Book..");
        progress.setMessage("Please wait..");
        progress.setCancelable(false);

        numberPickerQuantity.setMinValue(1);
        numberPickerQuantity.setMaxValue(30);

        name = getIntent().getStringExtra("sellerName");
        phone = getIntent().getStringExtra("sellerPhone");
        location = getIntent().getStringExtra("sellerLocation");
        cash = getIntent().getStringExtra("sellerCash");

        autoCompleteTextViewBook.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, books));

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookName = autoCompleteTextViewBook.getText().toString().trim();
                bookStock = numberPickerQuantity.getValue();
                autoCompleteTextViewBook.setText("");
                numberPickerQuantity.setValue(1);
                if(books.contains(bookName)){
                    fetchBookData(bookName, bookStock);
                } else{
                    Toast.makeText(AddNewStock.this, "Enter a valid book name..", Toast.LENGTH_LONG).show();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SellerStockAlloted.class);
                intent.putExtra("sellerName", name);
                intent.putExtra("sellerPhone", phone);
                intent.putExtra("sellerLocation", location);
                intent.putExtra("sellerCash", cash);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

    }

    private void fetchBookData(String bookName, final int bookStock) {
        String str = bookName.trim().replaceAll(" ", "");
        Query query;

        if((!str.equals(""))
                && (str.matches("^[a-zA-Z]*$"))){
            query = booksRef.whereEqualTo("englishName", bookName);

        } else {
            query = booksRef.whereEqualTo("marathiName", bookName);
        }

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> books = queryDocumentSnapshots.getDocuments();
                if(books.size()>0){
                    final Book book = books.get(0).toObject(Book.class);
                    if(book!=null){
                        int invStock = book.getStocks();
                        if(invStock >= bookStock){
                            progress.show();
                            addSellerStock(book);
                        } else {
                            if (progress != null && progress.isShowing()) {
                                progress.dismiss();
                            }
                            AddNewStock.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddNewStock.this,
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
                    Toast.makeText(AddNewStock.this, "Failed to fetch book from inventory..Try again!", Toast.LENGTH_LONG).show();
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


    private void addSellerStock(final Book book) {

        final DocumentReference allocatedBookRef = db.collection("Sellers").document(phone)
                .collection("StockAlloted").document(book.getEnglishName());

        final DocumentReference returnBookRef = db.collection("Sellers").document(phone)
                .collection("StockReturn").document(book.getEnglishName());

        allocatedBookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if(!(document.exists())){
                        final Map<String, Object> doc = new HashMap<>();
                        doc.put("englishName", book.getEnglishName());
                        doc.put("marathiName", book.getMarathiName());
                        doc.put("id", book.getId());
                        doc.put("imgUrl", book.getImgUrl());
                        doc.put("stocks", bookStock);
                        doc.put("price", book.getPrice());
                        allocatedBookRef.set(doc)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        returnBookRef.set(doc)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        //Toast.makeText(getApplicationContext(), "Added in stock return..", Toast.LENGTH_LONG).show();
                                                        updateInventory(book, 0, 0);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                allocatedBookRef.delete();
                                                if (progress != null && progress.isShowing()) {
                                                    progress.dismiss();
                                                }
                                                Toast.makeText(getApplicationContext(), "Failed to Add in stock return..Try again!", Toast.LENGTH_LONG).show();
                                                //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (progress != null && progress.isShowing()) {
                                    progress.dismiss();
                                }
                                Toast.makeText(getApplicationContext(), "Failed to add in stock alloted..Try again!", Toast.LENGTH_LONG).show();
                                //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        updateSellerStock(book, document);
                    }
                } else {
                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }
                    Toast.makeText(getApplicationContext(), "Failed..Check your internet connection and try again!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateSellerStock(final Book book, DocumentSnapshot document) {

        final DocumentReference allocatedBookRef = db.collection("Sellers").document(phone)
                .collection("StockAlloted").document(book.getEnglishName());

        final DocumentReference returnBookRef = db.collection("Sellers").document(phone)
                .collection("StockReturn").document(book.getEnglishName());

        Book bookAlloc = document.toObject(Book.class);

        final int prevStock = bookAlloc.getStocks();

        final int stock = bookAlloc.getStocks() + bookStock;

        allocatedBookRef.update("stocks", stock).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(getApplicationContext(), "Updated in stock alloted", Toast.LENGTH_LONG).show();
                returnBookRef.update("stocks", stock).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Toast.makeText(getApplicationContext(), "Updated in stock return", Toast.LENGTH_LONG).show();
                        updateInventory(book, 1, prevStock);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        allocatedBookRef.update("stocks", prevStock);
                        if (progress != null && progress.isShowing()) {
                            progress.dismiss();
                        }
                        Toast.makeText(getApplicationContext(), "Failed to update in stock return..Try again!", Toast.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });
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

    private void updateInventory(final Book book, final int flag, final int prevStock) {

        final DocumentReference allocatedBookRef = db.collection("Sellers").document(phone)
                .collection("StockAlloted").document(book.getEnglishName());

        final DocumentReference returnBookRef = db.collection("Sellers").document(phone)
                .collection("StockReturn").document(book.getEnglishName());

        int stock = book.getStocks() - bookStock;
        booksRef.document(book.getEnglishName()).update("stocks", stock).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Book Added..", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(flag == 0){
                    allocatedBookRef.delete();
                    returnBookRef.delete();
                }else {
                    allocatedBookRef.update("stocks", prevStock);
                    returnBookRef.update("stocks", prevStock);
                }
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Failed..Check your internet connection and try again!", Toast.LENGTH_LONG).show();
            }
        });
    }

}
