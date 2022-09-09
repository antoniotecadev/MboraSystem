package com.yoga.mborasystem.view;

import static android.content.Context.VIBRATOR_SERVICE;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentLoginBinding;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;
import com.yoga.mborasystem.viewmodel.LoginViewModel;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class LoginFragment extends Fragment {

    private Bundle bundle;
    private Handler handler;
    private List<String> digitos;
    private ObjectAnimator animation;
    private FragmentLoginBinding binding;
    private LoginViewModel loginViewModel;
    private ClienteViewModel clienteViewModel;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private void instanceOfBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        biometricPrompt = new BiometricPrompt(requireActivity(),
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(requireContext(),
                        getString(R.string.err_aut) + ": " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                clienteViewModel.logarComBiometria();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(requireContext(), getString(R.string.fal_aut),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.titlte_biometricprompt))
                .setSubtitle(getString(R.string.subtitlte_biometricprompt))
                .setAllowedAuthenticators(BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
                .build();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = new Bundle();
        digitos = new ArrayList<>();
        handler = new Handler(Looper.getMainLooper());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        clienteViewModel = new ViewModelProvider(this).get(ClienteViewModel.class);

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

//    @SuppressLint("MissingPermission")
//    public Task<Location> getLastLocationIfApiAvailable(FragmentActivity context) {
//        FusedLocationProviderClient client =
//                getFusedLocationProviderClient(context);
//
//        return GoogleApiAvailability.getInstance()
//                .checkApiAvailability(client)
//                .onSuccessTask(unused -> client.getLastLocation().addOnSuccessListener(context, location -> {
//                    if (location != null) {
//                        Log.d("localização", ""+location.getLatitude()+" - "+location.getLongitude());
//                        try {
//                            Geocoder geo = new Geocoder(getActivity(), Locale.getDefault());
//                            List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//                            if (addresses.isEmpty()) {
//                                Log.d("localização", "Endereço vazio");
//                            } else {
//                                if (addresses.size() > 0) {
//                                    Log.d("localização",addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
//                                }
//                            }
//                        }
//                        catch (Exception e) {
//                            e.printStackTrace(); // getFromLocation() may sometimes fail
//                            Log.d("localização", "Erro: " + e.getMessage());
//                        }
//                    }
//                })).addOnFailureListener(e -> Log.d("localização", "Location unavailable."));
//    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        if (ContextCompat.checkSelfPermission(getActivity(),
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                    Manifest.permission.ACCESS_FINE_LOCATION)){
//                ActivityCompat.requestPermissions(getActivity(),
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            }else{
//                ActivityCompat.requestPermissions(getActivity(),
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            }
//        }
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        instanceOfBiometricPrompt();
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
            bundle.putString("nome", cliente.get(0).getNome() + " " + cliente.get(0).getSobrenome());
            bundle.putBoolean("master", cliente.get(0).isMaster());
            bundle.putParcelable("cliente", cliente.get(0));
            try {
                Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_navigation, bundle);
            } catch (Exception e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        loginViewModel.getUsuarioMutableLiveData().observe(getViewLifecycleOwner(), usuario -> {
            bundle.putString("nome", usuario.getNome());
            bundle.putBoolean("master", false);
            bundle.putLong("idusuario", usuario.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_navigation, bundle);
        });

        binding.btnAuthBiometric.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));

        if (!Ultilitario.getActivarAutenticacaoBiometrica(requireContext())) {
            binding.btnAuthBiometric.setVisibility(View.INVISIBLE);
        }
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
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
            limparCodigoPin();
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

    @SuppressLint("MissingPermission")
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

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
//        switch (requestCode){
//            case 1: {
//                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    if (ContextCompat.checkSelfPermission(getActivity(),
//                            Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
//                        getLastLocationIfApiAvailable(requireActivity());
//
//                    }
//                }else{
//                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//        }
//    }

}
