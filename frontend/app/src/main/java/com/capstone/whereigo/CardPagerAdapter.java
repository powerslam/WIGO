package com.capstone.whereigo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.capstone.whereigo.databinding.CardNodeLabelingBinding;

import java.util.List;

public class CardPagerAdapter extends FragmentStateAdapter {

    private List<NodeLabelData> nodeList;

    public CardPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<NodeLabelData> nodeList) {
        super(fragmentActivity);
        this.nodeList = nodeList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        NodeLabelData nodeLabelData = nodeList.get(position % nodeList.size());
        CardFragment ret = CardFragment.newInstance(nodeLabelData);
        ret.cardInputListener = (String newText) -> {
            NodeLabelData newNodeLabelData = new NodeLabelData(newText, nodeLabelData.getPosition(), nodeLabelData.getImageResId());
            nodeList.set(position % nodeList.size(), newNodeLabelData);
        };

        return ret;
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE; // 사실상 무한
    }
}
