package com.yoga.mborasystem.caixadialogo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogExportarImportarVendaBinding;
import com.yoga.mborasystem.util.Event;
import com.yoga.mborasystem.viewmodel.VendaViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogExportarImportarVenda extends DialogFragment {

    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private VendaViewModel vendaViewModel;
    private DialogExportarImportarVendaBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);
        binding = DialogExportarImportarVendaBinding.inflate(LayoutInflater.from(getContext()));

        long idcliente = DialogExportarImportarVendaArgs.fromBundle(getArguments()).getIdcliente();
        long idusuario = DialogExportarImportarVendaArgs.fromBundle(getArguments()).getIdusuario();
        boolean isDivida = DialogExportarImportarVendaArgs.fromBundle(getArguments()).getIsDivida();

        builder = new AlertDialog.Builder(getActivity());

        if (idusuario == 0) {
            if (idcliente == 0 && !isDivida) {
                builder.setTitle(getString(R.string.exp_tds_vd));
            } else if (idcliente > 0 && !isDivida) {
                builder.setTitle(getString(R.string.exp_tds_vd_cli));
            } else if (idcliente == 0 && isDivida) {
                builder.setTitle(getString(R.string.exp_tds_vd_dvd));
            } else if (idcliente > 0 && isDivida) {
                builder.setTitle(getString(R.string.exp_tds_vd_dvd_cli));
            }
        } else if (isDivida) {
            builder.setTitle(getString(R.string.exp_tds_vd_dvd_use));
        } else {
            builder.setTitle(getString(R.string.exp_tds_vd_use));
        }

        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        binding.btnSelectedData.setOnClickListener(v -> vendaViewModel.getSelectedDataMutableLiveData().setValue(true));

        binding.btnExportarLocal.setOnClickListener(v -> vendaViewModel.getExportarLocalLiveData().setValue(new Event<>(true)));

        binding.btnExportarNuvem.setOnClickListener(v -> vendaViewModel.getExportarLocalLiveData().setValue(new Event<>(false)));

        binding.buttonFechar.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }


}
