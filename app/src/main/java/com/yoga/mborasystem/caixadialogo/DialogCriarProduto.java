package com.yoga.mborasystem.caixadialogo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogCriarProdutoBinding;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogCriarProduto extends DialogFragment {

    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private ProdutoViewModel produtoViewModel;
    private DialogCriarProdutoBinding binding;

    private long idcategoria;
    private String categoria;
    private Produto produto;
    private long idproduto;
    private Locale pt_AO;

    private float montanteIVA;
    private int preco, quantidade, precoFornecedor, lucro, receita;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        pt_AO = new Locale("pt", "AO");
        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);
        binding = DialogCriarProdutoBinding.inflate(LayoutInflater.from(getContext()));

        Ultilitario.addItemOnSpinner(binding.spinnerQuantidade, getContext());

        if (getArguments() != null) {
            produto = getArguments().getParcelable("produto");
            long idcategoria = getArguments().getLong("idcategoria");
            String categoria = getArguments().getString("categoria");

            this.idcategoria = idcategoria;
            this.categoria = categoria;

            if (produto != null) {
                binding.txtNomeProduto.setText(produto.getNome());
                binding.txtPrecoProduto.setText(Ultilitario.formatPreco(String.valueOf(produto.getPreco())));
                binding.txtPrecoProdutoFornecedor.setText(Ultilitario.formatPreco(String.valueOf(produto.getPrecofornecedor())));
                binding.checkIva.setChecked(produto.isIva() ? true : false);
                binding.txtCodigoBar.setText(produto.getCodigoBarra());
                binding.switchEstado.setChecked(produto.getEstado() == 1 ? false : true);
                binding.switchEstado.setText(produto.getEstado() == 1 ? getString(R.string.estado_desbloqueado) : getString(R.string.estado_bloqueado));

                if (produto.isIva()) {
                    binding.btnLimparPreco.setEnabled(false);
                }

                if (produto.getData_cria() != null) {
                    binding.textDataCria.setVisibility(View.VISIBLE);
                    binding.textDataCria.setText(getText(R.string.data_cria) + ":          " + produto.getData_cria());
                }
                if (produto.getData_modifica() != null) {
                    binding.textDataModifica.setVisibility(View.VISIBLE);
                    binding.textDataModifica.setText(getText(R.string.data_modifica) + ": " + produto.getData_modifica());
                }

                binding.btnCriarProduto.setVisibility(View.GONE);
                binding.switchContinuar.setVisibility(View.GONE);

                this.idproduto = produto.getId();
                this.idcategoria = produto.getIdcategoria();
                this.categoria = produto.getNome();

                if (getArguments().getBoolean("master")) {
                    binding.btnSalvarProduto.setVisibility(View.VISIBLE);
                    binding.btnEliminarProduto.setVisibility(View.VISIBLE);
                } else {
                    binding.txtNomeProduto.setEnabled(false);
                    binding.txtPrecoProduto.setEnabled(false);
                    binding.txtPrecoProdutoFornecedor.setEnabled(false);
                    binding.txtQuantidadeProduto.setEnabled(false);
                    binding.txtCodigoBar.setEnabled(false);
                    binding.checkIva.setEnabled(false);
                    binding.btnLimparPreco.setVisibility(View.GONE);
                    binding.btnLimparPrecoFornecedor.setVisibility(View.GONE);
                    binding.spinnerQuantidade.setVisibility(View.GONE);
                    binding.btnScannerFront.setVisibility(View.GONE);
                    binding.btnScannerBack.setVisibility(View.GONE);
                    binding.divider11.setVisibility(View.GONE);
                    binding.switchEstado.setEnabled(false);
                    binding.btnCancelar.setText(getText(R.string.fechar));
                }

            }
        }

        binding.checkIva.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                calcularIVA(binding, isChecked);
            }
        });
        setPreco(binding.txtPrecoProduto);
        setPreco(binding.txtPrecoProdutoFornecedor);
        binding.txtQuantidadeProduto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    calularMargemLucro(binding);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(this.categoria);
        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        binding.spinnerQuantidade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (produto != null) {
                    binding.txtQuantidadeProduto.setText(String.valueOf(produto.getQuantidade()));
                } else {
                    binding.txtQuantidadeProduto.setText(parent.getItemAtPosition(position).toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.btnScannerBack.setOnClickListener(v -> scanearCodigoBar(0));
        binding.btnScannerFront.setOnClickListener(v -> scanearCodigoBar(1));

        binding.switchEstado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setText(getString(R.string.estado_bloqueado));
                    Ultilitario.showToast(getContext(), Color.rgb(102, 153, 0), getString(R.string.estado_bloqueado), R.drawable.ic_toast_erro);
                } else {
                    buttonView.setText(getString(R.string.estado_desbloqueado));
                    Ultilitario.showToast(getContext(), Color.rgb(102, 153, 0), getString(R.string.estado_desbloqueado), R.drawable.ic_toast_feito);
                }
            }
        });

        binding.btnCriarProduto.setOnClickListener(v -> createProduto(idcategoria));
        binding.btnSalvarProduto.setOnClickListener(v -> updateProduto(idproduto, idcategoria));
        binding.btnEliminarProduto.setOnClickListener(v -> deleteProduto(produto));
        binding.btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Ultilitario.precoFormat(getContext(), binding.txtPrecoProduto);
        Ultilitario.precoFormat(getContext(), binding.txtPrecoProdutoFornecedor);

        binding.btnLimparPreco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ultilitario.zerarPreco(binding.txtPrecoProduto);
            }
        });

        binding.btnLimparPrecoFornecedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ultilitario.zerarPreco(binding.txtPrecoProdutoFornecedor);
            }
        });

        return dialog;
    }

    private void scanearCodigoBar(int camera) {
        new IntentIntegrator(getActivity())
                .setPrompt(getString(R.string.alinhar_codigo_barra))
                .setOrientationLocked(false)
                .setCameraId(camera)
                .initiateScan();
    }

    private void createProduto(long idcategoria) {
        if (binding.switchContinuar.isChecked()) {
            produtoViewModel.criarProduto(binding.txtNomeProduto, binding.txtPrecoProduto, binding.txtPrecoProdutoFornecedor, binding.txtQuantidadeProduto, binding.txtCodigoBar, binding.checkIva, binding.switchEstado, dialog, false, idcategoria);
        } else {
            produtoViewModel.criarProduto(binding.txtNomeProduto, binding.txtPrecoProduto, binding.txtPrecoProdutoFornecedor, binding.txtQuantidadeProduto, binding.txtCodigoBar, binding.checkIva, binding.switchEstado, dialog, true, idcategoria);
        }
    }

    private void updateProduto(long idproduto, long idcategoria) {
        produtoViewModel.actualizarProduto(idproduto, binding.txtNomeProduto, binding.txtPrecoProduto, binding.txtPrecoProdutoFornecedor, binding.txtQuantidadeProduto, binding.txtCodigoBar, binding.checkIva, binding.switchEstado, idcategoria, dialog);
    }

    private void deleteProduto(Produto produto) {
        produto.setId(produto.getId());
        produto.setEstado(Ultilitario.TRES);
        produto.setData_elimina(Ultilitario.getDateCurrent());
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getString(R.string.eliminar_produto) + " (" + produto.getNome() + ")");
        alert.setMessage(getString(R.string.tem_certeza_eliminar_produto));
        alert.setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss());
        alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> produtoViewModel.eliminarProduto(produto, true, dialog));
        alert.show();
    }

    private void calularMargemLucro(DialogCriarProdutoBinding b) {
        preco = Ultilitario.removerKZ(b.txtPrecoProduto);
        quantidade = Integer.parseInt(b.txtQuantidadeProduto.getText().toString());
        precoFornecedor = Ultilitario.removerKZ(b.txtPrecoProdutoFornecedor);
        receita = preco * quantidade;
        lucro = receita - precoFornecedor;
        float percentagem = receita == 0 ? 0 : (float) lucro / (float) receita * 100;
        b.textPossivelReceita.setText(Ultilitario.formatPreco(String.valueOf(receita)));
        if (lucro >= Ultilitario.ZERO) {
            b.textPossivelLucro.setText(Ultilitario.formatPreco(String.valueOf(lucro)));
            b.textPossivelLucro.setTextColor(Color.GREEN);
            b.textPossivelLucroPercentagem.setText(((int) percentagem + " %"));
            b.textPossivelLucroPercentagem.setTextColor(Color.GREEN);
        } else {
            b.textPossivelLucro.setText(getString(R.string.lucro_negativo));
            b.textPossivelLucro.setTextColor(Color.RED);
            b.textPossivelLucroPercentagem.setText(getString(R.string.lucro_negativo));
            b.textPossivelLucroPercentagem.setTextColor(Color.RED);
        }
    }

    private void calcularIVA(DialogCriarProdutoBinding b, boolean isChecked) {
        if (isChecked) {
            preco = Ultilitario.removerKZ(b.txtPrecoProduto);
            montanteIVA = (float) (preco * 0.14);
            b.txtPrecoProduto.setText(String.valueOf(preco + montanteIVA));
            b.textMontanteIva.setText(Ultilitario.formatPreco(String.valueOf((int) montanteIVA)));
            binding.txtPrecoProduto.setEnabled(false);
            binding.btnLimparPreco.setEnabled(false);
        } else {
            binding.txtPrecoProduto.setEnabled(true);
            binding.btnLimparPreco.setEnabled(true);
            b.txtPrecoProduto.setText(String.valueOf(preco / 1.14));
            b.textMontanteIva.setText("");
        }
    }

    private void setPreco(TextInputEditText text) {
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("Kz")) {
                    calularMargemLucro(binding);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                produtoViewModel.codigoBarra(result, binding.txtCodigoBar);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
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

