package com.yoga.mborasystem.view;


import static com.yoga.mborasystem.util.Ultilitario.Existe.NAO;
import static com.yoga.mborasystem.util.Ultilitario.Existe.SIM;
import static com.yoga.mborasystem.util.Ultilitario.getSharedPreferencesIdioma;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

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
        if (getSharedPreferencesIdioma(requireContext()).equalsIgnoreCase("Francês")) {
            Ultilitario.getSelectedIdioma(requireActivity(), "FR", null, false, true);
        } else if (getSharedPreferencesIdioma(requireContext()).equalsIgnoreCase("Inglês")) {
            Ultilitario.getSelectedIdioma(requireActivity(), "EN", null, false, true);
        } else if (getSharedPreferencesIdioma(requireContext()).equalsIgnoreCase("Português")) {
            Ultilitario.getSelectedIdioma(requireActivity(), "PT", null, false, true);
        }
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

    @Override
    public void onResume() {
        super.onResume();
        if (composeFactura.equals(requireActivity().getIntent().getAction())) {
            Uri uri = Uri.parse("https://mborasystem://factura");
            Navigation.findNavController(requireView()).navigate(uri);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bundle != null)
            bundle.clear();
    }
}