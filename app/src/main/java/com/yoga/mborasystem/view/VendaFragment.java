package com.yoga.mborasystem.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentVendaListBinding;
import com.yoga.mborasystem.model.entidade.Venda;
import com.yoga.mborasystem.util.EventObserver;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.VendaViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

public class VendaFragment extends Fragment {

    private boolean isLocal;
    private String data = "";
    private StringBuilder dataBuilder;
    private GroupAdapter adapter;
    private VendaViewModel vendaViewModel;
    private FragmentVendaListBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GroupAdapter();
        dataBuilder = new StringBuilder();
        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentVendaListBinding.inflate(inflater, container, false);

        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> {
            MainActivity.getProgressBar();
            vendaViewModel.consultarVendas(binding.mySwipeRefreshLayout);
        });

        binding.recyclerViewListaVenda.setAdapter(adapter);
        binding.recyclerViewListaVenda.setLayoutManager(new LinearLayoutManager(getContext()));
        vendaViewModel.consultarVendas(binding.mySwipeRefreshLayout);
        vendaViewModel.getListaVendasLiveData().observe(getViewLifecycleOwner(), vendas -> {
            binding.chipQuantVenda.setText(String.valueOf(vendas.size()));
            adapter.clear();
            if (vendas.isEmpty()) {
                Ultilitario.naoEncontrado(getContext(), adapter, R.string.venda_nao_encontrada);
            } else {
                for (Venda venda : vendas)
                    adapter.add(new ItemVenda(venda));
            }
        });

        vendaViewModel.getSelectedDataMutableLiveData().setValue(false);
        vendaViewModel.getSelectedDataMutableLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                Navigation.findNavController(getView()).navigate(R.id.action_dialogExportarImportarVenda_to_datePickerExpImp2);
            }
        });

        vendaViewModel.getDataExportAppLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(data -> {
            this.data = data;
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.dat_sel))
                    .setMessage(getString(R.string.exp_v) + " " + data)
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        dialog.dismiss();
                    }).show();
        }));

        vendaViewModel.getExportarLocalLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (this.data.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.selec_data), Toast.LENGTH_LONG).show();
            } else {
                if (aBoolean) {
                    isLocal = true;
                    vendaViewModel.getVendasPorData(this.data, true);
                } else if (!aBoolean) {
                    isLocal = false;
                    vendaViewModel.getVendasPorData(this.data, true);
                }

            }
        });
        vendaViewModel.getVendasParaExportar().observe(getViewLifecycleOwner(), new EventObserver<>(vendas -> {
            StringBuilder dt = new StringBuilder();
            if (vendas.isEmpty()) {
                Ultilitario.showToast(getContext(), Color.rgb(254, 207, 65), getString(R.string.nao_tem_venda), R.drawable.ic_toast_erro);
            } else {
                for (Venda venda : vendas) {
                    dt.append(venda.getNome_cliente() + "," + venda.getCodigo_qr() + "," + venda.getQuantidade() + "," + venda.getTotal_venda() + "," + venda.getDesconto() + "," + venda.getTotal_desconto() + "," + venda.getValor_pago() + "," + venda.getDivida() + "," + venda.getValor_base() + "," + venda.getValor_iva() + "," + venda.getPagamento() + "," + venda.getData_cria() + "," + venda.getIdoperador() + "," + venda.getIdclicant() + "," + venda.getData_elimina() + "," + venda.getEstado() + "\n");
                }
                dataBuilder = dt;
                if (isLocal) {
                    Ultilitario.exportarLocal(getActivity(), dataBuilder, "vendas.csv", getString(R.string.vendas), this.data, Ultilitario.CREATE_FILE_PRODUTO);
                } else {
                    Ultilitario.exportarNuvem(getContext(), dataBuilder, "vendas.csv", getString(R.string.vendas), this.data);
                }
            }
            this.data = "";
        }));

        return binding.getRoot();
    }

    private void scanearCodigoQr(int camera) {
        new IntentIntegrator(getActivity())
                .setPrompt(getString(R.string.alinhar_codigo_qr))
                .setOrientationLocked(false)
                .setCameraId(camera)
                .initiateScan();
    }

    class ItemVenda extends Item<GroupieViewHolder> {

        private Venda venda;
        private CardView btnEntrar;
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

            btnEntrar = viewHolder.itemView.findViewById(R.id.btnEntrar);

            nomeCliente.setText(venda.getNome_cliente());
            codigoQr.setText(venda.getCodigo_qr());
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

            btnEntrar.setOnClickListener(v -> {
                MainActivity.getProgressBar();
                VendaFragmentDirections.ActionVendaFragmentToListaProdutoVendaFragment directions = VendaFragmentDirections.actionVendaFragmentToListaProdutoVendaFragment(venda.getQuantidade()).setIdvenda(venda.getId()).setVendaTotal(venda.getTotal_venda());
                Navigation.findNavController(getView()).navigate(directions);
            });

            btnEntrar.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                menu.setHeaderTitle(venda.getCodigo_qr());
                menu.add(getString(R.string.ver_prod)).setOnMenuItemClickListener(item -> {
                    MainActivity.getProgressBar();
                    VendaFragmentDirections.ActionVendaFragmentToListaProdutoVendaFragment directions = VendaFragmentDirections.actionVendaFragmentToListaProdutoVendaFragment(venda.getQuantidade()).setIdvenda(venda.getId()).setVendaTotal(venda.getTotal_venda());
                    Navigation.findNavController(getView()).navigate(directions);
                    return false;
                });//groupId, itemId, order, title
                menu.add(getString(R.string.liq_div)).setOnMenuItemClickListener(item -> {
                    if (venda.getDivida() == Ultilitario.ZERO)
                        Snackbar.make(getView(), getText(R.string.sem_dvd), Snackbar.LENGTH_LONG).show();
                    else
                        caixaDialogo(R.string.liq_div, R.string.enc_div_vend, true);
                    return false;
                });
                menu.add(getString(R.string.elim_vend)).setOnMenuItemClickListener(item -> {
                    caixaDialogo(R.string.elim_vend, R.string.cert_elim_vend, false);
                    return false;
                });
            });
        }

        @Override
        public int getLayout() {
            return R.layout.fragment_venda;
        }

        private void caixaDialogo(int titulo, int mensagem, boolean isliquidar) {
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(titulo))
                    .setMessage(getString(mensagem))
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        MainActivity.getProgressBar();
                        if (isliquidar) {
                            vendaViewModel.liquidarDivida(Ultilitario.ZERO, venda.getId());
                        } else {
                            vendaViewModel.eliminarVendaLixeira(Ultilitario.TRES, Ultilitario.getDateCurrent(), venda.getId());
                        }
                    }).setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_venda, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getString(R.string.cod_qr));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.onActionViewExpanded();
        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                vendaViewModel.consultarVendas(binding.mySwipeRefreshLayout);
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
                    vendaViewModel.consultarVendas(binding.mySwipeRefreshLayout);
                } else {
                    vendaViewModel.searchVendas(newText);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(getActivity(), R.id.fragment);
        switch (item.getItemId()) {
            case R.id.btnScannerBack:
                scanearCodigoQr(0);
                break;
            case R.id.btnData:
                Navigation.findNavController(getView()).navigate(R.id.action_vendaFragment_to_datePickerFragment);
                break;
            case R.id.exportarvenda:
                exportarVenda(Ultilitario.EXPORTAR_VENDA);
                break;
            case R.id.importarvenda:
                Ultilitario.importarCategoriasProdutos(getActivity(), Ultilitario.QUATRO);
                break;
            default:
                break;
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    private void exportarVenda(int exportarVenda) {
        Navigation.findNavController(getView()).navigate(R.id.action_vendaFragment_to_dialogExportarImportarVenda);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent resultData) {

        if (requestCode == Ultilitario.CREATE_FILE_PRODUTO && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Ultilitario.alterDocument(uri, dataBuilder, getActivity());
                dataBuilder.delete(0, data.length());
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
        } else if (resultCode == Activity.RESULT_OK) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, resultData);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(getContext(), R.string.scaner_cod_qr_cancel, Toast.LENGTH_LONG).show();
                } else {
                    vendaViewModel.searchVendas(result.getContents());
                }
            } else {
                super.onActivityResult(requestCode, resultCode, resultData);
            }
        }

    }

    public void readTextFromUri(Uri uri) throws IOException {
        List<String> vendas = new ArrayList<>();
        try (InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                vendas.add(line);
            }
            vendaViewModel.importarVenda(vendas);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }
}