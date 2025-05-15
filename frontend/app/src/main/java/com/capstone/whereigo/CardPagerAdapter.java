package com.capstone.whereigo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

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
        // nodeList에서 position에 해당하는 데이터를 가져와 CardFragment로 전달
        NodeLabelData nodeLabelData = nodeList.get(position % nodeList.size()); // 무한 슬라이딩을 위해 % 사용
        return CardFragment.newInstance(nodeLabelData);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE; // 사실상 무한
    }
}
