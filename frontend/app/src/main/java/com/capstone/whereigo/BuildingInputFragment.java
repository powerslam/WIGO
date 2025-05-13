package com.capstone.whereigo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.capstone.whereigo.databinding.FragmentBuildingInputBinding;

public class BuildingInputFragment extends Fragment {
    private FragmentBuildingInputBinding binding;
    private PoseStampViewModel viewModel;

    private Button btnNext;
    private Spinner spinnerMin, spinnerMax;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBuildingInputBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(PoseStampViewModel.class);

        btnNext = binding.buttonNext;
        btnNext.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new MappingTargetInfoFragment())
                .commit());

        spinnerMin = binding.spinnerMin;
        ArrayAdapter<String> floorMinItemAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                viewModel.getFloorMinItem()
        );
        floorMinItemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMin.setAdapter(floorMinItemAdapter);
        spinnerMin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.updateFloorMinIdx(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerMax = binding.spinnerMax;
        ArrayAdapter<String> floorMaxItemAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                viewModel.getFloorMaxItem()
        );
        floorMaxItemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMax.setAdapter(floorMaxItemAdapter);
        spinnerMax.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.updateFloorMaxIdx(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
