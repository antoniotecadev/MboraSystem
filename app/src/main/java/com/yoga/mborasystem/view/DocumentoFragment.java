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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentDocumentoBinding;
import com.yoga.mborasystem.util.Ultilitario;

import java.io.File;
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

    private void getDocumentPDF(String uriPath, int title, int msg) {
        List<Ultilitario.Documento> pdfList = new ArrayList<>();
        pdfList.addAll(getPdfList(uriPath, requireContext()));
        binding.chipQuantDoc.setText(String.valueOf(pdfList.size()));
        adapter.clear();
        requireActivity().setTitle(getString(title));
        if (pdfList.isEmpty()) {
            Ultilitario.naoEncontrado(getContext(), adapter, msg);
        } else {
            for (Ultilitario.Documento documento : pdfList)
                adapter.add(new ItemDocumento(documento, requireContext()));
        }
    }

    static class ItemDocumento extends Item<GroupieViewHolder> {

        private Context context;
        private final Ultilitario.Documento documento;

        public ItemDocumento(Ultilitario.Documento documento, Context context) {
            this.documento = documento;
            this.context = context;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView nomeDocumento = viewHolder.itemView.findViewById(R.id.txtNomeDocumento);
            TextView descricao = viewHolder.itemView.findViewById(R.id.txtDescricao);
            nomeDocumento.setText(documento.getNome());
            descricao.setText(Ultilitario.converterData(documento.getData_modifica()) + " - " + formatSize(documento.getTamanho()));
            viewHolder.itemView.setOnClickListener(v -> {
                abrirDocumentoPDF(v);
            });
        }

        @Override
        public int getLayout() {
            return R.layout.layout_documento_fragment;
        }

        private void abrirDocumentoPDF(View v) {
            v.setBackgroundColor(Color.parseColor("#6BD3D8D7"));
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                v.setBackgroundColor(Color.WHITE);
            }, 1000);
            File file = new File(documento.getCaminho());
            Uri fileURI = FileProvider.getUriForFile(context, "com.yoga.mborasystem", file);
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