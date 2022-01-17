package com.yoga.mborasystem.view;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentCadastrarClienteBinding;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import static com.yoga.mborasystem.util.Ultilitario.internetIsConnected;
import static com.yoga.mborasystem.util.Ultilitario.isNetworkConnected;

public class CadastrarClienteFragment extends Fragment {

    private ClienteViewModel clienteViewModel;
    private FragmentCadastrarClienteBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return criarCliente(inflater, container);
    }

    private void spinnerProvincias() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.provincias, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerProvincias.setAdapter(adapter);
    }

    private void spinnerMunicipios() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.municipios, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMunicipios.setAdapter(adapter);
    }

    String[] municipio, bairro;

    private void spinnerBairros() {
        MainActivity.getProgressBar();
        String URL = "http://192.168.18.3/mborasystem-admin/public/api/bairros";
        ArrayAdapter<String> bairros = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item);
        Ion.with(requireActivity())
                .load(URL)
                .asJsonArray()
                .setCallback((e, jsonElements) -> {
                    try {
                        for (int i = 0; i < jsonElements.size(); i++) {
                            JsonObject parceiro = jsonElements.get(i).getAsJsonObject();
                            bairros.add("");
                            bairros.add(parceiro.get("mr_bt").getAsString());
                        }
                    } catch (Exception ex) {
                        Toast.makeText(requireContext(), "Erro:" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        MainActivity.dismissProgressBar();
                    }
                });
        bairros.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerBairros.setAdapter(bairros);
    }

    private View criarCliente(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentCadastrarClienteBinding.inflate(inflater, container, false);
        spinnerProvincias();
        spinnerMunicipios();
        if (isNetworkConnected(requireContext())) {
            if (internetIsConnected()) {
                spinnerBairros();
            } else {
                Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.sm_int), R.drawable.ic_toast_erro);
            }
        } else {
            Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.conec_wif_dad), R.drawable.ic_toast_erro);
        }

        binding.spinnerBairros.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.editTextBairro.setText(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.buttonCriarConta.setOnClickListener(v -> {
            try {
                clienteViewModel.validarCliente(Ultilitario.Operacao.CRIAR, binding.editTextNome, binding.editTextSobreNome, binding.editTextNif, binding.editTextNumeroTelefone, binding.editTextNumeroTelefoneAlternativo, binding.editTextEmail, binding.editTextNomeLoja, binding.spinnerProvincias, binding.spinnerMunicipios, binding.editTextBairro, binding.editTextRua, binding.editTextSenha, binding.editTextSenhaNovamente);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), getText(R.string.erro) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), getText(R.string.erro) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        binding.buttonTermoCondicao.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.18.3/mborasystem-admin/public/api/termoscondicoes")));
        });
        binding.checkTermoCondicao.setOnCheckedChangeListener((buttonView, isChecked) -> binding.buttonCriarConta.setEnabled(isChecked));

        Ultilitario.getValido().observe(getViewLifecycleOwner(), operacao -> {
            switch (operacao) {
                case CRIAR:
                    Ultilitario.dialogConta(getString(R.string.conta_criada), getContext()).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_cadastrarClienteFragment_to_bloquearFragment);
                    break;
                case NENHUMA:
                    Ultilitario.dialogConta(getString(R.string.conta_nao_criada), getContext()).show();
                    break;
                default:
                    break;
            }
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(), getContext()));
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_configuracao, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
        switch (item.getItemId()) {
            case R.id.configRede:
                Navigation.findNavController(requireView()).navigate(R.id.action_cadastrarClienteFragment_to_configuracaoRedeFragment);
                break;
            default:
                break;
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
