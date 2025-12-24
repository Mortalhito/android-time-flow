package com.example.timeflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timeflow.R;
import com.example.timeflow.room.entity.Habit;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {

    private List<Habit> habitList;
    private OnHabitCheckedListener listener;

    public interface OnHabitCheckedListener {
        void onHabitChecked(Habit habit, int position, boolean isChecked);
    }

    public HabitAdapter(List<Habit> habitList, OnHabitCheckedListener listener) {
        this.habitList = habitList;
        this.listener = listener;
    }

    public void updateData(List<Habit> newList) {
        this.habitList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }

    // 在onBindViewHolder方法中更新进度显示逻辑
    // 修复后的 onBindViewHolder 方法
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 添加空值检查
        if (habitList == null || position < 0 || position >= habitList.size()) {
            return;
        }

        Habit habit = habitList.get(position);
        if (habit == null) {
            return;
        }

        // 添加空值检查
        if (holder.tvHabitName != null) {
            holder.tvHabitName.setText(habit.getName());

            // 显示每日目标次数
            String targetText = habit.getDailyTargetCount() > 1 ?
                    String.format("(%d/%d)", habit.getTodayCompletedCount(), habit.getDailyTargetCount()) : "";
            holder.tvHabitName.setText(habit.getName() + targetText);
        }

        if (holder.cbCompleted != null) {
            holder.cbCompleted.setChecked(habit.isCompletedToday());
            holder.cbCompleted.setEnabled(habit.isTodayInFrequency());

            // 修复监听器设置，避免重复触发
            holder.cbCompleted.setOnCheckedChangeListener(null);
            holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                    listener.onHabitChecked(habit, adapterPosition, isChecked);
                }
            });
        }

        // 进度显示逻辑
        if (holder.tvProgress != null && holder.progressBar != null) {
            float totalCompletionRate = 0;
            if (habit.getTotalDays() > 0) {
                totalCompletionRate = (float) habit.getCompletedDays() / habit.getTotalDays() * 100;
            }

            holder.tvProgress.setText(String.format("%d/%d 天 (%.0f%%)",
                    habit.getCompletedDays(), habit.getTotalDays(), totalCompletionRate));
            holder.progressBar.setProgress((int) totalCompletionRate);

            // 设置进度条颜色
            try {
                int color;
                if (totalCompletionRate >= 80) {
                    color = holder.itemView.getContext().getResources().getColor(R.color.red);
                } else if (totalCompletionRate >= 50) {
                    color = holder.itemView.getContext().getResources().getColor(R.color.yellow);
                } else {
                    color = holder.itemView.getContext().getResources().getColor(R.color.green);
                }
                holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHabitName, tvProgress;
        CheckBox cbCompleted;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}