package com.example.timeflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {
    private List<Integer> colorList;
    private int selectedColor = -1;
    private int selectedPosition = -1;

    public ColorAdapter(List<Integer> colorList) {
        this.colorList = colorList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int color = colorList.get(position);
        holder.ivColor.setBackgroundColor(color);

        if (position == selectedPosition) {
            holder.ivCheck.setVisibility(View.VISIBLE);
        } else {
            holder.ivCheck.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedColor = color;
            selectedPosition = position;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivColor;
        ImageView ivCheck;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivColor = itemView.findViewById(R.id.ivColor);
            ivCheck = itemView.findViewById(R.id.ivCheck);
        }
    }
}