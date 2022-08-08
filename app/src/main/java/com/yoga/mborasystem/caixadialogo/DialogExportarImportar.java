
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
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogExportarImportarBinding;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.CategoriaProdutoViewModel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DialogExportarImportar extends DialogFragment {

    private AlertDialog dialog;
    private DialogExportarImportarBinding binding;
    private ArrayList<Integer> estado;
    private ArrayList<String> categorias, descricoes;
    private CategoriaProdutoViewModel categoriaProdutoViewModel;
    private StringBuilder data;

    private ExecutorService executor;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        data = new StringBuilder();
        binding = DialogExportarImportarBinding.inflate(getLayoutInflater());
        categoriaProdutoViewModel = new ViewModelProvider(requireActivity()).get(CategoriaProdutoViewModel.class);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setIcon(R.drawable.ic_baseline_insert_drive_file_24);
        if (getArguments() != null) {
            switch (getArguments().getInt("typeoperation")) {
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

    private void viewItemCategoria(int textButton, boolean isExport) {
        if (isExport)
            visibleGone(View.VISIBLE);
        else
            visibleGone(View.GONE);
        binding.btnExportarImportarNuvem.setText(getString(textButton));
    }

    private void visibleGone(int view) {
        binding.btnExportarImportarLocal.setVisibility(view);
    }

    private void importarExportar(boolean isLocal) {
        if (getArguments() != null) {
            estado = getArguments().getIntegerArrayList("estado");
            categorias = getArguments().getStringArrayList("categorias");
            descricoes = getArguments().getStringArrayList("descricao");
            switch (getArguments().getInt("typeoperation")) {
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
            data.append(categorias.get(i)).append(",").append(descricoes.get(i)).append(",").append(estado.get(i)).append("\n");
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
            List<String> categorias = new ArrayList<>();
            try (InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    categorias.add(line);
                }
            } catch (FileNotFoundException e) {
                handler.post(() -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
            }
            categoriaProdutoViewModel.crud = true;
            categoriaProdutoViewModel.importarCategorias(categorias, handler, dialog);
        });
    }

    ActivityResultLauncher<Intent> importCategoryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri uri;
                    if (data != null) {
                        uri = data.getData();
                        new AlertDialog.Builder(requireContext())
                                .setIcon(R.drawable.ic_baseline_insert_drive_file_24)
                                .setTitle(getString(R.string.importar))
                                .setMessage(uri.getPath())
                                .setNegativeButton(getString(R.string.cancelar), (dialogInterface, i) -> dialogInterface.dismiss())
                                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                                    try {
                                        readTextFromUri(uri);
                                    } catch (IOException e) {
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                })
                                .show();
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
