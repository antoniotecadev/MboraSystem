package com.yoga.mborasystem.view;


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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);
        if (!composeFactura.equals(requireActivity().getIntent().getAction()))
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                View vw = getView();
                Context context = getContext();
                if (context != null && vw != null)
                    clienteViewModel.empresaExiste(false, null, context, vw, requireActivity());
            }, 5000);
        return view;
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