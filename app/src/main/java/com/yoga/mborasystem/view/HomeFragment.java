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

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentHomeBinding;
import com.yoga.mborasystem.util.Ultilitario;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import static com.yoga.mborasystem.MainActivity.progressDialog;

public class HomeFragment extends Fragment {

    private Bundle bundle;
    private FragmentHomeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = new Bundle();
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

        binding.floatingActionButtonVenda.setOnClickListener(v -> {
            getProgressBar();
            bundle.putLong("idoperador", getArguments().getLong("idusuario", 0));
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_facturaFragment, bundle);
        });

        binding.btnUsuario.setOnClickListener(v -> {
            if (getArguments() != null) {
                bundle.putBoolean("master", getArguments().getBoolean("master"));
            }
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_usuarioFragment, bundle);
        });

        binding.btnProduto.setOnClickListener(v -> {
            if (getArguments() != null) {
                bundle.putBoolean("master", getArguments().getBoolean("master"));
            }
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_categoriaProdutoFragment, bundle);
        });

        binding.btnVenda.setOnClickListener(v -> {
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_vendaFragment);
        });

        binding.btnCliente.setOnClickListener(v -> {
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_listaClienteFragment);
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(), getContext()));
        return binding.getRoot();
    }

    private void getProgressBar() {
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialogo_view);
        progressDialog.getWindow().setLayout(200, 200);
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
            case R.id.itemSair:
                sairApp();
                break;
            default:
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    private void sairApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.sair));
        builder.setMessage(getString(R.string.tem_certeza_sair_app));
        builder.setNegativeButton(getString(R.string.nao), (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(getString(R.string.sim), (dialog, which) -> getActivity().finish());
        builder.show();
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
        if (progressDialog.isShowing() && progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}