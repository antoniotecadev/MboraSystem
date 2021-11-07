package com.yoga.mborasystem.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yoga.mborasystem.databinding.FragmentActivarMborasytemBinding;
import com.yoga.mborasystem.util.MaskEditUtil;
import com.yoga.mborasystem.util.Ultilitario;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class ActivarMboraSystem extends Fragment {

    FragmentActivarMborasytemBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentActivarMborasytemBinding.inflate(inflater, container, false);

        binding.editTextChaveApp.addTextChangedListener(MaskEditUtil.mask(binding.editTextChaveApp, MaskEditUtil.FORMAT_KEY));
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(),getContext()));
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}