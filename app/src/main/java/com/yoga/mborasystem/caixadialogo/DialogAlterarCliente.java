package com.yoga.mborasystem.caixadialogo;

import static com.yoga.mborasystem.util.Ultilitario.getPositionSpinner;
import static com.yoga.mborasystem.util.Ultilitario.getValueSharedPreferences;
import static com.yoga.mborasystem.util.Ultilitario.setItemselectedSpinner;
import static com.yoga.mborasystem.util.Ultilitario.spinnerMunicipios;
import static com.yoga.mborasystem.util.Ultilitario.spinnerProvincias;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentCadastrarClienteBinding;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class DialogAlterarCliente extends DialogFragment {

    private AlertDialog dialog;
    private ClienteViewModel clienteViewModel;
    private FragmentCadastrarClienteBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);

        binding = FragmentCadastrarClienteBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setIcon(R.drawable.ic_cliente_60);
        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        spinnerProvincias(requireContext(), binding.spinnerProvincias);
        setItemselectedSpinner(requireContext(), R.array.array_regime_iva_posicao, getValueSharedPreferences(requireContext(), "regime_iva", "0"), binding.spinnerRegimeIva);

        binding.buttonTermoCondicao.setVisibility(View.GONE);
        binding.checkTermoCondicao.setVisibility(View.GONE);
        binding.buttonCriarConta.setEnabled(true);
        binding.buttonCriarConta.setText(getString(R.string.actu_dads));
        binding.buttonCancelar.setVisibility(View.VISIBLE);
        binding.editTextIMEI.setVisibility(View.VISIBLE);
        binding.textSenha.setVisibility(View.VISIBLE);
        binding.buttonAlterarSenha.setVisibility(View.VISIBLE);
        binding.buttonAlterarSenha.setEnabled(true);
        binding.textEquipa.setVisibility(View.GONE);
        binding.editTextCodEqui.setVisibility(View.GONE);
        binding.buttonCancelar.setOnClickListener(v -> dialog.dismiss());
        binding.divider5.setVisibility(View.GONE);

        if (getArguments() != null) {

            Cliente cliente = getArguments().getParcelable("cliente");

            clienteViewModel.getMunicipios(binding.spinnerProvincias, binding.spinnerMunicipios);

            binding.editTextNome.setText(cliente.getNome());
            binding.editTextSobreNome.setText(cliente.getSobrenome());
            binding.editTextNif.setText(cliente.getNifbi());
            binding.editTextNumeroTelefone.setText(cliente.getTelefone());
            binding.editTextNumeroTelefoneAlternativo.setText(cliente.getTelefonealternativo());
            binding.editTextEmail.setText(cliente.getEmail());
            binding.editTextNomeEmpresa.setText(cliente.getNomeEmpresa());
            binding.editTextBairro.setText(cliente.getBairro());

            setItemselectedSpinner(requireContext(), R.array.provincias, cliente.getProvincia(), binding.spinnerProvincias);
            spinnerMunicipios(requireContext(), binding.spinnerMunicipios, cliente.getMunicipio());

            binding.editTextRua.setText(cliente.getRua());
            binding.editTextIMEI.setText(cliente.getImei());
            binding.textBairros.setVisibility(View.GONE);
            binding.spinnerBairros.setVisibility(View.GONE);
            binding.editTextIMEI.setEnabled(false);
            binding.editTextNome.setEnabled(false);
            binding.editTextSobreNome.setEnabled(false);
        }

        binding.buttonAlterarSenha.setOnClickListener(v -> {
            try {
                clienteViewModel.alterarSenha(binding.editTextSenha, binding.editTextSenhaNovamente);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        //Actualizar(reutilizando o Button)
        binding.buttonCriarConta.setOnClickListener(view -> {
            try {
                clienteViewModel.validarDadosEmpresa(Ultilitario.Operacao.ACTUALIZAR, binding.editTextNome, binding.editTextSobreNome, binding.editTextNif, binding.editTextNumeroTelefone, binding.editTextNumeroTelefoneAlternativo, binding.editTextEmail, binding.editTextNomeEmpresa, binding.spinnerProvincias, binding.spinnerMunicipios, binding.editTextBairro, binding.editTextRua, binding.editTextSenha, binding.editTextSenhaNovamente, binding.editTextCodigoEquipa, "0", getRegimeIva(), requireActivity());
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        return dialog;
    }

    private String getRegimeIva() {
        return getPositionSpinner(requireContext(), binding.spinnerRegimeIva, R.array.array_regime_iva_valor, R.array.array_regime_iva_posicao, "0");
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
