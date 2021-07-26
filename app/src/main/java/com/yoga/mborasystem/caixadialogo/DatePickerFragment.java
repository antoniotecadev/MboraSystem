package com.yoga.mborasystem.caixadialogo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogCriarUsuarioBinding;

import java.util.Calendar;

import androidx.fragment.app.DialogFragment;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private DialogCriarUsuarioBinding binding;

    public DatePickerFragment(DialogCriarUsuarioBinding binding) {
        this.binding = binding;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//        binding.editTextData.setText("");
//        binding.editTextData.setText(((dayOfMonth < 10 ? "0" : "") + dayOfMonth) + ((month < 10 ? "0" : "") + (month + 1)) + year);
    }
}
