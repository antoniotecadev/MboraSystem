package com.yoga.mborasystem.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VendaFragment extends Fragment {

    private boolean vazio;
    private String data = "";
    private GroupAdapter adapter;
    private StringBuilder dataBuilder;
    private long idcliente, idusuario;
    private VendaViewModel vendaViewModel;
    private String nomeUsuario, nomeCliente;
    private FragmentVendaListBinding binding;
    private boolean isLocal, isDivida, isLixeira, isMaster;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GroupAdapter();
        dataBuilder = new StringBuilder();
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
        setHasOptionsMenu(true);
    }

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVendaListBinding.inflate(inflater, container, false);

        isLixeira = CategoriaProdutoFragmentArgs.fromBundle(getArguments()).getIsLixeira();
        isMaster = VendaFragmentArgs.fromBundle(getArguments()).getIsMaster();

        if (isLixeira) {
            requireActivity().setTitle(getString(R.string.lix) + " (" + getString(R.string.venda) + ")");
            binding.bottomNav.setVisibility(View.GONE);
        }
        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> {
            MainActivity.getProgressBar();
            vendaViewModel.consultarVendas(binding.mySwipeRefreshLayout, idcliente, isDivida, idusuario, isLixeira);
        });

        binding.bottomNav.setOnNavigationItemReselectedListener(item -> {
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
                    Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.tds_vd), R.drawable.ic_toast_feito);
                    vendaViewModel.consultarVendas(binding.mySwipeRefreshLayout, idcliente, false, idusuario, isLixeira);
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
                    Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.vd_dvd), R.drawable.ic_toast_feito);
                    vendaViewModel.consultarVendas(binding.mySwipeRefreshLayout, idcliente, true, idusuario, isLixeira);
                    break;
                default:
                    break;
            }
        });

        binding.recyclerViewListaVenda.setAdapter(adapter);
        binding.recyclerViewListaVenda.setLayoutManager(new LinearLayoutManager(getContext()));
        vendaViewModel.consultarVendas(binding.mySwipeRefreshLayout, idcliente, false, idusuario, isLixeira);
        vendaViewModel.getListaVendasLiveData().observe(getViewLifecycleOwner(), vendas -> {
            binding.chipQuantVenda.setText(String.valueOf(vendas.size()));
            adapter.clear();
            if (vendas.isEmpty()) {
                vazio = true;
                Ultilitario.naoEncontrado(getContext(), adapter, R.string.venda_nao_encontrada);
            } else {
                vazio = false;
                for (Venda venda : vendas)
                    adapter.add(new ItemVenda(venda));
            }
        });

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
                isLocal = aBoolean;
                vendaViewModel.getVendasPorData(this.data, true, idcliente, isDivida, idusuario);
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
                    Ultilitario.exportarLocal(null, getActivity(), dataBuilder, "vendas.csv", getString(R.string.vendas), this.data, Ultilitario.CREATE_FILE_PRODUTO);
                } else {
                    Ultilitario.exportarNuvem(getContext(), dataBuilder, "vendas.csv", getString(R.string.vendas), this.data);
                }
            }
            this.data = "";
        }));

        return binding.getRoot();
    }

    private void scanearCodigoQr() {
        new IntentIntegrator(getActivity())
                .setPrompt(getString(R.string.alinhar_codigo_qr))
                .setOrientationLocked(false)
                .setCameraId(0)
                .initiateScan();
    }

    class ItemVenda extends Item<GroupieViewHolder> {

        private final Venda venda;
        private TextView divida;

        public ItemVenda(Venda venda) {
            this.venda = venda;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView nomeCliente = viewHolder.itemView.findViewById(R.id.textCliente);
            TextView codigoQr = viewHolder.itemView.findViewById(R.id.textCodBar);
            TextView quantidade = viewHolder.itemView.findViewById(R.id.textQtProd);
            TextView total = viewHolder.itemView.findViewById(R.id.textTotVend);
            TextView desconto = viewHolder.itemView.findViewById(R.id.textDesc);
            TextView totalDesc = viewHolder.itemView.findViewById(R.id.textTotDesc);
            TextView valorPago = viewHolder.itemView.findViewById(R.id.textPago);
            divida = viewHolder.itemView.findViewById(R.id.textDivida);
            TextView valorBase = viewHolder.itemView.findViewById(R.id.textValBas);
            TextView iva = viewHolder.itemView.findViewById(R.id.textVaIva);
            TextView forPag = viewHolder.itemView.findViewById(R.id.textForPag);
            TextView dataVenda = viewHolder.itemView.findViewById(R.id.textDatVen);
            TextView operador = viewHolder.itemView.findViewById(R.id.textOper);

            CardView btnEntrar = viewHolder.itemView.findViewById(R.id.btnEntrar);

            if (venda.getDivida() > 0) {
                divida.setBackgroundColor(Color.RED);
            }

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
                VendaFragmentDirections.ActionVendaFragmentToListaProdutoVendaFragment directions = VendaFragmentDirections.actionVendaFragmentToListaProdutoVendaFragment(venda.getQuantidade(), venda.getCodigo_qr()).setIdvenda(venda.getId()).setVendaTotal(venda.getTotal_venda());
                Navigation.findNavController(requireView()).navigate(directions);
            });

            btnEntrar.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                menu.setHeaderTitle(venda.getCodigo_qr());
                if (!isLixeira) {
                    menu.add(getString(R.string.ver_prod)).setOnMenuItemClickListener(item -> {
                        MainActivity.getProgressBar();
                        VendaFragmentDirections.ActionVendaFragmentToListaProdutoVendaFragment directions = VendaFragmentDirections.actionVendaFragmentToListaProdutoVendaFragment(venda.getQuantidade(), venda.getCodigo_qr()).setIdvenda(venda.getId()).setVendaTotal(venda.getTotal_venda());
                        Navigation.findNavController(requireView()).navigate(directions);
                        return false;
                    });//groupId, itemId, order, title
                    if (getArguments() != null) {
                        if (getArguments().getBoolean("master")) {
                            menu.add(getString(R.string.liq_div)).setOnMenuItemClickListener(item -> {
                                if (venda.getDivida() == Ultilitario.ZERO)
                                    Snackbar.make(requireView(), getText(R.string.sem_dvd), Snackbar.LENGTH_LONG).show();
                                else
                                    caixaDialogo(getString(R.string.liq_div) + " (" + venda.getCodigo_qr() + ")", R.string.enc_div_vend, true, false);
                                return false;
                            });
                            menu.add(getString(R.string.env_lx)).setOnMenuItemClickListener(item -> {
                                caixaDialogo(getString(R.string.env_lx) + " (" + venda.getCodigo_qr() + ")", R.string.env_vend_lix, false, false);
                                return false;
                            });
                            menu.add(getString(R.string.elim_vend)).setOnMenuItemClickListener(item -> {
                                caixaDialogo(getString(R.string.elim_vend_perm) + " (" + venda.getCodigo_qr() + ")", R.string.env_vend_n_lix, false, true);
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
                                restaurarVenda();
                                return false;
                            });
                            menu.add(getString(R.string.eliminar)).setOnMenuItemClickListener(item -> {
                                dialogEliminarVenda(getString(R.string.cert_elim_vend));
                                return false;
                            });
                            menu.add("Add " + getString(R.string.lix) + ": " + venda.getData_elimina()).setEnabled(false).setOnMenuItemClickListener(item -> {
                                return false;
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        private void dialogEliminarVenda(String msg) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.elim_vend) + " (" + venda.getCodigo_qr() + ")")
                    .setMessage(msg)
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> vendaViewModel.eliminarVendaLixeira(Ultilitario.TRES, Ultilitario.getDateCurrent(), venda, true, false))
                    .show();
        }

        private void restaurarVenda() {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.rest) + " (" + venda.getCodigo_qr() + ")")
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> vendaViewModel.restaurarVenda(Ultilitario.UM, venda.getId(), false))
                    .show();
        }

        @Override
        public int getLayout() {
            return R.layout.fragment_venda;
        }

        private void caixaDialogo(String titulo, int mensagem, boolean isliquidar, boolean permanente) {

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
                MainActivity.getProgressBar();
                if (isliquidar) {
                    if (editText.length() < 15) {
                        if (venda.getDivida() >= Ultilitario.removerKZ(editText)) {
                            vendaViewModel.liquidarDivida(venda.getDivida() - Ultilitario.removerKZ(editText), venda.getId());
                        } else {
                            MainActivity.dismissProgressBar();
                            Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.vl_n_sp), R.drawable.ic_toast_erro);
                        }
                    } else {
                        Ultilitario.showToast(getContext(), Color.RED, getString(R.string.vl_inv), R.drawable.ic_toast_erro);
                        divida.setError(getString(R.string.vl_inv));
                        MainActivity.dismissProgressBar();
                    }
                } else {
                    if (permanente) {
                        vendaViewModel.eliminarVendaLixeira(Ultilitario.TRES, Ultilitario.getDateCurrent(), venda, true, false);
                    } else {
                        vendaViewModel.eliminarVendaLixeira(Ultilitario.TRES, Ultilitario.getDateCurrent(), venda, false, false);
                    }
                }
            }).setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_venda, menu);

        if (isLixeira) {
            menu.findItem(R.id.btnScannerBack).setVisible(false);
            menu.findItem(R.id.btnData).setVisible(false);
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
        searchView.setQueryHint(getString(R.string.cod_qr));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.onActionViewExpanded();
        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                vendaViewModel.consultarVendas(binding.mySwipeRefreshLayout, idcliente, isDivida, idusuario, isLixeira);
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
                    vendaViewModel.consultarVendas(binding.mySwipeRefreshLayout, idcliente, isDivida, idusuario, isLixeira);
                } else {
                    vendaViewModel.searchVendas(newText, idcliente, isDivida, idusuario, isLixeira);
                }
                return false;
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
        switch (item.getItemId()) {
            case R.id.btnScannerBack:
                scanearCodigoQr();
                break;
            case R.id.btnData:
                VendaFragmentDirections.ActionVendaFragmentToDatePickerFragment direction = VendaFragmentDirections.actionVendaFragmentToDatePickerFragment().setIdcliente(idcliente).setIsDivida(isDivida).setIdusuario(idusuario);
                Navigation.findNavController(requireView()).navigate(direction);
                break;
            case R.id.exportarvenda:
                exportarVenda();
                break;
            case R.id.importarvenda:
                //Importa as vendas
                Ultilitario.importarCategoriasProdutos(null);
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
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
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
                alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> vendaViewModel.eliminarVendaLixeira(0, null, null, false, true));
            } else {
                alert.setPositiveButton(getString(R.string.ok), (dialog1, which) -> vendaViewModel.restaurarVenda(Ultilitario.UM, 0, true));
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

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent resultData) {

        if (requestCode == Ultilitario.CREATE_FILE_PRODUTO && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                Ultilitario.alterDocument(uri, dataBuilder, requireActivity());
                dataBuilder.delete(0, data.length());
            }
        } else if (requestCode == Ultilitario.QUATRO && resultCode == Activity.RESULT_OK) {
            Uri uri;
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
                    vendaViewModel.searchVendas(result.getContents(), idcliente, isDivida, idusuario, isLixeira);
                }
            } else {
                super.onActivityResult(requestCode, resultCode, resultData);
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    public void readTextFromUri(Uri uri) throws IOException {
        new AsyncTask<Void, Void, List<String>>() {

            @Override
            protected List<String> doInBackground(Void... voids) {
                List<String> vendas = new ArrayList<>();

                try (InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        vendas.add(line);
                    }
                } catch (
                        FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
                return vendas;
            }

            @Override
            protected void onPostExecute(List<String> vendas) {
                super.onPostExecute(vendas);
                vendaViewModel.importarVenda(vendas);
            }
        }.execute();

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