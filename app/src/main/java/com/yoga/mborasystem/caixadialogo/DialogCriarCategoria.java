package com.yoga.mborasystem.caixadialogo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;

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

import static com.yoga.mborasystem.MainActivity.progressDialog;

public class DialogCriarCategoria extends DialogFragment {

    private AlertDialog dialog;
    private Categoria categoria;
    private AlertDialog.Builder builder;
    private DialogCriarCategoriaBinding binding;
    private CategoriaProdutoViewModel categoriaProdutoViewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        categoriaProdutoViewModel = new ViewModelProvider(requireActivity()).get(CategoriaProdutoViewModel.class);
        binding = DialogCriarCategoriaBinding.inflate(LayoutInflater.from(getContext()));
        builder = new AlertDialog.Builder(getActivity());

        if (getArguments() != null) {
            categoria = getArguments().getParcelable("categoria");
            if (categoria != null) {
                builder.setTitle(R.string.alterar_categoria);
                binding.btnCriarCategoria.setText(getString(R.string.salvar));
                binding.nomeCategoria.setText(categoria.getCategoria());
                binding.descricao.setText(categoria.getDescricao());
                binding.switchEstado.setChecked(categoria.getEstado() == 1 ? false : true);
                binding.switchEstado.setText(categoria.getEstado() == 1 ? getString(R.string.estado_desbloqueado) : getString(R.string.estado_bloqueado));
                if (categoria.getData_cria() != null) {
                    binding.textDataCria.setVisibility(View.VISIBLE);
                    binding.textDataCria.setText(getText(R.string.data_cria) + ":          " + categoria.getData_cria());
                }
                if (categoria.getData_modifica() != null) {
                    binding.textDataModifica.setVisibility(View.VISIBLE);
                    binding.textDataModifica.setText(getText(R.string.data_modifica) + ": " + categoria.getData_modifica());
                }
                binding.btnCriarCategoria.setOnClickListener(v -> categoriaProdutoViewModel.validarCategoria(Ultilitario.Operacao.ACTUALIZAR, binding.nomeCategoria, binding.descricao, binding.switchEstado, dialog, categoria.getId()));
            }
        } else {
            builder.setTitle(R.string.nova_categoria);
            binding.btnCriarCategoria.setOnClickListener(v -> categoriaProdutoViewModel.validarCategoria(Ultilitario.Operacao.CRIAR, binding.nomeCategoria, binding.descricao, binding.switchEstado, dialog, 0));
        }

        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        binding.switchEstado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setText(getString(R.string.estado_bloqueado));
                } else {
                    buttonView.setText(getString(R.string.estado_desbloqueado));
                }
            }
        });

        binding.btnCancelar.setOnClickListener(v -> dialog.dismiss());

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
        if (progressDialog.isShowing() && progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
