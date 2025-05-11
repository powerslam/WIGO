package com.capstone.whereigo;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.capstone.whereigo.databinding.FragmentMappingBinding;

import java.util.ArrayList;
import java.util.List;

public class MappingFragment extends Fragment {
    List<PoseStampData> poseStampDataList;
    ArrayList<Integer> indexList;
    private PoseStampDataAdapter poseStampDataAdapter;
    private FragmentMappingBinding binding;
    private ConstraintLayout main_layout;
    private boolean isScaledDown = false;
    private TextView tv_number_of_recorded_node;
    private Button btn_start_save_pose_graph;
    private Button btn_pose_stamp;
    private int originalWidth;
    private NativeHolderViewModel viewModel;

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

        viewModel = new ViewModelProvider(requireActivity()).get(NativeHolderViewModel.class);

        registerNativeSelf(viewModel.getNativePtr());

        tv_number_of_recorded_node = binding.numberOfRecordedNode;

        poseStampDataList = new ArrayList<>();
        poseStampDataAdapter = new PoseStampDataAdapter(requireContext(), poseStampDataList);

        indexList = new ArrayList<>();

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
            JniInterface.getPoseStamp(viewModel.getNativePtr());
            float x = JniInterface.getX();
            float z = JniInterface.getZ();

            poseStampDataList.add(new PoseStampData(
                    x, z
            ));

            indexList.add(poseStampDataList.size());

            poseStampDataAdapter.notifyItemInserted(poseStampDataList.size());
            recyclerView.post(() -> {
                recyclerView.scrollToPosition(poseStampDataAdapter.getItemCount() - 1);
            });
        });
    }

    public native void registerNativeSelf(long nativeApplicationPtr);

    private void toggleScaleListener(View v) {
        isScaledDown = !isScaledDown;

        if(isScaledDown){
            btn_start_save_pose_graph.setText("저장하기");
        } else {
            btn_start_save_pose_graph.setText("매핑하기");
        }

        animateConstraintLayout();
        fadeBtnPoseStamp();

        if(isScaledDown){
            JniInterface.changeStatus(viewModel.getNativePtr());
        }

        else if(!isScaledDown && !indexList.isEmpty()){
            NodeLabelingDialog dialog = NodeLabelingDialog.newInstance(indexList);
            dialog.show(requireActivity().getSupportFragmentManager(), "nodeLabelDialog");
        }
    }

    public void updateKeyFrameListSize(int size){
        tv_number_of_recorded_node.post(() -> {
            tv_number_of_recorded_node.setText("지금까지 기록된 노드 수 : " + size);
        });
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
