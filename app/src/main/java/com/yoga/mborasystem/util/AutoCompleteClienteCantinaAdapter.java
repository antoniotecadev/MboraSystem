package com.yoga.mborasystem.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.yoga.mborasystem.databinding.ClienteAutocompleteRowBinding;
import com.yoga.mborasystem.model.entidade.ClienteCantina;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AutoCompleteClienteCantinaAdapter extends ArrayAdapter<ClienteCantina> {

    private ClienteAutocompleteRowBinding binding;
    private List<ClienteCantina> clienteCantinaList;

    public AutoCompleteClienteCantinaAdapter(@NonNull Context context, @NonNull List<ClienteCantina> objects) {
        super(context, 0, objects);
        clienteCantinaList = new ArrayList<>(objects);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return clienteCantinaFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        binding = ClienteAutocompleteRowBinding.inflate(LayoutInflater.from(getContext()));

        ClienteCantina clienteCantina = getItem(position);

        if (clienteCantina != null) {
            binding.textNome.setText(clienteCantina.getNome());
            binding.textId.setText("" + clienteCantina.getId());
            binding.textTelefone.setText(clienteCantina.getTelefone());
        }

        return binding.getRoot();
    }

    private Filter clienteCantinaFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            List<ClienteCantina> clienteCantinas = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                clienteCantinas.addAll(clienteCantinaList);
            } else {
                String filterPatern = constraint.toString().toLowerCase().trim();
                for (ClienteCantina clienteCantina : clienteCantinaList) {
                    if (clienteCantina.getNome().toLowerCase().contains(filterPatern)) {
                        clienteCantinas.add(clienteCantina);
                    }
                }
            }
            filterResults.values = clienteCantinas;
            filterResults.count = clienteCantinas.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((ClienteCantina) resultValue).getNome();
        }
    };

}