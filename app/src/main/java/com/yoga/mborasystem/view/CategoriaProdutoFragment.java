package com.yoga.mborasystem.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentCategoriaProdutoBinding;
import com.yoga.mborasystem.model.entidade.Categoria;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.util.EventObserver;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.CategoriaProdutoViewModel;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoriaProdutoFragment extends Fragment {

    private boolean vazio;
    private Random random;
    private Bundle bundle;
    private StringBuilder data;
    private GroupAdapter adapter;
    private boolean isLixeira, isMaster;
    private ProdutoViewModel produtoViewModel;
    private FragmentCategoriaProdutoBinding binding;
    private ArrayList<String> stringList, stringListDesc;
    private CategoriaProdutoViewModel categoriaProdutoViewModel;

    private ExecutorService executor;

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
        setHasOptionsMenu(true);
    }

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
        binding.btncriarCategoriaDialog.setOnClickListener(v -> criarCategoria());
        binding.recyclerViewCategoriaProduto.setAdapter(adapter);
        binding.recyclerViewCategoriaProduto.setHasFixedSize(true);
        binding.recyclerViewCategoriaProduto.setLayoutManager(new LinearLayoutManager(getContext()));
        categoriaProdutoViewModel.consultarCategorias(null, isLixeira);
        categoriaProdutoViewModel.getListaCategorias().observe(getViewLifecycleOwner(), categorias -> {
            adapter.clear();
            if (categorias.isEmpty()) {
                vazio = true;
                Ultilitario.naoEncontrado(getContext(), adapter, R.string.categoria_nao_encontrada);
            } else {
                vazio = false;
                stringList.clear();
                random = new Random();
                for (Categoria categoria : categorias) {
                    stringList.add(categoria.getId() + " - " + categoria.getCategoria());
                    stringListDesc.add(categoria.getDescricao());
                    adapter.add(new ItemCategoria(categoria));
                }
            }
        });
        produtoViewModel.getListaProdutosisExport().observe(getViewLifecycleOwner(), new EventObserver<>(prod -> {
            StringBuilder dt = new StringBuilder();
            if (prod.isEmpty()) {
                Ultilitario.showToast(getContext(), Color.rgb(254, 207, 65), getString(R.string.produto_nao_encontrado), R.drawable.ic_toast_erro);
            } else {
                for (Produto produto : prod) {
                    dt.append(produto.getNome()).append(",").append(produto.getPreco()).append(",").append(produto.getPrecofornecedor()).append(",").append(produto.getQuantidade()).append(",").append(produto.getCodigoBarra()).append(",").append(produto.isIva()).append(",").append(produto.getEstado()).append(",").append(produto.getIdcategoria()).append("\n");
                }
                data = dt;
                exportarProdutos(Ultilitario.categoria, Ultilitario.isLocal);
            }
        }));
        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> categoriaProdutoViewModel.consultarCategorias(binding.mySwipeRefreshLayout, isLixeira));
        return binding.getRoot();
    }

    private void exportarProdutos(String nomeFicheiro, boolean isLocal) {
        if (isLocal) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Ultilitario.exportarLocal(exportProductActivityResultLauncher, getActivity(), data, "produtos.csv", nomeFicheiro, Ultilitario.getDateCurrent());
            } else {
                Ultilitario.alertDialog(getString(R.string.avs), getString(R.string.exp_dis_api_sup), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
        } else {
            Ultilitario.exportarNuvem(getContext(), data, "produtos.csv", nomeFicheiro, Ultilitario.getDateCurrent());
        }
    }

    private void criarCategoria() {
        MainActivity.getProgressBar();
        Navigation.findNavController(requireView()).navigate(R.id.action_categoriaProdutoFragment_to_dialogCriarCategoria);
    }

    private void exportarImportar(int typeOperetion) {
        bundle.putInt("typeoperation", typeOperetion);
        bundle.putStringArrayList("categorias", stringList);
        bundle.putStringArrayList("descricao", stringListDesc);
        Navigation.findNavController(requireView()).navigate(R.id.action_categoriaProdutoFragment_to_dialogExportarImportar, bundle);
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
                menu.findItem(R.id.exinpCategoria).setVisible(false);
                menu.findItem(R.id.exinpProduto).setVisible(false);
                binding.btncriarCategoriaDialog.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
        }
        if (isLixeira) {
            menu.findItem(R.id.exinpProduto).setVisible(false);
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
                newTextIsEmpty();
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
                    newTextIsEmpty();
                } else {
                    categoriaProdutoViewModel.searchCategoria(newText, isLixeira);
                }
                return false;
            }
        });
    }

    private void newTextIsEmpty() {
        categoriaProdutoViewModel.consultarCategorias(binding.mySwipeRefreshLayout, isLixeira);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
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
            case R.id.btnEliminarTodosLixo:
                dialogEliminarReataurarTodasCategoriasLixeira(getString(R.string.elim_cts), getString(R.string.tem_cert_elim_cts), true);
                break;
            case R.id.btnRestaurarTodosLixo:
                dialogEliminarReataurarTodasCategoriasLixeira(getString(R.string.rest_cts), getString(R.string.rest_tdas_cats), false);
                break;
            default:
                break;
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    private void dialogEliminarReataurarTodasCategoriasLixeira(String titulo, String msg, boolean isEliminar) {
        if (vazio) {
            Snackbar.make(binding.mySwipeRefreshLayout, getString(R.string.lx_vz), Snackbar.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
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

    class ItemCategoria extends Item<GroupieViewHolder> {
        private final Categoria categoria;

        public ItemCategoria(Categoria categoria) {
            this.categoria = categoria;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView nome = viewHolder.itemView.findViewById(R.id.txtNomeCategoria);
            TextView descricao = viewHolder.itemView.findViewById(R.id.txtDescricao);
            ImageView img = viewHolder.itemView.findViewById(R.id.imgCategoriaProduto);
            ImageButton menu = viewHolder.itemView.findViewById(R.id.imgBtnMenu);

            Ultilitario.colorRandomImage(img, random);

            nome.setText(categoria.getCategoria() + " | " + categoria.getId());
            descricao.setText(categoria.getDescricao() + (isLixeira ? "\nAdd " + getString(R.string.lix) + ": " + categoria.getData_elimina() : ""));
            if (categoria.getEstado() == Ultilitario.DOIS) {
                descricao.setTextColor(Color.RED);
                descricao.setText(getString(R.string.desactivado));
            }
            registerForContextMenu(menu);
            if (!isLixeira) {
                viewHolder.itemView.setOnClickListener(v -> {
                    MainActivity.getProgressBar();
                    v.setBackgroundColor(Color.parseColor("#6BD3D8D7"));
                    listaProdutos(categoria.getId(), categoria.getCategoria());
                });
            }
            menu.setOnClickListener(View::showContextMenu);
            viewHolder.itemView.setOnCreateContextMenuListener((menu1, v, menuInfo) -> {
                menu1.setHeaderIcon(R.drawable.ic_baseline_store_24);
                menu1.setHeaderTitle(categoria.getCategoria());
                if (!isLixeira) {
                    menu1.add(getString(R.string.entrar)).setOnMenuItemClickListener(item -> {
                        MainActivity.getProgressBar();
                        listaProdutos(categoria.getId(), categoria.getCategoria());
                        return false;
                    });//groupId, itemId, order, title
                    if (getArguments() != null) {
                        if (getArguments().getBoolean("master")) {
                            menu1.add(getString(R.string.alterar_categoria)).setOnMenuItemClickListener(item -> {
                                MainActivity.getProgressBar();
                                bundle.putParcelable("categoria", categoria);
                                Navigation.findNavController(requireView()).navigate(R.id.action_categoriaProdutoFragment_to_dialogCriarCategoria, bundle);
                                return false;
                            });
                            menu1.add(getString(R.string.env_lx)).setOnMenuItemClickListener(item -> {
                                caixaDialogo(categoria, getString(R.string.env_lx) + " (" + categoria.getCategoria() + ")", R.string.env_cat_lixe, false);
                                return false;
                            });
                            menu1.add(getString(R.string.eliminar_categoria)).setOnMenuItemClickListener(item -> {
                                caixaDialogo(categoria, getString(R.string.elim_cat_perm) + " (" + categoria.getCategoria() + ")", R.string.env_cat_n_lix, true);
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
                                restaurarCategoria();
                                return false;
                            });
                            menu1.add(getString(R.string.eliminar)).setOnMenuItemClickListener(item -> {
                                dialogEliminarCategoria(getString(R.string.tem_certeza_eliminar_categoria));
                                return false;
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
                    }
                }

            });
        }

        private void caixaDialogo(Categoria categoria, String titulo, int mensagem, boolean permanente) {
            categoria.setId(categoria.getId());
            categoria.setEstado(Ultilitario.TRES);
            categoria.setData_elimina(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getContext());
            alert.setTitle(titulo);
            alert.setMessage(getString(mensagem));

            alert.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        MainActivity.getProgressBar();
                        if (permanente) {
                            categoriaProdutoViewModel.eliminarCategoria(categoria, false, false);
                        } else {
                            categoriaProdutoViewModel.eliminarCategoria(categoria, true, false);
                        }
                        categoriaProdutoViewModel.consultarCategorias(null, isLixeira);
                    }
            ).setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .show();
        }

        @Override
        public int getLayout() {
            return R.layout.fragment_categoria;
        }

        private void listaProdutos(long id, String categoria) {
            bundle.putLong("idcategoria", id);
            bundle.putString("categoria", categoria);
            Navigation.findNavController(requireView()).navigate(R.id.action_categoriaProdutoFragment_to_listProdutoFragment, bundle);
        }

        private void dialogEliminarCategoria(String msg) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.eliminar_categoria) + " (" + categoria.getCategoria() + ")")
                    .setMessage(msg)
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> categoriaProdutoViewModel.eliminarCategoria(categoria, !isLixeira, false))
                    .show();
        }

        private void restaurarCategoria() {
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.rest) + " (" + categoria.getCategoria() + ")")
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> categoriaProdutoViewModel.restaurarCategoria(Ultilitario.UM, categoria.getId(), false))
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (bundle != null) {
            bundle.clear();
        }
        if (executor != null)
            executor.shutdownNow();
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }

    public void readTextFromUri(Uri uri) throws IOException {
        executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<String> produtos = new ArrayList<>();
            try (InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    produtos.add(line);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            produtoViewModel.importarProdutos(produtos, handler);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == Ultilitario.QUATRO && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    readTextFromUri(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    ActivityResultLauncher<Intent> exportProductActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent resultData = result.getData();
                    Uri uri;
                    if (resultData != null) {
                        uri = resultData.getData();
                        Ultilitario.alterDocument(uri, data, requireActivity());
                        data.delete(0, data.length());
                    }
                }
            });
}
