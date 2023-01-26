package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.alertDialog;
import static com.yoga.mborasystem.util.Ultilitario.conexaoInternet;
import static com.yoga.mborasystem.util.Ultilitario.getBooleanPreference;
import static com.yoga.mborasystem.util.Ultilitario.getSelectedIdioma;
import static com.yoga.mborasystem.util.Ultilitario.getValueSharedPreferences;
import static com.yoga.mborasystem.util.Ultilitario.showToast;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.repository.UsuarioRepository;
import com.yoga.mborasystem.util.Ultilitario;

import java.util.Objects;
import java.util.concurrent.Executors;

public class ConfiguracaoFragment extends PreferenceFragmentCompat {

    private String codigoIdioma;
    private ListPreference motivoIsento;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.backgroundColor));
        return view;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_config, rootKey);
        SwitchPreferenceCompat bloAut = findPreference("bloaut");
        EditTextPreference pin = findPreference("pinadmin");
        ListPreference listaIdioma = findPreference("lista_idioma");
        ListPreference taxaIva = findPreference("taxa_iva");
        motivoIsento = findPreference("motivo_isencao");
        ListPreference modEsc = findPreference("mod_esc");
        SwitchPreferenceCompat notificaoVenda = findPreference("notificacao_venda");
        PreferenceCategory  categoriaNotificacao = findPreference("categoria_notificacao");

        if (modEsc != null) {
            modEsc.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.equals("0"))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                else if (newValue.equals("1"))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else if (newValue.equals("2"))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                return true;
            });
        }

        if (bloAut != null) {
            bloAut.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!bloAut.isChecked())
                    alertDialog(getString(R.string.avs), getString(R.string.bloaut_msg), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                return true;
            });
        }

        if (pin != null) {
            pin.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                if (TextUtils.isEmpty(preference.getText()))
                    return getString(R.string.nao_def);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Objects.requireNonNull(pin.getText()).length(); i++)
                    sb.append("●");
                return sb.toString();
            });
            pin.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD));
            pin.setOnPreferenceChangeListener((preference, codigoPin) -> {
                UsuarioRepository usuarioRepository = new UsuarioRepository(requireContext());
                Handler handler = new Handler(Looper.getMainLooper());
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        if (codigoPin.toString().length() != 6 || !usuarioRepository.confirmarCodigoPin(Ultilitario.gerarHash(codigoPin.toString())).isEmpty())
                            handler.post(() -> {
                                pin.setText("");
                                showToast(requireContext(), Color.rgb(204, 0, 0), requireContext().getString(R.string.codigopin_invalido), R.drawable.ic_toast_erro);
                            });
                    } catch (Exception e) {
                        handler.post(() -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                });
                return true;
            });
        }

        if (taxaIva != null) {
            desabilitarMotivoIsencao(taxaIva.getValue(), motivoIsento);
            taxaIva.setOnPreferenceChangeListener((preference, newValue) -> {
                int taxas = Integer.parseInt(newValue.toString());
                String[] taxa_values = getResources().getStringArray(R.array.array_taxa_iva_valor);
//                String taxa = taxa_values[taxas == 14 ? 3 : taxas == 7 ? 2 : (taxas == 5 ? 1 : 0)];
                String taxa = taxa_values[taxas == 14 ? 1 : 0];
                desabilitarMotivoIsencao(taxa, motivoIsento);
                return true;
            });
        }
        if (listaIdioma != null) {
            listaIdioma.setOnPreferenceChangeListener((preference, newValue) -> {
                switch (newValue.toString()) {
                    case "Francês":
                        codigoIdioma = "fr";
                        break;
                    case "Inglês":
                        codigoIdioma = "en";
                        break;
                    case "Português":
                        codigoIdioma = "pt";
                        break;
                    default:
                        return false;
                }
                getSelectedIdioma(requireActivity(), codigoIdioma, newValue.toString(), false, false);
                return true;
            });
        }

        if (notificaoVenda != null) {
            if(!getBooleanPreference(requireContext(), "master")){
                notificaoVenda.setVisible(false);
                categoriaNotificacao.setVisible(false);
            }
            notificaoVenda.setOnPreferenceChangeListener((preference, newValue) -> {
                if (conexaoInternet(requireContext())) {
                    FirebaseMessaging messaging = FirebaseMessaging.getInstance();
                    String imei = getValueSharedPreferences(requireContext(), "imei", "");
                    if (!notificaoVenda.isChecked() && !imei.isEmpty())
                        messaging.subscribeToTopic(imei).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                alertDialog(getString(R.string.ntfc_vd_acti), getString(R.string.avs_con_int_not), requireContext(), R.drawable.ic_baseline_done_24);
                            } else
                                alertDialog(getString(R.string.erro), task.getException().getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        });
                    else if (!imei.isEmpty())
                        messaging.unsubscribeFromTopic(imei).addOnCompleteListener(task -> {
                            if (task.isSuccessful())
                                showToast(requireContext(), Color.rgb(102, 153, 0), getString(R.string.ntfc_vd_desac), R.drawable.ic_toast_feito);
                            else
                                alertDialog(getString(R.string.erro), task.getException().getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        });
                    else {
                        alertDialog(getString(R.string.erro), getString(R.string.imei_n_enc), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        return false;
                    }
                    MainActivity.dismissProgressBar();
                } else
                    return false;
                return true;
            });
        }
    }

    private void desabilitarMotivoIsencao(String taxa, ListPreference motivoIsento) {
        motivoIsento.setEnabled(taxa.equals("0"));
    }
}
