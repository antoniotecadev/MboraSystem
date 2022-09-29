package com.yoga.mborasystem.view;


import static com.yoga.mborasystem.util.Ultilitario.Existe.NAO;
import static com.yoga.mborasystem.util.Ultilitario.Existe.SIM;
import static com.yoga.mborasystem.util.Ultilitario.getSharedPreferencesIdioma;
import static com.yoga.mborasystem.util.Ultilitario.restartActivity;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SplashFragment extends Fragment {

    private Bundle bundle;
    private ClienteViewModel clienteViewModel;
    private static final String composeFactura = "android.intent.action.VIEW";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = new Bundle();
        definirIdioma(requireActivity());
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!composeFactura.equals(requireActivity().getIntent().getAction())) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                clienteViewModel.clienteExiste(false, null);
                clienteViewModel.getExisteMutableLiveData().observe(getViewLifecycleOwner(), clienteMap -> {
                    for (Map.Entry<Enum, Cliente> entry : clienteMap.entrySet())
                        if (SIM.equals(entry.getKey())) {
                            if (Ultilitario.getBooleanPreference(requireContext(), "bloaut")) {
                                List<Cliente> clienteList = new ArrayList<>();
                                clienteList.add(entry.getValue());
                                Ultilitario.setValueUsuarioMaster(bundle, clienteList, requireContext());
                                Navigation.findNavController(requireView()).navigate(R.id.navigation, bundle);
                            } else if (Objects.requireNonNull(Navigation.findNavController(requireView()).getCurrentDestination()).getId() == R.id.splashFragment)
                                Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_loginFragment);
                            break;
                        } else if (NAO.equals(entry.getKey())) {
                            Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_cadastrarClienteFragment);
                            break;
                        }
                });
            }, 5000);
        }
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    private void definirIdioma(Activity activity) {
        if (getSharedPreferencesIdioma(activity).equalsIgnoreCase("Francês"))
            Ultilitario.getSelectedIdioma(activity, "FR", null, false, true);
        else if (getSharedPreferencesIdioma(activity).equalsIgnoreCase("Inglês"))
            Ultilitario.getSelectedIdioma(activity, "EN", null, false, true);
        else if (getSharedPreferencesIdioma(activity).equalsIgnoreCase("Português"))
            Ultilitario.getSelectedIdioma(activity, "PT", null, false, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (composeFactura.equals(requireActivity().getIntent().getAction())) {
            if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("atalfact", false) && !Ultilitario.getValueSharedPreferences(requireContext(), "imei", "").isEmpty()) {
                Uri uri = Uri.parse("https://mborasystem://facturacao");
                Navigation.findNavController(requireView()).navigate(uri);
            } else {
                new android.app.AlertDialog.Builder(getContext())
                        .setCancelable(false)
                        .setIcon(R.drawable.ic_baseline_shopping_cart)
                        .setTitle(getString(R.string.atalho))
                        .setMessage(getString(R.string.atl_des))
                        .setNegativeButton(getString(R.string.abr_app_com), (dialog, which) -> {
                            requireActivity().getIntent().setAction("android.intent.action.MAIN").addCategory("android.intent.category.LAUNCHER");
                            restartActivity(requireActivity());
                        })
                        .setPositiveButton(getString(R.string.sair), (dialog, which) -> System.exit(0))
                        .show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bundle != null)
            bundle.clear();
    }
}