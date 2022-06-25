package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.internetIsConnected;
import static com.yoga.mborasystem.util.Ultilitario.isNetworkConnected;

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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentCadastrarClienteBinding;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class CadastrarClienteFragment extends Fragment {

    private String imei;
    private Query query;
    private DatabaseReference mDatabase;
    private ClienteViewModel clienteViewModel;
    private FragmentCadastrarClienteBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference("cliente");
//        query = FirebaseDatabase.getInstance().getReference("cliente").limitToLast(1);
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        query.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                if (snapshot.exists()) {
//                    Cliente cliente = snapshot.getValue(Cliente.class);
//                    Log.i("cliente", cliente.getData_cria() + "");
//                } else {
//                    Toast.makeText(requireActivity(), "Vazio", Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });

//        setHasOptionsMenu(true);
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

    private void spinnerBairros(String município) {
        MainActivity.getProgressBar();
        String URL = Ultilitario.getAPN(requireActivity()) + "/mborasystem-admin/public/api/" + município.trim().replaceAll("\\s+", "") + "/bairros";
        ArrayAdapter<String> bairros = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item);
        Ion.with(requireActivity())
                .load(URL)
                .asJsonArray()
                .setCallback((e, jsonElements) -> {
                    try {
                        bairros.add("");
                        for (int i = 0; i < jsonElements.size(); i++) {
                            JsonObject parceiro = jsonElements.get(i).getAsJsonObject();
                            bairros.add(parceiro.get("br").getAsString());
                        }
                        if (bairros.getItem(1).isEmpty())
                            Toast.makeText(requireContext(), getString(R.string.br_na_enc_mun), Toast.LENGTH_LONG).show();
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
        binding.spinnerMunicipios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isNetworkConnected(requireContext())) {
                    if (internetIsConnected()) {
                        if (!parent.getItemAtPosition(position).toString().isEmpty())
                            spinnerBairros(parent.getItemAtPosition(position).toString());
                    } else {
                        Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.sm_int), R.drawable.ic_toast_erro);
                    }
                } else {
                    Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.conec_wif_dad), R.drawable.ic_toast_erro);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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
                imei = System.currentTimeMillis() / 1000 + String.valueOf(new Random().nextInt((100000 - 1) + 1) + 1);
                clienteViewModel.validarCliente(Ultilitario.Operacao.CRIAR, binding.editTextNome, binding.editTextSobreNome, binding.editTextNif, binding.editTextNumeroTelefone, binding.editTextNumeroTelefoneAlternativo, binding.editTextEmail, binding.editTextNomeLoja, binding.spinnerProvincias, binding.spinnerMunicipios, binding.editTextBairro, binding.editTextRua, binding.editTextSenha, binding.editTextSenhaNovamente, binding.editTextCodigoEquipa, imei);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), getText(R.string.erro) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), getText(R.string.erro) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        binding.buttonTermoCondicao.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Ultilitario.getAPN(requireActivity()) + "/mborasystem-admin/public/api/termoscondicoes"))));
        binding.checkTermoCondicao.setOnCheckedChangeListener((buttonView, isChecked) -> binding.buttonCriarConta.setEnabled(isChecked));

        Ultilitario.getValido().observe(getViewLifecycleOwner(), operacao -> {
            switch (operacao) {
                case CRIAR:
                    try {
                        writeNewClient(binding, imei);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                        Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    MainActivity.dismissProgressBar();
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    public void writeNewClient(FragmentCadastrarClienteBinding binding, String imei) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Cliente cliente = new Cliente();
        cliente.setId(1);
        cliente.setNome(Objects.requireNonNull(binding.editTextNome.getText()).toString());
        cliente.setSobrenome(Objects.requireNonNull(binding.editTextSobreNome.getText()).toString());
        cliente.setNifbi(Objects.requireNonNull(binding.editTextNif.getText()).toString());
        cliente.setMaster(true);
        cliente.setTelefone(Objects.requireNonNull(binding.editTextNumeroTelefone.getText()).toString());
        cliente.setTelefonealternativo(Objects.requireNonNull(binding.editTextNumeroTelefoneAlternativo.getText()).toString());
        cliente.setEmail(Objects.requireNonNull(binding.editTextEmail.getText()).toString());
        cliente.setNomeEmpresa(Objects.requireNonNull(binding.editTextNomeLoja.getText()).toString());
        cliente.setProvincia(binding.spinnerProvincias.getSelectedItem().toString());
        cliente.setMunicipio(binding.spinnerMunicipios.getSelectedItem().toString());
        cliente.setBairro(Objects.requireNonNull(binding.editTextBairro.getText()).toString());
        cliente.setRua(binding.editTextBairro.getText().toString());
        cliente.setSenha(Objects.requireNonNull(binding.editTextSenha.getText()).toString());
        cliente.setImei(imei);
        cliente.setCodigoEquipa(Objects.requireNonNull(binding.editTextCodigoEquipa.getText()).toString());
        cliente.setData_cria(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
        cliente.setVisualizado(false);
        mDatabase.child(cliente.getImei()).setValue(cliente);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
