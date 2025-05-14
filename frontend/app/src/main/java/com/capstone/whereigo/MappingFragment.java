package com.capstone.whereigo;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.capstone.whereigo.databinding.FragmentMappingBinding;

import java.util.List;

public class MappingFragment extends Fragment {
    private NativeHolderViewModel nativeHolderViewModel;
    private PoseStampViewModel poseStampViewModel;
    private PoseStampRecyclerViewAdapter poseStampRecyclerViewAdapter;
    private FragmentMappingBinding binding;
    private ConstraintLayout main_layout;
    private boolean isScaledDown = false;
    private TextView tvNumberOfRecordedNode;
    private Button btnStartSavePoseGraph;
    private Button btnPoseStamp;

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

        nativeHolderViewModel = new ViewModelProvider(requireActivity()).get(NativeHolderViewModel.class);
        registerNativeSelf(nativeHolderViewModel.getNativePtr());

        poseStampRecyclerViewAdapter = new PoseStampRecyclerViewAdapter();
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setAdapter(poseStampRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

        poseStampViewModel = new ViewModelProvider(requireActivity()).get(PoseStampViewModel.class);
        poseStampViewModel.getPoseStampList().observe(getViewLifecycleOwner(), poseStampList -> {
            if(!poseStampList.isEmpty()){
                PoseStamp last = poseStampList.get(poseStampList.size() - 1);
                poseStampRecyclerViewAdapter.addPoseStamp(last);
                recyclerView.post(() -> {
                    recyclerView.scrollToPosition(poseStampRecyclerViewAdapter.getItemCount() - 1);
                });
            }
        });

        tvNumberOfRecordedNode = binding.numberOfRecordedNode;

        main_layout = binding.buttonGroup;

        btnStartSavePoseGraph = binding.buttonSavePosegraph;
        btnStartSavePoseGraph.setOnClickListener(this::toggleScaleListener);

        btnPoseStamp = binding.buttonPoseStamp;
        btnPoseStamp.setOnClickListener(v -> {
            JniInterface.getPoseStamp(nativeHolderViewModel.getNativePtr());
            float x = JniInterface.getX();
            float z = JniInterface.getZ();

            PoseStamp newPoseStamp = new PoseStamp(x, z, R.drawable.test);
            poseStampViewModel.addPoseStampData(newPoseStamp);
        });
    }

    public native void registerNativeSelf(long nativeApplicationPtr);

    private void toggleScaleListener(View v) {
        if(!isScaledDown){
            isScaledDown = true;

            btnStartSavePoseGraph.setText("저장하기");
            animateConstraintLayout();
            fadeBtnPoseStamp();

            JniInterface.changeStatus(nativeHolderViewModel.getNativePtr());
        }

        else if(/* isScaledDown && */ poseStampViewModel.getPoseStampListSize() > 0){
            isScaledDown = false;

            btnStartSavePoseGraph.setText("지도 작성 하기");
            animateConstraintLayout();
            fadeBtnPoseStamp();

            JniInterface.changeStatus(nativeHolderViewModel.getNativePtr());

            PoseStampLabelingDialog dialog = PoseStampLabelingDialog.newInstance(poseStampViewModel);
            dialog.onDismissListener = (String mapName) -> {
                List<String> data = poseStampViewModel.getPoseStampLabelList().getValue();
                data.add(mapName);

                JniInterface.savePoseGraph(
                        nativeHolderViewModel.getNativePtr(),
                        data.toArray(new String[0]));
            };
            
            dialog.show(requireActivity().getSupportFragmentManager(), "nodeLabelDialog");
        }

        else /* isScaledDown && !indexList.isEmpty() */ {
            Toast.makeText(requireContext(), "Stamp가 찍히지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateKeyFrameListSize(int size){
        tvNumberOfRecordedNode.post(() -> {
            tvNumberOfRecordedNode.setText("지금까지 기록된 노드 수 : " + size);
        });
    }

    private void animateConstraintLayout(){
        if(isScaledDown){
            btnPoseStamp.setAlpha(0f);
            btnPoseStamp.setVisibility(View.VISIBLE);
        }

        else {
            btnPoseStamp.setVisibility(View.GONE);
        }

        Transition transition = new AutoTransition();
        transition.setDuration(500);

        final ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(main_layout);

        constraintSet.clear(btnStartSavePoseGraph.getId(), ConstraintSet.END);
        if(isScaledDown){
            constraintSet.connect(btnStartSavePoseGraph.getId(), ConstraintSet.END,
                    binding.buttonGroupGuideLineLeft.getId(), ConstraintSet.END, 0);

        }

        else {
            constraintSet.connect(btnStartSavePoseGraph.getId(), ConstraintSet.END,
                    ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
        }

        TransitionManager.beginDelayedTransition(main_layout, transition);
        constraintSet.applyTo(main_layout);
    }

    private void fadeBtnPoseStamp() {
        if (isScaledDown) {
            btnPoseStamp.setAlpha(0f);
            btnPoseStamp.setVisibility(View.VISIBLE);
        }

        final float startAlpha = isScaledDown ? 0f : 1f;
        final float endAlpha = isScaledDown ? 1f : 0f;

        ObjectAnimator fade = ObjectAnimator.ofFloat(btnPoseStamp, "alpha", startAlpha, endAlpha);
        fade.setDuration(500);
        fade.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
