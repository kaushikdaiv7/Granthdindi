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

public class AddAdminDialog extends AppCompatDialogFragment {

    EditText editTextAdminFirstName, editTextAdminLastName, editTextAdminContact;
    AddAdminDialogListener listener;
    AlertDialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.add_admin_dialog, null);

        editTextAdminFirstName = view.findViewById(R.id.et_admin_first_name);
        editTextAdminLastName  = view.findViewById(R.id.et_admin_last_name);
        editTextAdminContact = view.findViewById(R.id.et_admin_contact);

        builder.setView(view).setTitle("Add Admin")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        String first_name = editTextAdminFirstName.getText().toString().trim();
                        String last_name = editTextAdminLastName.getText().toString().trim();
                        String phn_number = editTextAdminContact.getText().toString().trim();
                        String password = "jvm888***";
                        mgr.hideSoftInputFromWindow(editTextAdminContact.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(editTextAdminFirstName.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(editTextAdminLastName.getWindowToken(), 0);
                        listener.addAdmin(first_name+" "+last_name, Long.parseLong(phn_number), password);
                    }
                });

        dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(editTextAdminFirstName.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextAdminLastName.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextAdminContact.getText().toString().trim()) &&
                 editTextAdminContact.getText().toString().trim().length() == 10);

        editTextAdminFirstName.addTextChangedListener(textWatcher);
        editTextAdminLastName.addTextChangedListener(textWatcher);
        editTextAdminContact.addTextChangedListener(textWatcher);
        return dialog;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddAdminDialogListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public interface AddAdminDialogListener {
        void addAdmin(String name, Long contact, String password);
    }

    TextWatcher textWatcher = new TextWatcher(){

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(editTextAdminFirstName.getText().toString().trim()) &&
                    !TextUtils.isEmpty(editTextAdminLastName.getText().toString().trim()) &&
                    !TextUtils.isEmpty(editTextAdminContact.getText().toString().trim()) && editTextAdminContact.getText().toString().trim().length() == 10);
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(editTextAdminFirstName.getText().toString().trim())) {
                editTextAdminFirstName.setError("This field cannot be empty");
            }
            if (TextUtils.isEmpty(editTextAdminLastName.getText().toString().trim())) {
                editTextAdminLastName.setError("This field cannot be empty");
            }
            if (TextUtils.isEmpty(editTextAdminContact.getText().toString().trim()) || editTextAdminContact.getText().toString().trim().length() != 10) {
                editTextAdminContact.setError("Enter valid 10 digit phone number");
            }

        }
    };
}
