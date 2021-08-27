package com.yoga.mborasystem.caixadialogo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogCriarClienteCantinaBinding;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.viewmodel.ClienteCantinaViewModel;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogCriarClienteCantina extends DialogFragment {

    private ClienteCantina clienteCantina;
    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private ClienteCantinaViewModel clienteCantinaViewModel;
    private DialogCriarClienteCantinaBinding binding;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = DialogCriarClienteCantinaBinding.inflate(LayoutInflater.from(getContext()));
        clienteCantinaViewModel = new ViewModelProvider(requireActivity()).get(ClienteCantinaViewModel.class);

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.criar_cliente));

        binding.editTextNome.setText(DialogCriarClienteCantinaArgs.fromBundle(getArguments()).getNomeCliente());

        binding.buttonCriarCliente.setOnClickListener(v -> {
            clienteCantinaViewModel.criarCliente(binding.editTextNome, binding.editTextNumeroTelefone, dialog);
        });

        binding.buttonCancelar.setOnClickListener(v -> dialog.dismiss());

        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

}
