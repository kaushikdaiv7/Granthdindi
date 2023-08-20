package com.example.granthdindi.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Book;
import com.example.granthdindi.model.DatabaseHelper;
import com.example.granthdindi.view.FirestoreBooksAllocatedAdapter;
import com.example.granthdindi.view.SearchRecyclerViewAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SellerHomePage extends AppCompatActivity implements FirestoreBooksAllocatedAdapter.OnBookItemClick, SearchRecyclerViewAdapter.OnSearchBookItemClick {

    public static DatabaseHelper helper;
    FirebaseAuth mAuth;
    Toolbar toolbar;
    FirebaseFirestore firebaseFirestore;
    RecyclerView recyclerViewBooks;
    SwipeRefreshLayout swipeRefreshLayout;
    AutoCompleteTextView autoCompleteTextView;
    ImageView imageViewSearch;
    SearchRecyclerViewAdapter searchRecyclerViewAdapter;
    private FirestoreBooksAllocatedAdapter adapter;
    ArrayList<String> invBooks = new ArrayList<String>();
    public static String userContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_home_page);
        helper = new DatabaseHelper(this);
        mAuth = FirebaseAuth.getInstance();

        userContact = getUser();

        if(userContact.equals("")){
            Toast.makeText(this, "Failed to get current user. Sign out and try signing in again.", Toast.LENGTH_LONG).show();
        } else{
            userContact = userContact.substring(3);
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_seller);
        autoCompleteTextView = findViewById(R.id.atv_book_name_seller);
        imageViewSearch = findViewById(R.id.iv_search_seller);

        invBooks = MainActivity.books;
        autoCompleteTextView.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, invBooks));

        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerViewBooks = findViewById(R.id.recycler_view_seller_books);
        recyclerViewBooks.addItemDecoration(new DividerItemDecoration(recyclerViewBooks.getContext(), DividerItemDecoration.VERTICAL));

        Query query = firebaseFirestore.collection("Sellers").document(userContact).collection("StockReturn").orderBy("price");
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        adapter = new FirestoreBooksAllocatedAdapter(options, getApplicationContext(), this);
        recyclerViewBooks.setAdapter(adapter);
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));

        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bookName = autoCompleteTextView.getText().toString().trim();
                autoCompleteTextView.setText("");
                if(invBooks.contains(bookName)){
                    InputMethodManager imm =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    fetchBook(bookName);
                } else{
                    Toast.makeText(SellerHomePage.this, "Enter a valid book name..", Toast.LENGTH_LONG).show();
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

    }

    private String getUser() {
        String contact;
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            contact = user.getPhoneNumber();
            return contact;
        } else{
            return "";
        }
    }

    private void fetchBook(String bookName) {
        String str = bookName.trim().replaceAll(" ", "");
        Query query;

        if((!str.equals(""))
                && (str.matches("^[a-zA-Z]*$"))){
            query = firebaseFirestore.collection("Books").whereEqualTo("englishName", bookName);

        } else {
            query = firebaseFirestore.collection("Books").whereEqualTo("marathiName", bookName);
        }

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<Book> searchBooks = queryDocumentSnapshots.toObjects(Book.class);
                if(searchBooks.size()>0){

                    setUpSearchRecyclerView(searchBooks);

                } else {

                    Toast.makeText(SellerHomePage.this, "No book available.Try scroll and search", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(SellerHomePage.this, "Failed.Try scroll and search", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setUpSearchRecyclerView(List<Book> searchBooks) {
        searchRecyclerViewAdapter = new SearchRecyclerViewAdapter(SellerHomePage.this, searchBooks, this);
        recyclerViewBooks.setAdapter(searchRecyclerViewAdapter);
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(SellerHomePage.this));
    }

    private void refresh() {
        Intent intent = new Intent(SellerHomePage.this, SellerHomePage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.profile:
                intent = new Intent(getApplicationContext(),Profile.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                return true;
            case R.id.sign_out:
                signOut();
                return true;
            case R.id.cart:
                intent = new Intent(getApplicationContext(),Cart.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(SellerHomePage.this, MainActivity.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar_seller,menu);

        return true;
    }

    @Override
    public void onBookClick(final Book book, View view) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(),view);
        popupMenu.inflate(R.menu.menu_add_to_cart);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.add_to_cart:
                        showDialougeBox(book);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    public void showDialougeBox(final Book book){
        final int[] qty = {1};
        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMaxValue(book.getStocks());
        numberPicker.setMinValue(1);
        NumberPicker.OnValueChangeListener valueChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                qty[0] = newVal;
            }
        };
        numberPicker.setOnValueChangedListener(valueChangeListener);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(numberPicker);
        builder.setMessage("Select quantity")
                .setCancelable(false)
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("Add to Cart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        book.setQuantity(qty[0]);
                        boolean s = helper.doesExists(book.getEnglishName());
                        if(s){
                            helper.updateData(book);
                            Toast.makeText(SellerHomePage.this, "Book added to cart. ", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            helper.insertData(book.getEnglishName(),book.getMarathiName(),book.getImgUrl(),
                                    book.getStocks(),book.getPrice(),book.getQuantity());
                            Toast.makeText(SellerHomePage.this, "Book added to cart. ", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        alert.setCanceledOnTouchOutside(true);
    }

    @Override
    public void onSearchBookClick(final Book book, View view) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(),view);
        popupMenu.inflate(R.menu.menu_add_to_cart);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.add_to_cart:
                        showDialougeBox(book);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
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
