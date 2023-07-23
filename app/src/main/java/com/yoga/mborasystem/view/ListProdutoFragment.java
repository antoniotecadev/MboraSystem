package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.alertDialogSelectImage;
import static com.yoga.mborasystem.util.Ultilitario.conexaoInternet;
import static com.yoga.mborasystem.util.Ultilitario.getAPN;
import static com.yoga.mborasystem.util.Ultilitario.getFileName;
import static com.yoga.mborasystem.util.Ultilitario.getValueSharedPreferences;
import static com.yoga.mborasystem.util.Ultilitario.showToast;
import static com.yoga.mborasystem.util.Ultilitario.storageImageAndProduct;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.paging.PagingDataAdapter;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.koushikdutta.ion.Ion;
import com.xwray.groupie.GroupAdapter;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentProdutoBinding;
import com.yoga.mborasystem.databinding.FragmentProdutoListBinding;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.util.EventObserver;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("rawtypes")
public class ListProdutoFragment extends Fragment {

    private Gson gson;
    private int tipo, quantidade;
    private boolean vazio;
    private Bundle bundle;
    private Long idcategoria;
    private String categoria;
    private StringBuilder data;
    private boolean isLixeira, isMaster;
    private GroupAdapter adapter;
    private Map<String, String> detalhes;
    private List<Long> idprodutoCarrinho;
    private ProdutoViewModel produtoViewModel;
    private FragmentProdutoListBinding binding;
    private ProdutoAdapter pagingAdapter;
    private ExecutorService executor;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new Gson();
        bundle = new Bundle();
        data = new StringBuilder();
        adapter = new GroupAdapter();
        idprodutoCarrinho = new ArrayList<>();
        pagingAdapter = new ProdutoAdapter(new ProdutoComparator());
        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);
        sharedPreferences = requireContext().getSharedPreferences("PRODUTO_CARRINHO", Context.MODE_PRIVATE);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProdutoListBinding.inflate(inflater, container, false);
        isLixeira = CategoriaProdutoFragmentArgs.fromBundle(getArguments()).getIsLixeira();
        isMaster = ListProdutoFragmentArgs.fromBundle(getArguments()).getIsMaster();
        binding.recyclerViewListaProduto.setAdapter(pagingAdapter);
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
            binding.btnCriarProdutoFragment.setVisibility(View.GONE);
        }
        produtoViewModel.getQuantidadeProduto(this.idcategoria, isLixeira).observe(getViewLifecycleOwner(), quantidade -> {
            this.quantidade = quantidade.intValue();
            vazio = quantidade == 0;
            binding.chipQuantidadeProduto.setText(String.valueOf(quantidade));
            binding.recyclerViewListaProduto.setAdapter(quantidade == 0 ? Ultilitario.naoEncontrado(getContext(), adapter, R.string.produto_nao_encontrada) : pagingAdapter);
        });
        consultarProdutos(false, null, false);
        produtoViewModel.getListaProdutosPaging().observe(getViewLifecycleOwner(), produtoPagingData -> {
            Ultilitario.swipeRefreshLayout(binding.mySwipeRefreshLayout);
            pagingAdapter.submitData(getLifecycle(), produtoPagingData);
        });

        binding.btnCriarProdutoFragment.setOnClickListener(v ->
        {
            if (!categoria.isEmpty())
                createProduto(idcategoria, categoria);
        });
        binding.floatingActionButtonCima.setOnClickListener(view -> binding.recyclerViewListaProduto.smoothScrollToPosition(0));
        binding.floatingActionButtonBaixo.setOnClickListener(view -> binding.recyclerViewListaProduto.smoothScrollToPosition(quantidade));
        binding.switchOcultarFloatCimaBaixo.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                ocultarFloatButtonCimaBaixo(true, View.VISIBLE);
            else
                ocultarFloatButtonCimaBaixo(false, View.GONE);
        });
        binding.switchOcultarFloatCimaBaixo.setChecked(Ultilitario.getBooleanPreference(requireContext(), "product_list_scroll"));
        produtoViewModel.getListaProdutosisExport().observe(getViewLifecycleOwner(), new EventObserver<>(prod ->
        {
            StringBuilder dt = new StringBuilder();
            if (prod.isEmpty())
                showToast(getContext(), Color.rgb(254, 207, 65), getString(R.string.produto_nao_encontrado), R.drawable.ic_toast_erro);
            else {
                for (Produto produto : prod)
                    dt.append(produto.getNome()).append(",").append(produto.getTipo()).append(",").append(produto.getUnidade()).append(",").append(produto.getCodigoMotivoIsencao()).append(",").append(produto.getPercentagemIva()).append(",").append(produto.getPreco()).append(",").append(produto.getPrecofornecedor()).append(",").append(produto.getQuantidade()).append(",").append(produto.getCodigoBarra()).append(",").append(produto.isIva()).append(",").append(produto.getEstado()).append(",").append(produto.isStock()).append(",").append(produto.getIdcategoria()).append("\n");

                data = dt;
                exportarProdutos(this.categoria, tipo == 0 && Ultilitario.isLocal);
            }
        }));
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_pesquisa_criar_produto, menu);
                if (getArguments() != null) {
                    if (!getArguments().getBoolean("master")) {
                        menu.findItem(R.id.dialogCriarProduto).setVisible(false);
                        menu.findItem(R.id.exinpProduto).setVisible(false);
                        binding.btnCriarProdutoFragment.setVisibility(View.GONE);
                    }
//                    else
//                        menu.findItem(R.id.itemSairFirebaseAuth).setVisible(verifyAuthenticationInFirebase() != null);
                }
                if (isLixeira) {
                    menu.findItem(R.id.dialogCriarProduto).setVisible(false);
                    menu.findItem(R.id.dialogFiltrarProduto).setVisible(false);
                    menu.findItem(R.id.btnScannerBack).setVisible(false);
                    menu.findItem(R.id.exportarproduto).setVisible(false);
                    menu.findItem(R.id.importarproduto).setVisible(false);
                    menu.findItem(R.id.facturaFragment).setVisible(false);
                    if (!Ultilitario.getBooleanPreference(requireContext(), "master")) {
                        menu.findItem(R.id.btnEliminarTodosLixo).setVisible(false);
                        menu.findItem(R.id.btnRestaurarTodosLixo).setVisible(false);
                    }
                } else {
                    menu.findItem(R.id.btnEliminarTodosLixo).setVisible(false);
                    menu.findItem(R.id.btnRestaurarTodosLixo).setVisible(false);
                }
                SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
                MenuItem menuItem = menu.findItem(R.id.app_bar_search);
                SearchView searchView = (SearchView) menuItem.getActionView();
                searchView.setQueryHint(getString(R.string.prod) + ", " + getString(R.string.codigo_bar));
                searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
                searchView.onActionViewExpanded();
                menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        consultarProdutos(false, null, false);
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
                        if (newText.isEmpty())
                            consultarProdutos(false, null, false);
                        else
                            consultarProdutos(true, newText, true);
                        return false;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.dialogCriarProduto) {
                    if (!categoria.isEmpty())
                        createProduto(idcategoria, categoria);
                } else if (itemId == R.id.facturaFragment) {
                    bundle.putLong("idoperador", requireArguments().getLong("idoperador", 0));
                    Navigation.findNavController(requireView()).navigate(R.id.action_listProdutoFragment_to_facturaFragment, bundle);
                } else if (itemId == R.id.dialogFiltrarProduto)
                    filtrarProduto(idcategoria);
                else if (itemId == R.id.btnScannerBack)
                    scanearCodigoQr();
                else if (itemId == R.id.exportarproduto)
                    exportarProduto();
                else if (itemId == R.id.importarproduto)
                    importarProdutos();
                else if (itemId == R.id.btnEliminarTodosLixo)
                    dialogEliminarReataurarTodasProdutosLixeira(getString(R.string.elim_pds), getString(R.string.tem_cert_elim_pds), true);
                else if (itemId == R.id.btnRestaurarTodosLixo)
                    dialogEliminarReataurarTodasProdutosLixeira(getString(R.string.rest_pds), getString(R.string.rest_tdos_prods), false);
//                else if (itemId == R.id.itemSairFirebaseAuth) {
//                    FirebaseAuth.getInstance().signOut();
//                    if (verifyAuthenticationInFirebase() == null)
//                        showToast(requireActivity(), Color.rgb(102, 153, 0), getString(R.string.sess_nuve_term), R.drawable.ic_toast_feito);
//                }
                return false;
            }
        }, getViewLifecycleOwner());
        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> {
            consultarProdutos(false, null, false);
            pagingAdapter.notifyDataSetChanged();
        });
        return binding.getRoot();
    }

    private void addProdutoCarrinho(Long idproduto, String nomeProduto) {
        if (!idprodutoCarrinho.contains(idproduto)) {
            idprodutoCarrinho.add(idproduto);
            sharedPreferences.edit().putString("idprodutocarrinho", gson.toJson(idprodutoCarrinho)).apply();
            Toast.makeText(requireContext(), getText(R.string.prod_add_car), Toast.LENGTH_LONG).show();
        } else
            Ultilitario.alertDialog(getString(R.string.avs), getString(R.string.prod_ja_add, nomeProduto), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
    }

    private void removeProdutoCarrinho(Long idproduto) {
        idprodutoCarrinho.remove(idproduto);
        sharedPreferences.edit().putString("idprodutocarrinho", gson.toJson(idprodutoCarrinho)).apply();
        Toast.makeText(requireContext(), getText(R.string.prod_elim_car), Toast.LENGTH_LONG).show();
    }

    private List<Long> getProdutoCarrinho() {
        List<Long> idprodutoList = new ArrayList<>();
        String idproduto = sharedPreferences.getString("idprodutocarrinho", null);
        if (idproduto != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Long>>() {
            }.getType();
            idprodutoList = gson.fromJson(idproduto, type);
        }
        return idprodutoList;
    }

    private void ocultarFloatButtonCimaBaixo(boolean switchHidden, int view) {
        Ultilitario.setBooleanPreference(requireContext(), switchHidden, "product_list_scroll");
        binding.floatingActionButtonCima.setVisibility(view);
        binding.floatingActionButtonBaixo.setVisibility(view);
    }

    private void exportarProdutos(String nomeCategoria, boolean isLocal) {
        if (isLocal) {
            Ultilitario.exportarLocal(exportProductActivityResultLauncher, getActivity(), nomeCategoria, Ultilitario.getDateCurrent());
        } else
            Ultilitario.exportarNuvem(getContext(), data, nomeCategoria + Ultilitario.getDateCurrent() + "_produtos.csv", nomeCategoria, Ultilitario.getDateCurrent());
    }

    private void importarProdutos() {
        Ultilitario.importarCategoriasProdutosClientes(importProductActivityResultLauncher, requireActivity(), false);
    }

    private void createProduto(long id, String categoria) {
        bundle.clear();
        bundle.putLong("idcategoria", id);
        bundle.putString("categoria", categoria);
        Navigation.findNavController(requireView()).navigate(R.id.action_listProdutoFragment_to_dialogCriarProduto, bundle);
    }

    private void filtrarProduto(long id) {
        bundle.clear();
        bundle.putLong("idcategoria", id);
        Navigation.findNavController(requireView()).navigate(R.id.action_listProdutoFragment_to_dialogFiltrarProduto2, bundle);
    }

    private void scanearCodigoQr() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
        intentIntegrator.setPrompt(getString(R.string.alinhar_codigo_qr));
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setCameraId(0);
        zxingActivityResultLauncher.launch(intentIntegrator.createScanIntent());
    }

    private void exportarProduto() {
        Handler handler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
        new android.app.AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_baseline_insert_drive_file_24)
                .setTitle(R.string.exportar)
                .setSingleChoiceItems(R.array.array_local_nuvem, 0, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            tipo = 0;
                            break;
                        case 1:
                            tipo = 1;
                            break;
                        default:
                            break;
                    }
                })
                .setNegativeButton(R.string.cancelar, (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> exportarProdutos(executor, handler, dialogInterface)).show();
    }

    public void exportarProdutos(ExecutorService executor, Handler handler, DialogInterface dialogInterface) {
        executor.execute(() -> {
            try {
                produtoViewModel.exportarProdutos(this.idcategoria);
            } catch (Exception e) {
                handler.post(() -> Ultilitario.alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24));
            }
        });
        dialogInterface.dismiss();
    }

    private void dialogEliminarReataurarTodasProdutosLixeira(String titulo, String msg, boolean isEliminar) {
        produtoViewModel.crud = true;
        if (vazio)
            Snackbar.make(binding.mySwipeRefreshLayout, getString(R.string.lx_vz), Snackbar.LENGTH_LONG).show();
        else {
            AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
            if (isEliminar)
                alert.setIcon(R.drawable.ic_baseline_delete_40);
            else
                alert.setIcon(android.R.drawable.ic_menu_revert);
            alert.setTitle(titulo);
            alert.setMessage(msg);
            alert.setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss());
            if (isEliminar) {
                Produto prod = new Produto();
                prod.setIdcategoria(idcategoria);
                alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> produtoViewModel.eliminarProduto(prod, false, null, true));
            } else
                alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> produtoViewModel.restaurarProduto(Ultilitario.UM, 0, true));

            if (getArguments() != null) {
                if (isMaster)
                    alert.show();
                else
                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.nao_alt_ope), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            } else
                Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.arg_null), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
        }
    }

    private void consultarProdutos(boolean iScrud, String produto, boolean isPesquisa) {
        produtoViewModel.crud = iScrud;
        produtoViewModel.consultarProdutos(idcategoria, produto, isLixeira, isPesquisa, getViewLifecycleOwner(), false, false, false, null);
    }

    class ProdutoAdapter extends PagingDataAdapter<Produto, ProdutoAdapter.ProdutoViewHolder> {

        public ProdutoAdapter(@NonNull DiffUtil.ItemCallback<Produto> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ProdutoViewHolder(FragmentProdutoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ProdutoViewHolder h, int position) {
            Produto produto = getItem(position);
            if (produto != null) {
                List<Long> carrinho = getProdutoCarrinho();
                h.binding.txtNomeProduto.setText(produto.getNome() + " " + (carrinho.contains(produto.getId()) ? "©" : ""));
                h.binding.txtPrecoProduto.setText(getText(R.string.preco) + ": " + Ultilitario.formatPreco(String.valueOf(produto.getPreco())) + " " + getString(R.string.iva) + ": " + produto.getPercentagemIva() + "%");
                h.binding.txtPrecoProdutoFornecedor.setText(getText(R.string.preco_fornecedor) + ": " + Ultilitario.formatPreco(String.valueOf(produto.getPrecofornecedor())));
                h.binding.txtQuantidadeProduto.setTextColor(!produto.isStock() ? Color.BLACK : produto.getQuantidade() == 0 ? Color.RED : Color.parseColor("#43A047"));
                h.binding.txtQuantidadeProduto.setText(!produto.isStock() ? getString(R.string.sem_cont_stoc) + " - " + getText(R.string.quantidade) + ": " + produto.getQuantidade() : (produto.getQuantidade() == 0 ? getString(R.string.sem_prod_stoc) + " - " : getString(R.string.prod_stoc) + " - ") + getText(R.string.quantidade) + ": " + produto.getQuantidade());
                h.binding.txtCodigoBarProduto.setText(getText(R.string.codigo_bar) + ": " + produto.getCodigoBarra() + (isLixeira ? "\nAdd " + getString(R.string.lix) + ": " + produto.getData_elimina() : ""));
                h.binding.txtReferenciaProduto.setText(getText(R.string.referencia) + ": MSP" + produto.getId());
                if (produto.getEstado() == Ultilitario.UM) {
                    h.binding.estadoProduto.setChecked(true);
                    h.binding.estadoProduto.setTextColor(Color.BLUE);
                    h.binding.estadoProduto.setText(getString(R.string.estado_desbloqueado));
                } else {
                    h.binding.txtNomeProduto.setText(Html.fromHtml(getString(R.string.risc_text, produto.getNome())));
                    h.binding.estadoProduto.setChecked(false);
                    h.binding.estadoProduto.setTextColor(Color.RED);
                    h.binding.estadoProduto.setText(getString(R.string.estado_bloqueado));
                }
                h.binding.btnEntrar.setOnClickListener(v -> entrarProduto(getItem(position)));
                if (isLixeira) {
                    h.binding.btnEntrar.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                        menu.setHeaderTitle(produto.getNome());
                        if (getArguments() != null) {
                            if (getArguments().getBoolean("master") || isMaster) {
                                menu.add(getString(R.string.rest)).setOnMenuItemClickListener(item -> {
                                    restaurarProduto(produto.getNome(), produto.getId());
                                    return false;
                                });
                                menu.add(getString(R.string.eliminar)).setOnMenuItemClickListener(item -> {
                                    dialogEliminarProduto(produto.getNome(), produto);
                                    return false;
                                });
                            }
                        } else
                            Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.arg_null), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                    });
                } else {
                    h.binding.btnEntrar.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                        menu.setHeaderTitle(produto.getNome());
                        menu.add(requireArguments().getBoolean("master") ? getString(R.string.editar) : getString(R.string.abrir)).setOnMenuItemClickListener(item -> {
                            entrarProduto(getItem(position));
                            return false;
                        });
                        if (PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean("activarcarrinho", false))
                            if (!carrinho.contains(produto.getId())) {
                                menu.add(getString(R.string.addCart)).setOnMenuItemClickListener(item -> {
                                    addProdutoCarrinho(produto.getId(), produto.getNome());
                                    notifyItemChanged(position);
                                    return false;
                                });
                            } else {
                                menu.add(getString(R.string.elmCart)).setOnMenuItemClickListener(item -> {
                                    removeProdutoCarrinho(produto.getId());
                                    notifyItemChanged(position);
                                    return false;
                                });
                            }
                        if (getArguments() != null) {
                            if (getArguments().getBoolean("master") || isMaster) {
                                menu.add(getString(R.string.env_lx)).setOnMenuItemClickListener(item -> {
                                    caixaDialogo(produto, getString(R.string.env_lx), "(" + produto.getNome() + ")\n" + getString(R.string.env_prod_p_lix), false);
                                    return false;
                                });
                                menu.add(getString(R.string.eliminar_produto)).setOnMenuItemClickListener(item -> {
                                    caixaDialogo(produto, getString(R.string.elim_prod_perm), "(" + produto.getNome() + ")\n" + getString(R.string.env_prod_n_lix), true);
                                    return false;
                                });
                                menu.add(getString(R.string.env, getString(R.string.mbora))).setOnMenuItemClickListener(item -> {
                                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                                    detalhes = new HashMap<>();
                                    detalhes.put("nome", produto.getNome());
                                    detalhes.put("preco", String.valueOf(produto.getPreco()));
                                    detalhes.put("quantidade", String.valueOf(produto.getQuantidade()));
                                    detalhes.put("codigo_barra", produto.getCodigoBarra());
                                    detalhes.put("tag", categoria.toLowerCase(Locale.ROOT));
                                    return false;
                                });
                            }
                        } else
                            Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.arg_null), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                    });
                }
            }
        }

        public class ProdutoViewHolder extends RecyclerView.ViewHolder {
            FragmentProdutoBinding binding;

            public ProdutoViewHolder(@NonNull FragmentProdutoBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        private void entrarProduto(Produto produto) {
            bundle.clear();
            bundle.putParcelable("produto", produto);
            bundle.putBoolean("master", requireArguments().getBoolean("master"));
            Navigation.findNavController(requireView()).navigate(R.id.action_listProdutoFragment_to_dialogCriarProduto, bundle);
        }

        private void restaurarProduto(String nome, long idproduto) {
            produtoViewModel.crud = true;
            new AlertDialog.Builder(requireContext())
                    .setIcon(android.R.drawable.ic_menu_revert)
                    .setTitle(getString(R.string.rest) + " (" + nome + ")")
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> produtoViewModel.restaurarProduto(Ultilitario.UM, idproduto, false))
                    .show();
        }

        private void dialogEliminarProduto(String nome, Produto produto) {
            produtoViewModel.crud = true;
            new AlertDialog.Builder(requireContext())
                    .setIcon(R.drawable.ic_baseline_delete_40)
                    .setTitle(getString(R.string.eliminar))
                    .setMessage("(" + nome + ")\n" + getString(R.string.tem_certeza_eliminar_produto))
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> produtoViewModel.eliminarProduto(produto, false, null, false))
                    .show();
        }

        private void caixaDialogo(Produto produto, String titulo, String mensagem, boolean permanente) {
            produtoViewModel.crud = true;
            produto.setId(produto.getId());
            produto.setEstado(Ultilitario.TRES);
            produto.setData_elimina(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getContext());
            alert.setIcon(R.drawable.ic_baseline_delete_40);
            alert.setTitle(titulo);
            alert.setMessage(mensagem);
            alert.setPositiveButton(getString(R.string.ok), (dialog, which) -> produtoViewModel.eliminarProduto(produto, !permanente, null, false)
                    ).setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    static class ProdutoComparator extends DiffUtil.ItemCallback<Produto> {

        @Override
        public boolean areItemsTheSame(@NonNull Produto oldItem, @NonNull Produto newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Produto oldItem, @NonNull Produto newItem) {
            return oldItem.getId() == newItem.getId();
        }
    }

    ActivityResultLauncher<Intent> zxingActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    IntentResult r = IntentIntegrator.parseActivityResult(result.getResultCode(), data);
                    consultarProdutos(true, r.getContents(), true);
                } else
                    Toast.makeText(requireActivity(), R.string.scaner_code_bar_cancelado, Toast.LENGTH_SHORT).show();
            });
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
    ActivityResultLauncher<Intent> importProductActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri uri;
                    if (data != null) {
                        uri = data.getData();
                        new AlertDialog.Builder(requireContext())
                                .setIcon(R.drawable.ic_baseline_insert_drive_file_24)
                                .setTitle(getString(R.string.importar))
                                .setMessage(getFileName(uri, requireContext()))
                                .setNegativeButton(getString(R.string.cancelar), (dialogInterface, i) -> dialogInterface.dismiss())
                                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                                    try {
                                        readTextFromUri(uri);
                                    } catch (IOException e) {
                                        Ultilitario.alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                                    }
                                })
                                .show();
                    }
                }
            });

    public void readTextFromUri(Uri uri) throws IOException {
        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<String> produtos = new ArrayList<>();
            try (InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(inputStream))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    produtos.add(line);
                }
            } catch (Exception e) {
                Ultilitario.alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
            produtoViewModel.crud = true;
            produtoViewModel.importarProdutos(produtos, false, this.idcategoria);
        });
    }

    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<Intent> imageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        MainActivity.getProgressBar();
                        try {
                            Uri uriImage = data.getData();
                            Bitmap selectedImage;
                            if (uriImage == null)
                                selectedImage = (Bitmap) data.getExtras().get("data");
                            else {
                                InputStream imageStream = requireActivity().getContentResolver().openInputStream(uriImage);
                                selectedImage = BitmapFactory.decodeStream(imageStream);
                            }
                            @SuppressLint("InflateParams") View view = LayoutInflater.from(requireContext()).inflate(R.layout.image_layout, null);
                            ImageView img = view.findViewById(R.id.image);
                            TextView detalhe = view.findViewById(R.id.detalhe_text);
                            AppCompatSpinner categoriasSpinner = view.findViewById(R.id.categoriaSpinner);

                            ArrayAdapter<String> categorias = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
                            categorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            categoriasSpinner.setAdapter(categorias);

                            detalhe.setTextColor(Color.BLACK);
                            detalhe.setText(getString(R.string.prod) + ": " + detalhes.get("nome") + "\n" + getString(R.string.preco) + ": " + Ultilitario.formatPreco(detalhes.get("preco")) + "\n" + (detalhes.get("codigo_barra").isEmpty() ? "" : "CB: " + detalhes.get("codigo_barra")) + "\n" + "Tag: " + detalhes.get("tag"));
                            img.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                            img.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
                            img.requestLayout();
                            img.setScaleType(ImageView.ScaleType.FIT_START);
                            img.setImageBitmap(selectedImage);
                            if (conexaoInternet(requireContext()))
                                carregarCategorias(categorias, view, categoriasSpinner);
                        } catch (Exception e) {
                            MainActivity.dismissProgressBar();
                            Ultilitario.alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        }
                    }
                }
            });

    private void carregarCategorias(ArrayAdapter<String> categorias, View view, AppCompatSpinner categoriasSpinner) {
        String URL = getAPN(requireActivity()) + "categorias/mbora";
        Ion.with(requireActivity())
                .load(URL)
                .asJsonArray()
                .setCallback((e, jsonElements) -> {
                    try {
                        categorias.add("");
                        for (int i = 0; i < jsonElements.size(); i++) {
                            JsonObject categoria = jsonElements.get(i).getAsJsonObject();
                            categorias.add(categoria.get("id").getAsInt() + "-" + categoria.get("nome").getAsString());
                        }
                        if (categorias.getItem(1).isEmpty())
                            tentarNovamenteCarregarCategorias(categorias, view, categoriasSpinner, getString(R.string.ct_na_enc));
                        else {
                            informacaoProduto(view, categoriasSpinner);
                            Snackbar.make(requireView(), getString(R.string.ct_car), Snackbar.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        tentarNovamenteCarregarCategorias(categorias, view, categoriasSpinner, ex.getMessage());
                    }
                });
    }

    private void tentarNovamenteCarregarCategorias(ArrayAdapter<String> categorias, View view, AppCompatSpinner categoriasSpinner, String message) {
        new AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_baseline_privacy_tip_24)
                .setTitle(getString(R.string.erro))
                .setMessage(message)
                .setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.tent_nov, (dialog, which) -> {
                    dialog.dismiss();
                    MainActivity.getProgressBar();
                    carregarCategorias(categorias, view, categoriasSpinner);
                })
                .show();
    }

    private void informacaoProduto(View view, AppCompatSpinner caSpinner) {
        new AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_baseline_cloud_upload_24)
                .setView(view)
                .setTitle(getString(R.string.env, getString(R.string.mbora)))
                .setNegativeButton(getString(R.string.cancelar), (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                    if (caSpinner.getSelectedItem().toString().isEmpty())
                        Snackbar.make(requireView(), getString(R.string.sl_ct_pd), Snackbar.LENGTH_LONG).show();
                    else {
                        detalhes.put("idcategoria", TextUtils.split(caSpinner.getSelectedItem().toString(), "-")[0]);
                        storageImageAndProduct(getValueSharedPreferences(requireContext(), "imei", "0000000000"), view.findViewById(R.id.image), detalhes, requireContext());
                    }
                }).show();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result)
                    alertDialogSelectImage(requireContext(), imageActivityResultLauncher);
                else
                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_perm_cam_gal), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            });

//    private final ActivityResultLauncher<String> requestPermissionLauncherFirebase = registerForActivityResult(
//            new ActivityResultContracts.RequestPermission(), result -> {
//                if (result) {
//                    if (verifyAuthenticationInFirebase() != null) {
//                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("empresas");
//                        reference.child(getValueSharedPreferences(requireContext(), "imei", "0000000000")).get().addOnCompleteListener(task -> {
//                            if (task.isSuccessful()) {
//                                MainActivity.dismissProgressBar();
//                                if (task.getResult().exists()) {
//                                    try {
//                                        Cliente cliente = task.getResult().getValue(Cliente.class);
//                                        if (cliente != null) {
//                                            detalhes.add(cliente.getMunicipio() + ", " + cliente.getBairro() + ", " + cliente.getRua());
//                                            detalhes.add(cliente.getNomeEmpresa());
//                                            detalhes.add(cliente.getImei());
//                                            alertDialogSelectImage(cliente, requireContext(), imageActivityResultLauncher);
//                                        } else
//                                            showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.dds_n_enc), R.drawable.ic_toast_erro);
//                                    } catch (Exception e) {
//                                        alertDialog(getString(R.string.erro), e.getMessage(), requireActivity(), R.drawable.ic_baseline_privacy_tip_24);
//                                    }
//                                } else
//                                    showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.imei_n_enc), R.drawable.ic_toast_erro);
//                            } else {
//                                FirebaseAuth.getInstance().signOut();
//                                MainActivity.dismissProgressBar();
//                                if (task.getException() != null)
//                                    alertDialog(getString(R.string.erro), task.getException().getMessage(), requireActivity(), R.drawable.ic_baseline_privacy_tip_24);
//                                else
//                                    showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.dds_n_enc), R.drawable.ic_toast_erro);
//                            }
//                        });
//                    } else {
//                        MainActivity.dismissProgressBar();
//                        DialogSenhaBinding binding = DialogSenhaBinding.inflate(getLayoutInflater());
//                        authenticationInFirebase(requireActivity(), binding, imageActivityResultLauncher);
//                    }
//                } else {
//                    MainActivity.dismissProgressBar();
//                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_perm_cam_gal), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
//                }
//            }
//    );

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (bundle != null)
            bundle.clear();

        if (executor != null)
            executor.shutdownNow();
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }
}
