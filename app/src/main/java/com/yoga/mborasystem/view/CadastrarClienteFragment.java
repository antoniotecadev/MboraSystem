package com.yoga.mborasystem.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentCadastrarClienteBinding;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

public class CadastrarClienteFragment extends Fragment {

    private ClienteViewModel clienteViewModel;
    private FragmentCadastrarClienteBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return criarCliente(inflater, container);
    }

    private void spinnerProvincias() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.provincias, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerProvincias.setAdapter(adapter);
    }

    private View criarCliente(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentCadastrarClienteBinding.inflate(inflater, container, false);
        spinnerProvincias();
        binding.buttonCriarConta.setOnClickListener(v -> {
            try {
                clienteViewModel.validarCliente(Ultilitario.Operacao.CRIAR, binding.editTextNome, binding.editTextSobreNome, binding.editTextNif, binding.editTextNumeroTelefone, binding.editTextNumeroTelefoneAlternativo, binding.editTextEmail, binding.editTextNomeLoja, binding.spinnerProvincias, binding.editTextMunicipio, binding.editTextBairro, binding.editTextRua, binding.editTextSenha, binding.editTextSenhaNovamente);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), getText(R.string.erro) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), getText(R.string.erro) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        binding.checkTermoCondicao.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.buttonCriarConta.setEnabled(true);
            } else {
                binding.buttonCriarConta.setEnabled(false);
            }
        });

        binding.buttonEntrarConta.setOnClickListener(v -> Navigation.findNavController(getView()).navigate(R.id.action_cadastrarClienteFragment_to_activarMbora));

        Ultilitario.getValido().observe(getViewLifecycleOwner(), operacao ->  {
                switch (operacao) {
                    case CRIAR:
                        Ultilitario.dialogConta(getString(R.string.conta_criada), getContext());
                        Navigation.findNavController(getView()).navigate(R.id.action_cadastrarClienteFragment_to_activarMbora);
                        break;
                    case NENHUMA:
                        Ultilitario.dialogConta(getString(R.string.conta_nao_criada), getContext());
                        break;
                    default:
                        break;
                }
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(), getContext()));
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
