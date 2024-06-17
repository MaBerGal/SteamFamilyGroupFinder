package com.mi.steamfamilygroupfinder.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.mi.steamfamilygroupfinder.R;

public class AboutDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.titleDialogAbout)
                .setMessage(getContext().getResources().getString(R.string.authorDialogAbout) + " MBG\n" + getContext().getResources().getString(R.string.versionDialogAbout) + " 1.20")
                .setPositiveButton("OK", (dialog, id) -> {
                    dismiss();
                });
        return builder.create();
    }
}
