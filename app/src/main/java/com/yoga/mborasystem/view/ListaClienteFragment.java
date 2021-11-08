package com.yoga.mborasystem.view;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
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
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteCantinaViewModel;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
                            TextView dataCira = viewHolder.itemView.findViewById(R.id.textDataCriacao);
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
                            telefoneCliente.setText(cliente.getTelefone());
                            dataCira.setText(getString(R.string.data_cria) + ": " + cliente.getData_cria());
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
                                            ListaClienteFragmentDirections.ActionListaClienteFragmentToDialogClienteCantina direction = ListaClienteFragmentDirections.actionListaClienteFragmentToDialogClienteCantina(cliente.getNome(), cliente.getTelefone(), cliente.getId());
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
                                } else {
                                    Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
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

        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> clienteCantinaViewModel.consultarClientesCantina(binding.mySwipeRefreshLayout));

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_cliente, menu);

        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getString(R.string.nome) + " " + getString(R.string.ou) + " " + getString(R.string.telefone));
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
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    private void criarCliente() {
        MainActivity.getProgressBar();
        ListaClienteFragmentDirections.ActionListaClienteFragmentToDialogClienteCantina direction = ListaClienteFragmentDirections.actionListaClienteFragmentToDialogClienteCantina("", "", 0);
        Navigation.findNavController(requireView()).navigate(direction);
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