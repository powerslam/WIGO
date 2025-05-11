package com.capstone.whereigo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.capstone.whereigo.databinding.FragmentMappingBinding;
import com.google.ar.core.Pose;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MappingFragment extends Fragment {
    List<PoseStampData> poseStampDataList;
    private PoseStampDataAdapter poseStampDataAdapter;
    private FragmentMappingBinding binding;
    private ConstraintLayout main_layout;
    private boolean isScaledDown = false;
    private Button btn_start_save_pose_graph;
    private Button btn_pose_stamp;
    private int originalWidth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMappingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        androidx.recyclerview.widget.RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        poseStampDataList = new ArrayList<>();
        poseStampDataAdapter = new PoseStampDataAdapter(requireContext(), poseStampDataList);

        recyclerView.setAdapter(poseStampDataAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

        btn_start_save_pose_graph = binding.buttonSavePosegraph;
        btn_pose_stamp = binding.buttonPoseStamp;
        main_layout = binding.buttonGroup;

        btn_start_save_pose_graph.post(() -> {
            originalWidth = btn_start_save_pose_graph.getWidth();
        });
        btn_start_save_pose_graph.setOnClickListener(this::toggleScaleListener);

        btn_pose_stamp.setOnClickListener(v -> {
            poseStampDataList.add(new PoseStampData(
                    0f + 0.5f * poseStampDataList.size(),
                    0f + 0.5f * poseStampDataList.size()
            ));

            poseStampDataAdapter.notifyItemInserted(poseStampDataList.size());
            recyclerView.post(() -> {
                recyclerView.scrollToPosition(poseStampDataAdapter.getItemCount() - 1);
            });
        });
    }

    private void toggleScaleListener(View v) {
        isScaledDown = !isScaledDown;

        if(isScaledDown){
            btn_start_save_pose_graph.setText("저장하기");
        } else {
            btn_start_save_pose_graph.setText("매핑하기");
        }

        animateConstraintLayout();
        fadeBtnPoseStamp();
    }

    private void animateConstraintLayout(){
        if(isScaledDown){
            btn_pose_stamp.setAlpha(0f);
            btn_pose_stamp.setVisibility(View.VISIBLE);
        }

        else {
            btn_pose_stamp.setVisibility(View.GONE);
        }

        Transition transition = new AutoTransition();
        transition.setDuration(500);

        final ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(main_layout);

        final int endWidth = isScaledDown ? originalWidth / 2 : originalWidth;
        constraintSet.constrainWidth(btn_start_save_pose_graph.getId(), endWidth); // 원하는 너비 지정

        constraintSet.clear(btn_start_save_pose_graph.getId(), ConstraintSet.END);
        if(isScaledDown){
            constraintSet.connect(btn_start_save_pose_graph.getId(), ConstraintSet.END,
                    btn_pose_stamp.getId(), ConstraintSet.START, 0);

        }

        else {
            constraintSet.connect(btn_start_save_pose_graph.getId(), ConstraintSet.END,
                    ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
        }

        TransitionManager.beginDelayedTransition(main_layout, transition);
        constraintSet.applyTo(main_layout);
    }

    private void fadeBtnPoseStamp() {
        if (isScaledDown) {
            btn_pose_stamp.setAlpha(0f);
            btn_pose_stamp.setVisibility(View.VISIBLE);
        }

        final float startAlpha = isScaledDown ? 0f : 1f;
        final float endAlpha = isScaledDown ? 1f : 0f;

        ObjectAnimator fade = ObjectAnimator.ofFloat(btn_pose_stamp, "alpha", startAlpha, endAlpha);
        fade.setDuration(500);
        fade.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
