package com.example.timeflow.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.room.entity.HabitWithStats;
import com.example.timeflow.viewmodel.HabitViewModel;

import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {

    private final HabitViewModel habitViewModel;
    private final List<HabitWithStats> currentList = new ArrayList<>();

    public HabitAdapter(HabitViewModel habitViewModel) {
        this.habitViewModel = habitViewModel;
        setHasStableIds(true);
    }

    public void submitList(List<HabitWithStats> list) {
        final List<HabitWithStats> newList =
                list == null ? new ArrayList<>() : new ArrayList<>(list);

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return currentList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return currentList.get(oldPos).habit.getId()
                        .equals(newList.get(newPos).habit.getId());
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                HabitWithStats oldItem = currentList.get(oldPos);
                HabitWithStats newItem = newList.get(newPos);
                return oldItem.isCompletedToday == newItem.isCompletedToday
                        && oldItem.totalDays == newItem.totalDays;
            }
        });

        currentList.clear();
        currentList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }


    private List<HabitWithStats> getUncompletedHabits() {
        List<HabitWithStats> list = new ArrayList<>();
        for (HabitWithStats item : currentList) {
            if (!item.isCompletedToday && item.habit.isTodayInFrequency()) {
                list.add(item);
            }
        }
        return list;
    }

    private List<HabitWithStats> getCompletedHabits() {
        List<HabitWithStats> list = new ArrayList<>();
        for (HabitWithStats item : currentList) {
            if (item.isCompletedToday) {
                list.add(item);
            }
        }
        return list;
    }

    @Override
    public int getItemCount() {
        return currentList.size();
    }

    @Override
    public long getItemId(int position) {
        return currentList.get(position).habit.getId();
    }

    @Override
    public int getItemViewType(int position) {
        HabitWithStats item = currentList.get(position);
        return item.isCompletedToday ? 1 : 0; // 0: 未完成, 1: 已完成
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HabitWithStats item = currentList.get(position);
        holder.bind(item, () -> {
            // 动画结束后重新排序
            submitList(currentList); // 触发 DiffUtil 重新排列
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
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

        public void bind(HabitWithStats item, Runnable onAnimationEnd) {
            tvHabitName.setText(item.habit.getName());
            tvProgress.setText(String.format("已坚持 %d 天", item.totalDays));

            // 清除旧监听器
            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(item.isCompletedToday);
            cbCompleted.setEnabled(!item.isCompletedToday);

            if (item.isCompletedToday) {
                tvHabitName.setPaintFlags(tvHabitName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvHabitName.setPaintFlags(tvHabitName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }

            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !item.isCompletedToday) {
                    // 执行打卡
                    habitViewModel.checkIn(item.habit.getId());

                    // 播放动画
                    ScaleAnimation scale = new ScaleAnimation(1f, 0.9f, 1f, 0.9f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    scale.setDuration(200);

                    TranslateAnimation translate = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, 1.5f);
                    translate.setDuration(400);
                    translate.setStartOffset(200);

                    AnimationSet set = new AnimationSet(true);
                    set.addAnimation(scale);
                    set.addAnimation(translate);
                    set.setAnimationListener(new Animation.AnimationListener() {
                        @Override public void onAnimationStart(Animation animation) {}
                        @Override public void onAnimationRepeat(Animation animation) {}
                        @Override public void onAnimationEnd(Animation animation) {
                            onAnimationEnd.run();
                        }
                    });

                    itemView.startAnimation(set);
                }
            });
        }
    }
}