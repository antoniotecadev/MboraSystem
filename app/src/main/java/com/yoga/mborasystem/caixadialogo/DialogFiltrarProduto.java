package com.yoga.mborasystem.caixadialogo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogFiltrarProdutoBinding;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;

import java.text.NumberFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogFiltrarProduto extends DialogFragment {

    private Bundle bundle;
    private AlertDialog dialog;
    private String formatted;
    private ProdutoViewModel produtoViewModel;
    private DialogFiltrarProdutoBinding binding;

    private long idcategoria;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        bundle = new Bundle();

        if (getArguments() != null) {
            idcategoria = getArguments().getLong("idcategoria");
        }

        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);
        binding = DialogFiltrarProdutoBinding.inflate(getLayoutInflater());

        formatted = NumberFormat.getCurrencyInstance(new Locale("pt", "AO")).format((0));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setIcon(R.drawable.ic_baseline_store_24);
        builder.setTitle(getString(R.string.filtrar_produto));
        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        Ultilitario.precoFormat(getContext(), binding.txtPrecoProdutoMin);
        Ultilitario.precoFormat(getContext(), binding.txtPrecoProdutoMax);

        binding.btnLimparMin.setOnClickListener(v -> limparPrecoFormat(binding.txtPrecoProdutoMin, formatted));
        binding.btnLimparMax.setOnClickListener(v -> limparPrecoFormat(binding.txtPrecoProdutoMax, formatted));

        binding.switchEstado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                buttonView.setText(getString(R.string.estado_bloqueado));
            } else {
                buttonView.setText(getString(R.string.estado_desbloqueado));
            }
        });

        binding.checkBoxNomeReferencia.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.inputReferenciaNomeProduto.setVisibility(View.VISIBLE);
            } else {
                binding.inputReferenciaNomeProduto.setVisibility(View.GONE);
            }
        });

        binding.checkBoxPreco.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.precoLinear.setVisibility(View.VISIBLE);
                binding.textViewPreco.setVisibility(View.VISIBLE);
                binding.btnLimparLinear.setVisibility(View.VISIBLE);
            } else {
                binding.precoLinear.setVisibility(View.GONE);
                binding.textViewPreco.setVisibility(View.GONE);
                binding.btnLimparLinear.setVisibility(View.GONE);
            }
        });

        binding.checkBoxCodigoBarra.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.inputCodigoBar.setVisibility(View.VISIBLE);
            } else {
                binding.inputCodigoBar.setVisibility(View.GONE);
            }
        });

        binding.checkBoxEstado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.divider1.setVisibility(View.VISIBLE);
                binding.textView8.setVisibility(View.VISIBLE);
                binding.switchEstado.setVisibility(View.VISIBLE);
            } else {
                binding.divider1.setVisibility(View.GONE);
                binding.textView8.setVisibility(View.GONE);
                binding.switchEstado.setVisibility(View.GONE);
            }
        });
        binding.btnFiltrarProduto.setOnClickListener(v -> {
            if (binding.checkBoxNomeReferencia.isChecked() && binding.checkBoxPreco.isChecked() && binding.checkBoxCodigoBarra.isChecked() && binding.checkBoxEstado.isChecked()) {
                produtoViewModel.validarProdutoFiltro(idcategoria, binding.txtReferenciaNomeProduto, binding.txtCodigoBar, binding.txtPrecoProdutoMin, binding.txtPrecoProdutoMax, binding.switchEstado, dialog);
            } else if (binding.checkBoxNomeReferencia.isChecked() && binding.checkBoxPreco.isChecked() && binding.checkBoxCodigoBarra.isChecked()) {
                produtoViewModel.validarProdutoNomeCodBarPreco(idcategoria, binding.txtReferenciaNomeProduto, binding.txtCodigoBar, binding.txtPrecoProdutoMin, binding.txtPrecoProdutoMax, dialog);
            } else if (binding.checkBoxNomeReferencia.isChecked() && binding.checkBoxPreco.isChecked() && binding.checkBoxEstado.isChecked()) {
                produtoViewModel.validarProdutoNomePrecoEstado(idcategoria, binding.txtReferenciaNomeProduto, binding.txtPrecoProdutoMin, binding.txtPrecoProdutoMax, binding.switchEstado, dialog);
            } else if (binding.checkBoxNomeReferencia.isChecked() && binding.checkBoxCodigoBarra.isChecked() && binding.checkBoxEstado.isChecked()) {
                produtoViewModel.validarProdutoNomeCodBarEstado(idcategoria, binding.txtReferenciaNomeProduto, binding.txtCodigoBar, binding.switchEstado, dialog);
            } else if (binding.checkBoxEstado.isChecked() && binding.checkBoxPreco.isChecked() && binding.checkBoxCodigoBarra.isChecked()) {
                produtoViewModel.validarProdutoCodBarPrecoEstado(idcategoria, binding.txtCodigoBar, binding.txtPrecoProdutoMin, binding.txtPrecoProdutoMax, binding.switchEstado, dialog);
            } else if (binding.checkBoxNomeReferencia.isChecked() && binding.checkBoxPreco.isChecked()) {
                produtoViewModel.validarProdutoNomePreco(idcategoria, binding.txtReferenciaNomeProduto, binding.txtPrecoProdutoMin, binding.txtPrecoProdutoMax, dialog);
            } else if (binding.checkBoxNomeReferencia.isChecked() && binding.checkBoxCodigoBarra.isChecked()) {
                produtoViewModel.validarProdutoNomeCodBar(idcategoria, binding.txtReferenciaNomeProduto, binding.txtCodigoBar, dialog);
            } else if (binding.checkBoxNomeReferencia.isChecked() && binding.checkBoxEstado.isChecked()) {
                produtoViewModel.validarProdutoNomeEstado(idcategoria, binding.txtReferenciaNomeProduto, binding.switchEstado, dialog);
            } else if (binding.checkBoxPreco.isChecked() && binding.checkBoxCodigoBarra.isChecked()) {
                produtoViewModel.validarProdutoCodBarPreco(idcategoria, binding.txtCodigoBar, binding.txtPrecoProdutoMin, binding.txtPrecoProdutoMax, dialog);
            } else if (binding.checkBoxPreco.isChecked() && binding.checkBoxEstado.isChecked()) {
                produtoViewModel.validarProdutoPrecoEstado(idcategoria, binding.txtPrecoProdutoMin, binding.txtPrecoProdutoMax, binding.switchEstado, dialog);
            } else if (binding.checkBoxCodigoBarra.isChecked() && binding.checkBoxEstado.isChecked()) {
                produtoViewModel.validarProdutoCodBarEstado(idcategoria, binding.txtCodigoBar, binding.switchEstado, dialog);
            } else if (binding.checkBoxNomeReferencia.isChecked()) {
                produtoViewModel.validarProdutoNome(idcategoria, binding.txtReferenciaNomeProduto, dialog);
            } else if (binding.checkBoxPreco.isChecked()) {
                produtoViewModel.validarProdutoPreco(idcategoria, binding.txtPrecoProdutoMin, binding.txtPrecoProdutoMax, dialog);
            } else if (binding.checkBoxCodigoBarra.isChecked()) {
                produtoViewModel.validarProdutoCodBar(idcategoria, binding.txtCodigoBar, dialog);
            } else if (binding.checkBoxEstado.isChecked()) {
                produtoViewModel.validarProdutoEstado(idcategoria, binding.switchEstado, dialog);
            }
        });
        binding.buttonFechar.setOnClickListener(v -> dialog.dismiss());
        return dialog;
    }

    public static void limparPrecoFormat(TextInputEditText preco, String formatted) {
        preco.setText(formatted);
        preco.setSelection(formatted.length());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (bundle != null) {
            bundle.clear();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }

}
