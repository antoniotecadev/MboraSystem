package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.alertDialog;
import static com.yoga.mborasystem.util.Ultilitario.bytesToHex;
import static com.yoga.mborasystem.util.Ultilitario.getDetailDevice;
import static com.yoga.mborasystem.util.Ultilitario.getDeviceUniqueID;
import static com.yoga.mborasystem.util.Ultilitario.getHash;
import static com.yoga.mborasystem.util.Ultilitario.internetIsConnected;
import static com.yoga.mborasystem.util.Ultilitario.isNetworkConnected;
import static com.yoga.mborasystem.util.Ultilitario.reverse;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
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

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CadastrarClienteFragment extends Fragment {

    private String imei;
    private Query query;
    private ExecutorService executor;
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

    private void spinnerBairros(String município) {
        String URL = Ultilitario.getAPN(requireActivity()) + "/mborasystem-admin/public/api/" + município.trim().replaceAll("\\s+", "") + "/bairros";
        ArrayAdapter<String> bairros = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
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
                            Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.br_na_enc_mun), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        else
                            Snackbar.make(requireView(), getString(R.string.br_car), Snackbar.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        Ultilitario.alertDialog(getString(R.string.erro), ex.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                    } finally {
                        MainActivity.dismissProgressBar();
                    }
                });
        bairros.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerBairros.setAdapter(bairros);
    }

    private View criarCliente(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentCadastrarClienteBinding.inflate(inflater, container, false);
        Ultilitario.spinnerProvincias(requireContext(), binding.spinnerProvincias);
        Ultilitario.spinnerMunicipios(requireContext(), binding.spinnerMunicipios);
        binding.spinnerMunicipios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.getProgressBar();
                if (isNetworkConnected(requireContext())) {
                    if (internetIsConnected()) {
                        if (!parent.getItemAtPosition(position).toString().isEmpty())
                            spinnerBairros(parent.getItemAtPosition(position).toString());
                        else
                            MainActivity.dismissProgressBar();
                    } else {
                        MainActivity.dismissProgressBar();
                        Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_int), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                    }
                } else {
                    MainActivity.dismissProgressBar();
                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.conec_wif_dad), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
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
                clienteViewModel.validarCliente(Ultilitario.Operacao.CRIAR, binding.editTextNome, binding.editTextSobreNome, binding.editTextNif, binding.editTextNumeroTelefone, binding.editTextNumeroTelefoneAlternativo, binding.editTextEmail, binding.editTextNomeEmpresa, binding.spinnerProvincias, binding.spinnerMunicipios, binding.editTextBairro, binding.editTextRua, binding.editTextSenha, binding.editTextSenhaNovamente, binding.editTextCodigoEquipa, imei, requireActivity());
            } catch (Exception e) {
                Ultilitario.alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
        });
        binding.buttonTermoCondicao.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Ultilitario.getAPN(requireActivity()) + "/mborasystem-admin/public/api/termoscondicoes"))));
        binding.checkTermoCondicao.setOnCheckedChangeListener((buttonView, isChecked) -> binding.buttonCriarConta.setEnabled(isChecked));

        clienteViewModel.getValido().observe(getViewLifecycleOwner(), operacao -> {
            switch (operacao) {
                case CRIAR:
                    try {
                        writeNewClient(binding, imei);
                    } catch (Exception e) {
                        Ultilitario.alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
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
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_home_ferramenta, menu);
                menu.findItem(R.id.dialogAlterarCliente).setVisible(false);
                menu.findItem(R.id.estadoCliente).setVisible(false);
                menu.findItem(R.id.gerarCodigoQr).setVisible(false);
                menu.findItem(R.id.dialogAlterarCodigoPin).setVisible(false);
                menu.findItem(R.id.termosCondicoes).setVisible(false);
                menu.findItem(R.id.politicaPrivacidade).setVisible(false);
                menu.findItem(R.id.acercaMborasytem).setVisible(false);
                menu.findItem(R.id.itemSair).setVisible(false);
                menu.findItem(R.id.bloquearFragment).setVisible(false);
                menu.findItem(R.id.idioma).setVisible(false);
                menu.findItem(R.id.expoBd).setVisible(false);
                menu.findItem(R.id.device).setTitle(reverse(getDeviceUniqueID(requireActivity())));
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
                if (menuItem.getItemId() == R.id.config)
                    Navigation.findNavController(requireView()).navigate(R.id.action_cadastrarClienteFragment_to_configuracaoFragment2);
                else if (menuItem.getItemId() == R.id.impoBd) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                        Ultilitario.importarCategoriasProdutosClientes(importarBaseDeDados, requireActivity(), true);
                    else
                        alertDialog(getString(R.string.avs), getString(R.string.imp_dis_api_sup), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                } else if (menuItem.getItemId() == R.id.device)
                    getDetailDevice(requireContext());
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        }, getViewLifecycleOwner());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(), getContext()));
        return binding.getRoot();
    }

    public void writeNewClient(FragmentCadastrarClienteBinding binding, String imei) {
        Cliente cliente = new Cliente();
        cliente.setId(1);
        cliente.setNome(Objects.requireNonNull(binding.editTextNome.getText()).toString());
        cliente.setSobrenome(Objects.requireNonNull(binding.editTextSobreNome.getText()).toString());
        cliente.setNifbi(Objects.requireNonNull(binding.editTextNif.getText()).toString());
        cliente.setMaster(true);
        cliente.setTelefone(Objects.requireNonNull(binding.editTextNumeroTelefone.getText()).toString());
        cliente.setTelefonealternativo(Objects.requireNonNull(binding.editTextNumeroTelefoneAlternativo.getText()).toString());
        cliente.setEmail(Objects.requireNonNull(binding.editTextEmail.getText()).toString());
        cliente.setNomeEmpresa(Objects.requireNonNull(binding.editTextNomeEmpresa.getText()).toString());
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

    private String uriPath;
    ActivityResultLauncher<Intent> importarBaseDeDados = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri uri;
                    if (data != null) {
                        uri = data.getData();
                        try {
                            uriPath = TextUtils.split(uri.getPath(), "/")[4];
                        } catch (IndexOutOfBoundsException e) {
                            alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        }
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setIcon(R.drawable.ic_baseline_insert_drive_file_24)
                                .setTitle(getString(R.string.impoBd))
                                .setMessage(uri.getPath() + "\n\n" + getString(R.string.imp_elim_bd))
                                .setNegativeButton(getString(R.string.cancelar), (dialogInterface, i) -> dialogInterface.dismiss())
                                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                                    try {
                                        String stringHash = TextUtils.split(uriPath, "-")[2];
                                        byte[] bytesHash = getHash(reverse(getDeviceUniqueID(requireActivity())));
                                        if (bytesToHex(bytesHash).equals(stringHash)) {
                                            MainActivity.getProgressBar();
                                            executor = Executors.newSingleThreadExecutor();
                                            executor.execute(() -> Ultilitario.importDB(requireContext(), new Handler(Looper.getMainLooper()), uriPath));
                                        } else
                                            alertDialog(getString(R.string.erro), getString(R.string.inc_bd), requireContext(), R.drawable.ic_baseline_close_24);
                                    } catch (Exception e) {
                                        alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                                    }
                                })
                                .show();
                    }
                }
            });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
