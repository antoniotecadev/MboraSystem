package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.getSelectedIdioma;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.yoga.mborasystem.R;

public class ConfiguracaoFragment extends PreferenceFragmentCompat {

    private String codigoIdioma;
    private ListPreference motivoIsento;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_config, rootKey);
        ListPreference listaIdioma = findPreference("lista_idioma");
        ListPreference taxaIva = findPreference("taxa_iva");
        motivoIsento = findPreference("motivo_isencao");

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
