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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchRecyclerViewAdapter extends RecyclerView.Adapter<SearchRecyclerViewAdapter.MyViewHolder> {

    Context context;
    List<Book> bookList;
    private OnSearchBookItemClick onSearchBookItemClick;

    public SearchRecyclerViewAdapter(Context context, List<Book> bookList, OnSearchBookItemClick onSearchBookItemClick){
        this.context = context;
        this.bookList = bookList;
        this.onSearchBookItemClick = onSearchBookItemClick;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_book_with_options, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

        Picasso.with(context).load(bookList.get(position).getImgUrl()).fit().into(holder.imageViewBookImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }

        });

        holder.textViewBookName.setText(bookList.get(position).getEnglishName() + " - " + bookList.get(position).getMarathiName());
        holder.textViewBookStock.setText("Stock: " + String.valueOf(bookList.get(position).getStocks()));
        holder.textViewBookPrice.setText("₹ " + String.valueOf(bookList.get(position).getPrice()));

        holder.imageButtonOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchBookItemClick.onSearchBookClick(bookList.get(position), v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewBookImage;
        private TextView textViewBookName;
        private TextView textViewBookPrice;
        private TextView textViewBookStock;
        private ImageButton imageButtonOptions;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewBookImage = itemView.findViewById(R.id.iv_book_image);
            textViewBookName = itemView.findViewById(R.id.tv_book_name);
            textViewBookStock = itemView.findViewById(R.id.tv_book_stock);
            textViewBookPrice = itemView.findViewById(R.id.tv_book_price);
            imageButtonOptions = itemView.findViewById(R.id.image_btn_options);

        }
    }

    public interface OnSearchBookItemClick {
        void onSearchBookClick(Book book, View view);

    }
}
