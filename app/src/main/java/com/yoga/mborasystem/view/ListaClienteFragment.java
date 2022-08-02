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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
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
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

public class ListaClienteFragment extends Fragment {

    private int tipo;
    private StringBuilder data;
    private GroupAdapter adapter;
    private ExecutorService executor;
    private ClienteCantina clienteCantina;
    private FragmentListaClienteBinding binding;
    private ClienteCantinaViewModel clienteCantinaViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = new StringBuilder();
        adapter = new GroupAdapter();
        clienteCantina = new ClienteCantina();
        clienteCantinaViewModel = new ViewModelProvider(requireActivity()).get(ClienteCantinaViewModel.class);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        binding = FragmentListaClienteBinding.inflate(inflater, container, false);

        binding.recyclerViewListaCliente.setAdapter(adapter);
        binding.recyclerViewListaCliente.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.btnCriarCliente.setOnClickListener(v -> criarCliente());

        clienteCantinaViewModel.consultarClientesCantina(binding.mySwipeRefreshLayout);
        clienteCantinaViewModel.getListaClientesCantina().observe(getViewLifecycleOwner(), clientes -> {
            binding.chipQuantidadeCliente.setText(clientes.size() + "");
            adapter.clear();
            if (clientes.isEmpty()) {
                Ultilitario.naoEncontrado(getContext(), adapter, R.string.cli_nao_enc);
            } else {
                Random random = new Random();
                for (ClienteCantina cliente : clientes)
                    adapter.add(new Item<GroupieViewHolder>() {

                        @Override
                        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
                            ImageView i = viewHolder.itemView.findViewById(R.id.imgCliente);
                            Ultilitario.colorRandomImage(i, random);

                            TextView nomeCliente = viewHolder.itemView.findViewById(R.id.txtNomeCliente);
                            TextView telefoneCliente = viewHolder.itemView.findViewById(R.id.txtTelefone);
                            TextView dataCria = viewHolder.itemView.findViewById(R.id.textDataCriacao);
                            TextView dataModifica = viewHolder.itemView.findViewById(R.id.textDataModificacao);
                            ImageButton menu = viewHolder.itemView.findViewById(R.id.imgBtnMenu);

                            viewHolder.itemView.setOnClickListener(v -> {
                                MainActivity.getProgressBar();
                                v.setBackgroundColor(Color.parseColor("#6BD3D8D7"));

                                ListaClienteFragmentDirections.ActionListaClienteFragmentToVendaFragment direction = ListaClienteFragmentDirections.actionListaClienteFragmentToVendaFragment().setIdcliente(cliente.getId()).setNomeCliente(cliente.getNome());
                                Navigation.findNavController(requireView()).navigate(direction);

                                new Handler(Looper.getMainLooper()).postDelayed(() -> v.setBackgroundColor(Color.parseColor("#FFFFFF")), 1000);
                            });

                            nomeCliente.setText(cliente.getNome());
                            telefoneCliente.setText(getString(R.string.tel) + " " + cliente.getTelefone() + " | " + getString(R.string.nif) + " " + cliente.getNif());
                            dataCria.setText(getString(R.string.data_cria) + ": " + cliente.getData_cria());
                            if (cliente.getData_modifica() != null) {
                                dataModifica.setVisibility(View.VISIBLE);
                                dataModifica.setText(getString(R.string.data_modifica) + ": " + cliente.getData_modifica());
                            }

                            registerForContextMenu(menu);
                            menu.setOnClickListener(View::showContextMenu);
                            viewHolder.itemView.setOnCreateContextMenuListener((menu1, v, menuInfo) -> {
                                menu1.setHeaderTitle(cliente.getNome());
                                menu1.add(getString(R.string.entrar)).setOnMenuItemClickListener(item -> {
                                    ListaClienteFragmentDirections.ActionListaClienteFragmentToVendaFragment direction = ListaClienteFragmentDirections.actionListaClienteFragmentToVendaFragment().setIdcliente(cliente.getId()).setNomeCliente(cliente.getNome());
                                    Navigation.findNavController(requireView()).navigate(direction);
                                    return false;
                                });//groupId, itemId, order, title
                                if (getArguments() != null) {
                                    if (getArguments().getBoolean("master")) {
                                        menu1.add(getString(R.string.alterar_cliente)).setOnMenuItemClickListener(item -> {
                                            MainActivity.getProgressBar();
                                            ListaClienteFragmentDirections.ActionListaClienteFragmentToDialogClienteCantina direction = ListaClienteFragmentDirections.actionListaClienteFragmentToDialogClienteCantina(cliente.getNome(), cliente.getTelefone(), cliente.getId(), cliente.getEmail(), cliente.getEndereco(), cliente.getNif());
                                            Navigation.findNavController(requireView()).navigate(direction);
                                            return false;
                                        });
                                        menu1.add(getString(R.string.eliminar_cliente)).setOnMenuItemClickListener(item -> {
                                            clienteCantina.setId(cliente.getId());
                                            clienteCantina.setEstado(Ultilitario.TRES);
                                            new AlertDialog.Builder(requireContext())
                                                    .setTitle(getString(R.string.eliminar) + " (" + cliente.getNome() + ")")
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

                        @Override
                        public int getLayout() {
                            return R.layout.fragment_cliente;
                        }
                    });
            }

        });

        clienteCantinaViewModel.getListaClientesExport().observe(getViewLifecycleOwner(), new EventObserver<>(cliente -> {
            StringBuilder dt = new StringBuilder();
            if (cliente.isEmpty()) {
                Ultilitario.showToast(getContext(), Color.rgb(254, 207, 65), getString(R.string.cliente_nao_encontrado), R.drawable.ic_toast_erro);
            } else {
                for (ClienteCantina clienteCantina : cliente) {
                    dt.append(clienteCantina.getNome().isEmpty() ? " " : clienteCantina.getNome()).append(",").append(clienteCantina.getTelefone().isEmpty() ? " " : clienteCantina.getTelefone()).append(",").append(clienteCantina.getEmail().isEmpty() ? " " : clienteCantina.getEmail()).append(",").append(clienteCantina.getEndereco().isEmpty() ? " " : clienteCantina.getEndereco()).append(",").append(clienteCantina.getNif().isEmpty() ? " " : clienteCantina.getNif()).append("\n");
                }
                data = dt;
                exportarClientes(getString(R.string.clientes), tipo == 0 ? Ultilitario.isLocal : false);
            }
        }));

        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> clienteCantinaViewModel.consultarClientesCantina(binding.mySwipeRefreshLayout));

        return binding.getRoot();
    }

    private void exportarClientes(String nomeFicheiro, boolean isLocal) {
        if (isLocal) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Ultilitario.exportarLocal(exportClientActivityResultLauncher, getActivity(), "clientes.csv", nomeFicheiro, Ultilitario.getDateCurrent());
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_cliente, menu);

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
                clienteCantinaViewModel.consultarClientesCantina(binding.mySwipeRefreshLayout);
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
                    clienteCantinaViewModel.consultarClientesCantina(binding.mySwipeRefreshLayout);
                } else {
                    clienteCantinaViewModel.searchCliente(newText);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
        if (item.getItemId() == R.id.criarClienteCantina) {
            criarCliente();
        } else if (item.getItemId() == R.id.exportarcliente) {
            exportarCliente();
        } else if (item.getItemId() == R.id.importarcliente) {
            importarClientes();
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    private void criarCliente() {
        MainActivity.getProgressBar();
        ListaClienteFragmentDirections.ActionListaClienteFragmentToDialogClienteCantina direction = ListaClienteFragmentDirections.actionListaClienteFragmentToDialogClienteCantina("", "", 0, "", "", "");
        Navigation.findNavController(requireView()).navigate(direction);
    }

    private void exportarCliente() {
        new android.app.AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_baseline_store_24)
                .setTitle(R.string.exportar)
                .setSingleChoiceItems(R.array.array_local_nuvem, 3, (dialogInterface, i) -> {
                    executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    switch (i) {
                        case 0:
                            tipo = 0;
                            exportarClientes(executor, handler, dialogInterface);
                            break;
                        case 1:
                            tipo = 1;
                            exportarClientes(executor, handler, dialogInterface);
                            break;
                        default:
                            break;
                    }
                })
                .setNegativeButton(R.string.cancelar, (dialogInterface, i) -> dialogInterface.dismiss()).show();
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
                        try {
                            readTextFromUri(uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
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