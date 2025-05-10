package com.capstone.whereigo;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.whereigo.databinding.ItemMapDataBinding;
import com.capstone.whereigo.databinding.ItemPoseStampDataBinding;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class PoseStampDataAdapter extends RecyclerView.Adapter<PoseStampDataAdapter.ViewHolder> {

    private final Context context;
    private final List<PoseStampData> poseStampList;

    public PoseStampDataAdapter(Context context, List<PoseStampData> poseStampList) {
        this.context = context;
        this.poseStampList = poseStampList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemPoseStampDataBinding binding;

        public ViewHolder(ItemPoseStampDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPoseStampDataBinding binding = ItemPoseStampDataBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PoseStampData poseStampData = poseStampList.get(position);

        holder.binding.tvPoseStamp.setText(
                String.format(Locale.ROOT, "x : %f y : %f label : %s", poseStampData.x(), poseStampData.y(), poseStampData.label())
        );

        // @TODO : 삭제 버튼 추가
    }

    @Override
    public int getItemCount() {
        return poseStampList.size();
    }
}
