package com.example.granthdindi.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Book;

public class UpdateDialogBookAllocated extends AppCompatDialogFragment {
    Book book;
    TextView textViewBookName, textViewBookPrice, textViewBookStock;
    EditText editTextBookStock;
    UpdateDialogBookAllocatedListener listener;
    public  UpdateDialogBookAllocated(Book book){
        this.book = book;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.update_dialog_book_allocated, null);

        textViewBookName = view.findViewById(R.id.tv_book_name);
        textViewBookPrice = view.findViewById(R.id.tv_book_price);
        textViewBookStock = view.findViewById(R.id.tv_book_stock);
        editTextBookStock = view.findViewById(R.id.et_book_stock);

        textViewBookName.setText(book.getEnglishName()+ " - " + book.getMarathiName());
        textViewBookPrice.setText("₹ " + String.valueOf(book.getPrice()));
        textViewBookStock.setText("Stock: ");
        editTextBookStock.setText(String.valueOf(book.getStocks()));

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
                        } else {
                            int quantity = Integer.parseInt(editTextBookStock.getText().toString());
                            listener.update(quantity);
                        }

                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (UpdateDialogBookAllocatedListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface UpdateDialogBookAllocatedListener{
        void update(int quantity);
    }
}
