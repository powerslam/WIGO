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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class NodeLabelingDialog extends DialogFragment {
    private List<NodeLabelData> nodeList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_node_labeling, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewPager2 viewPager = view.findViewById(R.id.dialog_view_pager);

        nodeList = new ArrayList<>();
        nodeList.add(new NodeLabelData("노드1", "(10, 20)", R.drawable.test));
        nodeList.add(new NodeLabelData("노드2", "(30, 40)", R.drawable.test));
        nodeList.add(new NodeLabelData("노드3", "(50, 60)", R.drawable.test));

        CardPagerAdapter adapter = new CardPagerAdapter(requireActivity(), nodeList);
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(new MarginPageTransformer(30));
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2, false); // 무한 슬라이딩처럼

        super.onViewCreated(view, savedInstanceState);
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
