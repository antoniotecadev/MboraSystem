package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.Existe.NAO;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.Objects;

public class SplashFragment extends Fragment {

    private ClienteViewModel clienteViewModel;
    private static final String composeFactura = "android.intent.action.VIEW";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            clienteViewModel.clienteExiste(false, null);
            clienteViewModel.getExisteMutableLiveData().observe(getViewLifecycleOwner(), existe -> {
                switch (existe) {
                    case SIM:
                        if (composeFactura.equals(requireActivity().getIntent().getAction())) {
                            Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_atalho);
                        } else {
                            if (Objects.requireNonNull(Navigation.findNavController(requireView()).getCurrentDestination()).getId() == R.id.splashFragment)
                                Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_loginFragment);
                        }
                        break;
                    case NAO:
                        Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_cadastrarClienteFragment);
                        break;
                }
            });
        }, 5000);
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }
}