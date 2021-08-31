package com.yoga.mborasystem.util;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import com.yoga.mborasystem.viewmodel.VendaViewModel;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DatePickerExpImp extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private VendaViewModel vendaViewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String data = (((dayOfMonth < 10 ? "0" : "") + dayOfMonth) + "-" + Ultilitario.getMonth(month + 1)) + "-" + year;
        vendaViewModel.getDataExportAppLiveData().setValue(new Event<>(data));
    }

}
