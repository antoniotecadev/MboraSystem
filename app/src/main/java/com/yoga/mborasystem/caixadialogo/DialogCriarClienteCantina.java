package com.yoga.mborasystem.caixadialogo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogCriarClienteCantinaBinding;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteCantinaViewModel;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogCriarClienteCantina extends DialogFragment {

    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private ClienteCantina clienteCantina;
    private DialogCriarClienteCantinaBinding binding;
    private ClienteCantinaViewModel clienteCantinaViewModel;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        clienteCantina = new ClienteCantina();

        binding = DialogCriarClienteCantinaBinding.inflate(LayoutInflater.from(getContext()));
        clienteCantinaViewModel = new ViewModelProvider(requireActivity()).get(ClienteCantinaViewModel.class);

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.criar_cliente));

        String nome = DialogCriarClienteCantinaArgs.fromBundle(getArguments()).getNomeCliente();
        String telefone = DialogCriarClienteCantinaArgs.fromBundle(getArguments()).getTelefoneCliente();
        long idcliente = DialogCriarClienteCantinaArgs.fromBundle(getArguments()).getIdcliente();

        binding.editTextNome.setText(nome);
        binding.editTextNumeroTelefone.setText(telefone);

        if (idcliente == Ultilitario.ZERO) {
            binding.buttonGuardar.setVisibility(View.GONE);
            binding.buttonCriarCliente.setVisibility(View.VISIBLE);
            binding.buttonEliminarCliente.setVisibility(View.GONE);
        } else {
            binding.buttonGuardar.setVisibility(View.VISIBLE);
            binding.buttonCriarCliente.setVisibility(View.GONE);
            binding.buttonEliminarCliente.setVisibility(View.VISIBLE);
        }
        binding.buttonCriarCliente.setOnClickListener(v -> {
            clienteCantinaViewModel.criarCliente(binding.editTextNome, binding.editTextNumeroTelefone, dialog);
        });

        binding.buttonGuardar.setOnClickListener(v -> {
            clienteCantinaViewModel.actualizarCliente(idcliente, binding.editTextNome, binding.editTextNumeroTelefone, dialog);
        });

        binding.buttonEliminarCliente.setOnClickListener(v -> {
            deleteClient(idcliente, binding.editTextNome.getText().toString());
        });


        binding.buttonCancelar.setOnClickListener(v -> dialog.dismiss());

        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private void deleteClient(long idcliente, String nome) {
        clienteCantina.setId(idcliente);
        clienteCantina.setEstado(Ultilitario.TRES);
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.eliminar) + " (" + nome + ")")
                .setMessage(getString(R.string.tem_cert_elim_cli))
                .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.ok), (dialog1, which) -> clienteCantinaViewModel.eliminarCliente(clienteCantina, dialog))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }

}
