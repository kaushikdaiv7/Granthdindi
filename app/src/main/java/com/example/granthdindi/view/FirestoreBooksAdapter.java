package com.example.granthdindi.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Book;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class FirestoreBooksAdapter extends FirestoreRecyclerAdapter<Book, FirestoreBooksAdapter.BooksViewHolder> {
    private Context context;

    public FirestoreBooksAdapter(@NonNull FirestoreRecyclerOptions<Book> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull BooksViewHolder holder, int position, @NonNull Book model) {
        Picasso.with(context).load(model.getImgUrl()).fit().into(holder.imageViewBookImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }

        });

        holder.textViewBookName.setText(model.getEnglishName() + " - " + model.getMarathiName());
        holder.textViewBookStock.setText("Stock: " + String.valueOf(model.getStocks()));
        holder.textViewBookPrice.setText("₹ " + String.valueOf(model.getPrice()));
    }

    @NonNull
    @Override
    public FirestoreBooksAdapter.BooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_book, parent, false);
        return new BooksViewHolder(view);
    }

    public class BooksViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewBookImage;
        private TextView textViewBookName;
        private TextView textViewBookPrice;
        private TextView textViewBookStock;

        public BooksViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewBookImage = itemView.findViewById(R.id.iv_book_image);
            textViewBookName = itemView.findViewById(R.id.tv_book_name);
            textViewBookStock = itemView.findViewById(R.id.tv_book_stock);
            textViewBookPrice = itemView.findViewById(R.id.tv_book_price);

        }

    }

}
