package com.example.timeflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.room.entity.Habit;
import com.example.timeflow.viewmodel.HabitViewModel;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {

    private List<Habit> habitList;
    private OnHabitCheckedListener listener;
    private HabitViewModel habitViewModel;
    public interface OnHabitCheckedListener {
        void onHabitChecked(Habit habit, int position, boolean isChecked);
    }

    public HabitAdapter(List<Habit> habitList, OnHabitCheckedListener listener) {
        this.habitList = habitList;
        this.listener = listener;
    }
    public HabitAdapter(List<Habit> habitList, OnHabitCheckedListener listener, HabitViewModel habitViewModel) {
        this.habitList = habitList;
        this.listener = listener;
        this.habitViewModel = habitViewModel; // 添加这一行
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

    // 在HabitAdapter的onBindViewHolder中更新完成逻辑
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habitList.get(position);

        // 显示习惯名称
        holder.tvHabitName.setText(habit.getName());

        // 设置复选框状态
        holder.cbCompleted.setChecked(false);
        holder.cbCompleted.setEnabled(habit.isTodayInFrequency());

        // 如果今天不在频率内，显示灰色并禁用

        if (!habit.isTodayInFrequency()) {
            holder.cbCompleted.setAlpha(0.5f);
            holder.tvHabitName.setAlpha(0.7f);
        } else {
            holder.cbCompleted.setAlpha(1f);
            holder.tvHabitName.setAlpha(1f);

            // 获取今天的记录
            habitViewModel.getOrCreateTodayRecord(habit.getId());
            habitViewModel.getCurrentRecord().observe((LifecycleOwner) holder.itemView.getContext(), record -> {
                if (record != null && record.getHabitId() == habit.getId()) {
                    holder.cbCompleted.setChecked(record.getCompletedCount() > 0);
                }
            });
        }

        // 复选框监听器
        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                habitViewModel.getOrCreateTodayRecord(habit.getId());
                habitViewModel.getCurrentRecord().observe((LifecycleOwner) holder.itemView.getContext(), record -> {
                    if (record != null) {
                        record.setCompletedCount(record.getCompletedCount() + 1);
                        habitViewModel.updateRecord(record);
                    }
                });
            }
        });

        // 显示统计信息
        habitViewModel.getTotalCompletedDays(habit.getId()).observe((LifecycleOwner) holder.itemView.getContext(), days -> {
            habitViewModel.getTotalCompletedCount(habit.getId()).observe((LifecycleOwner) holder.itemView.getContext(), count -> {
                holder.tvProgress.setText(String.format("已完成 %d 天，共 %d 次", days, count));
            });
        });
    }

    private void setProgressBarColor(ProgressBar progressBar, float progress) {
        int color;
        if (progress >= 80) {
            color = progressBar.getContext().getResources().getColor(R.color.green);
        } else if (progress >= 50) {
            color = progressBar.getContext().getResources().getColor(R.color.yellow);
        } else {
            color = progressBar.getContext().getResources().getColor(R.color.red);
        }
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public void clear() {
        if (habitList != null) {
            habitList.clear();
            notifyDataSetChanged();
        }
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