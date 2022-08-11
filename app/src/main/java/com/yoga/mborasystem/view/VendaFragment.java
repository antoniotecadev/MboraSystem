package com.yoga.mborasystem.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import com.yoga.mborasystem.model.entidade.Venda;
import com.yoga.mborasystem.util.EventObserver;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.VendaViewModel;

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
public class VendaFragment extends Fragment {

    private ExecutorService executor;
    private boolean vazio;
    private String data = "";
    private int quantidade;
    private GroupAdapter adapter;
    private StringBuilder dataBuilder;
    private long idcliente, idusuario;
    private VendaAdapter vendaAdapter;
    private VendaViewModel vendaViewModel;
    private String nomeUsuario, nomeCliente;
    private FragmentVendaListBinding binding;
    private boolean isLocal, isDivida, isLixeira, isMaster;

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

        if (idcliente > 0) {
            requireActivity().setTitle(nomeCliente);
        } else if (idusuario > 0) {
            requireActivity().setTitle(nomeUsuario);
        } else {
            requireActivity().setTitle(getString(R.string.vds));
        }
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVendaListBinding.inflate(inflater, container, false);

        isMaster = VendaFragmentArgs.fromBundle(getArguments()).getIsMaster();
        isLixeira = VendaFragmentArgs.fromBundle(getArguments()).getIsLixeira();

        if (isLixeira) {
            requireActivity().setTitle(getString(R.string.lix) + " (" + getString(R.string.venda) + ")");
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
                    if (idcliente > 0) {
                        requireActivity().setTitle(nomeCliente);
                    } else if (idusuario > 0) {
                        requireActivity().setTitle(nomeUsuario);
                    } else {
                        requireActivity().setTitle(getString(R.string.vds));
                    }
                    vendaViewModel.getQuantidadeVenda(isLixeira, idcliente, false, idusuario, false, null, getViewLifecycleOwner());
                    consultarVendas(false, false, false, null);
                    break;
                case R.id.vdDvd:
                    isDivida = true;
                    if (idcliente > 0) {
                        requireActivity().setTitle(nomeCliente);
                    } else if (idusuario > 0) {
                        requireActivity().setTitle(nomeUsuario);
                    } else {
                        requireActivity().setTitle(getString(R.string.dvd));
                    }
                    vendaViewModel.getQuantidadeVenda(isLixeira, idcliente, true, idusuario, false, null, getViewLifecycleOwner());
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

        vendaViewModel.getQuantidadeVenda(isLixeira, idcliente, isDivida, idusuario, false, null, getViewLifecycleOwner());
        vendaViewModel.getQuantidadeVenda().observe(getViewLifecycleOwner(), quantidade -> {
            this.quantidade = quantidade.intValue();
            vazio = quantidade == 0;
            binding.chipQuantVenda.setText(String.valueOf(quantidade));
            binding.recyclerViewListaVenda.setAdapter(quantidade == 0 ? Ultilitario.naoEncontrado(getContext(), adapter, R.string.produto_nao_encontrada) : vendaAdapter);
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
                ocultarFloatButtonCimaBaixo(true, View.GONE);
            else
                ocultarFloatButtonCimaBaixo(false, View.VISIBLE);
        });
        binding.switchOcultarFloatCimaBaixo.setChecked(Ultilitario.getBooleanPreference(requireContext(), "venda"));

        vendaViewModel.getSelectedDataMutableLiveData().setValue(false);
        vendaViewModel.getSelectedDataMutableLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                Navigation.findNavController(requireView()).navigate(R.id.action_dialogExportarImportarVenda_to_datePickerExpImp2);
            }
        });

        vendaViewModel.getDataExportAppLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(data -> {
            this.data = data;
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.dat_sel))
                    .setMessage(getString(R.string.exp_v) + " " + data)
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss()).show();
        }));

        vendaViewModel.getExportarLocalLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(aBoolean -> {
            if (this.data.isEmpty()) {
                Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.selec_data), R.drawable.ic_toast_erro);
            } else {
                executor = Executors.newSingleThreadExecutor();
                isLocal = aBoolean;
                executor.execute(() -> vendaViewModel.getVendasPorDataExport(this.data));
            }
        }));
        vendaViewModel.getVendasParaExportar().observe(getViewLifecycleOwner(), new EventObserver<>(vendas -> {
            StringBuilder dt = new StringBuilder();
            if (vendas.isEmpty()) {
                Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.nao_tem_venda), R.drawable.ic_toast_erro);
            } else {
                for (Venda venda : vendas) {
                    dt.append(venda.getNome_cliente()).append(",").append(venda.getCodigo_qr()).append(",").append(venda.getQuantidade()).append(",").append(venda.getTotal_venda()).append(",").append(venda.getDesconto()).append(",").append(venda.getTotal_desconto()).append(",").append(venda.getValor_pago()).append(",").append(venda.getDivida()).append(",").append(venda.getValor_base()).append(",").append(venda.getValor_iva()).append(",").append(venda.getPagamento()).append(",").append(venda.getData_cria()).append(",").append(venda.getIdoperador()).append(",").append(venda.getIdclicant()).append(",").append(venda.getData_elimina()).append(",").append(venda.getEstado()).append("\n");
                }
                dataBuilder = dt;
                if (isLocal) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        Ultilitario.exportarLocal(exportVendaActivityResultLauncher, getActivity(), getString(R.string.vendas), this.data);
                    } else {
                        Ultilitario.alertDialog(getString(R.string.avs), getString(R.string.exp_dis_api_sup), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                    }
                } else {
                    Ultilitario.exportarNuvem(getContext(), dataBuilder, "vendas.csv", getString(R.string.vendas), this.data);
                }
            }
            this.data = "";
        }));
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_venda, menu);
                if (isLixeira) {
                    menu.findItem(R.id.exportarvenda).setVisible(false);
                    menu.findItem(R.id.importarvenda).setVisible(false);
                } else {
                    menu.findItem(R.id.btnEliminarTodosLixo).setVisible(false);
                    menu.findItem(R.id.btnRestaurarTodosLixo).setVisible(false);
                }
                if (getArguments() != null) {
                    if (!getArguments().getBoolean("master")) {
                        menu.findItem(R.id.exportarvenda).setVisible(false);
                        menu.findItem(R.id.importarvenda).setVisible(false);
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
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
                        if (newText.isEmpty()) {
                            consultarVendas(false, isDivida, false, null);
                        } else {
                            consultarVendas(true, isDivida, true, newText);
                        }
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
                        VendaFragmentDirections.ActionVendaFragmentToDatePickerFragment direction = VendaFragmentDirections.actionVendaFragmentToDatePickerFragment(true).setIdcliente(idcliente).setIsDivida(isDivida).setIdusuario(idusuario).setIsLixeira(isLixeira);
                        Navigation.findNavController(requireView()).navigate(direction);
                        break;
                    case R.id.exportarvenda:
                        exportarVenda();
                        break;
                    case R.id.importarvenda:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            //Importa as vendas
                            Ultilitario.importarCategoriasProdutosClientes(importVendaActivityResultLauncher, null);
                        } else {
                            Ultilitario.alertDialog(getString(R.string.avs), getString(R.string.imp_dis_api_sup), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        }
                        break;
                    case R.id.btnEliminarTodosLixo:
                        dialogEliminarReataurarTodasVendasLixeira(getString(R.string.elim_vends), getString(R.string.tem_cert_elim_vds), true);
                        break;
                    case R.id.btnRestaurarTodosLixo:
                        dialogEliminarReataurarTodasVendasLixeira(getString(R.string.rest_vds), getString(R.string.rest_tdas_vds), false);
                        break;
                    default:
                        break;
                }
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        }, getViewLifecycleOwner());
        return binding.getRoot();
    }

    private void ocultarFloatButtonCimaBaixo(boolean switchHidden, int view) {
        Ultilitario.setBooleanPreference(requireContext(), switchHidden, "venda");
        binding.floatingActionButtonCima.setVisibility(view);
        binding.floatingActionButtonBaixo.setVisibility(view);
    }

    private void consultarVendas(boolean isCrud, boolean isDivida, boolean isPesquisa, String venda) {
        vendaViewModel.crud = isCrud;
        vendaViewModel.consultarVendas(getViewLifecycleOwner(), idcliente, isDivida, idusuario, isLixeira, isPesquisa, venda, false, null);
    }

    private void scanearCodigoQr() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(requireActivity());
        intentIntegrator.setPrompt(getString(R.string.alinhar_codigo_qr));
        intentIntegrator.setCameraId(0);
        zxingActivityResultLauncher.launch(intentIntegrator.createScanIntent());
    }

    class VendaAdapter extends PagingDataAdapter<Venda, VendaAdapter.VendaViewHolder> {
        private TextView divida;

        public VendaAdapter(@NonNull DiffUtil.ItemCallback<Venda> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public VendaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VendaViewHolder(FragmentVendaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VendaViewHolder h, int position) {
            Venda venda = getItem(position);
            if (venda != null) {
                divida = h.binding.textDivida;
                if (venda.getDivida() > 0)
                    h.binding.textDivida.setBackgroundColor(Color.RED);

                h.binding.textCliente.setText(venda.getNome_cliente());
                h.binding.textReferencia.setText(venda.getCodigo_qr());
                h.binding.textQtProd.setText(String.valueOf(venda.getQuantidade()));
                h.binding.textTotVend.setText(Ultilitario.formatPreco(String.valueOf(venda.getTotal_venda())));
                h.binding.textDesc.setText(Ultilitario.formatPreco(String.valueOf(venda.getDesconto())));
                h.binding.textTotDesc.setText(Ultilitario.formatPreco(String.valueOf(venda.getTotal_desconto())));
                h.binding.textPago.setText(Ultilitario.formatPreco(String.valueOf(venda.getValor_pago())));
                h.binding.textDivida.setText(Ultilitario.formatPreco(String.valueOf(venda.getDivida())));
                h.binding.textValBas.setText(Ultilitario.formatPreco(String.valueOf(venda.getValor_base())));
                h.binding.textVaIva.setText(Ultilitario.formatPreco(String.valueOf(venda.getValor_iva())));
                h.binding.textForPag.setText(venda.getPagamento());
                h.binding.textDatVen.setText(venda.getData_cria());
                h.binding.textOper.setText((venda.getIdoperador() > 0 ? " MSU" + venda.getIdoperador() : " MSA" + venda.getIdoperador()));
                h.binding.btnEntrar.setOnClickListener(v -> {
                    VendaFragmentDirections.ActionVendaFragmentToListaProdutoVendaFragment directions = VendaFragmentDirections.actionVendaFragmentToListaProdutoVendaFragment(venda.getQuantidade(), venda.getCodigo_qr()).setIdvenda(venda.getId()).setVendaTotal(venda.getTotal_venda());
                    Navigation.findNavController(requireView()).navigate(directions);
                });
                h.binding.btnEntrar.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                    menu.setHeaderTitle(venda.getCodigo_qr());
                    if (!isLixeira) {
                        menu.add(getString(R.string.ver_prod)).setOnMenuItemClickListener(item -> {
                            VendaFragmentDirections.ActionVendaFragmentToListaProdutoVendaFragment directions = VendaFragmentDirections.actionVendaFragmentToListaProdutoVendaFragment(venda.getQuantidade(), venda.getCodigo_qr()).setIdvenda(venda.getId()).setVendaTotal(venda.getTotal_venda());
                            Navigation.findNavController(requireView()).navigate(directions);
                            return false;
                        });//groupId, itemId, order, title
                        if (getArguments() != null) {
                            if (getArguments().getBoolean("master")) {
                                menu.add(getString(R.string.liq_div)).setOnMenuItemClickListener(item -> {
                                    if (venda.getDivida() == 0)
                                        Snackbar.make(requireView(), getText(R.string.sem_dvd), Snackbar.LENGTH_LONG).show();
                                    else
                                        caixaDialogo(getString(R.string.liq_div) + " (" + venda.getCodigo_qr() + ")", R.string.enc_div_vend, true, false, venda);
                                    return false;
                                });
                                menu.add(getString(R.string.env_lx)).setOnMenuItemClickListener(item -> {
                                    caixaDialogo(getString(R.string.env_lx) + " (" + venda.getCodigo_qr() + ")", R.string.env_vend_lix, false, false, venda);
                                    return false;
                                });
                                menu.add(getString(R.string.elim_vend)).setOnMenuItemClickListener(item -> {
                                    caixaDialogo(getString(R.string.elim_vend_perm) + " (" + venda.getCodigo_qr() + ")", R.string.env_vend_n_lix, false, true, venda);
                                    return false;
                                });
                            }
                        } else {
                            Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if (getArguments() != null) {
                            if (getArguments().getBoolean("master") || isMaster) {
                                menu.add(getString(R.string.rest)).setOnMenuItemClickListener(item -> {
                                    restaurarVenda(venda.getCodigo_qr(), venda.getId());
                                    return false;
                                });
                                menu.add(getString(R.string.eliminar)).setOnMenuItemClickListener(item -> {
                                    dialogEliminarVenda(getString(R.string.cert_elim_vend), venda);
                                    return false;
                                });
                                menu.add("Add " + getString(R.string.lix) + ": " + venda.getData_elimina()).setEnabled(false).setOnMenuItemClickListener(item -> false);
                            }
                        } else {
                            Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
                        }
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

        private void dialogEliminarVenda(String msg, Venda venda) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.elim_vend) + " (" + venda.getCodigo_qr() + ")")
                    .setMessage(msg)
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> {
                        vendaViewModel.crud = true;
                        vendaViewModel.eliminarVendaLixeira(Ultilitario.TRES, Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()), venda, true, false);
                    })
                    .show();
        }

        private void restaurarVenda(String codigoQr, long idvenda) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.rest) + " (" + codigoQr + ")")
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> {
                        vendaViewModel.crud = true;
                        vendaViewModel.restaurarVenda(Ultilitario.UM, idvenda, false);
                    })
                    .show();
        }

        private void caixaDialogo(String titulo, int mensagem, boolean isliquidar, boolean permanente, Venda venda) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(titulo);
            alert.setMessage(getString(mensagem));
            FrameLayout layout = new FrameLayout(getContext());
            layout.setPadding(45, 0, 45, 0);
            final TextInputEditText editText = new TextInputEditText(requireContext());
            editText.setHint(getString(R.string.valor_kwanza));
            editText.setMaxLines(1);
            Ultilitario.precoFormat(getContext(), editText);
            editText.setText(String.valueOf(venda.getDivida()));

            layout.addView(editText);

            if (isliquidar) {
                alert.setView(layout);
            }
            alert.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                if (isliquidar) {
                    if (editText.length() < 15) {
                        if (venda.getDivida() >= Ultilitario.removerKZ(editText)) {
                            vendaViewModel.crud = true;
                            vendaViewModel.liquidarDivida(venda.getDivida() - Ultilitario.removerKZ(editText), venda.getId());
                        } else {
                            Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.vl_n_sp), R.drawable.ic_toast_erro);
                        }
                    } else {
                        Ultilitario.showToast(getContext(), Color.RED, getString(R.string.vl_inv), R.drawable.ic_toast_erro);
                        divida.setError(getString(R.string.vl_inv));
                    }
                } else {
                    vendaViewModel.crud = true;
                    vendaViewModel.eliminarVendaLixeira(Ultilitario.TRES, Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()), venda, permanente, false);
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
        if (vazio) {
            Snackbar.make(binding.myCoordinatorLayout, getString(R.string.lx_vz), Snackbar.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
            alert.setTitle(titulo);
            alert.setMessage(msg);
            alert.setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss());
            if (isEliminar) {
                alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> {
                    vendaViewModel.crud = true;
                    vendaViewModel.eliminarVendaLixeira(0, null, null, false, true);
                });
            } else {
                alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> {
                    vendaViewModel.crud = true;
                    vendaViewModel.restaurarVenda(Ultilitario.UM, 0, true);
                });
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
                } else {
                    Toast.makeText(requireActivity(), R.string.scaner_cod_qr_cancel, Toast.LENGTH_LONG).show();
                }
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
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            vendaViewModel.importarVenda(vendas);
        });
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