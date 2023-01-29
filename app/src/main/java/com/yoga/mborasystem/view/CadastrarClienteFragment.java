package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.acercaMboraSystem;
import static com.yoga.mborasystem.util.Ultilitario.alertDialog;
import static com.yoga.mborasystem.util.Ultilitario.conexaoInternet;
import static com.yoga.mborasystem.util.Ultilitario.getAPN;
import static com.yoga.mborasystem.util.Ultilitario.getDetailDevice;
import static com.yoga.mborasystem.util.Ultilitario.getDeviceUniqueID;
import static com.yoga.mborasystem.util.Ultilitario.getPositionSpinner;
import static com.yoga.mborasystem.util.Ultilitario.reverse;
import static com.yoga.mborasystem.util.Ultilitario.showToast;
import static com.yoga.mborasystem.util.Ultilitario.spinnerProvincias;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentCadastrarClienteBinding;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.model.entidade.ContaBancaria;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CadastrarClienteFragment extends Fragment {

    private ExecutorService executor;
    private DatabaseReference mDatabase;
    private ClienteViewModel clienteViewModel;
    private FragmentCadastrarClienteBinding binding;
    private String errorClienteUser = "", imei, uriPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference("parceiros");
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return criarCliente(inflater, container);
    }

    private View criarCliente(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentCadastrarClienteBinding.inflate(inflater, container, false);
        spinnerProvincias(requireContext(), binding.spinnerProvincias);
        clienteViewModel.getMunicipios(binding.spinnerProvincias, binding.spinnerMunicipios);
        binding.spinnerMunicipios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (conexaoInternet(requireContext()))
                    if (!parent.getItemAtPosition(position).toString().isEmpty()) {
                        binding.spinnerBairros.setAdapter(clienteViewModel.consultarBairros(requireContext(), parent.getItemAtPosition(position).toString(), requireView()));
                    } else
                        MainActivity.dismissProgressBar();
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

        binding.buttonCriarConta.setOnClickListener(v -> cadastrarParceiro());
        binding.buttonTermoCondicao.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getAPN(requireActivity()) + "termoscondicoes"))));
        binding.checkTermoCondicao.setOnCheckedChangeListener((buttonView, isChecked) -> binding.buttonCriarConta.setEnabled(isChecked));

        clienteViewModel.getValido().observe(getViewLifecycleOwner(), operacao -> {
            switch (operacao) {
                case CRIAR:
                    saveUserInFirebase(imei);
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
                                                      Ultilitario.importarCategoriasProdutosClientes(importarBaseDeDados, requireActivity(), true);
                                                  } else if (menuItem.getItemId() == R.id.device) {
                                                      getDetailDevice(requireContext());
                                                  } else if (menuItem.getItemId() == R.id.formaPagamento) {
                                                      MainActivity.getProgressBar();
                                                      DatabaseReference reference = FirebaseDatabase.getInstance().getReference("yoga").child("contabancaria");
                                                      reference.get().addOnCompleteListener(task -> {
                                                          if (task.isSuccessful()) {
                                                              StringBuilder ddbc = new StringBuilder();
                                                              DataSnapshot snapshot = task.getResult();
                                                              if (snapshot.exists()) {
                                                                  String detalhe = snapshot.child("informacao").child("detalhe").getValue().toString();
                                                                  ddbc.append(getString(R.string.info_pagamento, detalhe)).append("\n\n");
                                                                  for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                                      if (dataSnapshot.exists()) {
                                                                          ContaBancaria cb = snapshot.child(dataSnapshot.getKey()).getValue(ContaBancaria.class);
                                                                          if (cb.getNome() != null) {
                                                                              ddbc.append(getString(R.string.nm_bc)).append(": ").append(cb.getNome()).append("\n");
                                                                              ddbc.append(getString(R.string.ppt_bc)).append(": ").append(cb.getProprietario()).append("\n");
                                                                              ddbc.append(getString(R.string.nib_bc)).append(": ").append(cb.getNib()).append("\n");
                                                                              ddbc.append(getString(R.string.iban_bc)).append(": ").append(cb.getIban()).append("\n");
                                                                              ddbc.append("\n\n");
                                                                          }
                                                                      } else
                                                                          showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.dds_n_enc), R.drawable.ic_toast_erro);
                                                                  }
                                                              } else
                                                                  showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.dds_n_enc), R.drawable.ic_toast_erro);
                                                              MainActivity.dismissProgressBar();
                                                              alertDialog(getString(R.string.forma_pagamento), ddbc.toString(), requireContext(), R.drawable.ic_baseline_store_24);
                                                          } else {
                                                              MainActivity.dismissProgressBar();
                                                              alertDialog(getString(R.string.erro), task.getException().getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                                                          }
                                                      });
                                                  } else if (menuItem.getItemId() == R.id.acercaMborasytem)
                                                      acercaMboraSystem(requireContext(), requireActivity());
                                                  return NavigationUI.onNavDestinationSelected(menuItem, navController);
                                              }
                                          },

                getViewLifecycleOwner());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(), getContext()));
        return binding.getRoot();
    }

    private String getRegimeIva() {
        return getPositionSpinner(requireContext(), binding.spinnerRegimeIva, R.array.array_regime_iva_valor, R.array.array_regime_iva_posicao, "0");
    }

    private void cadastrarParceiro() {
        if (binding.editTextEmail.getText().toString().isEmpty()) {
            binding.editTextEmail.requestFocus();
            binding.editTextEmail.setError(requireContext().getString(R.string.dig_eml));
        } else {
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(binding.editTextEmail.getText().toString()).addOnCompleteListener(requireActivity(), task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().getSignInMethods().isEmpty()) {
                        try {
                            imei = System.currentTimeMillis() / 1000 + String.valueOf(new Random().nextInt((100000 - 1) + 1) + 1);
                            clienteViewModel.validarCliente(Ultilitario.Operacao.CRIAR, binding.editTextNome, binding.editTextSobreNome, binding.editTextNif, binding.editTextNumeroTelefone, binding.editTextNumeroTelefoneAlternativo, binding.editTextEmail, binding.editTextNomeEmpresa, binding.spinnerProvincias, binding.spinnerMunicipios, binding.editTextBairro, binding.editTextRua, binding.editTextSenha, binding.editTextSenhaNovamente, binding.editTextCodigoEquipa, imei, getRegimeIva(), requireActivity());
                        } catch (Exception e) {
                            alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        }
                    } else {
                        binding.editTextEmail.requestFocus();
                        binding.editTextEmail.setError(requireContext().getString(R.string.email_invalido_msg));
                    }
                } else
                    alertDialog(requireContext().getString(R.string.erro), task.getException().getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            });
        }
    }

    private void saveUserInFirebase(String imei) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(Objects.requireNonNull(binding.editTextEmail.getText()).toString(), Objects.requireNonNull(binding.editTextSenha.getText()).toString())
                .addOnCompleteListener(requireActivity(), task -> {
                    Cliente cliente = new Cliente();
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        cliente.setId("");
                        cliente.setUid(uid);
                        cliente.setImei(imei);
                        cliente.setNome(binding.editTextNome.getText().toString());
                        cliente.setSobrenome(binding.editTextSobreNome.getText().toString());
                        cliente.setEmail(binding.editTextEmail.getText().toString());
                        cliente.setTelefone(binding.editTextNumeroTelefone.getText().toString());
                        cliente.setTelefonealternativo(binding.editTextNumeroTelefoneAlternativo.getText().toString());
                        cliente.setNomeEmpresa(binding.editTextNomeEmpresa.getText().toString());
                        cliente.setProvincia(binding.spinnerProvincias.getSelectedItem().toString());
                        cliente.setMunicipio(binding.spinnerMunicipios.getSelectedItem().toString());
                        cliente.setBairro(binding.editTextBairro.getText().toString());
                        cliente.setRua(binding.editTextRua.getText().toString());
                        cliente.setCodigoPlus("");
                        cliente.setFotoCapaUrl("");
                        cliente.setFotoPerfilUrl("");
                        binding.buttonCriarConta.setEnabled(false);
                        mDatabase.child(imei).setValue(cliente).addOnFailureListener(e -> FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(task1 -> {
                            if (!task1.isSuccessful())
                                errorClienteUser = task.getException().getMessage();
                        }));
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            CadastrarClienteFragmentDirections.ActionCadastrarClienteFragmentToBloquearFragment cadastrarClienteBinding = CadastrarClienteFragmentDirections.actionCadastrarClienteFragmentToBloquearFragment().setErrorCreateUser(errorClienteUser.isEmpty() ? "" : errorClienteUser).setIsCreateUser(true);
                            Navigation.findNavController(requireView()).navigate(cadastrarClienteBinding);
                        }, 1000);
                    } else
                        alertDialog(getString(R.string.erro), task.getException().getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                });
    }

    private final ActivityResultLauncher<String> requestPermissionLauncherImportDataBase = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    MainActivity.getProgressBar();
                    executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> Ultilitario.importDB(requireContext(), new Handler(Looper.getMainLooper()), uriPath));
                } else
                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_perm_n_pod_imp_bd), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
    );
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
                                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> requestPermissionLauncherImportDataBase.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE))
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
