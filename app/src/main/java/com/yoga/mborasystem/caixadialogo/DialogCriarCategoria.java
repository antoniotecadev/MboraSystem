package com.yoga.mborasystem.caixadialogo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogCriarCategoriaBinding;
import com.yoga.mborasystem.model.entidade.Categoria;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.CategoriaProdutoViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogCriarCategoria extends DialogFragment {

    private AlertDialog dialog;
    private Categoria categoria;
    private DialogCriarCategoriaBinding binding;
    private CategoriaProdutoViewModel categoriaProdutoViewModel;

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        categoriaProdutoViewModel = new ViewModelProvider(requireActivity()).get(CategoriaProdutoViewModel.class);
        binding = DialogCriarCategoriaBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setIcon(R.drawable.ic_baseline_store_24);
        if (getArguments() != null) {
            categoria = getArguments().getParcelable("categoria");
            if (categoria != null) {
                binding.btnCriarCategoria.setText(getString(R.string.salvar));
                binding.nomeCategoria.setText(categoria.getCategoria());
                binding.descricao.setText(categoria.getDescricao());
                binding.switchEstado.setChecked(categoria.getEstado() != 1);
                binding.switchEstado.setText(categoria.getEstado() == 1 ? getString(R.string.estado_desbloqueado) : getString(R.string.estado_bloqueado));
                if (categoria.getData_cria() != null) {
                    binding.textDataCria.setVisibility(View.VISIBLE);
                    binding.textDataCria.setText(getText(R.string.data_cria) + ":          " + categoria.getData_cria());
                }
                if (categoria.getData_modifica() != null) {
                    binding.textDataModifica.setVisibility(View.VISIBLE);
                    binding.textDataModifica.setText(getText(R.string.data_modifica) + ": " + categoria.getData_modifica());
                }
                binding.btnCriarCategoria.setOnClickListener(v -> {
                    categoriaProdutoViewModel.crud = true;
                    categoriaProdutoViewModel.validarCategoria(Ultilitario.Operacao.ACTUALIZAR, binding.nomeCategoria, binding.descricao, binding.switchEstado, dialog, categoria.getId());
                });
            }
        } else {
            binding.btnCriarCategoria.setOnClickListener(v -> {
                categoriaProdutoViewModel.crud = true;
                categoriaProdutoViewModel.validarCategoria(Ultilitario.Operacao.CRIAR, binding.nomeCategoria, binding.descricao, binding.switchEstado, dialog, 0);
            });
        }

        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        binding.switchEstado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                buttonView.setText(getString(R.string.estado_bloqueado));
            else
                buttonView.setText(getString(R.string.estado_desbloqueado));
        });

        binding.buttonFechar.setOnClickListener(v -> dialog.dismiss());

        return dialog;
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
