package com.yoga.mborasystem.view;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
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

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentListaClienteBinding;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteCantinaViewModel;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ListaClienteFragment extends Fragment {

    private GroupAdapter adapter;
    private ClienteCantina clienteCantina;
    private FragmentListaClienteBinding binding;
    private ClienteCantinaViewModel clienteCantinaViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GroupAdapter();
        clienteCantina = new ClienteCantina();
        clienteCantinaViewModel = new ViewModelProvider(requireActivity()).get(ClienteCantinaViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        binding = FragmentListaClienteBinding.inflate(inflater, container, false);

        binding.recyclerViewListaCliente.setAdapter(adapter);
        binding.recyclerViewListaCliente.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.btnCriarCliente.setOnClickListener(v -> {
            criarCliente();
        });

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
                        private TextView nomeCliente, telefoneCliente, dataCira, dataModifica;

                        @Override
                        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
                            ImageView i = viewHolder.itemView.findViewById(R.id.imgCliente);
                            Ultilitario.colorRandomImage(i, random);

                            nomeCliente = viewHolder.itemView.findViewById(R.id.txtNomeCliente);
                            telefoneCliente = viewHolder.itemView.findViewById(R.id.txtTelefone);
                            dataCira = viewHolder.itemView.findViewById(R.id.textDataCriacao);
                            dataModifica = viewHolder.itemView.findViewById(R.id.textDataModificacao);
                            ImageButton menu = viewHolder.itemView.findViewById(R.id.imgBtnMenu);

                            viewHolder.itemView.setOnClickListener(v -> {
                                MainActivity.getProgressBar();
                                v.setBackgroundColor(Color.parseColor("#6BD3D8D7"));

                                ListaClienteFragmentDirections.ActionListaClienteFragmentToVendaFragment direction = ListaClienteFragmentDirections.actionListaClienteFragmentToVendaFragment().setIdcliente(cliente.getId()).setNomeCliente(cliente.getNome());
                                Navigation.findNavController(getView()).navigate(direction);

                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    v.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                }, 1000);
                            });

                            nomeCliente.setText(cliente.getNome());
                            telefoneCliente.setText(cliente.getTelefone());
                            dataCira.setText(getString(R.string.data_cria) + ": " + cliente.getData_cria());
                            if (cliente.getData_modifica() != null) {
                                dataModifica.setVisibility(View.VISIBLE);
                                dataModifica.setText(getString(R.string.data_modifica) + ": " + cliente.getData_modifica());
                            }

                            registerForContextMenu(menu);
                            menu.setOnClickListener(v -> v.showContextMenu());
                            viewHolder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                                @Override
                                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                                    menu.setHeaderTitle(cliente.getNome());
                                    menu.add(getString(R.string.entrar)).setOnMenuItemClickListener(item -> {
                                        ListaClienteFragmentDirections.ActionListaClienteFragmentToVendaFragment direction = ListaClienteFragmentDirections.actionListaClienteFragmentToVendaFragment().setIdcliente(cliente.getId()).setNomeCliente(cliente.getNome());
                                        Navigation.findNavController(getView()).navigate(direction);
                                        return false;
                                    });//groupId, itemId, order, title
                                    menu.add(getString(R.string.alterar_cliente)).setOnMenuItemClickListener(item -> {
                                        MainActivity.getProgressBar();
                                        ListaClienteFragmentDirections.ActionListaClienteFragmentToDialogClienteCantina direction = ListaClienteFragmentDirections.actionListaClienteFragmentToDialogClienteCantina(cliente.getNome(), cliente.getTelefone(), cliente.getId());
                                        Navigation.findNavController(getView()).navigate(direction);
                                        return false;
                                    });
                                    menu.add(getString(R.string.eliminar_cliente)).setOnMenuItemClickListener(item -> {
                                        clienteCantina.setId(cliente.getId());
                                        clienteCantina.setEstado(Ultilitario.TRES);
                                        new AlertDialog.Builder(getContext())
                                                .setTitle(getString(R.string.eliminar) + " (" + cliente.getNome() + ")")
                                                .setMessage(getString(R.string.tem_cert_elim_cli))
                                                .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                                                .setPositiveButton(getString(R.string.ok), (dialog1, which) -> clienteCantinaViewModel.eliminarCliente(clienteCantina, null))
                                                .show();
                                        return false;
                                    });

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

        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> {
            clienteCantinaViewModel.consultarClientesCantina(binding.mySwipeRefreshLayout);
        });

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_cliente, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getString(R.string.nome) + " " + getString(R.string.ou) + " " + getString(R.string.telefone));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.onActionViewExpanded();
        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
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
        NavController navController = Navigation.findNavController(getActivity(), R.id.fragment);
        switch (item.getItemId()) {
            case R.id.criarClienteCantina:
                criarCliente();
                break;
            default:
                break;
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    private void criarCliente() {
        MainActivity.getProgressBar();
        ListaClienteFragmentDirections.ActionListaClienteFragmentToDialogClienteCantina direction = ListaClienteFragmentDirections.actionListaClienteFragmentToDialogClienteCantina("", "", 0);
        Navigation.findNavController(getView()).navigate(direction);
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