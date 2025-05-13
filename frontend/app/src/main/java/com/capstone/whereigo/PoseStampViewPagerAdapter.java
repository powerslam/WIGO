package com.capstone.whereigo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;
import java.util.regex.Pattern;

public class PoseStampViewPagerAdapter extends FragmentStateAdapter {
    private final PoseStampViewModel viewModel;
    private final int viewPagerSize;

    public PoseStampViewPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                     PoseStampViewModel viewModel) {
        super(fragmentActivity);
        this.viewModel = viewModel;
        this.viewPagerSize = viewModel.getPoseStampListSize();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        final int realPosition = position % viewPagerSize;

        PoseStamp poseStamp = viewModel.getPoseStampAt(realPosition);
        String label = viewModel.getLabelAt(realPosition);

        PoseStampCardFragment ret = PoseStampCardFragment.newInstance(poseStamp, label);
        ret.cardInputListener = newText -> {
            viewModel.updateLabel(realPosition, newText);
        };

        return ret;
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }
}
