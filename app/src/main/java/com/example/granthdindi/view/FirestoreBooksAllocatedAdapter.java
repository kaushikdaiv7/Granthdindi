package com.example.granthdindi.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class FirestoreBooksAllocatedAdapter extends FirestoreRecyclerAdapter<Book, FirestoreBooksAllocatedAdapter.BooksViewHolder> {

    private Context context;

    private OnBookItemClick onBookItemClick;

    public FirestoreBooksAllocatedAdapter(@NonNull FirestoreRecyclerOptions<Book> options, Context context, OnBookItemClick onBookItemClick) {
        super(options);
        this.context = context;
        this.onBookItemClick = onBookItemClick;
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
    public FirestoreBooksAllocatedAdapter.BooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_book_with_options, parent, false);
        return new BooksViewHolder(view);
    }

    public class BooksViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewBookImage;
        private TextView textViewBookName;
        private TextView textViewBookPrice;
        private TextView textViewBookStock;
        private ImageButton imageButtonOptions;

        public BooksViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewBookImage = itemView.findViewById(R.id.iv_book_image);
            textViewBookName = itemView.findViewById(R.id.tv_book_name);
            textViewBookStock = itemView.findViewById(R.id.tv_book_stock);
            textViewBookPrice = itemView.findViewById(R.id.tv_book_price);
            imageButtonOptions = itemView.findViewById(R.id.image_btn_options);
            imageButtonOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBookItemClick.onBookClick(getItem(getAdapterPosition()), v);
                }
            });

        }

    }

    public interface OnBookItemClick {
        void onBookClick(Book book, View view);

    }
}
