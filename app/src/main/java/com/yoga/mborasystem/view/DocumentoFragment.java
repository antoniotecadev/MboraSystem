package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.getPdfList;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentDocumentoBinding;
import com.yoga.mborasystem.util.Ultilitario;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DocumentoFragment extends Fragment {

    private GroupAdapter adapter;
    private FragmentDocumentoBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GroupAdapter();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDocumentoBinding.inflate(inflater, container, false);
        binding.recyclerViewListaDoc.setAdapter(adapter);
        getDocumentPDF("Facturas", R.string.fact_vend, R.string.fac_n_enc);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.factura:
                    getDocumentPDF("Facturas", R.string.fact_vend, R.string.fac_n_enc);
                    break;
                case R.id.relatorio:
                    getDocumentPDF("Relatorios", R.string.rel_dia_ven, R.string.rel_n_enc);
                    break;
                default:
                    break;
            }
            return true;
        });
        return binding.getRoot();
    }

    private void getDocumentPDF(String pasta, int title, int msg) {
        List<Ultilitario.Documento> pdfList = new ArrayList<>();
        pdfList.addAll(getPdfList(pasta, requireContext()));
        binding.chipQuantDoc.setText(String.valueOf(pdfList.size()));
        adapter.clear();
        requireActivity().setTitle(getString(title));
        if (pdfList.isEmpty()) {
            Ultilitario.naoEncontrado(getContext(), adapter, msg);
        } else {
            for (Ultilitario.Documento documento : pdfList)
                adapter.add(new ItemDocumento(documento, requireContext(), pasta, title, msg));
        }
    }

    class ItemDocumento extends Item<GroupieViewHolder> {

        private String pasta;
        private int title, msg;
        private Context context;
        private final Ultilitario.Documento documento;

        public ItemDocumento(Ultilitario.Documento documento, Context context, String pasta, int title, int msg) {
            this.documento = documento;
            this.context = context;
            this.pasta = pasta;
            this.title = title;
            this.msg = msg;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView nomeDocumento = viewHolder.itemView.findViewById(R.id.txtNomeDocumento);
            TextView descricao = viewHolder.itemView.findViewById(R.id.txtDescricao);
            ImageButton menu = viewHolder.itemView.findViewById(R.id.imgBtnMenu);
            nomeDocumento.setText(documento.getNome());
            descricao.setText(Ultilitario.converterData(documento.getData_modifica()) + " - " + formatSize(documento.getTamanho()));
            viewHolder.itemView.setOnClickListener(v -> {
                abrirDocumentoPDF(v);
            });
            registerForContextMenu(menu);
            menu.setOnClickListener(View::showContextMenu);
            viewHolder.itemView.setOnCreateContextMenuListener((menu1, v, menuInfo) -> {
                menu1.setHeaderIcon(R.drawable.ic_baseline_store_24);
                menu1.setHeaderTitle(documento.getNome());
                menu1.add(getString(R.string.abrir)).setOnMenuItemClickListener(item -> {
                    abrirDocumentoPDF(v);
                    return false;
                });
                menu1.add(getString(R.string.eliminar)).setOnMenuItemClickListener(item -> {
                    File file = new File(documento.getCaminho());
                    file.delete();
                    if (file.exists()) {
                        try {
                            file.getCanonicalFile().delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        if (file.exists()) {
                            requireContext().deleteFile(file.getName());
                        } else {
                            Snackbar.make(v, documento.getNome() + " " + getString(R.string.elmnd), Snackbar.LENGTH_LONG).show();
                            getDocumentPDF(pasta, title, msg);
                        }
                    } else {
                        Snackbar.make(v, documento.getNome() + " " + getString(R.string.elmnd), Snackbar.LENGTH_LONG).show();
                        getDocumentPDF(pasta, title, msg);
                    }
                    return false;
                });
            });
        }

        @Override
        public int getLayout() {
            return R.layout.layout_documento_fragment;
        }

        private void abrirDocumentoPDF(View v) {
            Uri fileURI;
            v.setBackgroundColor(Color.parseColor("#6BD3D8D7"));
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                v.setBackgroundColor(Color.WHITE);
            }, 1000);
            File file = new File(documento.getCaminho());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileURI = FileProvider.getUriForFile(context, "com.yoga.mborasystem", file);
            } else {
                fileURI = Uri.fromFile(file);
            }
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(fileURI, "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent intent = Intent.createChooser(target, context.getString(R.string.ab_fi));

            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            try {
                if (activities.size() > 0) {
                    context.startActivity(intent);
                } else {
                    Ultilitario.alertDialog(context.getString(R.string.fal_ab_pdf), context.getString(R.string.inst_app), context, R.drawable.ic_baseline_store_24);
                }
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        private String formatSize(long size) {
            if (size <= 0)
                return "0";
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }
    }
}