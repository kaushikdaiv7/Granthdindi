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

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.granthdindi.R;
import com.example.granthdindi.controller.Cart;
import com.example.granthdindi.controller.MainActivity;
import com.example.granthdindi.controller.SellerHomePage;
import com.example.granthdindi.model.Book;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartHolder> {

    public ArrayList<Book> cart_list;
    private Context context;
    public CartAdapter(ArrayList<Book> cart_list, Context context) {
        this.cart_list = cart_list;
        this.context = context;
    }

    @NonNull
    @Override
    public CartAdapter.CartHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_cart,parent,false);
        return new CartHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CartAdapter.CartHolder holder, int position) {
        final Book book = cart_list.get(position);
        Picasso.with(context).load(book.getImgUrl()).fit().into(holder.BookImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }

        });
        final int[] quantity = new int[1];
        holder.BookName.setText(book.getEnglishName() + " - " + book.getMarathiName());
        holder.Quantity_btn.setNumber(String.valueOf(book.getQuantity()));
        quantity[0] = book.getQuantity();
        holder.Quantity_btn.setRange(1,book.getStocks());
        holder.Quantity_btn.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                quantity[0] = newValue;
                holder.BookPrice.setText("₹"+book.getPrice()* quantity[0]);
                book.setQuantity(quantity[0]);
                SellerHomePage.helper.updateData(book);
                Cart.onValueChanged();
            }
        });
        holder.BookPrice.setText("₹"+book.getPrice()* quantity[0]);
        holder.ButtonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cart_list.remove(book);
                SellerHomePage.helper.deleteData(book.getEnglishName());
                notifyDataSetChanged();
                Cart.onValueChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cart_list.size();
    }

    public class CartHolder extends RecyclerView.ViewHolder {
        public ImageView BookImage;
        public TextView BookName;
        public ElegantNumberButton Quantity_btn;
        public TextView BookPrice;
        public ImageButton ButtonClear;
        public CartHolder(@NonNull View itemView) {
            super(itemView);
            BookImage = itemView.findViewById(R.id.iv_cart_book_image);
            BookName = itemView.findViewById(R.id.tv_cart_book_name);
            Quantity_btn = itemView.findViewById(R.id.enb_qty);
            BookPrice = itemView.findViewById(R.id.tv_cart_book_price);
            ButtonClear = itemView.findViewById(R.id.btn_clear);
        }
    }
}
