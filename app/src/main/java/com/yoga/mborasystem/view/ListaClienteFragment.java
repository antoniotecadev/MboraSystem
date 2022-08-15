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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xwray.groupie.GroupAdapter;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentClienteBinding;
import com.yoga.mborasystem.databinding.FragmentListaClienteBinding;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.util.EventObserver;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteCantinaViewModel;

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
public class ListaClienteFragment extends Fragment {

    private StringBuilder data;
    private int tipo, quantidade;
    private GroupAdapter adapter;
    private ExecutorService executor;
    private ClienteCantina clienteCantina;
    private ClienteAdapter clienteAdapter;
    private FragmentListaClienteBinding binding;
    private ClienteCantinaViewModel clienteCantinaViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = new StringBuilder();
        adapter = new GroupAdapter();
        clienteCantina = new ClienteCantina();
        clienteAdapter = new ClienteAdapter(new ClienteComparator());
        clienteCantinaViewModel = new ViewModelProvider(requireActivity()).get(ClienteCantinaViewModel.class);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListaClienteBinding.inflate(inflater, container, false);

        binding.recyclerViewListaCliente.setAdapter(clienteAdapter);
        binding.recyclerViewListaCliente.setHasFixedSize(true);
        binding.recyclerViewListaCliente.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.btnCriarCliente.setOnClickListener(v -> criarCliente());
        clienteCantinaViewModel.getQuantidadeCliente().observe(getViewLifecycleOwner(), quantidade -> {
            this.quantidade = quantidade.intValue();
            binding.chipQuantidadeCliente.setText(String.valueOf(quantidade));
            binding.recyclerViewListaCliente.setAdapter(quantidade == 0 ? Ultilitario.naoEncontrado(getContext(), adapter, R.string.produto_nao_encontrada) : clienteAdapter);
        });
        consultarClientes(false, false, null);
        clienteCantinaViewModel.getListaClientesCantina().observe(getViewLifecycleOwner(), clientes -> {
            clienteAdapter.submitData(getLifecycle(), clientes);
            Ultilitario.swipeRefreshLayout(binding.mySwipeRefreshLayout);
        });
        binding.floatingActionButtonCima.setOnClickListener(view -> binding.recyclerViewListaCliente.smoothScrollToPosition(0));
        binding.floatingActionButtonBaixo.setOnClickListener(view -> binding.recyclerViewListaCliente.smoothScrollToPosition(quantidade));
        binding.switchOcultarFloatCimaBaixo.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                ocultarFloatButtonCimaBaixo(true, View.GONE);
            else
                ocultarFloatButtonCimaBaixo(false, View.VISIBLE);
        });
        binding.switchOcultarFloatCimaBaixo.setChecked(Ultilitario.getBooleanPreference(requireContext(), "clientecantina"));
        clienteCantinaViewModel.getListaClientesExport().observe(getViewLifecycleOwner(), new EventObserver<>(cliente -> {
            StringBuilder dt = new StringBuilder();
            if (cliente.isEmpty()) {
                Ultilitario.showToast(getContext(), Color.rgb(254, 207, 65), getString(R.string.cliente_nao_encontrado), R.drawable.ic_toast_erro);
            } else {
                for (ClienteCantina clienteCantina : cliente) {
                    dt.append(clienteCantina.getNome().isEmpty() ? " " : clienteCantina.getNome()).append(",").append(clienteCantina.getTelefone().isEmpty() ? " " : clienteCantina.getTelefone()).append(",").append(clienteCantina.getEmail().isEmpty() ? " " : clienteCantina.getEmail()).append(",").append(clienteCantina.getEndereco().isEmpty() ? " " : clienteCantina.getEndereco()).append(",").append(clienteCantina.getNif().isEmpty() ? " " : clienteCantina.getNif()).append("\n");
                }
                data = dt;
                exportarClientes(getString(R.string.clientes), tipo == 0 && Ultilitario.isLocal);
            }
        }));

        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> {
            consultarClientes(false, false, null);
            clienteAdapter.notifyDataSetChanged();
        });
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_cliente, menu);
                SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
                MenuItem menuItem = menu.findItem(R.id.app_bar_search);
                SearchView searchView = (SearchView) menuItem.getActionView();
                searchView.setQueryHint(getString(R.string.nm) + ", " + getString(R.string.telefone) + ", " + getString(R.string.nif));
                searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
                searchView.onActionViewExpanded();
                menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        consultarClientes(false, false, null);
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
                            consultarClientes(false, false, null);
                        } else {
                            consultarClientes(true, true, newText);
                        }
                        return false;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
                if (menuItem.getItemId() == R.id.criarClienteCantina) {
                    criarCliente();
                } else if (menuItem.getItemId() == R.id.exportarcliente) {
                    exportarCliente();
                } else if (menuItem.getItemId() == R.id.importarcliente) {
                    importarClientes();
                }
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        }, getViewLifecycleOwner());
        return binding.getRoot();
    }

    private void ocultarFloatButtonCimaBaixo(boolean switchHidden, int view) {
        Ultilitario.setBooleanPreference(requireContext(), switchHidden, "clientecantina");
        binding.floatingActionButtonCima.setVisibility(view);
        binding.floatingActionButtonBaixo.setVisibility(view);
    }

    private void consultarClientes(boolean isCrud, boolean isPesquisa, String cliente) {
        clienteCantinaViewModel.crud = isCrud;
        clienteCantinaViewModel.consultarClientesCantina(getViewLifecycleOwner(), isPesquisa, cliente);
    }

    private void exportarClientes(String nomeFicheiro, boolean isLocal) {
        if (isLocal) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Ultilitario.exportarLocal(exportClientActivityResultLauncher, getActivity(), nomeFicheiro, Ultilitario.getDateCurrent());
            } else {
                Ultilitario.alertDialog(getString(R.string.avs), getString(R.string.exp_dis_api_sup), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
        } else {
            Ultilitario.exportarNuvem(getContext(), data, "clientes.csv", nomeFicheiro, Ultilitario.getDateCurrent());
        }
    }

    private void importarClientes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Ultilitario.importarCategoriasProdutosClientes(importClientActivityResultLauncher, requireActivity());
        } else {
            Ultilitario.alertDialog(getString(R.string.avs), getString(R.string.imp_dis_api_sup), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
        }
    }

    class ClienteAdapter extends PagingDataAdapter<ClienteCantina, ClienteAdapter.ClienteViewHolder> {
        public ClienteAdapter(@NonNull DiffUtil.ItemCallback<ClienteCantina> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ClienteViewHolder(FragmentClienteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ClienteViewHolder h, int position) {
            ClienteCantina ct = getItem(position);
            if (ct != null) {
                h.itemView.setOnClickListener(v -> {
                    MainActivity.getProgressBar();
                    ListaClienteFragmentDirections.ActionListaClienteFragmentToVendaFragment direction = ListaClienteFragmentDirections.actionListaClienteFragmentToVendaFragment().setIdcliente(ct.getId()).setNomeCliente(ct.getNome());
                    Navigation.findNavController(requireView()).navigate(direction);
                });
                h.binding.txtNomeCliente.setText(ct.getNome());
                h.binding.txtTelefone.setText(getString(R.string.tel) + " " + ct.getTelefone() + " | " + getString(R.string.nif) + " " + ct.getNif());
                h.binding.textDataCriacao.setText(getString(R.string.data_cria) + ": " + ct.getData_cria());
                if (ct.getData_modifica() != null) {
                    h.binding.textDataModificacao.setVisibility(View.VISIBLE);
                    h.binding.textDataModificacao.setText(getString(R.string.data_modifica) + ": " + ct.getData_modifica());
                }
                registerForContextMenu(h.binding.imgBtnMenu);
                h.binding.imgBtnMenu.setOnClickListener(View::showContextMenu);
                h.itemView.setOnCreateContextMenuListener((menu1, v, menuInfo) -> {
                    menu1.setHeaderTitle(ct.getNome());
                    menu1.add(getString(R.string.entrar)).setOnMenuItemClickListener(item -> {
                        ListaClienteFragmentDirections.ActionListaClienteFragmentToVendaFragment direction = ListaClienteFragmentDirections.actionListaClienteFragmentToVendaFragment().setIdcliente(ct.getId()).setNomeCliente(ct.getNome());
                        Navigation.findNavController(requireView()).navigate(direction);
                        return false;
                    });//groupId, itemId, order, title
                    if (getArguments() != null) {
                        if (getArguments().getBoolean("master")) {
                            menu1.add(getString(R.string.alterar_cliente)).setOnMenuItemClickListener(item -> {
                                ListaClienteFragmentDirections.ActionListaClienteFragmentToDialogClienteCantina direction = ListaClienteFragmentDirections.actionListaClienteFragmentToDialogClienteCantina(ct.getNome(), ct.getTelefone(), ct.getId(), ct.getEmail(), ct.getEndereco(), ct.getNif());
                                Navigation.findNavController(requireView()).navigate(direction);
                                return false;
                            });
                            menu1.add(getString(R.string.eliminar_cliente)).setOnMenuItemClickListener(item -> {
                                clienteCantina.setId(ct.getId());
                                clienteCantina.setEstado(Ultilitario.TRES);
                                new AlertDialog.Builder(requireContext())
                                        .setIcon(android.R.drawable.ic_menu_delete)
                                        .setTitle(getString(R.string.eliminar) + " (" + ct.getNome() + ")")
                                        .setMessage(getString(R.string.tem_cert_elim_cli))
                                        .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                                        .setPositiveButton(getString(R.string.ok), (dialog1, which) -> clienteCantinaViewModel.eliminarCliente(clienteCantina, null))
                                        .show();
                                return false;
                            });
                        }
                    }
                });
            }
        }

        private class ClienteViewHolder extends RecyclerView.ViewHolder {
            FragmentClienteBinding binding;

            public ClienteViewHolder(@NonNull FragmentClienteBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

    }

    static class ClienteComparator extends DiffUtil.ItemCallback<ClienteCantina> {

        @Override
        public boolean areItemsTheSame(@NonNull ClienteCantina oldItem, @NonNull ClienteCantina newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ClienteCantina oldItem, @NonNull ClienteCantina newItem) {
            return oldItem.getId() == newItem.getId();
        }
    }

    private void criarCliente() {
        ListaClienteFragmentDirections.ActionListaClienteFragmentToDialogClienteCantina direction = ListaClienteFragmentDirections.actionListaClienteFragmentToDialogClienteCantina("", "", 0, "", "", "");
        Navigation.findNavController(requireView()).navigate(direction);
    }

    private void exportarCliente() {
        executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        new android.app.AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_baseline_store_24)
                .setTitle(R.string.exportar)
                .setSingleChoiceItems(R.array.array_local_nuvem, 3, (dialogInterface, i) -> {
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
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> exportarClientes(executor, handler, dialogInterface)).show();

    }

    public void exportarClientes(ExecutorService executor, Handler handler, DialogInterface dialogInterface) {
        executor.execute(() -> {
            try {
                clienteCantinaViewModel.exportarClientes();
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
        dialogInterface.dismiss();
    }

    ActivityResultLauncher<Intent> exportClientActivityResultLauncher = registerForActivityResult(
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

    ActivityResultLauncher<Intent> importClientActivityResultLauncher = registerForActivityResult(
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
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<String> clientes = new ArrayList<>();
            try (InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    clientes.add(line);
                }
            } catch (FileNotFoundException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            clienteCantinaViewModel.crud = true;
            clienteCantinaViewModel.importarClientes(clientes, handler);
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