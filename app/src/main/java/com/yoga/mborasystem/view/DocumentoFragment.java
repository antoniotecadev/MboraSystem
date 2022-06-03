package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.getPdfList;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentDocumentoBinding;
import com.yoga.mborasystem.util.Ultilitario;

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
                adapter.add(new ItemDocumento(documento));
        }
    }

    static class ItemDocumento extends Item<GroupieViewHolder> {

        private final Ultilitario.Documento documento;

        public ItemDocumento(Ultilitario.Documento documento) {
            this.documento = documento;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView nomeDocumento = viewHolder.itemView.findViewById(R.id.txtNomeDocumento);
            TextView descricao = viewHolder.itemView.findViewById(R.id.txtDescricao);
            nomeDocumento.setText(documento.getNome());
            descricao.setText(Ultilitario.converterData(documento.getData_modifica()) + " - " + formatSize(documento.getTamanho()));
        }

        @Override
        public int getLayout() {
            return R.layout.layout_documento_fragment;
        }

        public String formatSize(long size) {
            if (size <= 0)
                return "0";
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }
    }
}