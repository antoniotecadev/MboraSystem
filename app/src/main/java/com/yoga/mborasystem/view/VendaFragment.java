package com.yoga.mborasystem.view;


import static com.yoga.mborasystem.util.FormatarDocumento.printPDF;
import static com.yoga.mborasystem.util.Ultilitario.getBooleanPreference;
import static com.yoga.mborasystem.util.Ultilitario.getDataFormatMonth;
import static com.yoga.mborasystem.util.Ultilitario.getDateCurrent;
import static com.yoga.mborasystem.util.Ultilitario.getFileName;
import static com.yoga.mborasystem.util.Ultilitario.getIntPreference;
import static com.yoga.mborasystem.util.Ultilitario.getValueWithDesconto;
import static com.yoga.mborasystem.util.Ultilitario.setIntPreference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.xwray.groupie.GroupAdapter;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentVendaBinding;
import com.yoga.mborasystem.databinding.FragmentVendaListBinding;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.model.entidade.Venda;
import com.yoga.mborasystem.util.CriarFactura;
import com.yoga.mborasystem.util.EventObserver;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.VendaViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("rawtypes")
public class VendaFragment extends Fragment {

    private ExecutorService executor;
    private boolean vazio;
    private String data = "";
    private int quantidade;
    private Cliente cliente;
    private GroupAdapter adapter;
    private Map<Long, Integer> pTtU;
    private StringBuilder dataBuilder;
    private long idcliente, idusuario;
    private VendaAdapter vendaAdapter;
    private VendaViewModel vendaViewModel;
    private String nomeUsuario, nomeCliente;
    private FragmentVendaListBinding binding;
    private boolean isLocal, isDivida, isNotaCredito, isMaster;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GroupAdapter();
        dataBuilder = new StringBuilder();
        vendaAdapter = new VendaAdapter(new VendaComparator());
        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);
        idcliente = VendaFragmentArgs.fromBundle(getArguments()).getIdcliente();
        idusuario = VendaFragmentArgs.fromBundle(getArguments()).getIdusuario();
        nomeUsuario = VendaFragmentArgs.fromBundle(getArguments()).getNomeUsuario();
        nomeCliente = VendaFragmentArgs.fromBundle(getArguments()).getNomeCliente();
        cliente = VendaFragmentArgs.fromBundle(getArguments()).getCliente();
        if (idcliente > 0)
            requireActivity().setTitle(nomeCliente);
        else if (idusuario > 0)
            requireActivity().setTitle(nomeUsuario);
        else
            requireActivity().setTitle(getString(R.string.vds));
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVendaListBinding.inflate(inflater, container, false);

        isMaster = VendaFragmentArgs.fromBundle(getArguments()).getIsMaster();
        isNotaCredito = VendaFragmentArgs.fromBundle(getArguments()).getIsNotaCredito();

        if (isNotaCredito) {
            requireActivity().setTitle(getString(R.string.nt_ct));
            binding.bottomNav.setVisibility(View.GONE);
        }
        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> {
            consultarVendas(false, isDivida, false, null);
            vendaAdapter.notifyDataSetChanged();
        });

        binding.bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.tdsVd:
                    isDivida = false;
                    if (idcliente > 0)
                        requireActivity().setTitle(nomeCliente);
                    else if (idusuario > 0)
                        requireActivity().setTitle(nomeUsuario);
                    else
                        requireActivity().setTitle(getString(R.string.vds));

                    vendaViewModel.getQuantidadeVenda(isNotaCredito, idcliente, false, idusuario, false, null, getViewLifecycleOwner());
                    consultarVendas(false, false, false, null);
                    break;
                case R.id.vdDvd:
                    isDivida = true;
                    if (idcliente > 0)
                        requireActivity().setTitle(nomeCliente);
                    else if (idusuario > 0)
                        requireActivity().setTitle(nomeUsuario);
                    else
                        requireActivity().setTitle(getString(R.string.dvd));

                    vendaViewModel.getQuantidadeVenda(isNotaCredito, idcliente, true, idusuario, false, null, getViewLifecycleOwner());
                    consultarVendas(false, true, false, null);
                    break;
                default:
                    break;
            }
            return true;
        });

        binding.recyclerViewListaVenda.setAdapter(vendaAdapter);
        binding.recyclerViewListaVenda.setHasFixedSize(true);
        binding.recyclerViewListaVenda.setLayoutManager(new LinearLayoutManager(getContext()));

        vendaViewModel.getQuantidadeVenda(isNotaCredito, idcliente, isDivida, idusuario, false, null, getViewLifecycleOwner());
        vendaViewModel.getQuantidadeVenda().observe(getViewLifecycleOwner(), quantidade -> {
            this.quantidade = quantidade.intValue();
            vazio = quantidade == 0;
            binding.chipQuantVenda.setText(String.valueOf(quantidade));
            binding.recyclerViewListaVenda.setAdapter(quantidade == 0 ? Ultilitario.naoEncontrado(getContext(), adapter, R.string.venda_nao_encontrada) : vendaAdapter);
        });

        consultarVendas(false, false, false, null);
        vendaViewModel.getListaVendasLiveData().observe(getViewLifecycleOwner(), vendas -> {
            vendaAdapter.submitData(getLifecycle(), vendas);
            Ultilitario.swipeRefreshLayout(binding.mySwipeRefreshLayout);
        });
        binding.floatingActionButtonCima.setOnClickListener(view -> binding.recyclerViewListaVenda.smoothScrollToPosition(0));
        binding.floatingActionButtonBaixo.setOnClickListener(view -> binding.recyclerViewListaVenda.smoothScrollToPosition(quantidade));
        binding.switchOcultarFloatCimaBaixo.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                ocultarFloatButtonCimaBaixo(true, View.VISIBLE);
            else
                ocultarFloatButtonCimaBaixo(false, View.GONE);
        });
        binding.switchOcultarFloatCimaBaixo.setChecked(getBooleanPreference(requireContext(), "sale_list_scroll"));

        vendaViewModel.getSelectedDataMutableLiveData().setValue(false);
        vendaViewModel.getSelectedDataMutableLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean)
                Navigation.findNavController(requireView()).navigate(R.id.action_dialogExportarImportarVenda_to_datePickerExpImp2);
        });

        vendaViewModel.getDataExportAppLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(data -> {
            this.data = data;
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.dat_sel))
                    .setMessage(getString(R.string.exp_v) + " " + data)
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss()).show();
        }));

        vendaViewModel.getExportarLocalLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(aBoolean -> {
            if (this.data.isEmpty())
                Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.selec_data), R.drawable.ic_toast_erro);
            else {
                executor = Executors.newSingleThreadExecutor();
                isLocal = aBoolean;
                executor.execute(() -> vendaViewModel.getVendasPorDataExport(this.data));
            }
        }));
        vendaViewModel.getVendasParaExportar().observe(getViewLifecycleOwner(), new EventObserver<>(vendas -> {
            StringBuilder dt = new StringBuilder();
            if (vendas.isEmpty())
                Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.nao_tem_venda), R.drawable.ic_toast_erro);
            else {
                for (Venda venda : vendas)
                    dt.append(venda.getNome_cliente()).append(",").append(venda.getReferenciaFactura()).append(",").append(venda.getQuantidade()).append(",").append(venda.getTotal_venda()).append(",").append(venda.getDesconto()).append(",").append(venda.getTotal_desconto()).append(",").append(venda.getValor_pago()).append(",").append(venda.getDivida()).append(",").append(venda.getValor_base()).append(",").append(venda.getValor_iva()).append(",").append(venda.getPagamento()).append(",").append(venda.getData_cria_hora()).append(",").append(venda.getIdoperador()).append(",").append(venda.getIdclicant()).append(",").append(venda.getData_elimina()).append(",").append(venda.getEstado()).append(",").append(venda.getPercentagemDesconto()).append("\n");

                dataBuilder = dt;
                if (isLocal) {
                    Ultilitario.exportarLocal(exportVendaActivityResultLauncher, getActivity(), getString(R.string.vendas), this.data);
                } else
                    Ultilitario.exportarNuvem(getContext(), dataBuilder, "vendas.csv", getString(R.string.vendas), this.data);
            }
            this.data = "";
        }));
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_venda, menu);
                if (isNotaCredito) {
                    menu.findItem(R.id.exportarvenda).setVisible(false);
                    menu.findItem(R.id.importarvenda).setVisible(false);
                    if (!isMaster) {
                        menu.findItem(R.id.btnEliminarTodosLixo).setVisible(false);
                        menu.findItem(R.id.btnRestaurarTodosLixo).setVisible(false);
                    }
                } else {
                    menu.findItem(R.id.btnEliminarTodosLixo).setVisible(false);
                    menu.findItem(R.id.btnRestaurarTodosLixo).setVisible(false);
                }
                if (!isMaster) {
                    menu.findItem(R.id.exportarvenda).setVisible(false);
                    menu.findItem(R.id.importarvenda).setVisible(false);
                }

                SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
                MenuItem menuItem = menu.findItem(R.id.app_bar_search);
                SearchView searchView = (SearchView) menuItem.getActionView();
                searchView.setQueryHint(getString(R.string.referencia));
                searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
                searchView.onActionViewExpanded();
                menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        consultarVendas(false, isDivida, false, null);
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
                            consultarVendas(false, isDivida, false, null);
                        else
                            consultarVendas(true, isDivida, true, newText);
                        return false;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
                switch (menuItem.getItemId()) {
                    case R.id.btnScannerBack:
                        scanearCodigoQr();
                        break;
                    case R.id.btnData:
                        VendaFragmentDirections.ActionVendaFragmentToDatePickerFragment direction = VendaFragmentDirections.actionVendaFragmentToDatePickerFragment(true).setIdcliente(idcliente).setIsDivida(isDivida).setIdusuario(idusuario).setIsNotaCredito(isNotaCredito);
                        Navigation.findNavController(requireView()).navigate(direction);
                        break;
                    case R.id.exportarvenda:
                        exportarVenda();
                        break;
                    case R.id.importarvenda:
                        //Importa as vendas
                        Ultilitario.importarCategoriasProdutosClientes(importVendaActivityResultLauncher, null, false);
                        break;
                    case R.id.btnEliminarTodosLixo:
                        dialogEliminarReataurarTodasVendasLixeira(getString(R.string.elim_vends), getString(R.string.tem_cert_elim_vds), true);
                        break;
                    case R.id.btnRestaurarTodosLixo:
                        dialogEliminarReataurarTodasVendasLixeira(getString(R.string.rest_vds), getString(R.string.rest_tdas_vds), false);
                        break;
                    case R.id.calculadoraFragmentItem:
                        Navigation.findNavController(requireView()).navigate(R.id.action_vendaFragment_to_calculadoraFragment);
                        break;
                    default:
                        break;
                }
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        }, getViewLifecycleOwner());

        vendaViewModel.getPrintNCLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(venda -> imprimirFacturaNotaCredito(venda, false, true, false)));

        return binding.getRoot();
    }

    private void ocultarFloatButtonCimaBaixo(boolean switchHidden, int view) {
        Ultilitario.setBooleanPreference(requireContext(), switchHidden, "sale_list_scroll");
        binding.floatingActionButtonCima.setVisibility(view);
        binding.floatingActionButtonBaixo.setVisibility(view);
    }

    private void consultarVendas(boolean isCrud, boolean isDivida, boolean isPesquisa, String venda) {
        vendaViewModel.crud = isCrud;
        vendaViewModel.consultarVendas(getViewLifecycleOwner(), idcliente, isDivida, idusuario, isNotaCredito, isPesquisa, venda, false, null);
    }

    private void scanearCodigoQr() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(requireActivity());
        intentIntegrator.setPrompt(getString(R.string.alinhar_codigo_qr));
        intentIntegrator.setCameraId(0);
        zxingActivityResultLauncher.launch(intentIntegrator.createScanIntent());
    }

    class VendaAdapter extends PagingDataAdapter<Venda, VendaAdapter.VendaViewHolder> {

        public VendaAdapter(@NonNull DiffUtil.ItemCallback<Venda> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public VendaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VendaViewHolder(FragmentVendaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull VendaViewHolder h, int position) {
            Venda venda = getItem(position);
            if (venda != null) {
                if (venda.getDivida() > 0)
                    h.binding.textDivida.setBackgroundColor(Color.RED);

                h.binding.textCliente.setText(TextUtils.split(venda.getNome_cliente(), "-")[0]);
                h.binding.textReferencia.setText(isNotaCredito ? venda.getReferenciaNC() : venda.getReferenciaFactura());
                h.binding.textQtProd.setText(String.valueOf(venda.getQuantidade()));
                h.binding.textTotVend.setText(Ultilitario.formatPreco(String.valueOf(venda.getTotal_venda())));
                h.binding.textView27.setText(getString(R.string.desconto) + "(" + venda.getPercentagemDesconto() + "%)");
                h.binding.textDesc.setText(Ultilitario.formatPreco(String.valueOf(venda.getDesconto())));
                h.binding.textTotDesc.setText(Ultilitario.formatPreco(String.valueOf(venda.getTotal_desconto())));
                h.binding.textPago.setText(Ultilitario.formatPreco(String.valueOf(venda.getValor_pago())));
                h.binding.textDivida.setText(Ultilitario.formatPreco(String.valueOf(venda.getDivida())));
                h.binding.textValBas.setText(Ultilitario.formatPreco(String.valueOf(venda.getDesconto() == 0 ? venda.getValor_base() : getValueWithDesconto(venda.getValor_base(), venda.getPercentagemDesconto()))));
                h.binding.textVaIva.setText(Ultilitario.formatPreco(String.valueOf(venda.getDesconto() == 0 ? venda.getValor_iva() : getValueWithDesconto(venda.getValor_iva(), venda.getPercentagemDesconto()))));
                h.binding.editTextForPag.setText(venda.getPagamento());
                try {
                    h.binding.textDatVen.setText(venda.getData_cria() + " " + TextUtils.split(venda.getData_cria_hora(), "T")[1]);
                } catch (Exception e) {
                    h.binding.textDatVen.setText(venda.getData_cria());
                }
                h.binding.textOper.setText((venda.getIdoperador() > 0 ? " MSU" + venda.getIdoperador() : " MSA0"));
                h.binding.btnEntrar.setOnClickListener(v -> {
                    VendaFragmentDirections.ActionVendaFragmentToListaProdutoVendaFragment directions = VendaFragmentDirections.actionVendaFragmentToListaProdutoVendaFragment(venda.getQuantidade(), venda.getReferenciaFactura()).setIdvenda(venda.getId()).setVendaTotal(venda.getTotal_venda());
                    Navigation.findNavController(requireView()).navigate(directions);
                });
                h.binding.btnEntrar.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                    menu.setHeaderTitle(venda.getReferenciaFactura());
                    if (!isNotaCredito) {
                        menu.add(getString(R.string.ver_prod)).setOnMenuItemClickListener(item -> {
                            VendaFragmentDirections.ActionVendaFragmentToListaProdutoVendaFragment directions = VendaFragmentDirections.actionVendaFragmentToListaProdutoVendaFragment(venda.getQuantidade(), venda.getReferenciaFactura()).setIdvenda(venda.getId()).setVendaTotal(venda.getTotal_venda());
                            Navigation.findNavController(requireView()).navigate(directions);
                            return false;
                        });//groupId, itemId, order, title
                        if (isMaster) {
                            menu.add(getString(R.string.imprimir)).setOnMenuItemClickListener(item -> {
                                imprimirFacturaNotaCredito(venda, true, false, false);
                                return false;
                            });
                            menu.add(getString(R.string.anular)).setOnMenuItemClickListener(item -> {
                                caixaDialogo(getString(R.string.nt_ct), getString(R.string.emt_nt_cd) + ":\n" + venda.getReferenciaFactura(), false, false, venda);
                                return false;
                            });
                            menu.add(getString(R.string.liq_div)).setOnMenuItemClickListener(item -> {
                                if (venda.getDivida() == 0)
                                    Snackbar.make(requireView(), getText(R.string.sem_dvd), Snackbar.LENGTH_LONG).show();
                                else
                                    caixaDialogo(getString(R.string.liq_div) + " (" + venda.getReferenciaFactura() + ")", getString(R.string.enc_div_vend), true, false, venda);
                                return false;
                            });
//                                menu.add(getString(R.string.env_lx)).setOnMenuItemClickListener(item -> {
//                                    caixaDialogo(getString(R.string.env_lx), "(" + venda.getReferenciaFactura() + ")\n" + getString(R.string.env_vend_lix), false, false, venda);
//                                    return false;
//                                });
                            menu.add(getString(R.string.elim_vend)).setOnMenuItemClickListener(item -> {
                                caixaDialogo(getString(R.string.elim_vend_perm), "(" + venda.getReferenciaFactura() + ")\n" + getString(R.string.env_vend_n_lix), false, true, venda);
                                return false;
                            });
                        }
                    } else {
                        menu.add(getString(R.string.imprimir)).setOnMenuItemClickListener(item -> {
                            imprimirFacturaNotaCredito(venda, false, true, true);
                            return false;
                        });
                        menu.add(getString(R.string.mt_nt_ct)).setOnMenuItemClickListener(item -> {
                            Ultilitario.alertDialog(getString(R.string.mt_nt_ct), venda.getMotivoEmissaoNC(),requireContext(), R.drawable.ic_baseline_dry_24);
                            return false;
                        });
//                            if (isMaster) {
//                                menu.add(getString(R.string.rest)).setOnMenuItemClickListener(item -> {
//                                    restaurarVenda(venda.getReferenciaFactura(), venda.getId());
//                                    return false;
//                                });
//                                menu.add(getString(R.string.eliminar)).setOnMenuItemClickListener(item -> {
//                                    dialogEliminarVenda(getString(R.string.cert_elim_vend), venda);
//                                    return false;
//                                });
//                                menu.add("Add " + getString(R.string.lix) + ": " + venda.getData_elimina()).setEnabled(false).setOnMenuItemClickListener(item -> false);
//                            }
                    }
                });
            }
        }

        private class VendaViewHolder extends RecyclerView.ViewHolder {
            FragmentVendaBinding binding;

            public VendaViewHolder(@NonNull FragmentVendaBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

//        private void dialogEliminarVenda(String msg, Venda venda) {
//            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
//                    .setIcon(R.drawable.ic_baseline_delete_40)
//                    .setTitle(getString(R.string.elim_vend))
//                    .setMessage("(" + venda.getReferenciaFactura() + ")\n" + msg)
//                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
//                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> {
//                        vendaViewModel.crud = true;
//                        vendaViewModel.eliminarVendaNotaCredito(3, "", Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()), venda, true, false);
//                    })
//                    .show();
//        }

//        private void restaurarVenda(String codigoQr, long idvenda) {
//            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
//                    .setIcon(android.R.drawable.ic_menu_revert)
//                    .setTitle(getString(R.string.rest) + " (" + codigoQr + ")")
//                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
//                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> {
//                        vendaViewModel.crud = true;
//                        vendaViewModel.restaurarVenda(Ultilitario.UM, idvenda, false);
//                    })
//                    .show();
//        }

        private void caixaDialogo(String titulo, String mensagem, boolean isliquidar, boolean permanente, Venda venda) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setCancelable(false);
            alert.setTitle(titulo);
            alert.setMessage(mensagem);
            final LinearLayoutCompat layout = new LinearLayoutCompat(requireContext());
            layout.setPadding(0, 0, 0, 0);
            layout.setGravity(Gravity.CENTER_HORIZONTAL);
            final TextInputEditText editText = new TextInputEditText(requireContext());
            final Button limpar = new Button(requireContext());
            limpar.setText(getText(R.string.limpar));
            if (isliquidar) {
                editText.setMaxLines(1);
                editText.setHint(getString(R.string.valor_kwanza));
                Ultilitario.precoFormat(getContext(), editText);
                editText.setText(String.valueOf(venda.getDivida()));
                limpar.setOnClickListener(view -> Ultilitario.zerarPreco(editText));
            } else {
                editText.setMaxLines(3);
                editText.setHint(getString(R.string.mt_nt_ct));
                limpar.setOnClickListener(view -> editText.setText(""));
                alert.setIcon(R.drawable.ic_baseline_dry_24);
            }
            layout.addView(editText);
            layout.addView(limpar);
            alert.setView(layout);
            alert.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        if (isliquidar) {
                            if (editText.length() < 15) {
                                if (venda.getDivida() >= Ultilitario.removerKZ(editText)) {
                                    vendaViewModel.crud = true;
                                    vendaViewModel.liquidarDivida(venda.getDivida() - Ultilitario.removerKZ(editText), venda.getId());
                                } else
                                    Ultilitario.alertDialog(titulo, getString(R.string.vl_n_sp), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                            } else
                                Ultilitario.alertDialog(titulo, getString(R.string.vl_inv), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        } else {
                            if (editText.getText().toString().trim().isEmpty()) {
                                caixaDialogo(getString(R.string.nt_ct), getString(R.string.emt_nt_cd) + ":\n" + venda.getReferenciaFactura(), false, false, venda);
                                Toast.makeText(requireContext(), getString(R.string.mt_nt_ct), Toast.LENGTH_SHORT).show();
                            } else {
                                vendaViewModel.crud = true;
                                setIntPreference(requireContext(), getIntPreference(requireContext(), "numeroserienc") + 1, "numeroserienc");
                                String refNC = "NC " + TextUtils.split(getDateCurrent(), "-")[2].trim() + "/" + getIntPreference(requireContext(), "numeroserienc");
                                vendaViewModel.vendaNotaCredito(3, editText.getText().toString(), refNC, Ultilitario.monthInglesFrances(getDateCurrent()), venda, permanente, false);
                            }
                        }
                    }).setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    static class VendaComparator extends DiffUtil.ItemCallback<Venda> {

        @Override
        public boolean areItemsTheSame(@NonNull Venda oldItem, @NonNull Venda newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Venda oldItem, @NonNull Venda newItem) {
            return oldItem.getId() == newItem.getId();
        }

    }

    private void exportarVenda() {
        VendaFragmentDirections.ActionVendaFragmentToDialogExportarImportarVenda direction = VendaFragmentDirections.actionVendaFragmentToDialogExportarImportarVenda().setIdcliente(idcliente).setIsDivida(isDivida).setIdusuario(idusuario);
        Navigation.findNavController(requireView()).navigate(direction);
    }

    private void dialogEliminarReataurarTodasVendasLixeira(String titulo, String msg, boolean isEliminar) {
        if (vazio)
            Snackbar.make(binding.myCoordinatorLayout, getString(R.string.lx_vz), Snackbar.LENGTH_LONG).show();
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
                alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> {
                    vendaViewModel.crud = true;
                    Venda venda = new Venda();
                    vendaViewModel.vendaNotaCredito(0, "", "", null, venda, false, true);
                });
            } else {
                alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> {
                    vendaViewModel.crud = true;
                    vendaViewModel.restaurarVenda(Ultilitario.UM, 0, true);
                });
            }
            if (isMaster)
                alert.show();
            else
                Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.nao_alt_ope), requireContext(), R.drawable.ic_baseline_privacy_tip_24);

        }
    }

    ActivityResultLauncher<Intent> importVendaActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri uri;
                    if (data != null) {
                        uri = data.getData();
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
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

    ActivityResultLauncher<Intent> exportVendaActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent resultData = result.getData();
                    Uri uri;
                    if (resultData != null) {
                        uri = resultData.getData();
                        Ultilitario.alterDocument(uri, dataBuilder, requireActivity());
                        dataBuilder.delete(0, data.length());
                    }
                }
            });
    ActivityResultLauncher<Intent> zxingActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    IntentResult r = IntentIntegrator.parseActivityResult(result.getResultCode(), data);
                    consultarVendas(true, isDivida, true, r.getContents());
                } else
                    Toast.makeText(requireActivity(), R.string.scaner_cod_qr_cancel, Toast.LENGTH_LONG).show();
            });

    public void readTextFromUri(Uri uri) throws IOException {
        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<String> vendas = new ArrayList<>();
            try (InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    vendas.add(line);
                }
            } catch (Exception e) {
                Ultilitario.alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
            vendaViewModel.importarVenda(vendas);
        });
    }

    private void imprimirFacturaNotaCredito(Venda vd, boolean isSegundaVia, boolean isAnulado, boolean isAnuladoSegundaVia) {
        pTtU = new HashMap<>();
        Map<Long, Produto> pds = new HashMap<>();
        vendaViewModel.getProdutosVenda(vd.getId(), vd.getReferenciaFactura(), null, false, false, null);
        vendaViewModel.getProdutosVendaLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(produtos -> {
            if (produtos.isEmpty())
                Toast.makeText(requireContext(), getString(R.string.produto_nao_encontrada), Toast.LENGTH_SHORT).show();
            else {
                for (ProdutoVenda pv : produtos) {
                    Produto pd = new Produto();
                    pd.setId(pv.getId());
                    pd.setNome(pv.getNome_produto());
                    pd.setTipo(pv.getTipo());
                    pd.setUnidade(pv.getUnidade());
                    pd.setCodigoMotivoIsencao(pv.getCodigoMotivoIsencao());
                    pd.setPreco(pv.getPreco_total() / pv.getQuantidade());
                    pd.setQuantidade(pv.getQuantidade());
                    pd.setCodigoBarra(pv.getCodigo_Barra());
                    pd.setPrecofornecedor(pv.getPreco_fornecedor());
                    pd.setIva(pv.isIva());
                    pd.setPercentagemIva(pv.getPercentagemIva());
                    pd.setData_cria(pv.getData_cria());
                    pds.put(pd.getId(), pd);
                    pTtU.put(pd.getId(), pv.getPreco_total());
                }
                AppCompatAutoCompleteTextView txtNomeCliente = new AppCompatAutoCompleteTextView(requireContext());
                txtNomeCliente.setText(vd.getNome_cliente());
                TextInputEditText desconto = new TextInputEditText(requireContext());
                desconto.setText(String.valueOf(vd.getDesconto()));
                int troco = vd.getValor_pago() - (vd.getTotal_venda() - vd.getDesconto());
                String ref = isAnulado ? vd.getReferenciaNC() : vd.getReferenciaFactura();
                String facturaPath = ref.replace("/", "_") + ".pdf";
                String dataCria = isAnulado ? vd.getData_cria_NC() : vd.getData_cria();
                String dataCriaHora = isAnulado ? vd.getData_cria_hora_NC() : vd.getData_cria_hora();
                String hash = isAnulado ? vd.getHashNC() : vd.getHashFR();
                CriarFactura.getPemissionAcessStoregeExternal(isSegundaVia, isAnulado, isAnuladoSegundaVia, vd.getMotivoEmissaoNC(), vd.getReferenciaFactura(), true, getActivity(), getContext(), facturaPath, cliente, vd.getIdoperador(), txtNomeCliente, desconto, vd.getPercentagemDesconto(), vd.getValor_base(), vd.getValor_iva(), vd.getPagamento(), vd.getTotal_desconto(), vd.getValor_pago(), troco, vd.getTotal_venda(), pds, pTtU, getDataFormatMonth(dataCria) + " " + TextUtils.split(dataCriaHora, "T")[1], ref, hash);
                printPDF(requireActivity(), requireContext(), facturaPath, "Facturas");
                VendaFragmentDirections.ActionVendaFragmentSelf dirSelf = VendaFragmentDirections.actionVendaFragmentSelf(cliente).setIsNotaCredito(isNotaCredito).setIsMaster(isMaster);
                Navigation.findNavController(requireView()).navigate(dirSelf);
            }
        }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (executor != null)
            executor.shutdownNow();
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }
}