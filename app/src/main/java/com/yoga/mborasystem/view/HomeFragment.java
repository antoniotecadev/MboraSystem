package com.yoga.mborasystem.view;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentHomeBinding;
import com.yoga.mborasystem.util.Ultilitario;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class HomeFragment extends Fragment {

    private Bundle bundle;
    private boolean isOpen = false;
    private FragmentHomeBinding binding;
    private Animation FabOpen, FabClose, FabRClockwise, FabRanticlockwise;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = new Bundle();
        FabOpen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        FabRClockwise = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_clockwise);
        FabRanticlockwise = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_anticlockwise);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        Toolbar toolbar = binding.toolbar;
        toolbar.inflateMenu(R.menu.menu_bloquear);
        toolbar.setOnMenuItemClickListener(item -> {
            Navigation.findNavController(getView()).navigate(R.id.action_global_bloquearFragment);
            return false;
        });

        binding.floatingActionButton.setOnClickListener(v -> {
            acercaMboraSystem();
        });

        binding.floatingActionButtonLixo.setOnClickListener(v -> {
            if (isOpen) {
                animationLixeira(FabClose, FabRanticlockwise, false);
            } else {
                animationLixeira(FabOpen, FabRClockwise, true);
            }
        });

        binding.floatingActionButtonCategoria.setOnClickListener(v -> {
            entrarCategorias(true);
        });

        binding.floatingActionButtonProduto.setOnClickListener(v -> {
            entrarProdutos(true);
        });

        binding.floatingActionButtonUsuario.setOnClickListener(v -> {
            entrarUsuarios(true);
        });

        MainActivity.navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.categoriaProdutoFragment1:
                    entrarCategorias(true);
                    break;
                case R.id.produtoFragment:
                    entrarProdutos(true);
                    break;
                case R.id.usuarioFragment1:
                    entrarUsuarios(true);
                    break;
                default:
                    break;
            }
            MainActivity.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        binding.floatingActionButtonVenda.setOnClickListener(v -> {
            MainActivity.getProgressBar();
            bundle.putLong("idoperador", getArguments().getLong("idusuario", 0));
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_facturaFragment, bundle);
        });

        binding.btnUsuario.setOnClickListener(v -> {
            if (getArguments() != null) {
                MainActivity.getProgressBar();
                bundle.putBoolean("master", getArguments().getBoolean("master"));
            }
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_usuarioFragment, bundle);
        });

        binding.btnProduto.setOnClickListener(v -> {
            if (getArguments() != null) {
                MainActivity.getProgressBar();
                bundle.putBoolean("master", getArguments().getBoolean("master"));
            }
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_categoriaProdutoFragment, bundle);
        });

        binding.btnVenda.setOnClickListener(v -> {
            MainActivity.getProgressBar();
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_vendaFragment);
        });

        binding.btnCliente.setOnClickListener(v -> {
            MainActivity.getProgressBar();
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_listaClienteFragment);
        });
        binding.btnDashboard.setOnClickListener(v -> {
            MainActivity.getProgressBar();
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_dashboardFragment);
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(), getContext()));
        return binding.getRoot();
    }

    private void animationLixeira(Animation animation, Animation animationLixo, boolean isOpen) {
        binding.floatingActionButtonCategoria.startAnimation(animation);
        binding.floatingActionButtonProduto.startAnimation(animation);
        binding.floatingActionButtonUsuario.startAnimation(animation);
        binding.floatingActionButtonCliente.startAnimation(animation);
        binding.floatingActionButtonVendaLixo.startAnimation(animation);

        binding.floatingActionButtonLixo.startAnimation(animationLixo);

        binding.floatingActionButtonCategoria.setClickable(isOpen);
        binding.floatingActionButtonProduto.setClickable(isOpen);
        binding.floatingActionButtonUsuario.setClickable(isOpen);
        binding.floatingActionButtonCliente.setClickable(isOpen);
        binding.floatingActionButtonVendaLixo.setClickable(isOpen);
        this.isOpen = isOpen;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_home_ferramenta, menu);
        menu.findItem(R.id.dialogAlterarCliente).setTitle(getArguments().getString("nome", ""));
        if (getArguments() != null) {
            if (!getArguments().getBoolean("master")) {
                binding.btnUsuario.setEnabled(false);
                binding.btnUsuario.setCardBackgroundColor(Color.GRAY);
                menu.findItem(R.id.dialogAlterarCliente).setEnabled(false);
                menu.findItem(R.id.dialogPlanoPacote).setVisible(false);
            } else {
                menu.findItem(R.id.dialogAlterarCodigoPin).setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(getActivity(), R.id.fragment);
        switch (item.getItemId()) {
            case R.id.dialogAlterarCliente:
                if (getArguments() != null) {
                    bundle.putParcelable("cliente", getArguments().getParcelable("cliente"));
                    Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_dialogAlterarCliente, bundle);
                }
                break;
            case R.id.dialogAlterarCodigoPin:
                if (getArguments() != null) {
                    bundle.putLong("idusuario", getArguments().getLong("idusuario"));
                    Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_dialogSenha, bundle);
                }
                break;
            case R.id.acercaMborasytem:
                acercaMboraSystem();
                break;
            case R.id.itemSair:
                sairApp();
                break;
            default:
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    private void sairApp() {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.sair))
                .setMessage(getString(R.string.tem_certeza_sair_app))
                .setNegativeButton(getString(R.string.nao), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.sim), (dialog, which) -> getActivity().finish())
                .show();
    }

    public void acercaMboraSystem() {
        new AlertDialog.Builder(getContext())
                .setIcon(R.drawable.ic_logotipo_yoga_original)
                .setTitle(getString(R.string.nome_sistema))
                .setMessage(R.string.acerca)
                .setNegativeButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void entrarCategorias(boolean isLixeira) {
        MainActivity.getProgressBar();
        HomeFragmentDirections.ActionHomeFragmentToCategoriaProdutoFragment direction = HomeFragmentDirections.actionHomeFragmentToCategoriaProdutoFragment().setIsLixeira(isLixeira);
        Navigation.findNavController(getView()).navigate(direction);
    }

    private void entrarProdutos(boolean isLixeira) {
        MainActivity.getProgressBar();
        HomeFragmentDirections.ActionHomeFragmentToListProdutoFragment direction = HomeFragmentDirections.actionHomeFragmentToListProdutoFragment().setIsLixeira(isLixeira);
        Navigation.findNavController(getView()).navigate(direction);
    }

    private void entrarUsuarios(boolean isLixeira) {
        MainActivity.getProgressBar();
        HomeFragmentDirections.ActionHomeFragmentToUsuarioFragment direction = HomeFragmentDirections.actionHomeFragmentToUsuarioFragment().setIsLixeira(isLixeira);
        Navigation.findNavController(getView()).navigate(direction);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (bundle != null) {
            bundle.clear();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }
}