package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.getPdfList;

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

import java.util.ArrayList;

public class DocumentoFragment extends Fragment {

    private GroupAdapter adapter;
    private FragmentDocumentoBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GroupAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDocumentoBinding.inflate(inflater, container, false);
        binding.recyclerViewListaDoc.setAdapter(adapter);
        getDocumentPDF("Factura", R.string.factura, R.string.fac_n_enc);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.factura:
                    getDocumentPDF("Factura", R.string.factura, R.string.fac_n_enc);
                    break;
                case R.id.relatorio:
                    getDocumentPDF("Mbora\nSystem/Relatório de venda diária", R.string.rel_dia_ven, R.string.rel_n_enc);
                    break;
                default:
                    break;
            }
            return true;
        });
        return binding.getRoot();
    }

    private void getDocumentPDF(String uriPath, int title, int msg) {
        adapter.clear();
        requireActivity().setTitle(getString(title));
        if (getPdfList(uriPath, requireContext()).isEmpty()) {
            Ultilitario.naoEncontrado(getContext(), adapter, msg);
        } else {
            for (String documento : getPdfList(uriPath, requireContext()))
                adapter.add(new ItemDocumento(documento));
        }
    }

    class ItemDocumento extends Item<GroupieViewHolder> {

        private String documento;

        public ItemDocumento(String documento) {
            this.documento = documento;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView nomeDocumento = viewHolder.itemView.findViewById(R.id.txtNomeDocumento);
            nomeDocumento.setText(documento);
        }

        @Override
        public int getLayout() {
            return R.layout.layout_documento_fragment;
        }
    }
}