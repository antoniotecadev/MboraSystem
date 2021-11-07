package com.yoga.mborasystem.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentProdutoListBinding;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ListProdutoFragment extends Fragment {

    private Bundle bundle;
    private Long idcategoria;
    private String categoria;
    private boolean isLixeira;
    private GroupAdapter adapter;
    private ProdutoViewModel produtoViewModel;
    private FragmentProdutoListBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        bundle = new Bundle();

        adapter = new GroupAdapter();
        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProdutoListBinding.inflate(inflater, container, false);

        isLixeira = CategoriaProdutoFragmentArgs.fromBundle(getArguments()).getIsLixeira();

        binding.recyclerViewListaProduto.setAdapter(adapter);
        binding.recyclerViewListaProduto.setHasFixedSize(true);
        binding.recyclerViewListaProduto.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            long idcategoria = getArguments().getLong("idcategoria");
            String categoria = getArguments().getString("categoria");
            this.idcategoria = idcategoria;
            this.categoria = categoria;
            requireActivity().setTitle(this.categoria);
        }

        if (isLixeira) {
            requireActivity().setTitle(getString(R.string.lix) + " (" + getString(R.string.prod) + ")");
            binding.btnCriarProdutoFragment.setVisibility(View.INVISIBLE);
        }


        produtoViewModel.consultarProdutos(this.idcategoria, false, null, isLixeira);
        produtoViewModel.getListaProdutos().observe(getViewLifecycleOwner(), produtos -> {
            binding.chipQuantidadeProduto.setText(produtos.size() + "");
            adapter.clear();
            if (produtos.isEmpty()) {
                Ultilitario.naoEncontrado(getContext(), adapter, R.string.produto_nao_encontrada);
            } else {
                for (Produto produto : produtos) {
                    adapter.add(new ProdutoListPageAdapter(produto, getContext()));
                }
            }
        });
//        produtoViewModel.consultarQuantidadeProduto(idcategoria).observe(getViewLifecycleOwner(), new Observer<Long>() {
//            @Override
//            public void onChanged(Long quant) {
//                binding.chipQuantidadeProduto.setText(quant + "");
//            }
//        });

        binding.btnCriarProdutoFragment.setOnClickListener(v -> {
            if (!categoria.isEmpty()) {
                createProduto(idcategoria, categoria);
            }
        });
        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> produtoViewModel.consultarProdutos(idcategoria, false, binding.mySwipeRefreshLayout, isLixeira)
        );

        return binding.getRoot();
    }

    private void createProduto(long id, String categoria) {
        MainActivity.getProgressBar();
        bundle.clear();
        bundle.putLong("idcategoria", id);
        bundle.putString("categoria", categoria);
        Navigation.findNavController(requireView()).navigate(R.id.action_listProdutoFragment_to_dialogCriarProduto, bundle);
    }

    private void filtrarProduto(long id) {
        MainActivity.getProgressBar();
        bundle.clear();
        bundle.putLong("idcategoria", id);
        Navigation.findNavController(requireView()).navigate(R.id.action_listProdutoFragment_to_dialogFiltrarProduto2, bundle);
    }

    private void scanearCodigoQr() {
        new IntentIntegrator(getActivity())
                .setPrompt(getString(R.string.alinhar_codigo_qr))
                .setOrientationLocked(false)
                .setCameraId(0)
                .initiateScan();
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
        if (isLixeira) {
            menu.findItem(R.id.dialogCriarProduto).setVisible(false);
            menu.findItem(R.id.dialogFiltrarProduto).setVisible(false);
            menu.findItem(R.id.btnScannerBack).setVisible(false);
        }
        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getString(R.string.prod) + " " + getString(R.string.ou) + " " + getString(R.string.codigo_bar));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.onActionViewExpanded();
        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                produtoViewModel.consultarProdutos(idcategoria, false, binding.mySwipeRefreshLayout, isLixeira);
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
                    produtoViewModel.consultarProdutos(idcategoria, false, binding.mySwipeRefreshLayout, isLixeira);
                } else {
                    produtoViewModel.searchProduto(newText, isLixeira);
                }
                return false;
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dialogCriarProduto:
                if (!categoria.isEmpty()) {
                    createProduto(idcategoria, categoria);
                }
                break;
            case R.id.dialogFiltrarProduto:
                filtrarProduto(idcategoria);
                break;
            case R.id.btnScannerBack:
                scanearCodigoQr();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ProdutoListPageAdapter extends Item<GroupieViewHolder> {

        private final Produto produto;
        private final Context context;
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        private Switch estadoProduto;

        public ProdutoListPageAdapter(Produto produto, Context context) {
            this.produto = produto;
            this.context = context;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView nomeProduto = viewHolder.itemView.findViewById(R.id.txtNomeProduto);
            TextView precoProduto = viewHolder.itemView.findViewById(R.id.txtPrecoProduto);
            TextView precoProdutoFronecedor = viewHolder.itemView.findViewById(R.id.txtPrecoProdutoFornecedor);
            TextView quantidadeProduto = viewHolder.itemView.findViewById(R.id.txtQuantidadeProduto);
            TextView codigoBarraProduto = viewHolder.itemView.findViewById(R.id.txtCodigoBarProduto);
            estadoProduto = viewHolder.itemView.findViewById(R.id.estado_produto);
            TextView referenciaProduto = viewHolder.itemView.findViewById(R.id.txtReferenciaProduto);
            CardView entrarProduto = viewHolder.itemView.findViewById(R.id.btnEntrar);
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
            if (isLixeira) {
                entrarProduto.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                    menu.setHeaderTitle(produto.getNome());
                    menu.add(getString(R.string.rest)).setOnMenuItemClickListener(item -> {
                        restaurarProduto();
                        return false;
                    });
                    menu.add(getString(R.string.eliminar)).setOnMenuItemClickListener(item -> {
                        dialogEliminarProduto();
                        return false;
                    });
                });
            } else {
                entrarProduto.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                    menu.setHeaderTitle(produto.getNome());
                    menu.add(getString(R.string.editar)).setOnMenuItemClickListener(item -> {
                        entrarProduto();
                        return false;
                    });
                });
                entrarProduto.setOnClickListener(v -> entrarProduto());
            }
        }

        private void entrarProduto() {
            bundle.clear();
            bundle.putParcelable("produto", produto);
            bundle.putBoolean("master", requireArguments().getBoolean("master"));
            Navigation.findNavController(requireView()).navigate(R.id.action_listProdutoFragment_to_dialogCriarProduto, bundle);
        }

        private void restaurarProduto() {
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.rest) + " (" + produto.getNome() + ")")
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> produtoViewModel.restaurarProduto(Ultilitario.UM, produto.getId()))
                    .show();
        }

        private void dialogEliminarProduto() {
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.eliminar) + " (" + produto.getNome() + ")")
                    .setMessage(getString(R.string.tem_certeza_eliminar_produto))
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> {
                        produtoViewModel.eliminarProduto(produto, false, null);
                    })
                    .show();
        }

        @Override
        public int getLayout() {
            return R.layout.fragment_produto;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, resultData);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(getContext(), R.string.scaner_cod_bar_cancel, Toast.LENGTH_LONG).show();
                } else {
                    produtoViewModel.searchProduto(result.getContents(), isLixeira);
                }
            } else {
                super.onActivityResult(requestCode, resultCode, resultData);
            }
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

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }
}
