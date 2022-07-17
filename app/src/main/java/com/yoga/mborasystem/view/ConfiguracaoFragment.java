package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.getSelectedIdioma;

import android.os.Bundle;
import android.widget.Toast;

import com.yoga.mborasystem.R;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.regex.Pattern;

public class ConfiguracaoFragment extends PreferenceFragmentCompat {

    private String codigoIdioma;
    private final Pattern numero = Pattern.compile("[^0-9 ]");

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_config, rootKey);
        ListPreference listaIdioma = findPreference("lista_idioma");
//        taxaIva.setOnPreferenceChangeListener((preference, newValue) -> {
//            return true;
//        });
        assert listaIdioma != null;
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
