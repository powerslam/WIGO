package com.capstone.whereigo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;
import java.util.regex.Pattern;

public class PoseStampViewPagerAdapter extends FragmentStateAdapter {
    private final PoseStampViewModel viewModel;
    private String mapName;

    public PoseStampViewPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                     PoseStampViewModel viewModel) {
        super(fragmentActivity);
        this.viewModel = viewModel;
        this.mapName = "새로운 지도";
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int size = viewModel.getPoseStampListSize() + 1;

        PoseStamp poseStamp = position == size ? null : viewModel.getPoseStampAt(position % size);
        String label = position == size ? this.mapName : viewModel.getLabelAt(position % size);

        PoseStampCardFragment ret = PoseStampCardFragment.newInstance(poseStamp, label);
        ret.cardInputListener = newText -> {
            if(position == size)
                this.mapName = newText;

            else
                viewModel.updateLabel(position, newText);
        };

        return ret;
    }

    public String getMapName(){
        return this.mapName;
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }
}
