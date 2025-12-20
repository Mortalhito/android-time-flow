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
import com.example.timeflow.entity.Habit;
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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habitList.get(position);

        holder.tvHabitName.setText(habit.getName());
        holder.cbCompleted.setChecked(habit.isCompletedToday());

        // 计算总完成率
        float totalCompletionRate = 0;
        if (habit.getTotalDays() > 0) {
            totalCompletionRate = (float) habit.getCompletedDays() / habit.getTotalDays() * 100;
        }

        holder.tvProgress.setText(String.format("%d/%d 天 (%.0f%%)",
                habit.getCompletedDays(), habit.getTotalDays(), totalCompletionRate));
        holder.progressBar.setProgress((int) totalCompletionRate);

        // 设置进度条颜色
        int color;
        if (totalCompletionRate >= 80) {
            color = holder.itemView.getContext().getResources().getColor(R.color.red);
        } else if (totalCompletionRate >= 50) {
            color = holder.itemView.getContext().getResources().getColor(R.color.yellow);
        } else {
            color = holder.itemView.getContext().getResources().getColor(R.color.green);
        }
        holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(color));

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onHabitChecked(habit, holder.getAdapterPosition(), isChecked);
            }
        });
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