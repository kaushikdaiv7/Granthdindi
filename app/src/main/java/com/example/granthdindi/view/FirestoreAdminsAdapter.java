package com.example.granthdindi.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Admin;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FirestoreAdminsAdapter extends FirestoreRecyclerAdapter<Admin, FirestoreAdminsAdapter.AdminsViewHolder> {

    private Context context;
    private OnAdminClickListener onAdminClickListener;

    public FirestoreAdminsAdapter(@NonNull FirestoreRecyclerOptions<Admin> options, Context context, OnAdminClickListener onAdminClickListener) {
        super(options);
        this.context = context;
        this.onAdminClickListener = onAdminClickListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull AdminsViewHolder holder, int position, @NonNull Admin model) {
        holder.textViewAdminName.setText(model.getName());
        holder.textViewAdminContact.setText(String.valueOf(model.getContact()));
    }

    @NonNull
    @Override
    public AdminsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_admin, parent, false);
        return new FirestoreAdminsAdapter.AdminsViewHolder(view);
    }

    public class AdminsViewHolder extends RecyclerView.ViewHolder{

        private TextView textViewAdminName;
        private TextView textViewAdminContact;
        private ImageButton imageButton;
        public AdminsViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAdminName = itemView.findViewById(R.id.tv_admin_name);
            textViewAdminContact = itemView.findViewById(R.id.tv_admin_contact);
            imageButton = itemView.findViewById(R.id.delete_menu);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final long phn_number = Long.parseLong(textViewAdminContact.getText().toString().trim());
                    onAdminClickListener.onOptionClick(String.valueOf(phn_number),v);
                }
            });
        }

    }

    public interface OnAdminClickListener{
        void onOptionClick(String id,View view);  //view is passed because the popup menu will be connected to the button
    }
}
