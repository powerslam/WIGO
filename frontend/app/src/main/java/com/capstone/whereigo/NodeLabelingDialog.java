package com.capstone.whereigo;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.capstone.whereigo.databinding.CardNodeLabelingBinding;
import com.capstone.whereigo.databinding.FragmentNodeLabelingBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NodeLabelingDialog extends DialogFragment {
    private static final String ARG_NODE_IDX_LIST = "arg_node_idx_list";
    private FragmentNodeLabelingBinding binding;

    public static NodeLabelingDialog newInstance(ArrayList<Integer> data) {
        NodeLabelingDialog fragment = new NodeLabelingDialog();
        Bundle args = new Bundle();
        args.putIntegerArrayList(ARG_NODE_IDX_LIST, data);
        fragment.setArguments(args);
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

        assert getArguments() != null;
        ArrayList<Integer> data = getArguments().getIntegerArrayList(ARG_NODE_IDX_LIST);
        assert data != null;

        List<NodeLabelData> nodeList = new ArrayList<>();
        for(Integer index: data){
            nodeList.add(new NodeLabelData(
                    String.format(Locale.ROOT, "노드 %d", index),
                    "(10, 20)",
                    R.drawable.test)
            );
        }

        CardPagerAdapter adapter = new CardPagerAdapter(requireActivity(), nodeList);
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(new MarginPageTransformer(30));
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2, false); // 무한 슬라이딩처럼

        Button btn = binding.buttonSaveStamp;
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
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
