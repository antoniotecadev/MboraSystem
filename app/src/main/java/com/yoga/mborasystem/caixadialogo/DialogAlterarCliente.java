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
        binding.editTextIMEI.setVisibility(View.VISIBLE);
        binding.textInputSenha.setVisibility(View.GONE);
        binding.textInputSenhaNovamente.setVisibility(View.GONE);
        binding.buttonCancelar.setOnClickListener(v -> dialog.dismiss());

        if (getArguments() != null) {

            Cliente cliente = getArguments().getParcelable("cliente");

            binding.editTextNome.setText(cliente.getNome());
            binding.editTextSobreNome.setText(cliente.getSobrenome());
            binding.editTextNif.setText(cliente.getNifbi());
            binding.editTextNumeroTelefone.setText(cliente.getTelefone());
            binding.editTextNumeroTelefoneAlternativo.setText(cliente.getTelefonealternativo());
            binding.editTextEmail.setText(cliente.getEmail());
            binding.editTextNomeLoja.setText(cliente.getNomeEmpresa());
            binding.textProvincia.setText("");
            binding.textMunicipio.setText("");
            binding.spinnerProvincias.setVisibility(View.GONE);
            binding.spinnerMunicipios.setVisibility(View.GONE);
            binding.editTextBairro.setText(cliente.getBairro());
            binding.editTextRua.setText(cliente.getRua());
            binding.editTextIMEI.setText(cliente.getImei());

            //Desabilitar campos
            binding.editTextNome.setEnabled(false);
            binding.editTextSobreNome.setEnabled(false);
            binding.editTextNif.setEnabled(false);
            binding.editTextNumeroTelefone.setEnabled(false);
            binding.editTextNumeroTelefoneAlternativo.setEnabled(false);
            binding.editTextEmail.setEnabled(false);
            binding.editTextNomeLoja.setEnabled(false);
            binding.textMunicipio.setEnabled(false);
            binding.editTextBairro.setEnabled(false);
            binding.editTextRua.setEnabled(false);
            binding.editTextIMEI.setEnabled(false);
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
