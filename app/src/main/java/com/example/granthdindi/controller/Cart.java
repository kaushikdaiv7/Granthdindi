package com.example.granthdindi.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Book;
import com.example.granthdindi.view.CartAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;

public class Cart extends AppCompatActivity {

    public ArrayList<Book> cart_list = SellerHomePage.helper.getList();

    SellerHomePage sellerHomePage;
    RecyclerView recyclerView_cart;
    Toolbar toolbar;
    RecyclerView.Adapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button btn_sell;
    static int total;
    public static TextView tv_total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView_cart = findViewById(R.id.recycler_cart);
        btn_sell = findViewById(R.id.btn_sell);
        tv_total = findViewById(R.id.tv_total);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView_cart.addItemDecoration(new DividerItemDecoration(recyclerView_cart.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new CartAdapter(cart_list,this);
        recyclerView_cart.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_cart.setAdapter(adapter);
        onValueChanged();

        btn_sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeTransaction();
            }
        });
    }

    private void executeTransaction() {
        final String phn = SellerHomePage.userContact;

        final CollectionReference reff_sold = db.collection("Sellers")
                .document(String.valueOf(phn)).collection("StockSold");
        final CollectionReference reff_return = db.collection("Sellers")
                .document(String.valueOf(phn)).collection("StockReturn");

        ArrayList<Book>list = SellerHomePage.helper.getList();

        for(final Book book:list){
            final DocumentReference docReff_sold = reff_sold.document(book.getEnglishName());
            final DocumentReference docReff_return = reff_return.document(book.getEnglishName());

            db.runTransaction(new Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot documentSnapshot_return = transaction.get(docReff_return);
                    int stock = (int) ((documentSnapshot_return.getLong("stocks")) - book.getQuantity());
                    if(transaction.get(docReff_sold).exists()){
                        DocumentSnapshot documentSnapshot_sold = transaction.get(docReff_sold);
                        int quantity = (int) (documentSnapshot_sold.getLong("quantity")+book.getQuantity());
                        transaction.update(docReff_sold,"quantity",quantity);
                    }
                    else {
                        transaction.set(docReff_sold,book);
                    }
                    transaction.update(docReff_return,"stocks",stock);
                    return null;
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if(book.getStocks()-book.getQuantity() == 0){
                        docReff_return.delete();
                    }
                    db.collection("Sellers").document(String.valueOf(phn))
                            .update("cash", FieldValue.increment(book.getPrice()*book.getQuantity()));
                    Toast.makeText(getApplicationContext(), "Sold", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Cart.this,"error please try again",Toast.LENGTH_LONG).show();
                }
            });


        }

        cart_list.clear();
        sellerHomePage.helper.deleteAll();
        adapter.notifyDataSetChanged();
        onValueChanged();
    }

    public static void onValueChanged(){
        total = 0;
        ArrayList<Book>list = SellerHomePage.helper.getList();
        for(Book book: list){
            total += book.getPrice()*book.getQuantity();
        }
        tv_total.setText(String.valueOf(total));
    }

}
