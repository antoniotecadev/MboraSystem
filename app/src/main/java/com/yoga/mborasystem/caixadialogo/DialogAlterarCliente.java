package com.yoga.mborasystem.caixadialogo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.yoga.mborasystem.databinding.FragmentCadastrarClienteBinding;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.util.Ultilitario;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogAlterarCliente extends DialogFragment {

    private AlertDialog dialog;
    private FragmentCadastrarClienteBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = FragmentCadastrarClienteBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        binding.buttonTermoCondicao.setVisibility(View.GONE);
        binding.checkTermoCondicao.setVisibility(View.GONE);
        binding.buttonCriarConta.setVisibility(View.GONE);
        binding.buttonCancelar.setVisibility(View.VISIBLE);
        binding.buttonCancelar.setOnClickListener(v -> dialog.dismiss());

        if (getArguments() != null) {

            Cliente cliente = getArguments().getParcelable("cliente");

            binding.editTextNome.setText(cliente.getNome());
            binding.editTextSobreNome.setText(cliente.getSobrenome());
            binding.editTextNif.setText(cliente.getNifbi());
            binding.editTextNumeroTelefone.setText(cliente.getTelefone());
            binding.editTextEmail.setText(cliente.getEmail());
            binding.editTextSenha.setText(String.valueOf(cliente.getSenha()));
            binding.editTextSenhaNovamente.setText(String.valueOf(cliente.getSenha()));
        }

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
