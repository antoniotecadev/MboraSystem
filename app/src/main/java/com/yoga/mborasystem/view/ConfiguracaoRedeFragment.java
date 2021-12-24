package com.yoga.mborasystem.view;

import android.os.Bundle;

import com.yoga.mborasystem.R;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class ConfiguracaoRedeFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_rede, rootKey);
        EditTextPreference salvar = findPreference("url_api");
    }
}
