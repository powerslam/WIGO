package com.capstone.whereigo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.whereigo.R;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private final List<String> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String selectedItem);
    }

    public SearchResultAdapter(List<String> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.itemText);
        }
    }

    @NonNull
    @Override
    public SearchResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultAdapter.ViewHolder holder, int position) {
        String item = items.get(position);
        holder.textView.setText(item);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
