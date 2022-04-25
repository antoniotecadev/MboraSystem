package com.yoga.mborasystem.view;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
import java.util.EventListener;
import java.util.Random;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import static com.yoga.mborasystem.util.Ultilitario.internetIsConnected;
import static com.yoga.mborasystem.util.Ultilitario.isNetworkConnected;

public class CadastrarClienteFragment extends Fragment {

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

//    private void spinnerBairros() {
//        MainActivity.getProgressBar();
//        String URL = "http://192.168.18.3/mborasystem-admin/public/api/luanda/bairros";
//        ArrayAdapter<String> bairros = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item);
//        Ion.with(requireActivity())
//                .load(URL)
//                .asJsonArray()
//                .setCallback((e, jsonElements) -> {
//                    try {
//                        bairros.add("");
//                        for (int i = 0; i < jsonElements.size(); i++) {
//                            JsonObject parceiro = jsonElements.get(i).getAsJsonObject();
//                            bairros.add(parceiro.get("br").getAsString());
//                        }
//                    } catch (Exception ex) {
//                        Toast.makeText(requireContext(), "Erro:" + ex.getMessage(), Toast.LENGTH_SHORT).show();
//                    } finally {
//                        MainActivity.dismissProgressBar();
//                    }
//                });
//        bairros.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.spinnerBairros.setAdapter(bairros);
//    }

    private View criarCliente(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentCadastrarClienteBinding.inflate(inflater, container, false);
        spinnerProvincias();
        spinnerMunicipios();
//        if (isNetworkConnected(requireContext())) {
//            if (internetIsConnected()) {
//                spinnerBairros();
//            } else {
//                Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.sm_int), R.drawable.ic_toast_erro);
//            }
//        } else {
//            Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.conec_wif_dad), R.drawable.ic_toast_erro);
//        }

//        binding.spinnerBairros.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                binding.editTextBairro.setText(parent.getItemAtPosition(position).toString());
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });

        binding.buttonCriarConta.setOnClickListener(v -> {
            try {
                clienteViewModel.validarCliente(Ultilitario.Operacao.CRIAR, binding.editTextNome, binding.editTextSobreNome, binding.editTextNif, binding.editTextNumeroTelefone, binding.editTextNumeroTelefoneAlternativo, binding.editTextEmail, binding.editTextNomeLoja, binding.spinnerProvincias, binding.spinnerMunicipios, binding.editTextBairro, binding.editTextRua, binding.editTextSenha, binding.editTextSenhaNovamente, binding.editTextCodigoEquipa);
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
                    try {
                        writeNewClient(binding);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                        Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
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
//        inflater.inflate(R.menu.menu_configuracao, menu);
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

    public void writeNewClient(FragmentCadastrarClienteBinding binding) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Cliente cliente = new Cliente();

        cliente.setId(1);
        cliente.setNome(binding.editTextNome.getText().toString());
        cliente.setSobrenome(binding.editTextSobreNome.getText().toString());
        cliente.setNifbi(binding.editTextNif.getText().toString());
        cliente.setMaster(true);
        cliente.setTelefone(binding.editTextNumeroTelefone.getText().toString());
        cliente.setTelefonealternativo(binding.editTextNumeroTelefoneAlternativo.getText().toString());
        cliente.setEmail(binding.editTextEmail.getText().toString());
        cliente.setNomeEmpresa(binding.editTextNomeLoja.getText().toString());
        cliente.setProvincia(binding.spinnerProvincias.getSelectedItem().toString());
        cliente.setMunicipio(binding.spinnerMunicipios.getSelectedItem().toString());
        cliente.setBairro(binding.editTextBairro.getText().toString());
        cliente.setRua(binding.editTextBairro.getText().toString());
        cliente.setSenha(binding.editTextSenha.getText().toString());
        cliente.setImei(System.currentTimeMillis() / 1000 + String.valueOf(new Random().nextInt((100000 - 1) + 1) + 1));
        cliente.setCodigoEquipa(binding.editTextCodigoEquipa.getText().toString());
        cliente.setData_cria(Ultilitario.getDateCurrent());

        mDatabase.child(cliente.getImei()).setValue(cliente);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
