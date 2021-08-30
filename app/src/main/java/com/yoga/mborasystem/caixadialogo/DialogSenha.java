package com.yoga.mborasystem.caixadialogo;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogSenhaBinding;
import com.yoga.mborasystem.model.entidade.Usuario;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;
import com.yoga.mborasystem.viewmodel.UsuarioViewModel;

import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogSenha extends DialogFragment {

    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private DialogSenhaBinding binding;
    private ClienteViewModel clienteViewModel;
    private UsuarioViewModel usuarioViewModel;

    private Usuario us;

    private Pattern letraNumero = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-Û0-9 ]");
    private Pattern numero = Pattern.compile("[^0-9]");

    private boolean isCampoVazio(String valor) {
        return (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        us = new Usuario();
        binding = DialogSenhaBinding.inflate(LayoutInflater.from(getContext()));
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
        usuarioViewModel = new ViewModelProvider(requireActivity()).get(UsuarioViewModel.class);

        builder = new AlertDialog.Builder(getActivity());

        if (getArguments() != null) {
            us.setId(getArguments().getLong("idusuario"));
            builder.setTitle(getString(R.string.alterar_codigo_pin));
            binding.senha.setHint(getString(R.string.codigopin));
            binding.layoutSenhaRepete.setVisibility(View.VISIBLE);
            binding.btnAlterar.setVisibility(View.VISIBLE);
            binding.btnEntrar.setVisibility(View.GONE);
        }

        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);


        binding.btnEntrar.setOnClickListener(v -> {
            if (isCampoVazio(binding.senha.getText().toString()) || letraNumero.matcher(binding.senha.getText().toString()).find()) {
                binding.senha.requestFocus();
                binding.senha.setError(getString(R.string.senha_invalida));
            } else {
                clienteViewModel.logar(binding.senha);
            }
        });

        binding.btnAlterar.setOnClickListener(v -> {
            try {
                alterarCodigoPin(dialog);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnCancelar.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private void alterarCodigoPin(AlertDialog ad) throws NoSuchAlgorithmException {
        if (isCampoVazio(binding.senha.getText().toString()) || numero.matcher(binding.senha.getText().toString()).find()) {
            binding.senha.requestFocus();
            binding.senha.setError(getString(R.string.senha_invalida));
        } else if (isCampoVazio(binding.senhaRepete.getText().toString()) || numero.matcher(binding.senhaRepete.getText().toString()).find()) {
            binding.senha.requestFocus();
            binding.senha.setError(getString(R.string.senha_invalida));
        } else if (binding.senha.length() > 6 || binding.senha.length() < 6) {
            binding.senha.requestFocus();
            binding.senha.setError(getString(R.string.codigopin_incompleto));
        } else if (binding.senhaRepete.length() > 6 || binding.senhaRepete.length() < 6) {
            binding.senhaRepete.requestFocus();
            binding.senhaRepete.setError(getString(R.string.codigopin_incompleto));
        } else if (!binding.senha.getText().toString().equals(binding.senhaRepete.getText().toString())) {
            binding.senhaRepete.requestFocus();
            binding.senhaRepete.setError(getString(R.string.pin_diferente));
        } else {
            us.setCodigoPin(Ultilitario.gerarHash(binding.senha.getText().toString()));
            usuarioViewModel.actualizarUsuario(us, true, ad);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
