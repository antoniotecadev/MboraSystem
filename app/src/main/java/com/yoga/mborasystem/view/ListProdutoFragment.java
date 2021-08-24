package com.yoga.mborasystem.view;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentProdutoListBinding;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ListProdutoFragment extends Fragment {

    private Bundle bundle;
    private GroupAdapter adapter;
    private ProdutoViewModel produtoViewModel;
    private FragmentProdutoListBinding binding;
    private Long idcategoria;
    private String categoria;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        bundle = new Bundle();
        adapter = new GroupAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProdutoListBinding.inflate(inflater, container, false);
        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);
        binding.recyclerViewListaProduto.setAdapter(adapter);
        binding.recyclerViewListaProduto.setHasFixedSize(true);
        binding.recyclerViewListaProduto.setLayoutManager(new LinearLayoutManager(getContext()));

//        ProgressBar progressBar = binding.viewStub.inflate().findViewById(R.id.spin_kit);
//        Sprite fadingCircle = new FadingCircle();
//        progressBar.setIndeterminateDrawable(fadingCircle);

        if (getArguments() != null) {
            long idcategoria = getArguments().getLong("idcategoria");
            String categoria = getArguments().getString("categoria");
            this.idcategoria = idcategoria;
            this.categoria = categoria;
            getActivity().setTitle(this.categoria);
        }
        produtoViewModel.consultarProdutos(this.idcategoria, false, null);
        produtoViewModel.getListaProdutos().observe(getViewLifecycleOwner(), new Observer<List<Produto>>() {
            @Override
            public void onChanged(List<Produto> produtos) {
                adapter.clear();
                if (produtos.isEmpty()) {
                    Ultilitario.naoEncontrado(getContext(), adapter, R.string.produto_nao_encontrada);
                } else {
                    for (Produto produto : produtos) {
                        adapter.add(new ProdutoListPageAdapter(produto, getContext()));
                    }
                }
            }
        });
        produtoViewModel.consultarQuantidadeProduto(idcategoria).observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                binding.chipQuantidadeProduto.setText(integer + "");
            }
        });
        binding.btnCriarProdutoFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!idcategoria.equals("") && !categoria.isEmpty()) {
                    createProduto(idcategoria, categoria);
                }
            }
        });
        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> {
                    produtoViewModel.consultarProdutos(idcategoria, false, binding.mySwipeRefreshLayout);
                }
        );
        return binding.getRoot();
    }

    private void createProduto(long id, String categoria) {
        bundle.clear();
        bundle.putLong("idcategoria", id);
        bundle.putString("categoria", categoria);
        Navigation.findNavController(getView()).navigate(R.id.action_listProdutoFragment_to_dialogCriarProduto, bundle);
    }

    private void filtrarProduto(long id) {
        bundle.clear();
        bundle.putLong("idcategoria", id);
        Navigation.findNavController(getView()).navigate(R.id.action_listProdutoFragment_to_dialogFiltrarProduto2, bundle);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_pesquisa_criar_produto, menu);
        if (getArguments() != null) {
            if (!getArguments().getBoolean("master")) {
                menu.findItem(R.id.dialogCriarProduto).setVisible(false);
                binding.btnCriarProdutoFragment.setVisibility(View.GONE);
            }
        }
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
                produtoViewModel.consultarProdutos(idcategoria, false, binding.mySwipeRefreshLayout);
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
                    produtoViewModel.consultarProdutos(idcategoria, false, binding.mySwipeRefreshLayout);
                } else {
                    produtoViewModel.searchProduto(newText);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dialogCriarProduto:
                if (!idcategoria.equals("") && !categoria.isEmpty()) {
                    createProduto(idcategoria, categoria);
                }
                break;
            case R.id.dialogFiltrarProduto:
                if (!idcategoria.equals("")) {
                    filtrarProduto(idcategoria);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ProdutoListPageAdapter extends Item<GroupieViewHolder> {

        private Produto produto;
        private Context context;
        private Switch estadoProduto;
        private CardView entrarProduto;
        private TextView nomeProduto, precoProduto, quantidadeProduto, codigoBarraProduto, referenciaProduto, precoProdutoFronecedor;

        public ProdutoListPageAdapter(Produto produto, Context context) {
            this.produto = produto;
            this.context = context;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            nomeProduto = viewHolder.itemView.findViewById(R.id.txtNomeProduto);
            precoProduto = viewHolder.itemView.findViewById(R.id.txtPrecoProduto);
            precoProdutoFronecedor = viewHolder.itemView.findViewById(R.id.txtPrecoProdutoFornecedor);
            quantidadeProduto = viewHolder.itemView.findViewById(R.id.txtQuantidadeProduto);
            codigoBarraProduto = viewHolder.itemView.findViewById(R.id.txtCodigoBarProduto);
            estadoProduto = viewHolder.itemView.findViewById(R.id.estado_produto);
            referenciaProduto = viewHolder.itemView.findViewById(R.id.txtReferenciaProduto);
            entrarProduto = viewHolder.itemView.findViewById(R.id.btnEntrar);
            nomeProduto.setText(produto.getNome());
            precoProduto.setText(context.getText(R.string.preco) + ": " + Ultilitario.formatPreco(String.valueOf(produto.getPreco())));
            precoProdutoFronecedor.setText(context.getText(R.string.preco_fornecedor) + ": " + Ultilitario.formatPreco(String.valueOf(produto.getPrecofornecedor())));
            quantidadeProduto.setText(context.getText(R.string.quantidade) + ": " + produto.getQuantidade());
            codigoBarraProduto.setText(context.getText(R.string.codigo_bar) + ": " + produto.getCodigoBarra());
            referenciaProduto.setText(context.getText(R.string.referencia) + ": MSP" + produto.getId());
            if (produto.getEstado() == Ultilitario.UM) {
                estadoProduto.setChecked(true);
                estadoProduto.setTextColor(Color.BLUE);
                estadoProduto.setText(context.getString(R.string.estado_desbloqueado));
            } else {
                estadoProduto.setChecked(false);
                estadoProduto.setTextColor(Color.RED);
                estadoProduto.setText(context.getString(R.string.estado_bloqueado));
            }
            entrarProduto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    entrarProduto();
                }

                private void entrarProduto() {
                    bundle.clear();
                    bundle.putParcelable("produto", produto);
                    bundle.putBoolean("master", getArguments().getBoolean("master"));
                    Navigation.findNavController(getView()).navigate(R.id.action_listProdutoFragment_to_dialogCriarProduto, bundle);
                }
            });
        }

        @Override
        public int getLayout() {
            return R.layout.fragment_produto;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (bundle != null) {
            bundle.clear();
        }
    }
}
