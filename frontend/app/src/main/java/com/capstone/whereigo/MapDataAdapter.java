package com.capstone.whereigo;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.whereigo.databinding.ItemMapDataBinding;

import java.util.List;

public class MapDataAdapter extends RecyclerView.Adapter<MapDataAdapter.ViewHolder> {

    private final Context context;
    private final List<MapData> mapList; // MutableList → List로, Java에서 remove 가능

    public MapDataAdapter(Context context, List<MapData> mapList) {
        this.context = context;
        this.mapList = mapList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemMapDataBinding binding;

        public ViewHolder(ItemMapDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public MapDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMapDataBinding binding = ItemMapDataBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MapDataAdapter.ViewHolder holder, int position) {
        MapData mapData = mapList.get(position);

        holder.binding.tvFileName.setText(mapData.getFileName());
        holder.binding.tvFileSize.setText(mapData.getFileSize());
        holder.binding.tvDate.setText(mapData.getSaveDate());

        holder.binding.btnFavorite.setImageResource(
                mapData.isFavorite() ? R.drawable.ic_favorite_selected : R.drawable.ic_favorite
        );

        // 삭제 버튼 클릭 이벤트
        holder.binding.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                .setTitle("삭제 확인")
                .setMessage(mapData.getFileName() + "을(를) 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    mapList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mapList.size());
                    Toast.makeText(context, mapData.getFileName() + "이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
        });

        // 즐겨찾기 버튼 클릭 이벤트
        holder.binding.btnFavorite.setOnClickListener(v -> {
            mapData.setFavorite(!mapData.isFavorite());

            holder.binding.btnFavorite.setImageResource(
                    mapData.isFavorite() ? R.drawable.ic_favorite_selected : R.drawable.ic_favorite
            );

            String message = mapData.isFavorite()
                    ? mapData.getFileName() + "이 즐겨찾기에 추가되었습니다."
                    : mapData.getFileName() + "이 즐겨찾기에서 제거되었습니다.";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }
}
