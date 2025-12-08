package com.example.timeflow.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.adapter.HabitAdapter;
import com.example.timeflow.entity.Habit;
import com.example.timeflow.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.List;

public class HabitFragment extends Fragment {

    private RecyclerView recyclerView;
    private HabitAdapter adapter;
    private List<Habit> habitList;
    private MaterialButtonToggleGroup timeRangeToggle;
    private Button btnAddHabit;
    private LinearLayout chartContainer;
    private SharedPreferencesManager prefsManager;
    private boolean isWeekMode = true;

    public HabitFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habit, container, false);

        initViews(view);
        loadData();
        setupRecyclerView();
        setupSimpleChart();
        setClickListeners();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewHabits);
        timeRangeToggle = view.findViewById(R.id.timeRangeToggle);
        btnAddHabit = view.findViewById(R.id.btnAddHabit);
        chartContainer = view.findViewById(R.id.chartContainer);
        prefsManager = new SharedPreferencesManager(getContext());
    }

    private void loadData() {
        habitList = prefsManager.getHabits();
        if (habitList.isEmpty()) {
            // 添加示例数据
            habitList.add(new Habit("背60个英语单词", 7, 5));
            habitList.add(new Habit("学习30分钟编程", 7, 6));
            habitList.add(new Habit("阅读30分钟", 7, 3));
            prefsManager.saveHabits(habitList);
        }
    }

    private void setupRecyclerView() {
        adapter = new HabitAdapter(habitList, (habit, position, isChecked) -> {
            // 打卡逻辑
            habit.setCompletedToday(isChecked);
            adapter.notifyItemChanged(position);
            setupSimpleChart();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSimpleChart() {
        chartContainer.removeAllViews();

        for (Habit habit : habitList) {
            // 创建图表项
            View chartItem = LayoutInflater.from(getContext()).inflate(R.layout.item_habit_chart, null);

            TextView tvHabitName = chartItem.findViewById(R.id.tvHabitName);
            ProgressBar progressBar = chartItem.findViewById(R.id.progressBar);
            TextView tvPercentage = chartItem.findViewById(R.id.tvPercentage);

            tvHabitName.setText(habit.getName());

            // 计算完成率
            float completionRate = isWeekMode ? habit.getWeeklyCompletionRate() : habit.getMonthlyCompletionRate();
            progressBar.setProgress((int) completionRate);
            tvPercentage.setText(String.format("%.0f%%", completionRate));

            // 设置不同的颜色
            int color;
            if (completionRate >= 80) {
                color = getResources().getColor(R.color.priority_low);
            } else if (completionRate >= 50) {
                color = getResources().getColor(R.color.priority_medium);
            } else {
                color = getResources().getColor(R.color.priority_high);
            }
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(color));

            chartContainer.addView(chartItem);
        }
    }

    private void setClickListeners() {
        timeRangeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnWeek) {
                    isWeekMode = true;
                } else if (checkedId == R.id.btnMonth) {
                    isWeekMode = false;
                }
                setupSimpleChart();
            }
        });

        btnAddHabit.setOnClickListener(v -> showAddHabitDialog());
    }

    private void showAddHabitDialog() {
        // TODO: 实现添加打卡任务的对话框
        // 这里暂时显示一个简单的提示
        android.widget.Toast.makeText(getContext(), "添加打卡功能开发中", android.widget.Toast.LENGTH_SHORT).show();
    }
}