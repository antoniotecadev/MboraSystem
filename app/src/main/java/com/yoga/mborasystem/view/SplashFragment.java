package com.yoga.mborasystem.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

public class SplashFragment extends Fragment {

    private ClienteViewModel clienteViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Ultilitario.getExisteMutableLiveData().observe(getViewLifecycleOwner(), existe -> {
                switch (existe) {
                    case SIM:
                        Navigation.findNavController(getView()).navigate(R.id.action_splashFragment_to_loginFragment);
                        break;
                    case NAO:
                        Navigation.findNavController(getView()).navigate(R.id.action_splashFragment_to_cadastrarClienteFragment);
                        break;
                }
            });
        }, 5000);
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }
}