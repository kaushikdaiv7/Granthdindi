package com.example.granthdindi.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Seller;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FirestoreSellersAdapter extends FirestoreRecyclerAdapter<Seller, FirestoreSellersAdapter.SellersViewHolder> {

    private OnSellerItemClick onSellerItemClick;

    public FirestoreSellersAdapter(@NonNull FirestoreRecyclerOptions<Seller> options, OnSellerItemClick onSellerItemClick) {
        super(options);
        this.onSellerItemClick = onSellerItemClick;
    }

    @Override
    protected void onBindViewHolder(@NonNull SellersViewHolder holder, int position, @NonNull Seller model) {
        holder.textViewSellerName.setText(model.getName());
        holder.textViewSellerContact.setText(model.getContact() + "");
        holder.textViewSellerLocation.setText(model.getLocation());
    }

    @NonNull
    @Override
    public SellersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_seller, parent, false);
        return new SellersViewHolder(view);
    }

    public class SellersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textViewSellerName;
        private TextView textViewSellerContact;
        private TextView textViewSellerLocation;

        public SellersViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewSellerName = itemView.findViewById(R.id.tv_seller_name);
            textViewSellerContact = itemView.findViewById(R.id.tv_seller_contact);
            textViewSellerLocation = itemView.findViewById(R.id.tv_seller_location);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onSellerItemClick.onSellerClick(getItem(getAdapterPosition()));
        }
    }

    public interface OnSellerItemClick {
        void onSellerClick(Seller seller);
    }
}
