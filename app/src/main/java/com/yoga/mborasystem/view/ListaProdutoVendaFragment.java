package com.yoga.mborasystem.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentListaProdutoVendaBinding;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.util.EventObserver;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.VendaViewModel;
@SuppressWarnings("rawtypes")
public class ListaProdutoVendaFragment extends Fragment {

    private GroupAdapter adapter;
    private VendaViewModel vendaViewModel;
    private FragmentListaProdutoVendaBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GroupAdapter();
        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListaProdutoVendaBinding.inflate(inflater, container, false);

        long idvenda = ListaProdutoVendaFragmentArgs.fromBundle(getArguments()).getIdvenda();
        int vendaTotal = ListaProdutoVendaFragmentArgs.fromBundle(getArguments()).getVendaTotal();
        int quant = ListaProdutoVendaFragmentArgs.fromBundle(getArguments()).getQuant();
        String codQr = ListaProdutoVendaFragmentArgs.fromBundle(getArguments()).getCodQr();

        requireActivity().setTitle(getString(R.string.total) + ": " + Ultilitario.formatPreco(String.valueOf(vendaTotal)));

        binding.recyclerViewListaProduto.setAdapter(adapter);
        binding.recyclerViewListaProduto.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.chipQuantidadeProduto.setText(quant + "");

        vendaViewModel.getProdutosVenda(idvenda, codQr, null, false);
        vendaViewModel.getProdutosVendaLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(produtos -> {
            adapter.clear();
            if (produtos.isEmpty()) {
                Ultilitario.naoEncontrado(getContext(), adapter, R.string.produto_nao_encontrada);
            } else {
                for (ProdutoVenda produto : produtos)
                    adapter.add(new Item<GroupieViewHolder>() {

                        @SuppressLint("UseSwitchCompatOrMaterialCode")
                        private SwitchCompat estadoProduto;

                        @Override
                        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
                            TextView nomeProduto = viewHolder.itemView.findViewById(R.id.txtNomeProduto);
                            TextView precoProduto = viewHolder.itemView.findViewById(R.id.txtPrecoProduto);
                            TextView precoProdutoFronecedor = viewHolder.itemView.findViewById(R.id.txtPrecoProdutoFornecedor);
                            TextView quantidadeProduto = viewHolder.itemView.findViewById(R.id.txtQuantidadeProduto);
                            TextView referenciaProduto = viewHolder.itemView.findViewById(R.id.txtCodigoBarProduto);
                            estadoProduto = viewHolder.itemView.findViewById(R.id.estado_produto);
                            TextView codigoQr = viewHolder.itemView.findViewById(R.id.txtReferenciaProduto);

                            nomeProduto.setText(produto.getNome_produto());
                            precoProduto.setText(getText(R.string.total) + ": " + Ultilitario.formatPreco(String.valueOf(produto.getPreco_total())));
                            precoProdutoFronecedor.setText(getString(R.string.preco_fornecedor) + ": " + Ultilitario.formatPreco(String.valueOf(produto.getPreco_fornecedor())));
                            quantidadeProduto.setText(getText(R.string.quantidade) + ": " + produto.getQuantidade());
                            referenciaProduto.setText(getText(R.string.referencia) + ": MSP" + produto.getId());
                            codigoQr.setText(getText(R.string.venda) + "Qr: " + produto.getCodigo_Barra());
                            if (produto.isIva()) {
                                estadoProduto.setChecked(true);
                                estadoProduto.setTextColor(Color.BLUE);
                                estadoProduto.setText(getText(R.string.montante_iva));
                            } else {
                                estadoProduto.setChecked(false);
                                estadoProduto.setTextColor(Color.GRAY);
                                estadoProduto.setText(getString(R.string.sem_iva));
                            }
                        }

                        @Override
                        public int getLayout() {
                            return R.layout.fragment_produto;
                        }
                    });
            }
        }));
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}