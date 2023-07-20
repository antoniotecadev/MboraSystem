package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.acercaMboraSystem;
import static com.yoga.mborasystem.util.Ultilitario.activityResultContracts;
import static com.yoga.mborasystem.util.Ultilitario.activityResultContractsSelectFile;
import static com.yoga.mborasystem.util.Ultilitario.alertDialog;
import static com.yoga.mborasystem.util.Ultilitario.getAPN;
import static com.yoga.mborasystem.util.Ultilitario.getDetailDevice;
import static com.yoga.mborasystem.util.Ultilitario.getDeviceUniqueID;
import static com.yoga.mborasystem.util.Ultilitario.getPositionSpinner;
import static com.yoga.mborasystem.util.Ultilitario.reverse;
import static com.yoga.mborasystem.util.Ultilitario.showToast;
import static com.yoga.mborasystem.util.Ultilitario.spinnerProvincias;
import static com.yoga.mborasystem.util.Ultilitario.uriPath;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.yoga.mborasystem.model.entidade.ContaBancaria;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class CadastrarClienteFragment extends Fragment {

    private DatabaseReference mDatabase;
    private ClienteViewModel empresaViewModel;
    private FragmentCadastrarClienteBinding binding;
    private String error = "", imei;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference("parceiros");
        empresaViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return criarCliente(inflater, container);
    }

    private View criarCliente(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentCadastrarClienteBinding.inflate(inflater, container, false);
        spinnerProvincias(requireContext(), binding.spinnerProvincias);
        empresaViewModel.getMunicipios(binding.spinnerProvincias, binding.spinnerMunicipios);
        empresaViewModel.getBairros(binding.spinnerMunicipios, binding.spinnerBairros);

        binding.spinnerBairros.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.editTextBairro.setText(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.buttonCriarConta.setOnClickListener(v -> criarContaEmpresa());
        binding.buttonTermoCondicao.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getAPN(requireActivity()) + "termoscondicoes"))));
        binding.checkTermoCondicao.setOnCheckedChangeListener((buttonView, isChecked) -> binding.buttonCriarConta.setEnabled(isChecked));

        empresaViewModel.getValido().observe(getViewLifecycleOwner(), operacao -> {
            switch (operacao) {
                case CRIAR:
                    sendNoticationMboraSystemAdmin(imei);
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

    private void criarContaEmpresa() {
        if (binding.editTextEmail.getText().toString().isEmpty()) {
            binding.editTextEmail.requestFocus();
            binding.editTextEmail.setError(requireContext().getString(R.string.dig_eml));
        } else {
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(binding.editTextEmail.getText().toString()).addOnCompleteListener(requireActivity(), task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().getSignInMethods().isEmpty()) {
                        imei = System.currentTimeMillis() / 1000 + String.valueOf(new Random().nextInt((100000 - 1) + 1) + 1);
                        empresaViewModel.validarDadosEmpresa(Ultilitario.Operacao.CRIAR, binding.editTextNome, binding.editTextSobreNome, binding.editTextNif, binding.editTextNumeroTelefone, binding.editTextNumeroTelefoneAlternativo, binding.editTextEmail, binding.editTextNomeEmpresa, binding.spinnerProvincias, binding.spinnerMunicipios, binding.editTextBairro, binding.editTextRua, binding.editTextSenha, binding.editTextSenhaNovamente, binding.editTextCodigoEquipa, imei, getRegimeIva(), requireActivity());
                    } else {
                        binding.editTextEmail.requestFocus();
                        binding.editTextEmail.setError(requireContext().getString(R.string.email_invalido_msg));
                    }
                } else
                    alertDialog(requireContext().getString(R.string.erro), task.getException().getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            });
        }
    }

    private void sendNoticationMboraSystemAdmin(String imei) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(Objects.requireNonNull(binding.editTextEmail.getText()).toString(), Objects.requireNonNull(binding.editTextSenha.getText()).toString())
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Map<String, String> empresa = new HashMap<>();
                        empresa.put("id", "");
                        empresa.put("imei", imei);
                        binding.buttonCriarConta.setEnabled(false);
                        mDatabase.child(imei).setValue(empresa).addOnFailureListener(e -> FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(task1 -> {
                            if (!task1.isSuccessful())
                                error = task.getException().getMessage();
                        }));
                        navegarTelaBloqueio(error.isEmpty() ? "" : error);
                    } else
                        navegarTelaBloqueio(task.getException().getMessage());
                });
    }

    private void navegarTelaBloqueio(String error) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            CadastrarClienteFragmentDirections.ActionCadastrarClienteFragmentToBloquearFragment cadastrarClienteBinding = CadastrarClienteFragmentDirections.actionCadastrarClienteFragmentToBloquearFragment().setErrorCreateUser(error.isEmpty() ? "" : error).setIsCreateUser(true);
            Navigation.findNavController(requireView()).navigate(cadastrarClienteBinding);
        }, 1000);
    }

    private final ActivityResultLauncher<Intent> requestIntentPermissionLauncherImportDataBase = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> activityResultContracts(requireContext(), result.getResultCode() == Activity.RESULT_OK, uriPath)
    );

    private final ActivityResultLauncher<String> requestPermissionLauncherImportDataBase = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> activityResultContracts(requireContext(), result, uriPath)
    );

    ActivityResultLauncher<Intent> importarBaseDeDados = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK)
                    activityResultContractsSelectFile(requireActivity(), requireContext(), true,"0000000000", result, requestPermissionLauncherImportDataBase, requestIntentPermissionLauncherImportDataBase);
            });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
