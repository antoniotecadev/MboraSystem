package com.yoga.mborasystem.caixadialogo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogVerificarVendaBinding;
import com.yoga.mborasystem.util.Ultilitario;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogVerificarVenda extends DialogFragment {

    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private DialogVerificarVendaBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = DialogVerificarVendaBinding.inflate(LayoutInflater.from(getContext()));

        builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.verificar_venda));
        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Ultilitario.fullScreenDialog(getDialog());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
