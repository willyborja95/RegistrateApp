package com.apptec.registrateapp.mainactivity.fpermission.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.apptec.registrateapp.R;

public class DialogPermission extends DialogFragment {
    /**
     * This dialog is for let the user create a new permission
     */
    private static final String TAG = "DialogPermission";

    // UI elements
    private EditText txtStartDate;
    private EditText txtEndDate;
    private DatePicker dpStartDate;
    private Spinner spnPermissionType;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        /**
         * This method is called for create the dialog.
         * When the method show is called.
         */

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View viewDialog = inflater.inflate(R.layout.permission_element, null);

        // Binding the UI elements


        return builder.create();
    }
}
