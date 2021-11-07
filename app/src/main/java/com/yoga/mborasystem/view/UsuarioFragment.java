package com.yoga.mborasystem.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentUsuarioListBinding;
import com.yoga.mborasystem.model.entidade.Usuario;
import com.yoga.mborasystem.viewmodel.UsuarioViewModel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

public class UsuarioFragment extends Fragment {

    private Bundle bundle;
    private GroupAdapter adapter;
    private UsuarioViewModel usuarioViewModel;
    private FragmentUsuarioListBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GroupAdapter();
        usuarioViewModel = new ViewModelProvider(requireActivity()).get(UsuarioViewModel.class);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentUsuarioListBinding.inflate(inflater, container, false);

        binding.recyclerViewListaUsuario.setAdapter(adapter);
        binding.recyclerViewListaUsuario.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.criarUsuarioFragment.setOnClickListener(v -> {
            MainActivity.getProgressBar();
            Navigation.findNavController(v).navigate(R.id.action_usuarioFragment_to_dialogCriarUsuario);
        });
        usuarioViewModel.consultarUsuarios();
        usuarioViewModel.getListaUsuarios().observe(getViewLifecycleOwner(), usuarios -> {
            requireActivity().setTitle(getString(R.string.usuarios) + " = " + usuarios.size());
            adapter.clear();
            for (Usuario usuario : usuarios)
                adapter.add(new ItemUsuario(usuario));
        });
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (getArguments() != null) {
            if (getArguments().getBoolean("master")) {
                inflater.inflate(R.menu.menu_criar_usuario, menu);
            } else {
                binding.criarUsuarioFragment.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    class ItemUsuario extends Item<GroupieViewHolder> {

        private final Usuario usuario;

        public ItemUsuario(Usuario usuarios) {
            this.usuario = usuarios;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {

            bundle = new Bundle();

            TextView nome = viewHolder.itemView.findViewById(R.id.txtNomeUsuario);
            TextView estado = viewHolder.itemView.findViewById(R.id.txtBloquear);
            TextView tel = viewHolder.itemView.findViewById(R.id.txtTel);
            TextView end = viewHolder.itemView.findViewById(R.id.txtEnd);

            if (getArguments() != null) {
                if (!getArguments().getBoolean("master")) {
                    viewHolder.itemView.findViewById(R.id.btnEntrar).setEnabled(false);
                    viewHolder.itemView.findViewById(R.id.btnEliminar).setVisibility(View.GONE);
                }
            }

            nome.setText(usuario.getNome());
            tel.setText(usuario.getTelefone() + " / MS" + usuario.getId());
            end.setText(usuario.getEndereco());
            estado.setText(usuario.getEstado() == 1 ? getString(R.string.estado_desbloqueado) : getString(R.string.estado_bloqueado));
            viewHolder.itemView.findViewById(R.id.btnEntrar).setOnClickListener(v -> {
                UsuarioFragmentDirections.ActionUsuarioFragmentToVendaFragment direction = UsuarioFragmentDirections.actionUsuarioFragmentToVendaFragment().setIdusuario(usuario.getId()).setNomeUsuario(usuario.getNome());
                Navigation.findNavController(requireView()).navigate(direction);
            });

            viewHolder.itemView.findViewById(R.id.btnEntrar).setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                menu.setHeaderTitle(usuario.getNome());
                menu.add(R.string.editar).setOnMenuItemClickListener(item -> {
                    verDadosUsuario();
                    return false;
                });//groupId, itemId, order, title
                menu.add(R.string.eliminar_usuario).setOnMenuItemClickListener(item -> {
                    deleteUser(getString(R.string.tem_certeza_eliminar_usuario));
                    return false;
                });
            });

            viewHolder.itemView.findViewById(R.id.btnEliminar).setOnClickListener(v -> deleteUser(getString(R.string.tem_certeza_eliminar_usuario)));

        }

        @Override
        public int getLayout() {
            return com.yoga.mborasystem.R.layout.fragment_usuario;
        }

        private void verDadosUsuario() {
            if (getArguments() != null) {
                MainActivity.getProgressBar();
                bundle.putBoolean("master", getArguments().getBoolean("master"));
            }
            bundle.putParcelable("usuario", usuario);
            Navigation.findNavController(requireView()).navigate(R.id.action_usuarioFragment_to_dialogCriarUsuario, bundle);
        }

        private void deleteUser(String msg) {
            usuario.setId(usuario.getId());
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.eliminar) + " (" + usuario.getNome() + ")")
                    .setMessage(msg)
                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.ok), (dialog1, which) -> usuarioViewModel.eliminarUsuario(usuario, null))
                    .show();
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