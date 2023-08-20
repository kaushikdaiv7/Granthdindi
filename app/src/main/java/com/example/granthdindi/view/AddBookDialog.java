package com.example.granthdindi.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.granthdindi.R;
import com.example.granthdindi.model.Book;

public class AddBookDialog extends AppCompatDialogFragment {
    private EditText editTextImageUrl;
    private EditText editTextEnglishName;
    private EditText editTextMarathiName;
    private EditText editTextPrice;
    private EditText editTextStocks;
    private EditText editTextId;

    private AddBookDialogListener listener;
    AlertDialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.add_book_dialog, null);

        editTextImageUrl = view.findViewById(R.id.et_img_url);
        editTextEnglishName = view.findViewById(R.id.et_english_name);
        editTextMarathiName = view.findViewById(R.id.et_marathi_name);
        editTextPrice = view.findViewById(R.id.et_price);
        editTextStocks = view.findViewById(R.id.et_stocks);
        editTextId = view.findViewById(R.id.et_book_id);

        builder.setView(view).setTitle("New Book").setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        String bookId = editTextId.getText().toString().trim();
                        String imgUrl = editTextImageUrl.getText().toString().trim();
                        String englishName = editTextEnglishName.getText().toString().trim();
                        String marathiName = editTextMarathiName.getText().toString().trim();
                        int price = Integer.parseInt(editTextPrice.getText().toString().trim());
                        int stocks = Integer.parseInt(editTextStocks.getText().toString().trim());
                        mgr.hideSoftInputFromWindow(editTextEnglishName.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(editTextMarathiName.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(editTextImageUrl.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(editTextId.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(editTextPrice.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(editTextStocks.getWindowToken(), 0);
                        Book book = new Book(englishName, marathiName, imgUrl, stocks, price, bookId);
                        listener.addBook(book);
                    }
                });

        dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(editTextEnglishName.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextMarathiName.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextId.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextImageUrl.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextPrice.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextStocks.getText().toString().trim()));

        editTextEnglishName.addTextChangedListener(textWatcher);
        editTextMarathiName.addTextChangedListener(textWatcher);
        editTextId.addTextChangedListener(textWatcher);
        editTextImageUrl.addTextChangedListener(textWatcher);
        editTextStocks.addTextChangedListener(textWatcher);
        editTextPrice.addTextChangedListener(textWatcher);

        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddBookDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement ExampleDialogListner");
        }
    }

    public interface AddBookDialogListener{
        void addBook(Book book);
    }

    TextWatcher textWatcher = new TextWatcher(){

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(editTextEnglishName.getText().toString().trim()) &&
                    !TextUtils.isEmpty(editTextMarathiName.getText().toString().trim()) &&
                    !TextUtils.isEmpty(editTextId.getText().toString().trim()) &&
                    !TextUtils.isEmpty(editTextImageUrl.getText().toString().trim()) &&
                    !TextUtils.isEmpty(editTextPrice.getText().toString().trim()) &&
                    !TextUtils.isEmpty(editTextStocks.getText().toString().trim()));
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(editTextId.getText().toString().trim())) {
                editTextId.setError("This field cannot be empty");
            }
            if (TextUtils.isEmpty(editTextEnglishName.getText().toString().trim())) {
                editTextEnglishName.setError("This field cannot be empty");
            }
            if (TextUtils.isEmpty(editTextMarathiName.getText().toString().trim())) {
                editTextMarathiName.setError("This field cannot be empty");
            }
            if (TextUtils.isEmpty(editTextImageUrl.getText().toString().trim())) {
                editTextImageUrl.setError("This field cannot be empty");
            }
            if (TextUtils.isEmpty(editTextPrice.getText().toString().trim())) {
                editTextPrice.setError("This field cannot be empty");
            }
            if (TextUtils.isEmpty(editTextStocks.getText().toString().trim())) {
                editTextStocks.setError("This field cannot be empty");
            }
        }
    };
}

