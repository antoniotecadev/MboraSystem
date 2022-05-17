package com.yoga.mborasystem.view;

import android.os.Bundle;
import android.widget.Toast;

import com.yoga.mborasystem.R;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.regex.Pattern;

public class ConfiguracaoFragment extends PreferenceFragmentCompat {

    private final Pattern numero = Pattern.compile("[^0-9 ]");

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_config, rootKey);
        EditTextPreference percentagemIva = findPreference("percentagem_iva");
        percentagemIva.setOnPreferenceChangeListener((preference, newValue) -> {
            if (numero.matcher(newValue.toString()).find() || newValue.toString().length() > 2) {
                Toast.makeText(getContext(), getString(R.string.numero_invalido), Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        });
    }
}
