package com.yoga.mborasystem.view;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentVendaListBinding;
import com.yoga.mborasystem.model.entidade.Venda;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.VendaViewModel;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

public class VendaFragment extends Fragment {

    private Venda venda;
    private GroupAdapter adapter;
    private VendaViewModel vendaViewModel;
    private FragmentVendaListBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GroupAdapter();
        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentVendaListBinding.inflate(inflater, container, false);

        binding.recyclerViewListaVenda.setAdapter(adapter);
        binding.recyclerViewListaVenda.setLayoutManager(new LinearLayoutManager(getContext()));
        vendaViewModel.consultarVendas();
        vendaViewModel.getListaVendasLiveData().observe(getViewLifecycleOwner(), vendas -> {
            adapter.clear();
            if (vendas.isEmpty()) {
                Ultilitario.naoEncontrado(getContext(), adapter, R.string.venda_nao_encontrada);
            } else {
                for (Venda venda : vendas)
                    adapter.add(new ItemVenda(venda));
            }
        });
        return binding.getRoot();
    }

    class ItemVenda extends Item<GroupieViewHolder> {

        private Venda venda;
        private TextView nomeCliente, codigoQr, quantidade, total, desconto, totalDesc, valorPago, divida, valorBase, iva, forPag, dataVenda, operador;

        public ItemVenda(Venda venda) {
            this.venda = venda;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            nomeCliente = viewHolder.itemView.findViewById(R.id.textCliente);
            codigoQr = viewHolder.itemView.findViewById(R.id.textCodBar);
            quantidade = viewHolder.itemView.findViewById(R.id.textQtProd);
            total = viewHolder.itemView.findViewById(R.id.textTotVend);
            desconto = viewHolder.itemView.findViewById(R.id.textDesc);
            totalDesc = viewHolder.itemView.findViewById(R.id.textTotDesc);
            valorPago = viewHolder.itemView.findViewById(R.id.textPago);
            divida = viewHolder.itemView.findViewById(R.id.textDivida);
            valorBase = viewHolder.itemView.findViewById(R.id.textValBas);
            iva = viewHolder.itemView.findViewById(R.id.textVaIva);
            forPag = viewHolder.itemView.findViewById(R.id.textForPag);
            dataVenda = viewHolder.itemView.findViewById(R.id.textDatVen);
            operador = viewHolder.itemView.findViewById(R.id.textOper);

            nomeCliente.setText(venda.getNome_cliente());
            codigoQr.setText(venda.getCodigo_Barra());
            quantidade.setText(String.valueOf(venda.getQuantidade()));
            total.setText(Ultilitario.formatPreco(String.valueOf(venda.getTotal_venda())));
            desconto.setText(Ultilitario.formatPreco(String.valueOf(venda.getDesconto())));
            totalDesc.setText(Ultilitario.formatPreco(String.valueOf(venda.getTotal_desconto())));
            valorPago.setText(Ultilitario.formatPreco(String.valueOf(venda.getValor_pago())));
            divida.setText(Ultilitario.formatPreco(String.valueOf(venda.getDivida())));
            valorBase.setText(Ultilitario.formatPreco(String.valueOf(venda.getValor_base())));
            iva.setText(Ultilitario.formatPreco(String.valueOf(venda.getValor_iva())));
            forPag.setText(venda.getPagamento());
            dataVenda.setText(venda.getData_cria());
            operador.setText((venda.getIdoperador() > 0 ? " MSU" + venda.getIdoperador() : " MSA" + venda.getIdoperador()));
        }

        @Override
        public int getLayout() {
            return R.layout.fragment_venda;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_venda, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.onActionViewExpanded();
        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                vendaViewModel.consultarVendas();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    vendaViewModel.consultarVendas();
                } else {
                    vendaViewModel.searchVendas(newText);
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}