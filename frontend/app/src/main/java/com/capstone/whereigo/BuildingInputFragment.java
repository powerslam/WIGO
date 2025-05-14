package com.capstone.whereigo;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.capstone.whereigo.databinding.FragmentBuildingInputBinding;

public class BuildingInputFragment extends Fragment {
    private FragmentBuildingInputBinding binding;
    private PoseStampViewModel viewModel;

    private ProgressBar progressBar;
    private Button btnPrev, btnNext;
    private boolean isScaledDown = false;

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

        progressBar = binding.progressBar;

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if(!isScaledDown)
                            return; // 설정화면으로 돌아가기

                        prevEvent();
                    }
                }
        );

        btnPrev = binding.buttonPrev;
        btnPrev.setOnClickListener(v -> {
            prevEvent();
        });

        btnNext = binding.buttonNext;
        btnNext.setOnClickListener(v -> {
            if(!isScaledDown){
                isScaledDown = true;

                binding.inputGroup1.setVisibility(View.GONE);
                binding.inputGroup2.setVisibility(View.VISIBLE);

                btnNext.setText("지도 작성 하기");
                binding.tvBuildingInput.setText("현재 지도를 작성하고자 하는 층을 입력해주세요.");

                animateConstraintLayout();
                fadeBtnPoseStamp();
            }

            else {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MappingFragment())
                        .commit();
            }
        });

        ArrayAdapter<String> floorMinItemAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                viewModel.getFloorMinItem()
        );
        floorMinItemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMin.setAdapter(floorMinItemAdapter);
        binding.spinnerMin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.updateFloorMinIdx(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter<String> floorMaxItemAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                viewModel.getFloorMaxItem()
        );
        floorMaxItemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMax.setAdapter(floorMaxItemAdapter);
        binding.spinnerMax.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.updateFloorMaxIdx(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void prevEvent() {
        if(!isScaledDown) return;
        isScaledDown = false;

        binding.inputGroup1.setVisibility(View.VISIBLE);
        binding.inputGroup2.setVisibility(View.GONE);

        btnNext.setText("다음");
        binding.tvBuildingInput.setText("건물 정보를 입력하세요.");

        animateConstraintLayout();
        fadeBtnPoseStamp();
    }

    private void animateConstraintLayout(){
        if(isScaledDown){

            btnPrev.setAlpha(0f);
            btnNext.setVisibility(View.VISIBLE);
        }

        else {
            btnPrev.setVisibility(View.GONE);
        }

        Transition transition = new AutoTransition();
        transition.setDuration(500);

        final ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(binding.buildingInputLayout);

        constraintSet.clear(btnNext.getId(), ConstraintSet.START);
        if(isScaledDown){
            constraintSet.connect(btnNext.getId(), ConstraintSet.START,
                    btnPrev.getId(), ConstraintSet.END, 0);
        }

        else {
            constraintSet.connect(btnNext.getId(), ConstraintSet.START,
                    binding.guideFormStart.getId(), ConstraintSet.START, 0);
        }

        TransitionManager.beginDelayedTransition(binding.buildingInputLayout, transition);
        constraintSet.applyTo(binding.buildingInputLayout);
    }

    private void fadeBtnPoseStamp() {
        if (isScaledDown) {
            btnPrev.setAlpha(0f);
            btnPrev.setVisibility(View.VISIBLE);
        }

        final float startAlpha = isScaledDown ? 0f : 1f;
        final float endAlpha = isScaledDown ? 1f : 0f;

        ObjectAnimator fade = ObjectAnimator.ofFloat(btnPrev, "alpha", startAlpha, endAlpha);
        fade.setDuration(500);

        final int startProgress = isScaledDown ? 50 : 100;
        final int endProgress = isScaledDown ? 100 : 50;

        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", startProgress, endProgress);
        animator.setDuration(1000); // 1초 동안 애니메이션

        fade.start();
        animator.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
