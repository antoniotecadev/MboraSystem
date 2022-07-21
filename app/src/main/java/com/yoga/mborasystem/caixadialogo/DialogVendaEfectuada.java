package com.yoga.mborasystem.caixadialogo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogVendaEfectuadaBinding;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.VendaViewModel;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogVendaEfectuada extends DialogFragment {

    private AlertDialog dialog;
    private VendaViewModel vendaViewModel;
    private DialogVendaEfectuadaBinding binding;

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);

        binding = DialogVendaEfectuadaBinding.inflate(getLayoutInflater());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setIcon(R.drawable.ic_baseline_store_24);
        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        int total = DialogVendaEfectuadaArgs.fromBundle(getArguments()).getPrecoTotal();
        long idvenda = DialogVendaEfectuadaArgs.fromBundle(getArguments()).getIdvenda();
        binding.textViewTotal.setText(getString(R.string.total) + ": " + Ultilitario.formatPreco(String.valueOf(total)));

        binding.btnGuardar.setOnClickListener(v -> vendaViewModel.getGuardarPdfLiveData().setValue(idvenda));

        binding.btnImprimir.setOnClickListener(v -> vendaViewModel.getPrintLiveData().setValue(idvenda));

        binding.btnAbrirWhatsApp.setOnClickListener(v -> {
            String numeroWhatsApp = Objects.requireNonNull(binding.numeroWhatsApp.getText()).toString();
            if (numeroWhatsApp.isEmpty()) {
                binding.numeroWhatsApp.requestFocus();
                binding.inputLayoutNumeroWhatsapp.setError(getString(R.string.digite_numero_w));
            } else {
                vendaViewModel.getEnviarWhatsAppLiveData().setValue(numeroWhatsApp);
            }
        });
        binding.btnFechar.setOnClickListener(v -> vendaViewModel.getAlertDialogLiveData().setValue(dialog));
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}