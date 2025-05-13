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
import com.capstone.whereigo.databinding.FragmentMappingTargetInfoBinding;

public class MappingTargetInfoFragment extends Fragment {
    private FragmentMappingTargetInfoBinding binding;
    private PoseStampViewModel viewModel;
    private Button btnNext;
    private Spinner spinnerFloor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMappingTargetInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(PoseStampViewModel.class);

        btnNext = binding.buttonNext;
        btnNext.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new MappingFragment())
                .commit());

        spinnerFloor = binding.spinnerFloor;

        int minIdx = viewModel.getFloorMinIdx() + 1;
        int maxIdx = viewModel.getFloorMaxIdx();

        String[] floorItem = new String[minIdx + maxIdx + 1];

        int idx = 0;
        for(int i = maxIdx; i >= 0; i--){
            floorItem[idx] = viewModel.getFloorMaxItem()[i];
            idx += 1;
        }

        for(int i = 0; i < minIdx; i++){
            floorItem[idx] = viewModel.getFloorMinItem()[i];
            idx += 1;
        }

        ArrayAdapter<String> floorItemAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                floorItem
        );
        floorItemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFloor.setAdapter(floorItemAdapter);
        spinnerFloor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.updateFloorName(parent.getItemAtPosition(position).toString());
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
