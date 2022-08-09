package com.yoga.mborasystem.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("rawtypes")
public class ListProdutoFragment extends Fragment {

    private int tipo, quantidade;
    private boolean vazio;
    private Bundle bundle;
    private Long idcategoria;
    private String categoria;
    private StringBuilder data;
    private boolean isLixeira, isMaster;
    private GroupAdapter adapter;
    private ProdutoViewModel produtoViewModel;
    private FragmentProdutoListBinding binding;
    private ProdutoAdapter pagingAdapter;
    private ExecutorService executor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = new Bundle();
        data = new StringBuilder();
        adapter = new GroupAdapter();
        pagingAdapter = new ProdutoAdapter(new ProdutoComparator());
        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);
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
            if (!categoria.isEmpty()) {
                createProduto(idcategoria, categoria);
            }
        });
        binding.floatingActionButtonCima.setOnClickListener(view -> binding.recyclerViewListaProduto.smoothScrollToPosition(0));
        binding.floatingActionButtonBaixo.setOnClickListener(view -> binding.recyclerViewListaProduto.smoothScrollToPosition(quantidade));
        binding.switchOcultarFloatCimaBaixo.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                ocultarFloatButtonCimaBaixo(true, View.GONE);
            else
                ocultarFloatButtonCimaBaixo(false, View.VISIBLE);
        });
        binding.switchOcultarFloatCimaBaixo.setChecked(Ultilitario.getBooleanPreference(requireContext(), "produto"));
        produtoViewModel.getListaProdutosisExport().observe(getViewLifecycleOwner(), new EventObserver<>(prod ->
        {
            StringBuilder dt = new StringBuilder();
            if (prod.isEmpty()) {
                Ultilitario.showToast(getContext(), Color.rgb(254, 207, 65), getString(R.string.produto_nao_encontrado), R.drawable.ic_toast_erro);
            } else {
                for (Produto produto : prod) {
                    dt.append(produto.getNome()).append(",").append(produto.getTipo()).append(",").append(produto.getUnidade()).append(",").append(produto.getCodigoMotivoIsencao()).append(",").append(produto.getPercentagemIva()).append(",").append(produto.getPreco()).append(",").append(produto.getPrecofornecedor()).append(",").append(produto.getQuantidade()).append(",").append(produto.getCodigoBarra()).append(",").append(produto.isIva()).append(",").append(produto.getEstado()).append(",").append(produto.isStock()).append(",").append(produto.getIdcategoria()).append("\n");
                }
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
                        binding.btnCriarProdutoFragment.setVisibility(View.GONE);
                    }
                }
                if (isLixeira) {
                    menu.findItem(R.id.dialogCriarProduto).setVisible(false);
                    menu.findItem(R.id.dialogFiltrarProduto).setVisible(false);
                    menu.findItem(R.id.btnScannerBack).setVisible(false);
                    menu.findItem(R.id.exportarproduto).setVisible(false);
                    menu.findItem(R.id.importarproduto).setVisible(false);
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
                        if (newText.isEmpty()) {
                            consultarProdutos(false, null, false);
                        } else {
                            consultarProdutos(true, newText, true);
                        }
                        return false;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.dialogCriarProduto) {
                    if (!categoria.isEmpty()) {
                        createProduto(idcategoria, categoria);
                    }
                } else if (itemId == R.id.dialogFiltrarProduto) {
                    filtrarProduto(idcategoria);
                } else if (itemId == R.id.btnScannerBack) {
                    scanearCodigoQr();
                } else if (itemId == R.id.exportarproduto) {
                    exportarProduto();
                } else if (itemId == R.id.importarproduto) {
                    importarProdutos();
                } else if (itemId == R.id.btnEliminarTodosLixo) {
                    dialogEliminarReataurarTodasProdutosLixeira(getString(R.string.elim_pds), getString(R.string.tem_cert_elim_pds), true);
                } else if (itemId == R.id.btnRestaurarTodosLixo) {
                    dialogEliminarReataurarTodasProdutosLixeira(getString(R.string.rest_pds), getString(R.string.rest_tdos_prods), false);
                }
                return false;
            }
        }, getViewLifecycleOwner());
        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> {
            consultarProdutos(false, null, false);
            pagingAdapter.notifyDataSetChanged();
        });
        return binding.getRoot();
    }

    private void ocultarFloatButtonCimaBaixo(boolean switchHidden, int view) {
        Ultilitario.setBooleanPreference(requireContext(), switchHidden, "produto");
        binding.floatingActionButtonCima.setVisibility(view);
        binding.floatingActionButtonBaixo.setVisibility(view);
    }

    private void exportarProdutos(String nomeCategoria, boolean isLocal) {
        if (isLocal) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Ultilitario.exportarLocal(exportProductActivityResultLauncher, getActivity(), nomeCategoria, Ultilitario.getDateCurrent());
            } else {
                Ultilitario.alertDialog(getString(R.string.avs), getString(R.string.exp_dis_api_sup), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
        } else {
            Ultilitario.exportarNuvem(getContext(), data, nomeCategoria + Ultilitario.getDateCurrent() + "_produtos.csv", nomeCategoria, Ultilitario.getDateCurrent());
        }
    }

    private void importarProdutos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Ultilitario.importarCategoriasProdutosClientes(importProductActivityResultLauncher, getActivity());
        } else {
            Ultilitario.alertDialog(getString(R.string.avs), getString(R.string.imp_dis_api_sup), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
        }
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
                handler.post(() -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
        dialogInterface.dismiss();
    }

    private void dialogEliminarReataurarTodasProdutosLixeira(String titulo, String msg, boolean isEliminar) {
        produtoViewModel.crud = true;
        if (vazio) {
            Snackbar.make(binding.mySwipeRefreshLayout, getString(R.string.lx_vz), Snackbar.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
            alert.setTitle(titulo);
            alert.setMessage(msg);
            alert.setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss());
            if (isEliminar) {
                Produto prod = new Produto();
                prod.setIdcategoria(idcategoria);
                alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> produtoViewModel.eliminarProduto(prod, false, null, true));
            } else {
                alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> produtoViewModel.restaurarProduto(Ultilitario.UM, 0, true));
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

    private void consultarProdutos(boolean iScrud, String produto, boolean isPesquisa) {
        produtoViewModel.crud = iScrud;
        produtoViewModel.consultarProdutos(idcategoria, produto, isLixeira, isPesquisa, getViewLifecycleOwner(), false, false);
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
                h.binding.txtNomeProduto.setText(produto.getNome());
                h.binding.txtPrecoProduto.setText(getText(R.string.preco) + ": " + Ultilitario.formatPreco(String.valueOf(produto.getPreco())) + " " + getString(R.string.iva) + ": " + produto.getPercentagemIva() + "%");
                h.binding.txtPrecoProdutoFornecedor.setText(getText(R.string.preco_fornecedor) + ": " + Ultilitario.formatPreco(String.valueOf(produto.getPrecofornecedor())));
                h.binding.txtQuantidadeProduto.setText(getText(R.string.quantidade) + ": " + produto.getQuantidade());
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
                        } else {
                            Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    h.binding.btnEntrar.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                        menu.setHeaderTitle(produto.getNome());
                        menu.add(getString(R.string.editar)).setOnMenuItemClickListener(item -> {
                            entrarProduto(getItem(position));
                            return false;
                        });
                        if (getArguments() != null) {
                            if (getArguments().getBoolean("master") || isMaster) {
                                menu.add(getString(R.string.env_lx)).setOnMenuItemClickListener(item -> {
                                    caixaDialogo(produto, getString(R.string.env_lx) + " (" + produto.getNome() + ")", R.string.env_prod_p_lix, false);
                                    return false;
                                });
                                menu.add(getString(R.string.eliminar_produto)).setOnMenuItemClickListener(item -> {
                                    caixaDialogo(produto, getString(R.string.elim_prod_perm) + " (" + produto.getNome() + ")", R.string.env_prod_n_lix, true);
                                    return false;
                                });
                            }
                        } else {
                            Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
                        }
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
                    .setTitle(getString(R.string.rest) + " (" + nome + ")")
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> produtoViewModel.restaurarProduto(Ultilitario.UM, idproduto, false))
                    .show();
        }

        private void dialogEliminarProduto(String nome, Produto produto) {
            produtoViewModel.crud = true;
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.eliminar) + " (" + nome + ")")
                    .setMessage(getString(R.string.tem_certeza_eliminar_produto))
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> produtoViewModel.eliminarProduto(produto, false, null, false))
                    .show();
        }

        private void caixaDialogo(Produto produto, String titulo, int mensagem, boolean permanente) {
            produtoViewModel.crud = true;
            produto.setId(produto.getId());
            produto.setEstado(Ultilitario.TRES);
            produto.setData_elimina(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getContext());
            alert.setTitle(titulo);
            alert.setMessage(getString(mensagem));
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
                } else {
                    Toast.makeText(requireActivity(), R.string.scaner_code_bar_cancelado, Toast.LENGTH_SHORT).show();
                }
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
                                .setMessage(uri.getPath())
                                .setNegativeButton(getString(R.string.cancelar), (dialogInterface, i) -> dialogInterface.dismiss())
                                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                                    try {
                                        readTextFromUri(uri);
                                    } catch (IOException e) {
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
                         new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    produtos.add(line);
                }
            } catch (FileNotFoundException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            produtoViewModel.crud = true;
            produtoViewModel.importarProdutos(produtos, false, this.idcategoria);
        });
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
}
