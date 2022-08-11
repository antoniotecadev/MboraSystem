package com.yoga.mborasystem.caixadialogo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.DatePicker;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.util.Event;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.VendaViewModel;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private VendaViewModel vendaViewModel;

    public DatePickerFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        boolean isVenda = DatePickerFragmentArgs.fromBundle(getArguments()).getIsVenda();

        long idcliente = DatePickerFragmentArgs.fromBundle(getArguments()).getIdcliente();
        boolean isDivida = DatePickerFragmentArgs.fromBundle(getArguments()).getIsDivida();
        long idusuario = DatePickerFragmentArgs.fromBundle(getArguments()).getIdusuario();
        boolean isPesquisa = DatePickerFragmentArgs.fromBundle(getArguments()).getIsPesquisa();

        String data = (((dayOfMonth < 10 ? "0" : "") + dayOfMonth) + "-" + Ultilitario.getMonth(month + 1)) + "-" + year;
        if (isVenda) {
            Ultilitario.showToast(getContext(), Color.parseColor("#795548"), data, R.drawable.ic_toast_feito);
            vendaViewModel.crud = true;
            vendaViewModel.consultarVendas(this, idcliente, isDivida, idusuario, false, true, null, true, data);
        } else {
            if (isPesquisa) {
                vendaViewModel.getDocumentoDatatAppLiveData().setValue(new Event<>(data));
            } else {
                vendaViewModel.getVendaDatatAppLiveData().setValue(new Event<>(data));
            }
        }
    }

}
