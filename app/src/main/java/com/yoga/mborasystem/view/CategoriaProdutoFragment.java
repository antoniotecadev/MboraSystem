package com.yoga.mborasystem.view;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentCategoriaProdutoBinding;
import com.yoga.mborasystem.model.entidade.Categoria;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.util.EventObserver;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.CategoriaProdutoViewModel;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class CategoriaProdutoFragment extends Fragment {

    private Bundle bundle;
    private StringBuilder data;
    private GroupAdapter adapter;
    private ProdutoViewModel produtoViewModel;
    private FragmentCategoriaProdutoBinding binding;
    private ArrayList<String> stringList, stringListDesc;
    private CategoriaProdutoViewModel categoriaProdutoViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = new Bundle();
        data = new StringBuilder();
        adapter = new GroupAdapter();
        stringList = new ArrayList<>();
        stringListDesc = new ArrayList<>();
        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);
        categoriaProdutoViewModel = new ViewModelProvider(requireActivity()).get(CategoriaProdutoViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentCategoriaProdutoBinding.inflate(inflater, container, false);
        binding.recyclerViewCategoriaProduto.setAdapter(adapter);
        binding.recyclerViewCategoriaProduto.setHasFixedSize(true);
        binding.recyclerViewCategoriaProduto.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.btncriarCategoriaDialog.setOnClickListener(v -> createCategory());

        categoriaProdutoViewModel.getListaCategorias().observe(getViewLifecycleOwner(), new Observer<List<Categoria>>() {
            @Override
            public void onChanged(List<Categoria> categorias) {
                adapter.clear();
                if (categorias.isEmpty()) {
                    Ultilitario.naoEncontrado(getContext(), adapter, R.string.categoria_nao_encontrada);
                } else {
                    stringList.clear();
                    for (Categoria categoria : categorias) {
                        stringList.add(categoria.getId() + " - " + categoria.getCategoria());
                        stringListDesc.add(categoria.getDescricao());
                        adapter.add(new ItemCategoria(categoria));
                    }
                }
            }
        });
        produtoViewModel.getListaProdutosImport().observe(getViewLifecycleOwner(), new EventObserver<>(prod -> {
            StringBuilder dt = new StringBuilder();
            if (prod.isEmpty()) {
                Ultilitario.showToast(getContext(), Color.rgb(254, 207, 65), getString(R.string.produto_nao_encontrado), R.drawable.ic_toast_erro);
            } else {
                for (Produto produto : prod) {
                    dt.append(produto.getNome() + "," + produto.getPreco() + "," + produto.getPrecofornecedor() + "," + produto.getQuantidade() + "," + produto.getCodigoBarra() + "," + produto.isIva() + "," + produto.getEstado() + "," + produto.getIdcategoria() + "\n");
                }
                data = dt;
                exportarProdutos("produtos.csv", Ultilitario.categoria, Ultilitario.isLocal);
            }
        }));
        binding.mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        categoriaProdutoViewModel.consultarCategorias(binding.mySwipeRefreshLayout);
                    }
                }
        );
        return binding.getRoot();
    }

    private void exportarProdutos(String ficheiro, String nomeFicheiro, boolean isLocal) {
        if (isLocal) {
            Ultilitario.exportarLocal(getActivity(), data, ficheiro, nomeFicheiro, Ultilitario.getDateCurrent(), Ultilitario.CREATE_FILE_PRODUTO);
        } else {
            Ultilitario.exportarNuvem(getContext(), data, ficheiro, nomeFicheiro, Ultilitario.getDateCurrent());
        }
    }

    private void createCategory() {
        Navigation.findNavController(getView()).navigate(R.id.action_categoriaProdutoFragment_to_dialogCriarCategoria);
    }

    private void exportarImportar(int typeOperetion) {
        bundle.putInt("typeoperation", typeOperetion);
        bundle.putStringArrayList("categorias", stringList);
        bundle.putStringArrayList("descricao", stringListDesc);
        Navigation.findNavController(getView()).navigate(R.id.action_categoriaProdutoFragment_to_dialogExportarImportar, bundle);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_pesquisar_criar_categoria, menu);
        if (getArguments() != null) {
            if (getArguments().getBoolean("master")) {
                bundle.putBoolean("master", getArguments().getBoolean("master"));
            } else {
                menu.findItem(R.id.dialogCriarCategoria).setVisible(false);
                binding.btncriarCategoriaDialog.setVisibility(View.GONE);
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
                categoriaProdutoViewModel.consultarCategorias(binding.mySwipeRefreshLayout);
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
                    categoriaProdutoViewModel.consultarCategorias(binding.mySwipeRefreshLayout);
                } else {
                    categoriaProdutoViewModel.searchCategoria(newText);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(getActivity(), R.id.fragment);
        switch (item.getItemId()) {
            case R.id.exportarproduto:
                exportarImportar(Ultilitario.EXPORTAR_PRODUTO);
                break;
            case R.id.importarproduto:
                exportarImportar(Ultilitario.IMPORTAR_PRODUTO);
                break;
            case R.id.exportarcategoria:
                exportarImportar(Ultilitario.EXPORTAR_CATEGORIA);
                break;
            case R.id.importarcategoria:
                exportarImportar(Ultilitario.IMPORTAR_CATEGORIA);
                break;
            default:
                break;
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    class ItemCategoria extends Item<GroupieViewHolder> {
        private Categoria categoria;

        public ItemCategoria(Categoria categoria) {
            this.categoria = categoria;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView nome = viewHolder.itemView.findViewById(R.id.txtNomeCategoria);
            TextView descricao = viewHolder.itemView.findViewById(R.id.txtDescricao);
            ImageButton menu = viewHolder.itemView.findViewById(R.id.imgBtnMenu);
            nome.setText(categoria.getCategoria() + " | " + categoria.getId());
            descricao.setText(categoria.getDescricao());
            if (categoria.getEstado() == Ultilitario.DOIS) {
                descricao.setTextColor(Color.RED);
                descricao.setText(getString(R.string.desactivado));
            }
            registerForContextMenu(menu);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setBackgroundColor(Color.parseColor("#6BD3D8D7"));
                    listaProdutos(categoria.getId(), categoria.getCategoria());
                }
            });
            menu.setOnClickListener(v -> v.showContextMenu());
            viewHolder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.setHeaderTitle(categoria.getCategoria());
                    menu.add(getString(R.string.abrir)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            listaProdutos(categoria.getId(), categoria.getCategoria());
                            return false;
                        }
                    });//groupId, itemId, order, title
                    menu.add(getString(R.string.alterar_categoria)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (getArguments() != null) {
                                if (getArguments().getBoolean("master")) {
                                    bundle.putParcelable("categoria", categoria);
                                    Navigation.findNavController(getView()).navigate(R.id.action_categoriaProdutoFragment_to_dialogCriarCategoria, bundle);
                                }
                            }
                            return false;
                        }
                    });
                    menu.add(getString(R.string.eliminar_categoria)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (getArguments() != null) {
                                if (getArguments().getBoolean("master", false)) {
                                    categoria.setId(categoria.getId());
                                    categoria.setEstado(Ultilitario.TRES);
                                    categoria.setData_elimina(Ultilitario.getDateCurrent());
                                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                    alert.setTitle(getString(R.string.eliminar_categoria) + " (" + categoria.getCategoria() + ")");
                                    alert.setMessage(getString(R.string.tem_certeza_eliminar_categoria));
                                    alert.setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss());
                                    alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> categoriaProdutoViewModel.eliminarCategoria(categoria, true));
                                    alert.show();
                                }
                            }
                            return false;
                        }
                    });

                }
            });
        }

        @Override
        public int getLayout() {
            return R.layout.fragment_categoria;
        }

        private void listaProdutos(long id, String categoria) {
            bundle.putLong("idcategoria", id);
            bundle.putString("categoria", categoria);
            Navigation.findNavController(getView()).navigate(R.id.action_categoriaProdutoFragment_to_listProdutoFragment, bundle);
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

    public void readTextFromUri(Uri uri) throws IOException {
        List<String> produtos = new ArrayList<>();
        try (InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                produtos.add(line);
            }
            produtoViewModel.importarProdutos(produtos);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == Ultilitario.CREATE_FILE_PRODUTO && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Ultilitario.alterDocument(uri, data, getActivity());
                data.delete(0, data.length());
                Toast.makeText(getContext(), uri.toString(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Ultilitario.QUATRO && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    readTextFromUri(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
