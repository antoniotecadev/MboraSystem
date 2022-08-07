
package com.yoga.mborasystem.caixadialogo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogExportarImportarBinding;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.CategoriaProdutoViewModel;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogExportarImportar extends DialogFragment {

    private AlertDialog dialog;
    private ProdutoViewModel produtoViewModel;
    private DialogExportarImportarBinding binding;
    private ArrayList<String> categorias, descricoes;
    private CategoriaProdutoViewModel categoriaProdutoViewModel;
    private StringBuilder data;

    private ExecutorService executor;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        data = new StringBuilder();
        binding = DialogExportarImportarBinding.inflate(getLayoutInflater());
        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);
        categoriaProdutoViewModel = new ViewModelProvider(requireActivity()).get(CategoriaProdutoViewModel.class);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setIcon(R.drawable.ic_baseline_store_24);
        if (getArguments() != null) {
            categorias = getArguments().getStringArrayList("categorias");
            ArrayAdapter<String> adapterCategorias = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categorias);
            binding.spinnerCategoria.setAdapter(adapterCategorias);
            switch (getArguments().getInt("typeoperation")) {
                case Ultilitario.EXPORTAR_PRODUTO:
                    builder.setTitle(getString(R.string.exportar_produto));
                    viewItemProduto(R.string.exportar_nuvem, false);
                    break;
                case Ultilitario.IMPORTAR_PRODUTO:
                    builder.setTitle(getString(R.string.importar_produto));
                    viewItemProduto(R.string.importar, true);
                    break;
                case Ultilitario.EXPORTAR_CATEGORIA:
                    builder.setTitle(getString(R.string.exportar_categoria));
                    viewItemCategoria(R.string.exportar_nuvem, true);
                    break;
                case Ultilitario.IMPORTAR_CATEGORIA:
                    builder.setTitle(getString(R.string.importar_categoria));
                    viewItemCategoria(R.string.importar, false);
                    break;
                default:
                    break;
            }
        }
        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        binding.btnExportarImportarLocal.setOnClickListener(v -> importarExportar(true));
        binding.btnExportarImportarNuvem.setOnClickListener(v -> importarExportar(false));
        binding.buttonFechar.setOnClickListener(v -> dialog.dismiss());
        return dialog;
    }

    private void viewItemProduto(int textButton, boolean isImport) {
        if (isImport) {
            visibleGone(View.GONE, false);
        } else {
            visibleGone(View.VISIBLE, true);
        }
        binding.btnExportarImportarNuvem.setText(getString(textButton));
    }

    private void viewItemCategoria(int textButton, boolean isExport) {
        binding.textView.setVisibility(View.GONE);
        if (isExport) {
            visibleGone(View.VISIBLE, false);
        } else {
            visibleGone(View.GONE, false);
        }
        binding.btnExportarImportarNuvem.setText(getString(textButton));
    }

    private void visibleGone(int view, boolean isSpinnerCategoria) {
        binding.btnExportarImportarLocal.setVisibility(view);
        if (isSpinnerCategoria) {
            binding.textView.setVisibility(view);
            binding.spinnerCategoria.setVisibility(View.VISIBLE);
        } else {
            binding.textView.setVisibility(View.GONE);
            binding.spinnerCategoria.setVisibility(View.GONE);
        }
    }

    private void importarExportar(boolean isLocal) {
        if (getArguments() != null) {
            categorias = getArguments().getStringArrayList("categorias");
            descricoes = getArguments().getStringArrayList("descricao");
            switch (getArguments().getInt("typeoperation")) {
                case Ultilitario.EXPORTAR_PRODUTO:
                    String[] idcategoria = TextUtils.split(binding.spinnerCategoria.getSelectedItem().toString(), "-");
                    Ultilitario.isLocal = isLocal;
                    Ultilitario.categoria = idcategoria[1].trim();
                    produtoViewModel.consultarProdutos(Long.parseLong(idcategoria[0].trim()), true, null, false);
                    dialog.dismiss();
                    break;
                case Ultilitario.IMPORTAR_PRODUTO:
                    importarProdutos();
                    dialog.dismiss();
                    break;
                case Ultilitario.EXPORTAR_CATEGORIA:
                    exportarCategorias(isLocal);
                    break;
                case Ultilitario.IMPORTAR_CATEGORIA:
                    importarCategorias();
                    break;
                default:
                    break;
            }
        }
    }

    private void exportarCategorias(boolean isLocal) {
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < categorias.size(); i++) {
            data.append(categorias.get(i).split("-")[1]).append(",").append(descricoes.get(i)).append("\n");
        }
        this.data = data;
        if (isLocal) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Ultilitario.exportarLocal(exportCategoryActivityResultLauncher, getActivity(), "categorias", Ultilitario.getDateCurrent());
            } else {
                Ultilitario.alertDialog(getString(R.string.avs), getString(R.string.exp_dis_api_sup), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
        } else {
            Ultilitario.exportarNuvem(getContext(), data, "categorias.csv", "categorias", Ultilitario.getDateCurrent());
        }
    }

    private void importarProdutos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Ultilitario.importarCategoriasProdutosClientes(null, getActivity());
        } else {
            Ultilitario.alertDialog(getString(R.string.avs), getString(R.string.imp_dis_api_sup), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
        }
    }

    private void importarCategorias() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Ultilitario.importarCategoriasProdutosClientes(importCategoryActivityResultLauncher, null);
        } else {
            Ultilitario.alertDialog(getString(R.string.avs), getString(R.string.imp_dis_api_sup), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void readTextFromUri(Uri uri) throws IOException {
        executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            Map<String, String> categorias = new HashMap<>();
            try (InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] categoria = line.split(",");
                    categorias.put(categoria[0], categoria[1]);
                }
            } catch (FileNotFoundException e) {
                handler.post(() -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
            }
            categoriaProdutoViewModel.crud = true;
            categoriaProdutoViewModel.importarCategorias(categorias, handler);
        });
    }

    ActivityResultLauncher<Intent> importCategoryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri uri;
                    if (data != null) {
                        uri = data.getData();
                        try {
                            readTextFromUri(uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

    ActivityResultLauncher<Intent> exportCategoryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent resultData = result.getData();
                    Uri uri;
                    if (resultData != null) {
                        uri = resultData.getData();
                        Ultilitario.alterDocument(uri, data, requireActivity());
                        data.delete(0, data.length());
                    }
                }
            });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (executor != null)
            executor.shutdownNow();
    }
}
