package com.yoga.mborasystem.caixadialogo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogCriarUsuarioBinding;
import com.yoga.mborasystem.model.entidade.Usuario;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.UsuarioViewModel;

import java.security.NoSuchAlgorithmException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogCriarUsuario extends DialogFragment {

    private Usuario usuario;
    private AlertDialog dialog;
    private UsuarioViewModel usuarioViewModel;
    private DialogCriarUsuarioBinding binding;

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = DialogCriarUsuarioBinding.inflate(getLayoutInflater());
        usuarioViewModel = new ViewModelProvider(requireActivity()).get(UsuarioViewModel.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.criar_usuario));

        if (getArguments() != null) {
            usuario = getArguments().getParcelable("usuario");
            if (usuario != null) {
                builder.setTitle(usuario.getNome());
                binding.editTextNome.setText(usuario.getNome());
                binding.editTextNumeroTelefone.setText(usuario.getTelefone());
                binding.editTextEndereco.setText(usuario.getEndereco());
                binding.switchEstado.setChecked(usuario.getEstado() != 1);
                binding.switchEstado.setText(usuario.getEstado() == 1 ? getString(R.string.estado_desbloqueado) : getString(R.string.estado_bloqueado));

                if (usuario.getData_cria() != null) {
                    binding.textDataCria.setVisibility(View.VISIBLE);
                    binding.textDataCria.setText(getText(R.string.data_cria) + ":          " + usuario.getData_cria());
                }
                if (usuario.getData_modifica() != null) {
                    binding.textDataModifica.setVisibility(View.VISIBLE);
                    binding.textDataModifica.setText(getText(R.string.data_modifica) + ": " + usuario.getData_modifica());
                }

                if (getArguments().getBoolean("master")) {
                    binding.editCodigoPin.setVisibility(View.GONE);
                    binding.editCodigoPinNovamente.setVisibility(View.GONE);
                }

                binding.buttonCriarUsuario.setVisibility(View.GONE);
                binding.buttonGuardar.setVisibility(View.VISIBLE);
                binding.buttonEliminarUsuario.setVisibility(View.VISIBLE);
            }
        }

        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        binding.switchEstado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                buttonView.setText(getString(R.string.estado_bloqueado));
                Ultilitario.showToast(getContext(), Color.rgb(102, 153, 0), getString(R.string.usuario_bloqueado), R.drawable.ic_toast_erro);
            } else {
                buttonView.setText(getString(R.string.estado_desbloqueado));
                Ultilitario.showToast(getContext(), Color.rgb(102, 153, 0), getString(R.string.usuario_desbloqueado), R.drawable.ic_toast_feito);
            }
        });

        binding.buttonCriarUsuario.setOnClickListener(v -> {
            try {
                createUser();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        binding.buttonGuardar.setOnClickListener(v -> {
            try {
                updateUser();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.buttonEliminarUsuario.setOnClickListener(v -> deleteUser());
        binding.buttonCancelar.setOnClickListener(v -> dialog.dismiss());
        return dialog;
    }

    private void createUser() throws NoSuchAlgorithmException {
        usuarioViewModel.criarUsuario(binding.editTextNome, binding.editTextNumeroTelefone, binding.editTextEndereco, binding.switchEstado, binding.editTextCodigoPin, binding.editTextCodigoPinNovamente, dialog);
    }

    private void updateUser() throws NoSuchAlgorithmException {
        usuarioViewModel.actualizarUsuario(usuario.getId(), binding.editTextNome, binding.editTextNumeroTelefone, binding.editTextEndereco, binding.switchEstado, binding.editTextCodigoPin, binding.editTextCodigoPinNovamente, dialog);
    }

    private void deleteUser() {
        usuario.setId(usuario.getId());
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.eliminar) + " (" + usuario.getNome() + ")")
                .setMessage(getString(R.string.tem_certeza_eliminar_usuario))
                .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.ok), (dialog1, which) -> usuarioViewModel.eliminarUsuario(usuario, dialog))
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();
        Ultilitario.fullScreenDialog(getDialog());
        MainActivity.dismissProgressBar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
