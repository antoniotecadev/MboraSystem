package com.yoga.mborasystem.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import com.yoga.mborasystem.databinding.FragmentUsuarioBinding;
import com.yoga.mborasystem.databinding.FragmentUsuarioListBinding;
import com.yoga.mborasystem.model.entidade.Usuario;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.UsuarioViewModel;

@SuppressWarnings("rawtypes")
public class UsuarioFragment extends Fragment {

    private Bundle bundle;
    private GroupAdapter adapter;
    private UsuarioAdapter usuarioAdapter;
    private UsuarioViewModel usuarioViewModel;
    private FragmentUsuarioListBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = new Bundle();
        adapter = new GroupAdapter();
        usuarioAdapter = new UsuarioAdapter(new UsuarioComparator());
        usuarioViewModel = new ViewModelProvider(requireActivity()).get(UsuarioViewModel.class);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentUsuarioListBinding.inflate(inflater, container, false);

        binding.recyclerViewListaUsuario.setAdapter(usuarioAdapter);
        binding.recyclerViewListaUsuario.setHasFixedSize(true);
        binding.recyclerViewListaUsuario.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.criarUsuarioFragment.setOnClickListener(v -> {
            MainActivity.getProgressBar();
            Navigation.findNavController(v).navigate(R.id.action_usuarioFragment_to_dialogCriarUsuario);
        });

        consultarUsuarios();
        usuarioViewModel.getListaUsuarios().observe(getViewLifecycleOwner(), usuarios -> {
            usuarioAdapter.submitData(getLifecycle(), usuarios);
            Ultilitario.swipeRefreshLayout(binding.mySwipeRefreshLayout);
        });

        usuarioViewModel.getQuantidadeUsuario().observe(getViewLifecycleOwner(), quantidade -> {
            requireActivity().setTitle(getString(R.string.usuarios) + " = " + quantidade);
            binding.recyclerViewListaUsuario.setAdapter(quantidade == 0 ? Ultilitario.naoEncontrado(getContext(), adapter, R.string.produto_nao_encontrada) : usuarioAdapter);
        });
        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> {
            consultarUsuarios();
            usuarioAdapter.notifyDataSetChanged();
        });
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                if (getArguments() != null) {
                    if (getArguments().getBoolean("master")) {
                        menuInflater.inflate(R.menu.menu_criar_usuario, menu);
                    } else {
                        binding.criarUsuarioFragment.setVisibility(View.GONE);
                    }
                } else {
                    binding.criarUsuarioFragment.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        }, getViewLifecycleOwner());
        return binding.getRoot();
    }

    private void consultarUsuarios() {
        usuarioViewModel.crud = false;
        usuarioViewModel.consultarUsuarios(getViewLifecycleOwner());
    }

    class UsuarioAdapter extends PagingDataAdapter<Usuario, UsuarioAdapter.UsuarioViewHolder> {

        public UsuarioAdapter(@NonNull DiffUtil.ItemCallback<Usuario> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new UsuarioViewHolder(FragmentUsuarioBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull UsuarioViewHolder h, int position) {
            Usuario us = getItem(position);
            if (us != null) {
                if (getArguments() != null) {
                    if (!getArguments().getBoolean("master")) {
                        h.binding.btnEntrar.setEnabled(false);
                        h.binding.btnEliminar.setVisibility(View.GONE);
                    }
                } else {
                    h.binding.btnEntrar.setEnabled(false);
                    h.binding.btnEliminar.setVisibility(View.GONE);
                }
                if (us.getEstado() == Ultilitario.DOIS)
                    h.binding.txtNomeUsuario.setText(Html.fromHtml(getString(R.string.risc_text, us.getNome())));
                else
                    h.binding.txtNomeUsuario.setText(us.getNome());
                h.binding.txtTel.setText(us.getTelefone() + " / MSU" + us.getId());
                h.binding.txtEnd.setText(us.getEndereco());
                h.binding.txtBloquear.setText(us.getEstado() == 1 ? getString(R.string.estado_desbloqueado) : getString(R.string.estado_bloqueado));
                h.itemView.findViewById(R.id.btnEntrar).setOnClickListener(v -> {
                    UsuarioFragmentDirections.ActionUsuarioFragmentToVendaFragment direction = UsuarioFragmentDirections.actionUsuarioFragmentToVendaFragment().setIdusuario(us.getId()).setNomeUsuario(us.getNome());
                    Navigation.findNavController(requireView()).navigate(direction);
                });

                h.itemView.findViewById(R.id.btnEntrar).setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                    menu.setHeaderTitle(us.getNome());
                    menu.add(R.string.editar).setOnMenuItemClickListener(item -> {
                        verDadosUsuario(us);
                        return false;
                    });//groupId, itemId, order, title
                    menu.add(R.string.eliminar_usuario).setOnMenuItemClickListener(item -> {
                        deleteUser(getString(R.string.tem_certeza_eliminar_usuario), us);
                        return false;
                    });
                });

                h.itemView.findViewById(R.id.btnEliminar).setOnClickListener(v -> deleteUser(getString(R.string.tem_certeza_eliminar_usuario), us));
            }
        }

        private void verDadosUsuario(Usuario usuario) {
            if (getArguments() != null) {
                MainActivity.getProgressBar();
                bundle.putBoolean("master", getArguments().getBoolean("master"));
            }
            bundle.putParcelable("usuario", usuario);
            Navigation.findNavController(requireView()).navigate(R.id.action_usuarioFragment_to_dialogCriarUsuario, bundle);
        }

        private void deleteUser(String msg, Usuario usuario) {
            usuarioViewModel.crud = true;
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.eliminar) + " (" + usuario.getNome() + ")")
                    .setMessage(msg)
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> usuarioViewModel.eliminarUsuario(usuario, null))
                    .show();
        }

        private class UsuarioViewHolder extends RecyclerView.ViewHolder {
            FragmentUsuarioBinding binding;

            public UsuarioViewHolder(@NonNull FragmentUsuarioBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    static class UsuarioComparator extends DiffUtil.ItemCallback<Usuario> {

        @Override
        public boolean areItemsTheSame(@NonNull Usuario oldItem, @NonNull Usuario newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Usuario oldItem, @NonNull Usuario newItem) {
            return oldItem.getId() == newItem.getId();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (bundle != null) {
            bundle.clear();
        }
    }
}