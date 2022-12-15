package com.yoga.mborasystem.caixadialogo;

import static com.yoga.mborasystem.util.Ultilitario.isCampoVazio;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        us = new Usuario();
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
        usuarioViewModel = new ViewModelProvider(requireActivity()).get(UsuarioViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogSenhaBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setIcon(R.drawable.ic_baseline_store_24);
        if (getArguments() != null) {
            us.setId(getArguments().getLong("idusuario"));
            builder.setTitle(getArguments().getString("nome"));
            binding.layoutPin.setVisibility(View.VISIBLE);
            binding.layoutPinRepete.setVisibility(View.VISIBLE);
            binding.textInputSenha.setVisibility(View.GONE);
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
                clienteViewModel.logar(requireParentFragment().requireView(), binding.senha, binding.textInputSenha);
            }
        });

        binding.btnAlterar.setOnClickListener(v -> {
            try {
                alterarCodigoPin(dialog);
            } catch (NoSuchAlgorithmException e) {
                Ultilitario.alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
        });

        binding.buttonFechar.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private void alterarCodigoPin(AlertDialog ad) throws NoSuchAlgorithmException {
        if (isCampoVazio(Objects.requireNonNull(binding.pin.getText()).toString()) || numero.matcher(binding.pin.getText().toString()).find() || PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("pinadmin", "0").equals(binding.pin.getText().toString().trim())) {
            binding.pin.requestFocus();
            binding.layoutPin.setError(getString(R.string.codigopin_invalido));
        } else if (binding.pin.length() != 6) {
            binding.pin.requestFocus();
            binding.layoutPin.setError(getString(R.string.codigopin_incompleto));
        } else if (!binding.pin.getText().toString().equals(Objects.requireNonNull(binding.pinRepete.getText()).toString())) {
            binding.pinRepete.requestFocus();
            binding.layoutPinRepete.setError(getString(R.string.pin_diferente));
        } else {
            us.setData_cria(getArguments().getString("datacria"));
            us.setCodigoPin(Ultilitario.gerarHash(binding.pin.getText().toString()));
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
