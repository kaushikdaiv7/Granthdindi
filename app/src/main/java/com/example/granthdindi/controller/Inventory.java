package com.example.granthdindi.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Book;
import com.example.granthdindi.view.AddBookDialog;
import com.example.granthdindi.view.FirestoreBooksAllocatedAdapter;
import com.example.granthdindi.view.SearchRecyclerViewAdapter;
import com.example.granthdindi.view.UpdateDialogBookInventory;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inventory extends AppCompatActivity implements FirestoreBooksAllocatedAdapter.OnBookItemClick, AddBookDialog.AddBookDialogListener, PopupMenu.OnMenuItemClickListener, UpdateDialogBookInventory.UpdateDialogBookInventoryListener, SearchRecyclerViewAdapter.OnSearchBookItemClick {

    BottomNavigationView bottomNavigationView;
    AutoCompleteTextView autoCompleteTextViewBook;
    ImageView imageViewSearch;
    ProgressDialog progress;
    FloatingActionButton floatingActionButton;
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    Toolbar toolbar;
    Book bookClicked;
    SearchRecyclerViewAdapter searchRecyclerViewAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference booksCollection = db.collection("Books");
    private FirestoreBooksAllocatedAdapter adapter;

    ArrayList<String> invBooks = new ArrayList<String>();

    public static final String fileName = "adminLogin";
    public static final String adminPhone = "phoneNo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.inventory);

        autoCompleteTextViewBook = findViewById(R.id.atv_book_name);
        imageViewSearch = findViewById(R.id.iv_search);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);

        recyclerView = findViewById(R.id.recycler_view_inventory);
        floatingActionButton = findViewById(R.id.btn_add_book);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        invBooks = MainActivity.books;
        autoCompleteTextViewBook.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, invBooks));

        Query query = booksCollection.orderBy("price");

        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query,Book.class)
                .build();

        adapter = new FirestoreBooksAllocatedAdapter(options, getApplicationContext(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bookName = autoCompleteTextViewBook.getText().toString().trim();
                autoCompleteTextViewBook.setText("");
                if(invBooks.contains(bookName)){

                    progress = new ProgressDialog(Inventory.this);
                    progress.setTitle("Searching Book..");
                    progress.setMessage("Please wait..");
                    progress.setCancelable(false);

                    InputMethodManager imm =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    fetchBook(bookName);
                } else{
                    Toast.makeText(Inventory.this, "Enter a valid book name..", Toast.LENGTH_LONG).show();
                }
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;

                switch (item.getItemId()){
                    case R.id.sellers:
                        intent = new Intent(getApplicationContext(), AdminHomePage.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        return true;

                    case R.id.inventory:
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

    private void refresh() {

        Intent intent = new Intent(Inventory.this, Inventory.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        swipeRefreshLayout.setRefreshing(false);

    }

    private void fetchBook(String bookName) {
        String str = bookName.trim().replaceAll(" ", "");
        Query query;

        if((!str.equals(""))
                && (str.matches("^[a-zA-Z]*$"))){
            query = db.collection("Books").whereEqualTo("englishName", bookName);

        } else {
            query = db.collection("Books").whereEqualTo("marathiName", bookName);
        }

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<Book> searchBooks = queryDocumentSnapshots.toObjects(Book.class);
                if(searchBooks.size()>0){

                    setUpSearchRecyclerView(searchBooks);

                } else {
                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }
                    Toast.makeText(Inventory.this, "No book available.Try scroll and search", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(Inventory.this, "Failed.Try scroll and search", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setUpSearchRecyclerView(List<Book> searchBooks) {

        searchRecyclerViewAdapter = new SearchRecyclerViewAdapter(Inventory.this, searchBooks, this);
        recyclerView.setAdapter(searchRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(Inventory.this));
    }

    private void openDialog() {
        AddBookDialog addBookDialog = new AddBookDialog();
        addBookDialog.show(getSupportFragmentManager(), "Add Book Dialog");
    }

    @Override
    public void addBook(final Book book) {

        final Map<String, Object> doc = new HashMap<>();
        doc.put("id", book.getId());
        doc.put("englishName", book.getEnglishName());
        doc.put("imgUrl", book.getImgUrl());
        doc.put("marathiName", book.getMarathiName());
        doc.put("price", book.getPrice());
        doc.put("stocks", book.getStocks());

        db.collection("Books").whereEqualTo("id",book.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<Book> books = queryDocumentSnapshots.toObjects(Book.class);
                if(books.isEmpty()){
                    progress = new ProgressDialog(Inventory.this);
                    progress.setTitle( "Adding Book..");
                    progress.setMessage("Please wait..");
                    progress.setCancelable(false);
                    progress.show();
                    db.collection("Books").document(book.getEnglishName()).set(doc).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                    if (progress != null && progress.isShowing()) {
                                        progress.dismiss();
                                    }
                                    Toast.makeText(getApplicationContext(), "Failed..Try again..", Toast.LENGTH_SHORT).show();
                                }
                            });

                }
                else{
                    Toast.makeText(getApplicationContext(), "Book already exists in the database..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBookClick(Book book, View view) {
        bookClicked = book;
        showPopupMenu(view);
    }

    @Override
    public void onSearchBookClick(Book book, View view) {
        bookClicked = book;
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
            UpdateDialogBookInventory updateDialogBookInventory = new UpdateDialogBookInventory(bookClicked);
            updateDialogBookInventory.show(getSupportFragmentManager(), "update dialog book inventory");
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(Inventory.this);

            builder.setTitle("Deleting "+bookClicked.getEnglishName());

            builder.setMessage("Are you sure?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    progress = new ProgressDialog(Inventory.this);
                    progress.setTitle( "Deleting Book..");
                    progress.setMessage("Please wait..");
                    progress.setCancelable(false);
                    progress.show();
                    deleteBook(bookClicked.getEnglishName());
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

    private void deleteBook(String englishName) {
        db.collection("Books").document(englishName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getApplicationContext(),"Book deleted..",
                        Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getApplicationContext(),"Failed..Try again..",Toast.LENGTH_LONG).show();
            }
        });;
    }

    @Override
    public void update(int newStock, int newPrice) {

        progress = new ProgressDialog(Inventory.this);
        progress.setTitle( "Updating Book..");
        progress.setMessage("Please wait..");
        progress.setCancelable(false);
        progress.show();

        WriteBatch batch = db.batch();

        DocumentReference bookClickedRef = db.collection("Books").document(bookClicked.getEnglishName());

        batch.update(bookClickedRef,"stocks", newStock);
        batch.update(bookClickedRef,"price", newPrice);

        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Book updated successfully..", Toast.LENGTH_LONG).show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void signOut() {
        mAuth.signOut();
        sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        finish();
        Intent intent = new Intent(Inventory.this, MainActivity.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_signout,menu);

        return true;
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
