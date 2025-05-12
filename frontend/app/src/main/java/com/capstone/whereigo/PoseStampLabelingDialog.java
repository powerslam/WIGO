package com.capstone.whereigo;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.capstone.whereigo.databinding.FragmentNodeLabelingBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class PoseStampLabelingDialog extends DialogFragment implements DialogInterface.OnDismissListener {
    private FragmentNodeLabelingBinding binding;
    private PoseStampViewPagerAdapter adapter;
    private PoseStampViewModel viewModel;

    @FunctionalInterface
    public interface OnDismissListener {
        void OnDismiss(String map_name);
    }

    public OnDismissListener onDismissListener;

    public static PoseStampLabelingDialog newInstance(PoseStampViewModel viewModel) {
        PoseStampLabelingDialog fragment = new PoseStampLabelingDialog();
        fragment.viewModel = viewModel;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentNodeLabelingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 viewPager = binding.dialogViewPager;

        adapter = new PoseStampViewPagerAdapter(requireActivity(), viewModel);
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(new MarginPageTransformer(30));
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2, false); // 무한 슬라이딩처럼

        Button btn = binding.buttonLabelingDone;
        btn.setOnClickListener(v -> {
            int backStackCount = requireActivity().getSupportFragmentManager().getBackStackEntryCount();
            Toast.makeText(requireContext(), "저장이완료됐어용가리치킨더조이 : " + backStackCount, Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HelloArFragment())
                    .commit();
            dismiss();
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.OnDismiss(adapter.getMapName());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
