package com.yoga.mborasystem.caixadialogo;

import static com.yoga.mborasystem.util.Ultilitario.internetIsConnected;
import static com.yoga.mborasystem.util.Ultilitario.isNetworkConnected;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogCriarClienteCantinaBinding;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteCantinaViewModel;

import java.util.Objects;
import java.util.regex.Pattern;

public class DialogCriarClienteCantina extends DialogFragment {

    private AlertDialog dialog;
    private ClienteCantina clienteCantina;
    private DialogCriarClienteCantinaBinding binding;
    private ClienteCantinaViewModel clienteCantinaViewModel;

    public static Pattern letraNumero = Pattern.compile("[^a-zA-Z0-9]");

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        clienteCantina = new ClienteCantina();

        binding = DialogCriarClienteCantinaBinding.inflate(getLayoutInflater());
        clienteCantinaViewModel = new ViewModelProvider(requireActivity()).get(ClienteCantinaViewModel.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setIcon(R.drawable.ic_baseline_store_24);
        builder.setTitle(getString(R.string.criar_cliente));

        String nif = DialogCriarClienteCantinaArgs.fromBundle(getArguments()).getNif();
        String nome = DialogCriarClienteCantinaArgs.fromBundle(getArguments()).getNomeCliente();
        String telefone = DialogCriarClienteCantinaArgs.fromBundle(getArguments()).getTelefoneCliente();
        String email = DialogCriarClienteCantinaArgs.fromBundle(getArguments()).getEmail();
        String endereco = DialogCriarClienteCantinaArgs.fromBundle(getArguments()).getEndereco();
        long idcliente = DialogCriarClienteCantinaArgs.fromBundle(getArguments()).getIdcliente();

        binding.txtNIF.setText(nif);
        binding.editTextNome.setText(nome);
        binding.editTextNumeroTelefone.setText(telefone);
        binding.editTextEmail.setText(email);
        binding.editTextEndereco.setText(endereco);

        if (idcliente == Ultilitario.ZERO) {
            binding.buttonGuardar.setVisibility(View.GONE);
            binding.buttonCriarCliente.setVisibility(View.VISIBLE);
            binding.buttonEliminarCliente.setVisibility(View.GONE);
        } else {
            binding.buttonGuardar.setVisibility(View.VISIBLE);
            binding.buttonCriarCliente.setVisibility(View.GONE);
            binding.buttonEliminarCliente.setVisibility(View.VISIBLE);
        }

        binding.btnValidarNIF.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            if (isNetworkConnected(requireContext())) {
                if (internetIsConnected()) {
                    if (letraNumero.matcher(Objects.requireNonNull(binding.txtNIF.getText()).toString()).find() || binding.txtNIF.length() > 14 || binding.txtNIF.length() < 10) {
                        Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.nif_bi) + " " + getString(R.string.ivld), R.drawable.ic_toast_erro);
                    } else {
                        MainActivity.getProgressBar();
                        Ion.with(requireActivity())
                                .load("https://api.gov.ao/consultarBI/v2/?bi=" + binding.txtNIF.getText().toString().toUpperCase())
                                .asJsonArray()
                                .setCallback((e, jsonElements) -> {
                                    try {
                                        if (jsonElements.size() == 0) {
                                            Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.dados_nao_encontrado), R.drawable.ic_toast_erro);
                                        } else {
                                            JsonObject cliente = jsonElements.get(0).getAsJsonObject();
                                            sb.append(getString(R.string.nm)).append(": ").append("\n").append(cliente.get("FIRST_NAME").getAsString()).append(" ").append(cliente.get("LAST_NAME").getAsString()).append("\n")
                                                    .append(getString(R.string.rsdc)).append(": ").append("\n").append(cliente.get("RESIDENCE_ADDRESS")).append("\n").append(cliente.get("RESIDENCE_NEIGHBOR"))
                                                    .append("\n").append(cliente.get("RESIDENCE_MUNICIPALITY_NAME")).append("\n").append(cliente.get("RESIDENCE_PROVINCE_NAME")).append("\n")
                                                    .append(getString(R.string.nif_bi)).append(": ").append("\n").append(cliente.get("ID_NUMBER")).append("\n");
                                            Ultilitario.alertDialog(getString(R.string.nif_bi) + " " + getString(R.string.vld), sb.toString(), requireContext(), R.drawable.ic_baseline_done_24);
                                        }
                                    } catch (Exception exception) {
                                        Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.erro) + "\n" + exception.getMessage(), R.drawable.ic_toast_erro);
                                    } finally {
                                        MainActivity.dismissProgressBar();
                                    }
                                });
                    }
                } else {
                    Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.sm_int), R.drawable.ic_toast_erro);
                    MainActivity.dismissProgressBar();
                }
            } else {
                Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.conec_wif_dad), R.drawable.ic_toast_erro);
                MainActivity.dismissProgressBar();
            }
        });

        binding.buttonCriarCliente.setOnClickListener(v -> {
            clienteCantinaViewModel.crud = true;
            clienteCantinaViewModel.criarCliente(binding.txtNIF, binding.editTextNome, binding.editTextNumeroTelefone, binding.editTextEmail, binding.editTextEndereco, dialog);
        });

        binding.buttonGuardar.setOnClickListener(v -> {
            clienteCantinaViewModel.crud = true;
            clienteCantinaViewModel.actualizarCliente(idcliente, binding.txtNIF, binding.editTextNome, binding.editTextNumeroTelefone, binding.editTextEmail, binding.editTextEndereco, dialog);
        });
        binding.buttonEliminarCliente.setOnClickListener(v -> deleteClient(idcliente, Objects.requireNonNull(binding.editTextNome.getText()).toString()));

        binding.buttonCancelar.setOnClickListener(v -> dialog.dismiss());

        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private void deleteClient(long idcliente, String nome) {
        clienteCantinaViewModel.crud = true;
        clienteCantina.setId(idcliente);
        clienteCantina.setEstado(Ultilitario.TRES);
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.eliminar) + " (" + nome + ")")
                .setMessage(getString(R.string.tem_cert_elim_cli))
                .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.ok), (dialog1, which) -> clienteCantinaViewModel.eliminarCliente(clienteCantina, dialog))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

}
