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

import java.io.File;
import java.util.List;

public class MapDataAdapter extends RecyclerView.Adapter<MapDataAdapter.ViewHolder> {

    private final Context context;
    private final List<MapData> mapList;

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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMapDataBinding binding = ItemMapDataBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    private boolean deleteFileOrFolder(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFileOrFolder(child);
                }
            }
        }
        return file.delete();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MapData mapData = mapList.get(position);

        holder.binding.tvFileName.setText(mapData.getFileName());
        holder.binding.tvFileSize.setText(mapData.getFileSize());
        holder.binding.tvDate.setText(mapData.getSaveDate());

        holder.binding.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("삭제 확인")
                    .setMessage(mapData.getFileName() + "을(를) 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        // FileDownloader와 동일한 구조에 맞춤:
                        File file = new File(context.getExternalFilesDir(null), "Maps/" + mapData.getFileName());
                        boolean deleted = false;

                        Log.d("DeleteCheck", "삭제 대상 경로: " + file.getAbsolutePath());

                        if (file.exists()) {
                            deleted = deleteFileOrFolder(file);
                            Log.d("DeleteCheck", "삭제 결과: " + deleted);
                        } else {
                            Log.d("DeleteCheck", "파일이 존재하지 않음");
                        }

                        // 리스트에서 제거
                        mapList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, mapList.size());

                        if (deleted) {
                            Toast.makeText(context, mapData.getFileName() + "이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, mapData.getFileName() + " 파일 삭제 실패 (목록에서만 제거됨)", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }
}
