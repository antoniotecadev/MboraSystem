package com.yoga.mborasystem.caixadialogo;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogSenhaBinding;
import com.yoga.mborasystem.model.entidade.Usuario;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;
import com.yoga.mborasystem.viewmodel.UsuarioViewModel;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.regex.Pattern;

public class DialogSenha extends DialogFragment {

    private AlertDialog dialog;
    private DialogSenhaBinding binding;
    private ClienteViewModel clienteViewModel;
    private UsuarioViewModel usuarioViewModel;

    private Usuario us;

    private final Pattern letraNumero = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-Û0-9 ]");
    private final Pattern numero = Pattern.compile("[^0-9]");

    private boolean isCampoVazio(String valor) {
        return (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        us = new Usuario();
        binding = DialogSenhaBinding.inflate(getLayoutInflater());
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
        usuarioViewModel = new ViewModelProvider(requireActivity()).get(UsuarioViewModel.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setIcon(R.drawable.ic_baseline_store_24);
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
            if (isCampoVazio(Objects.requireNonNull(binding.senha.getText()).toString()) || letraNumero.matcher(binding.senha.getText().toString()).find()) {
                binding.senha.requestFocus();
                binding.textInputSenha.setError(getString(R.string.senha_invalida));
            } else {
                MainActivity.getProgressBar();
                clienteViewModel.logar(binding.senha, binding.textInputSenha );
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

        binding.buttonFechar.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private void alterarCodigoPin(AlertDialog ad) throws NoSuchAlgorithmException {
        if (isCampoVazio(Objects.requireNonNull(binding.senha.getText()).toString()) || numero.matcher(binding.senha.getText().toString()).find()) {
            binding.senha.requestFocus();
            binding.senha.setError(getString(R.string.senha_invalida));
        } else if (isCampoVazio(Objects.requireNonNull(binding.senhaRepete.getText()).toString()) || numero.matcher(binding.senhaRepete.getText().toString()).find()) {
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

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }
}
