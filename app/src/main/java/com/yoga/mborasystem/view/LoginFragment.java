package com.yoga.mborasystem.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentLoginBinding;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;
import com.yoga.mborasystem.viewmodel.LoginViewModel;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import static android.content.Context.VIBRATOR_SERVICE;

public class LoginFragment extends Fragment {

    private Bundle bundle;
    private Handler handler;
    private List<String> digitos;
    private ObjectAnimator animation;
    private FragmentLoginBinding binding;
    private LoginViewModel loginViewModel;
    private ClienteViewModel clienteViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = new Bundle();
        digitos = new ArrayList<>();
        handler = new Handler(Looper.getMainLooper());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);

        final Observer<String> infoPinObserver = s -> {
            if (s.equals("4")) {
                desabilitarTecladoPersonalisado();
                contarTempoDeEspera();
            } else {
                binding.tvinfoCodigoPin.setText(s);
                limparCodigoPin();
                vibrarTelefone(getContext());
            }
        };
        //  observador está vinculado ao objeto Lifecycle associado ao proprietário
        loginViewModel.getinfoPin().observe(this, infoPinObserver);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);

        binding.btn1.setOnClickListener(v -> digitarCodigoPin(1));
        binding.btn2.setOnClickListener(v -> digitarCodigoPin(2));
        binding.btn3.setOnClickListener(v -> digitarCodigoPin(3));
        binding.btn4.setOnClickListener(v -> digitarCodigoPin(4));
        binding.btn5.setOnClickListener(v -> digitarCodigoPin(5));
        binding.btn6.setOnClickListener(v -> digitarCodigoPin(6));
        binding.btn7.setOnClickListener(v -> digitarCodigoPin(7));
        binding.btn8.setOnClickListener(v -> digitarCodigoPin(8));
        binding.btn9.setOnClickListener(v -> digitarCodigoPin(9));
        binding.btn0.setOnClickListener(v -> digitarCodigoPin(0));
        binding.btnApagar.setOnClickListener(v -> limparCodigoPin());
        binding.btnMenu.setOnClickListener(v -> {
            MainActivity.getProgressBar();
            Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_dialogCodigoPin);
        });

        clienteViewModel.getClienteMutableLiveData().observe(getViewLifecycleOwner(), cliente -> {
            bundle.putString("nome", cliente.getNome() + " " + cliente.getSobrenome());
            bundle.putBoolean("master", cliente.isMaster());
            bundle.putParcelable("cliente", cliente);
            try {
                Navigation.findNavController(requireView()).navigate(R.id.action_dialogCodigoPin_to_navigation, bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        loginViewModel.getUsuarioMutableLiveData().observe(getViewLifecycleOwner(), usuario -> {
            bundle.putString("nome", usuario.getNome());
            bundle.putBoolean("master", false);
            bundle.putLong("idusuario", usuario.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_navigation, bundle);
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(), getContext()));
        return binding.getRoot();
    }

    private void digitarCodigoPin(Integer digito) {

        digitos.add(String.valueOf(digito));

        switch (digitos.size()) {

            case 1:
                animarBolinhas(binding.d1);
                binding.d1.setTextColor(Color.rgb(111, 55, 0));
                break;
            case 2:
                animarBolinhas(binding.d2);
                binding.d2.setTextColor(Color.rgb(111, 55, 0));
                break;
            case 3:
                animarBolinhas(binding.d3);
                binding.d3.setTextColor(Color.rgb(111, 55, 0));
                break;
            case 4:
                animarBolinhas(binding.d4);
                binding.d4.setTextColor(Color.rgb(111, 55, 0));
                break;
            case 5:
                animarBolinhas(binding.d5);
                binding.d5.setTextColor(Color.rgb(111, 55, 0));
                break;
            case 6:
                animarBolinhas(binding.d6);
                binding.d6.setTextColor(Color.rgb(111, 55, 0));
                break;
            default:
                break;
        }

        if (digitos.size() == 6) {
            try {
                logarComTecladoPersonalizado();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void desabilitarTecladoPersonalisado() {
        binding.gridLayout.setVisibility(View.GONE);
    }

    private void habilitarTecladoPersonalisado() {
        handler.postDelayed(() -> {
            binding.tvinfoCodigoPin.setText(R.string.tvIntroduzirCodigoPin);
            binding.gridLayout.setVisibility(View.VISIBLE);
        }, 60000);
    }

    private void contarTempoDeEspera() {
        binding.tvinfoCodigoPin.setText(R.string.tentar_novamente);
        habilitarTecladoPersonalisado();
    }

    private void logarComTecladoPersonalizado() throws NoSuchAlgorithmException {
        StringBuilder codigoPin = new StringBuilder();
        for (String pin : digitos) {
            codigoPin.append(pin);
        }
        if (codigoPin.length() != 6) {
            binding.tvinfoCodigoPin.setError(getString(R.string.infoPinIncorreto));
        } else {
            MainActivity.getProgressBar();
            loginViewModel.logar(codigoPin.toString());
        }
    }

    private void animarBolinhas(TextView bolinha) {
        animation = ObjectAnimator.ofFloat(bolinha, "translationY", -10f);
        animation.setDuration(200);
        animation.start();
    }

    private void limparCodigoPin() {

        desabilitarHabilitarButton(false);
        digitos.clear();

        retirarBolinhasDeDigitos(binding.d6, 250);
        retirarBolinhasDeDigitos(binding.d5, 350);
        retirarBolinhasDeDigitos(binding.d4, 450);
        retirarBolinhasDeDigitos(binding.d3, 550);
        retirarBolinhasDeDigitos(binding.d2, 650);
        retirarBolinhasDeDigitos(binding.d1, 750);

        handler.postDelayed(() -> desabilitarHabilitarButton(true), 850);
    }

    private void retirarBolinhasDeDigitos(TextView bolinha, int tempo) {
        handler.postDelayed(() -> {
            bolinha.setTextColor(Color.GRAY);
            animation = ObjectAnimator.ofFloat(bolinha, "translationY", 0f);
            animation.setDuration(200);
            animation.start();
        }, tempo);

    }

    private void desabilitarHabilitarButton(Boolean estado) {
        binding.btn1.setEnabled(estado);
        binding.btn2.setEnabled(estado);
        binding.btn3.setEnabled(estado);
        binding.btn4.setEnabled(estado);
        binding.btn5.setEnabled(estado);
        binding.btn6.setEnabled(estado);
        binding.btn7.setEnabled(estado);
        binding.btn8.setEnabled(estado);
        binding.btn9.setEnabled(estado);
        binding.btn0.setEnabled(estado);
        binding.btnApagar.setEnabled(estado);
    }

    public void vibrarTelefone(Context context) {

        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) context.getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) context.getSystemService(VIBRATOR_SERVICE)).vibrate(500);
        }

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
