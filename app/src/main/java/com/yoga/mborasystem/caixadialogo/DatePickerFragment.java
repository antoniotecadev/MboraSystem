package com.yoga.mborasystem.caixadialogo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import com.yoga.mborasystem.viewmodel.VendaViewModel;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private VendaViewModel vendaViewModel;
    private Map<Integer, String> listMonth;

    public DatePickerFragment() {
        listMonth = new HashMap<>();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);

        listMonth.put(1, "janeiro");
        listMonth.put(2, "fevereiro");
        listMonth.put(3, "março");
        listMonth.put(4, "abril");
        listMonth.put(5, "maio");
        listMonth.put(6, "junho");
        listMonth.put(7, "julho");
        listMonth.put(8, "agosto");
        listMonth.put(9, "setembro");
        listMonth.put(10, "outubro");
        listMonth.put(11, "novembro");
        listMonth.put(12, "dezembro");

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//        Log.i("Picker", ((dayOfMonth < 10 ? "0" : "") + dayOfMonth) + "-" + ((month < 10 ? "0" : "") + (month + 1)) + "-" + year);
        String data = (((dayOfMonth < 10 ? "0" : "") + dayOfMonth) + "-" + getMonthString(month + 1)) + "-" + year;
        vendaViewModel.getVendasPoData(data);
    }

    private String getMonthString(int month) {
        return listMonth.get(month);
    }
}
