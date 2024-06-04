package com.mi.steamfamilygroupfinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class AboutDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("About")
                .setMessage("Author: MBG\nVersion: 1.20")
                .setPositiveButton("OK", (dialog, id) -> {
                    dismiss();
                });
        return builder.create();
    }
}
