package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.getSelectedIdioma;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.util.Ultilitario;

import java.util.Objects;

public class ConfiguracaoFragment extends PreferenceFragmentCompat {

    private String codigoIdioma;
    private ListPreference motivoIsento;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_config, rootKey);
        SwitchPreferenceCompat bloAut = findPreference("bloaut");
        EditTextPreference pin = findPreference("pinadmin");
        ListPreference listaIdioma = findPreference("lista_idioma");
        ListPreference taxaIva = findPreference("taxa_iva");
        motivoIsento = findPreference("motivo_isencao");

        if (bloAut != null) {
            bloAut.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!bloAut.isChecked())
                    Ultilitario.alertDialog(getString(R.string.avs), getString(R.string.bloaut_msg), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                return true;
            });
        }

        if (pin != null) {
            pin.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                if (TextUtils.isEmpty(preference.getText()))
                    return getString(R.string.nao_def);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Objects.requireNonNull(pin.getText()).length(); i++) {
                    sb.append("●");
                }
                return sb.toString();
            });
            pin.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD));
        }

        if (taxaIva != null) {
            desabilitarMotivoIsencao(taxaIva.getValue(), motivoIsento);
            taxaIva.setOnPreferenceChangeListener((preference, newValue) -> {
                int taxas = Integer.parseInt(newValue.toString());
                String[] taxa_values = getResources().getStringArray(R.array.array_taxa_iva_valor);
                String taxa = taxa_values[taxas == 14 ? 3 : taxas == 7 ? 2 : (taxas == 5 ? 1 : 0)];
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
    }

    private void desabilitarMotivoIsencao(String taxa, ListPreference motivoIsento) {
        motivoIsento.setEnabled(taxa.equals("0"));
    }
}
