
package com.yoga.mborasystem.caixadialogo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogExportarImportarBinding;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.CategoriaProdutoViewModel;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogExportarImportar extends DialogFragment {

    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private ProdutoViewModel produtoViewModel;
    private DialogExportarImportarBinding binding;
    private ArrayAdapter<String> adapterCategorias;
    private ArrayList<String> categorias, descricoes;
    private CategoriaProdutoViewModel categoriaProdutoViewModel;
    private StringBuilder data;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        data = new StringBuilder();
        binding = DialogExportarImportarBinding.inflate(LayoutInflater.from(getContext()));
        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);
        categoriaProdutoViewModel = new ViewModelProvider(requireActivity()).get(CategoriaProdutoViewModel.class);
        builder = new AlertDialog.Builder(getActivity());
        if (getArguments() != null) {
            categorias = getArguments().getStringArrayList("categorias");
            adapterCategorias = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categorias);
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
        binding.btnCancelar.setOnClickListener(v -> dialog.dismiss());
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
                    Ultilitario.isLocal = isLocal ? true : false;
                    Ultilitario.categoria = idcategoria[1].trim();
                    produtoViewModel.consultarProdutos(Long.parseLong(idcategoria[0].trim()), true, null);
                    dialog.dismiss();
                    break;
                case Ultilitario.IMPORTAR_PRODUTO:
                    importarProdutos();
                    dialog.dismiss();
                    break;
                case Ultilitario.EXPORTAR_CATEGORIA:
                    exportarCategorias("categorias.csv", "categorias", isLocal);
                    break;
                case Ultilitario.IMPORTAR_CATEGORIA:
                    importarCategorias();
                    break;
                default:
                    break;
            }
        }
    }

    private void exportarCategorias(String ficheiro, String nomeFicheiro, boolean isLocal) {
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < categorias.size(); i++) {
            data.append(categorias.get(i).split("-")[1] + "," + descricoes.get(i) + "\n");
        }
        this.data = data;
        if (isLocal) {
            Ultilitario.exportarLocal(getActivity(), data, ficheiro, nomeFicheiro, Ultilitario.CREATE_FILE_CATEGORIA);
        } else {
            Ultilitario.exportarNuvem(getContext(), data, ficheiro, nomeFicheiro);
        }
    }

    private void importarProdutos() {
        Ultilitario.importarCategoriasProdutos(getActivity(), Ultilitario.QUATRO);
    }

    private void importarCategorias() {
        Ultilitario.importarCategoriasProdutos(getActivity(), Ultilitario.SINCO);
    }

    public void readTextFromUri(Uri uri) throws IOException {
        Map<String, String> categorias = new HashMap<>();
        try (InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] categoria = line.split(",");
                categorias.put(categoria[0], categoria[1]);
            }
            categoriaProdutoViewModel.importarCategorias(categorias, dialog);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == Ultilitario.CREATE_FILE_CATEGORIA && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Ultilitario.alterDocument(uri, data, getActivity());
                data.delete(0, data.length());
                Toast.makeText(getContext(), uri.toString(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Ultilitario.SINCO && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    readTextFromUri(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
