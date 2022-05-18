package com.yoga.mborasystem.caixadialogo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogCriarProdutoBinding;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;

import java.util.Objects;

public class DialogCriarProduto extends DialogFragment {

    private AlertDialog dialog;
    private ProdutoViewModel produtoViewModel;
    private DialogCriarProdutoBinding binding;

    private long idcategoria;
    private String categoria;
    private Produto produto;
    private long idproduto;

    private int preco;

    private IntentIntegrator intentIntegrator;

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        intentIntegrator = new IntentIntegrator(getActivity());
        binding = DialogCriarProdutoBinding.inflate(getLayoutInflater());
        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);

        Ultilitario.addItemOnSpinner(binding.spinnerQuantidade, 255, getContext());
        Ultilitario.addItemOnSpinner(binding.spinnerIva, 20, getContext());

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
                binding.checkIva.setChecked(produto.isIva());
                binding.spinnerIva.setSelection(produto.getPercentagemIva() == 0 ? Integer.parseInt(Ultilitario.getPercentagemIva(requireActivity())) - 1 : produto.getPercentagemIva() - 1);
                binding.txtCodigoBar.setText(produto.getCodigoBarra());
                binding.switchEstado.setChecked(produto.getEstado() != 1);
                binding.switchEstado.setText(produto.getEstado() == 1 ? getString(R.string.estado_desbloqueado) : getString(R.string.estado_bloqueado));

                if (produto.isIva()) {
                    binding.txtPrecoProduto.setEnabled(false);
                    binding.btnLimparPreco.setEnabled(false);
                    binding.spinnerIva.setEnabled(false);
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
                    binding.buttonFechar.setText(getText(R.string.fechar));
                }

            } else {
                binding.spinnerIva.setSelection(Integer.parseInt(Ultilitario.getPercentagemIva(requireActivity())) - 1);
            }
        }

        binding.checkIva.setOnCheckedChangeListener((buttonView, isChecked) -> calcularIVA(binding, isChecked));
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

        binding.spinnerIva.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (produto != null) {
                    binding.checkIva.setText(getText(R.string.montante_iva) + "(" + (produto.getPercentagemIva() == 0 ? parent.getItemAtPosition(position).toString() : produto.getPercentagemIva()) + "%)");
                } else {
                    binding.checkIva.setText(getText(R.string.montante_iva) + "(" + parent.getItemAtPosition(position).toString() + "%)");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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

        binding.switchEstado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                buttonView.setText(getString(R.string.estado_bloqueado));
                Ultilitario.showToast(getContext(), Color.rgb(102, 153, 0), getString(R.string.estado_bloqueado), R.drawable.ic_toast_erro);
            } else {
                buttonView.setText(getString(R.string.estado_desbloqueado));
                Ultilitario.showToast(getContext(), Color.rgb(102, 153, 0), getString(R.string.estado_desbloqueado), R.drawable.ic_toast_feito);
            }
        });

        binding.btnCriarProduto.setOnClickListener(v -> createProduto(idcategoria));
        binding.btnSalvarProduto.setOnClickListener(v -> updateProduto(idproduto, idcategoria));
        binding.btnEliminarProduto.setOnClickListener(v -> deleteProduto(produto));
        binding.buttonFechar.setOnClickListener(v -> dialog.dismiss());

        Ultilitario.precoFormat(getContext(), binding.txtPrecoProduto);
        Ultilitario.precoFormat(getContext(), binding.txtPrecoProdutoFornecedor);

        binding.btnLimparPreco.setOnClickListener(v -> Ultilitario.zerarPreco(binding.txtPrecoProduto));

        binding.btnLimparPrecoFornecedor.setOnClickListener(v -> Ultilitario.zerarPreco(binding.txtPrecoProdutoFornecedor));

        return dialog;
    }

    private void scanearCodigoBar(int camera) {
        intentIntegrator.setPrompt(getString(R.string.alinhar_codigo_barra));
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setCameraId(camera);
        zxingActivityResultLauncher.launch(intentIntegrator.createScanIntent());
    }

    private void createProduto(long idcategoria) {
        produtoViewModel.criarProduto(binding.txtNomeProduto, binding.txtPrecoProduto, binding.txtPrecoProdutoFornecedor, binding.txtQuantidadeProduto, binding.txtCodigoBar, binding.checkIva, Integer.valueOf(binding.spinnerIva.getSelectedItem().toString()), binding.switchEstado, dialog, !binding.switchContinuar.isChecked(), idcategoria);
    }

    private void updateProduto(long idproduto, long idcategoria) {
        produtoViewModel.actualizarProduto(idproduto, binding.txtNomeProduto, binding.txtPrecoProduto, binding.txtPrecoProdutoFornecedor, binding.txtQuantidadeProduto, binding.txtCodigoBar, binding.checkIva, Integer.valueOf(binding.spinnerIva.getSelectedItem().toString()), binding.switchEstado, idcategoria, dialog);
    }

    private void deleteProduto(Produto produto) {
        produto.setId(produto.getId());
        produto.setEstado(Ultilitario.TRES);
        produto.setData_elimina(Ultilitario.getDateCurrent());
        AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
        alert.setTitle(getString(R.string.env_lx) + " (" + produto.getNome() + ")");
        alert.setMessage(getString(R.string.env_prod_lix));
        alert.setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss());
        alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> produtoViewModel.eliminarProduto(produto, true, dialog, false));
        alert.show();
    }

    @SuppressLint("SetTextI18n")
    private void calularMargemLucro(DialogCriarProdutoBinding b) {
        preco = Ultilitario.removerKZ(b.txtPrecoProduto);
        int quantidade = Integer.parseInt(Objects.requireNonNull(b.txtQuantidadeProduto.getText()).toString());
        int precoFornecedor = Ultilitario.removerKZ(b.txtPrecoProdutoFornecedor);
        int receita = preco * quantidade;
        int lucro = receita - precoFornecedor;
        float percentagem = receita == 0 ? 0 : (float) lucro / (float) receita * 100;
        b.textPossivelReceita.setText(Ultilitario.formatPreco(String.valueOf(receita)));
        if (lucro >= Ultilitario.ZERO) {
            b.textPossivelLucro.setText(Ultilitario.formatPreco(String.valueOf(lucro)));
            b.textPossivelLucro.setTextColor(Color.parseColor("#32CD32"));
            b.textPossivelLucroPercentagem.setText(((int) percentagem + " %"));
            b.textPossivelLucroPercentagem.setTextColor(Color.parseColor("#32CD32"));
        } else {
            b.textPossivelLucro.setText(getString(R.string.lucro_negativo));
            b.textPossivelLucro.setTextColor(Color.RED);
            b.textPossivelLucroPercentagem.setText(getString(R.string.lucro_negativo));
            b.textPossivelLucroPercentagem.setTextColor(Color.RED);
        }
    }

    private void calcularIVA(DialogCriarProdutoBinding b, boolean isChecked) {
        if (isChecked) {
            float montanteIVA;
            preco = Ultilitario.removerKZ(b.txtPrecoProduto);
            if (Integer.valueOf(b.spinnerIva.getSelectedItem().toString()) > 9) {
                montanteIVA = (float) (preco * Float.parseFloat("0." + b.spinnerIva.getSelectedItem().toString()));
            } else {
                montanteIVA = (float) (preco * Float.parseFloat("0.0" + b.spinnerIva.getSelectedItem().toString()));
            }
            b.txtPrecoProduto.setText(String.valueOf(preco + montanteIVA));
            b.textMontanteIva.setText(Ultilitario.formatPreco(String.valueOf((int) montanteIVA)));
            binding.txtPrecoProduto.setEnabled(false);
            binding.btnLimparPreco.setEnabled(false);
            b.spinnerIva.setEnabled(false);
            binding.checkIva.setText(getText(R.string.montante_iva) + "(" + b.spinnerIva.getSelectedItem().toString() + "%)");
        } else {
            binding.txtPrecoProduto.setEnabled(true);
            binding.btnLimparPreco.setEnabled(true);
            b.spinnerIva.setEnabled(true);
            if (Integer.valueOf(b.spinnerIva.getSelectedItem().toString()) > 9) {
                b.txtPrecoProduto.setText(String.valueOf(preco / Float.parseFloat("1." + b.spinnerIva.getSelectedItem().toString())));
            } else {
                b.txtPrecoProduto.setText(String.valueOf(preco / Float.parseFloat("1.0" + b.spinnerIva.getSelectedItem().toString())));
            }
            b.checkIva.setText(getText(R.string.montante_iva) + "(1%)");
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

    ActivityResultLauncher<Intent> zxingActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    IntentResult r = IntentIntegrator.parseActivityResult(result.getResultCode(), data);
                    produtoViewModel.codigoBarra(r.getContents(), binding.txtCodigoBar);
                } else {
                    Toast.makeText(requireActivity(), R.string.scaner_code_bar_cancelado, Toast.LENGTH_SHORT).show();
                }
            });

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

