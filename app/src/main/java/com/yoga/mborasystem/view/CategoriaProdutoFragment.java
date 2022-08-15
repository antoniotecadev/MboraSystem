package com.yoga.mborasystem.view;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.xwray.groupie.GroupAdapter;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentCategoriaBinding;
import com.yoga.mborasystem.databinding.FragmentCategoriaProdutoBinding;
import com.yoga.mborasystem.model.entidade.Categoria;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.CategoriaProdutoViewModel;

@SuppressWarnings("rawtypes")
public class CategoriaProdutoFragment extends Fragment {

    private boolean vazio;
    private Bundle bundle;
    private int quantidade;
    private GroupAdapter adapter;
    private boolean isLixeira, isMaster;
    private FragmentCategoriaProdutoBinding binding;
    private CategoriaProdutoViewModel categoriaProdutoViewModel;
    private CategoriaAdapter categoriaAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = new Bundle();
        adapter = new GroupAdapter();
        categoriaAdapter = new CategoriaAdapter(new CategoriaComparator());
        categoriaProdutoViewModel = new ViewModelProvider(requireActivity()).get(CategoriaProdutoViewModel.class);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCategoriaProdutoBinding.inflate(inflater, container, false);

        isLixeira = CategoriaProdutoFragmentArgs.fromBundle(getArguments()).getIsLixeira();
        isMaster = CategoriaProdutoFragmentArgs.fromBundle(getArguments()).getIsMaster();

        if (isLixeira) {
            requireActivity().setTitle(getString(R.string.lix) + " (" + getString(R.string.cat) + ")");
            binding.btncriarCategoriaDialog.setVisibility(View.GONE);
        }

        recyclerViewConfig(binding.recyclerViewCategoriaProduto, categoriaAdapter);
        binding.btncriarCategoriaDialog.setOnClickListener(v -> criarCategoria());

        categoriaProdutoViewModel.getQuantidadeCategoria(isLixeira).observe(getViewLifecycleOwner(), quantidade -> {
            this.quantidade = quantidade.intValue();
            vazio = quantidade == 0;
            binding.chipQuantidadeCategoria.setText(String.valueOf(quantidade));
            binding.recyclerViewCategoriaProduto.setAdapter(quantidade == 0 ? Ultilitario.naoEncontrado(getContext(), adapter, R.string.produto_nao_encontrada) : categoriaAdapter);
        });

        consultarCategorias(false, null, false);
        categoriaProdutoViewModel.getListaCategorias().observe(getViewLifecycleOwner(), categorias -> {
            categoriaAdapter.submitData(getLifecycle(), categorias);
            Ultilitario.swipeRefreshLayout(binding.mySwipeRefreshLayout);
        });
        binding.floatingActionButtonCima.setOnClickListener(view -> binding.recyclerViewCategoriaProduto.smoothScrollToPosition(0));
        binding.floatingActionButtonBaixo.setOnClickListener(view -> binding.recyclerViewCategoriaProduto.smoothScrollToPosition(quantidade));
        binding.switchOcultarFloatCimaBaixo.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                ocultarFloatButtonCimaBaixo(true, View.GONE);
            else
                ocultarFloatButtonCimaBaixo(false, View.VISIBLE);
        });
        binding.switchOcultarFloatCimaBaixo.setChecked(Ultilitario.getBooleanPreference(requireContext(), "categoria"));
        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> {
            consultarCategorias(false, null, false);
            categoriaAdapter.notifyDataSetChanged();
        });
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_pesquisar_criar_categoria, menu);
                if (getArguments() != null) {
                    if (getArguments().getBoolean("master")) {
                        bundle.putBoolean("master", getArguments().getBoolean("master"));
                    } else {
                        menu.findItem(R.id.dialogCriarCategoria).setVisible(false);
                        menu.findItem(R.id.exinpCategoria).setVisible(false);
                        binding.btncriarCategoriaDialog.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
                }
                if (isLixeira) {
                    menu.findItem(R.id.exinpCategoria).setVisible(false);
                } else {
                    menu.findItem(R.id.btnEliminarTodosLixo).setVisible(false);
                    menu.findItem(R.id.btnRestaurarTodosLixo).setVisible(false);
                }
                SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
                MenuItem menuItem = menu.findItem(R.id.app_bar_search);
                SearchView searchView = (SearchView) menuItem.getActionView();
                searchView.setQueryHint(getString(R.string.cat));
                searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
                searchView.onActionViewExpanded();
                menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        consultarCategorias(false, null, false);
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
                            consultarCategorias(false, null, false);
                        } else {
                            consultarCategorias(true, newText, true);
                        }
                        return false;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
                int itemId = menuItem.getItemId();
                if (itemId == R.id.dialogCriarCategoria) {
                    Navigation.findNavController(requireView()).navigate(R.id.action_categoriaProdutoFragment_to_dialogCriarCategoria);
                } else if (itemId == R.id.exportarcategoria) {
                    exportarImportar(Ultilitario.EXPORTAR_CATEGORIA);
                } else if (itemId == R.id.importarcategoria) {
                    exportarImportar(Ultilitario.IMPORTAR_CATEGORIA);
                } else if (itemId == R.id.btnEliminarTodosLixo) {
                    dialogEliminarReataurarTodasCategoriasLixeira(getString(R.string.elim_cts), getString(R.string.tem_cert_elim_cts), true);
                } else if (itemId == R.id.btnRestaurarTodosLixo) {
                    dialogEliminarReataurarTodasCategoriasLixeira(getString(R.string.rest_cts), getString(R.string.rest_tdas_cats), false);
                }
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        }, getViewLifecycleOwner());
        return binding.getRoot();
    }

    private void ocultarFloatButtonCimaBaixo(boolean switchHidden, int view) {
        Ultilitario.setBooleanPreference(requireContext(), switchHidden, "categoria");
        binding.floatingActionButtonCima.setVisibility(view);
        binding.floatingActionButtonBaixo.setVisibility(view);
    }

    private void recyclerViewConfig(RecyclerView recyclerView, CategoriaAdapter adapter) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void criarCategoria() {
        Navigation.findNavController(requireView()).navigate(R.id.action_categoriaProdutoFragment_to_dialogCriarCategoria);
    }

    private void exportarImportar(int typeOperetion) {
        categoriaProdutoViewModel.categoriasSpinner(false, typeOperetion, requireView());
    }

    private void consultarCategorias(boolean isCrud, String categoria, boolean isPesquisa) {
        categoriaProdutoViewModel.crud = isCrud;
        categoriaProdutoViewModel.consultarCategorias(categoria, isLixeira, isPesquisa, getViewLifecycleOwner());
    }

    private void dialogEliminarReataurarTodasCategoriasLixeira(String titulo, String msg, boolean isEliminar) {
        categoriaProdutoViewModel.crud = true;
        if (vazio) {
            Snackbar.make(binding.mySwipeRefreshLayout, getString(R.string.lx_vz), Snackbar.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
            if (isEliminar)
                alert.setIcon(android.R.drawable.ic_menu_delete);
            alert.setTitle(titulo);
            alert.setMessage(msg);
            alert.setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss());
            if (isEliminar) {
                alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> categoriaProdutoViewModel.eliminarCategoria(null, false, true));
            } else {
                alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> categoriaProdutoViewModel.restaurarCategoria(Ultilitario.UM, 0, true));
            }
            if (getArguments() != null) {
                if (isMaster) {
                    alert.show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.nao_alt_ope), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
            }
        }
    }

    //    class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder> {
    class CategoriaAdapter extends PagingDataAdapter<Categoria, CategoriaAdapter.CategoriaViewHolder> {

        public CategoriaAdapter(@NonNull DiffUtil.ItemCallback<Categoria> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CategoriaViewHolder(FragmentCategoriaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull CategoriaViewHolder h, int position) {
            Categoria ct = getItem(position);

            if (ct != null) {
                h.binding.txtNomeCategoria.setText(ct.getCategoria());
                h.binding.txtDescricao.setText(ct.getDescricao() + (isLixeira ? "\nAdd " + getString(R.string.lix) + ": " + ct.getData_elimina() : ""));
                if (ct.getEstado() == Ultilitario.DOIS) {
                    h.binding.txtNomeCategoria.setText(Html.fromHtml(getString(R.string.risc_text, ct.getCategoria())));
                    h.binding.txtDescricao.setText(getString(R.string.estado_bloqueado));
                }
                registerForContextMenu(h.binding.imgBtnMenu);
                if (!isLixeira) {
                    h.itemView.setOnClickListener(v -> {
                        MainActivity.getProgressBar();
                        listaProdutos(ct.getId(), ct.getCategoria());
                    });
                }
                h.binding.imgBtnMenu.setOnClickListener(View::showContextMenu);
                h.itemView.setOnCreateContextMenuListener((menu1, v, menuInfo) -> {
                    menu1.setHeaderIcon(R.drawable.ic_baseline_store_24);
                    menu1.setHeaderTitle(ct.getCategoria());
                    if (!isLixeira) {
                        menu1.add(getString(R.string.entrar)).setOnMenuItemClickListener(item -> {
                            MainActivity.getProgressBar();
                            listaProdutos(ct.getId(), ct.getCategoria());
                            return false;
                        });//groupId, itemId, order, title
                        if (getArguments() != null) {
                            if (getArguments().getBoolean("master")) {
                                menu1.add(getString(R.string.alterar_categoria)).setOnMenuItemClickListener(item -> {
                                    bundle.putParcelable("categoria", ct);
                                    Navigation.findNavController(requireView()).navigate(R.id.action_categoriaProdutoFragment_to_dialogCriarCategoria, bundle);
                                    return false;
                                });
                                menu1.add(getString(R.string.env_lx)).setOnMenuItemClickListener(item -> {
                                    caixaDialogo(ct, getString(R.string.env_lx), "(" + ct.getCategoria() + ")\n" + getString(R.string.env_cat_lixe), false);
                                    return false;
                                });
                                menu1.add(getString(R.string.eliminar_categoria)).setOnMenuItemClickListener(item -> {
                                    caixaDialogo(ct, getString(R.string.elim_cat_perm), "(" + ct.getCategoria() + ")\n" + getString(R.string.env_cat_n_lix), true);
                                    return false;
                                });
                            }
                        } else {
                            Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if (getArguments() != null) {
                            if (getArguments().getBoolean("master") || isMaster) {
                                menu1.add(getString(R.string.rest)).setOnMenuItemClickListener(item -> {
                                    restaurarCategoria(ct.getCategoria(), ct.getId());
                                    return false;
                                });
                                menu1.add(getString(R.string.eliminar)).setOnMenuItemClickListener(item -> {
                                    dialogEliminarCategoria(getString(R.string.tem_certeza_eliminar_categoria), ct.getCategoria(), ct);
                                    return false;
                                });
                            }
                        } else {
                            Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

        private class CategoriaViewHolder extends RecyclerView.ViewHolder {
            FragmentCategoriaBinding binding;

            public CategoriaViewHolder(@NonNull FragmentCategoriaBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        private void caixaDialogo(Categoria categoria, String titulo, String mensagem, boolean permanente) {
            categoria.setId(categoria.getId());
            categoria.setEstado(Ultilitario.TRES);
            categoria.setData_elimina(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getContext());
            alert.setIcon(android.R.drawable.ic_menu_delete);
            alert.setTitle(titulo);
            alert.setMessage(mensagem);
            alert.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        categoriaProdutoViewModel.crud = true;
                        categoriaProdutoViewModel.eliminarCategoria(categoria, !permanente, false);
                    }
            ).setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .show();
        }

        private void listaProdutos(long id, String categoria) {
            bundle.putLong("idcategoria", id);
            bundle.putString("categoria", categoria);
            Navigation.findNavController(requireView()).navigate(R.id.action_categoriaProdutoFragment_to_listProdutoFragment, bundle);
        }

        private void dialogEliminarCategoria(String msg, String categoria, Categoria ct) {
            categoriaProdutoViewModel.crud = true;
            new AlertDialog.Builder(requireContext())
                    .setIcon(android.R.drawable.ic_menu_delete)
                    .setTitle(getString(R.string.eliminar_categoria))
                    .setMessage(" (" + categoria + ")\n" + msg)
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> categoriaProdutoViewModel.eliminarCategoria(ct, !isLixeira, false))
                    .show();
        }

        private void restaurarCategoria(String categoria, long idcategoria) {
            categoriaProdutoViewModel.crud = true;
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.rest) + " (" + categoria + ")")
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> categoriaProdutoViewModel.restaurarCategoria(Ultilitario.UM, idcategoria, false))
                    .show();
        }

    }

    static class CategoriaComparator extends DiffUtil.ItemCallback<Categoria> {

        @Override
        public boolean areItemsTheSame(@NonNull Categoria oldItem, @NonNull Categoria newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Categoria oldItem, @NonNull Categoria newItem) {
            return oldItem.getId() == newItem.getId();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (bundle != null)
            bundle.clear();
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }
}
