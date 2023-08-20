package com.example.granthdindi.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.granthdindi.R;
import com.example.granthdindi.controller.Inventory;
import com.example.granthdindi.model.Book;

public class UpdateDialogBookInventory extends AppCompatDialogFragment {
    Book book;
    TextView textViewBookPrice, textViewBookStock, textViewBookName;
    EditText editTextBookStock, editTextBookPrice;
    UpdateDialogBookInventoryListener listener;
    public  UpdateDialogBookInventory(Book book){
        this.book = book;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.update_dialog_book_inventory, null);

        textViewBookStock = view.findViewById(R.id.tv_stocks);
        textViewBookPrice = view.findViewById(R.id.tv_price);
        editTextBookStock = view.findViewById(R.id.et_stocks);
        editTextBookPrice = view.findViewById(R.id.et_price);
        textViewBookName = view.findViewById(R.id.tv_name);

        editTextBookPrice.setText(String.valueOf(book.getPrice()));
        editTextBookStock.setText(String.valueOf(book.getStocks()));
        textViewBookName.setText(book.getEnglishName()+ "-" + book.getMarathiName());

        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(editTextBookStock.getText().toString().trim()==null || editTextBookStock.getText().toString().trim().isEmpty()){
                            editTextBookStock.setError("This field cannot be empty");
                        }
                        else if(editTextBookPrice.getText().toString().trim()==null || editTextBookPrice.getText().toString().trim().isEmpty()){
                            editTextBookPrice.setError("This field cannot be empty");
                        } else {
                            int newStock = Integer.parseInt(editTextBookStock.getText().toString().trim());
                            int newPrice = Integer.parseInt(editTextBookPrice.getText().toString().trim());
                            listener.update(newStock, newPrice);
                        }

                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (UpdateDialogBookInventoryListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface UpdateDialogBookInventoryListener{
        void update(int newStock, int newPrice);
    }
}
