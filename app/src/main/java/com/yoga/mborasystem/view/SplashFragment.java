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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

public class SplashFragment extends Fragment {

    ClienteViewModel clienteViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
                Ultilitario.getExisteMutableLiveData().observe(getViewLifecycleOwner(), new Observer<Ultilitario.Existe>() {
                    @Override
                    public void onChanged(Ultilitario.Existe clienteExiste) {
                        switch (clienteExiste) {
                            case SIM:
                                Navigation.findNavController(getView()).navigate(R.id.action_splashFragment_to_loginFragment);
                                break;
                            case NAO:
                                Navigation.findNavController(getView()).navigate(R.id.action_splashFragment_to_cadastrarClienteFragment);
                                break;
                        }
                    }
                });
            }
        }, 5000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }
}