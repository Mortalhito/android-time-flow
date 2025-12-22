package com.example.timeflow.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.adapter.HabitAdapter;
import com.example.timeflow.entity.Habit;
import com.example.timeflow.viewmodel.HabitViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HabitFragment extends Fragment {

    private RecyclerView recyclerView;
    private HabitAdapter adapter;
    private List<Habit> habitList;
    private MaterialButtonToggleGroup timeRangeToggle;
    private Button btnAddHabit;
    private LinearLayout chartContainer;
    private HabitViewModel habitViewModel;
    private boolean isWeekMode = true;

    public HabitFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habit, container, false);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setClickListeners();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewHabits);
        timeRangeToggle = view.findViewById(R.id.timeRangeToggle);
        btnAddHabit = view.findViewById(R.id.btnAddHabit);
        chartContainer = view.findViewById(R.id.chartContainer);
    }


    private void addSampleData() {
        Habit habit1 = new Habit("背60个英语单词", 7, 5);
        habit1.setFrequency(List.of(1, 2, 3, 4, 5, 6, 7)); // 每天
        habitViewModel.insert(habit1);

        Habit habit2 = new Habit("学习30分钟编程", 7, 6);
        habit2.setFrequency(List.of(2, 3, 4, 5, 6)); // 周一到周五
        habitViewModel.insert(habit2);

        Habit habit3 = new Habit("阅读30分钟", 7, 3);
        habit3.setFrequency(List.of(1, 7)); // 周末
        habitViewModel.insert(habit3);
    }

    private void setupRecyclerView() {
        // 确保 habitList 不为 null
        if (habitList == null) {
            habitList = new ArrayList<>();
        }

        adapter = new HabitAdapter(habitList, (habit, position, isChecked) -> {
            if (habit != null) {
                if (isChecked) {
                    habit.incrementTodayCount();
                } else {
                    habit.setCompletedToday(false);
                    habit.setTodayCompletedCount(Math.max(0, habit.getTodayCompletedCount() - 1));
                }
                habitViewModel.update(habit);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    // 修复 LiveData 观察
    private void setupViewModel() {
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
        habitViewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            if (habits != null) {
                habitList = new ArrayList<>(habits); // 创建新列表避免并发问题
                if (adapter != null) {
                    adapter.updateData(habitList);
                }
                setupSimpleChart();
            }

            // 添加空列表检查
            if (habitList == null || habitList.isEmpty()) {
                addSampleData();
            }
        });
    }

    private void setupSimpleChart() {
        chartContainer.removeAllViews();

        for (Habit habit : habitList) {
            View chartItem = LayoutInflater.from(getContext()).inflate(R.layout.item_habit_chart, null);

            TextView tvHabitName = chartItem.findViewById(R.id.tvHabitName);
            ProgressBar progressBar = chartItem.findViewById(R.id.progressBar);
            TextView tvPercentage = chartItem.findViewById(R.id.tvPercentage);

            tvHabitName.setText(habit.getName());

            float completionRate = isWeekMode ? habit.getWeeklyCompletionRate() : habit.getMonthlyCompletionRate();
            progressBar.setProgress((int) completionRate);
            tvPercentage.setText(String.format("%.0f%%", completionRate));

            int color;
            if (completionRate >= 80) {
                color = getResources().getColor(R.color.red);
            } else if (completionRate >= 50) {
                color = getResources().getColor(R.color.yellow);
            } else {
                color = getResources().getColor(R.color.green);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null);
        builder.setView(dialogView);

        // 初始化对话框中的视图
        TextInputEditText etHabitName = dialogView.findViewById(R.id.etHabitName);
        LinearLayout weekDayContainer = dialogView.findViewById(R.id.weekDayContainer);
        TextInputEditText etDailyCount = dialogView.findViewById(R.id.etDailyCount);
        Button btnStartDate = dialogView.findViewById(R.id.btnStartDate);
        Button btnEndDate = dialogView.findViewById(R.id.btnEndDate);
        CheckBox cbNoEndDate = dialogView.findViewById(R.id.cbNoEndDate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // 设置星期选择按钮
        String[] weekDays = {"日", "一", "二", "三", "四", "五", "六"};
        List<MaterialButton> dayButtons = new ArrayList<>();
        List<Integer> selectedDays = new ArrayList<>();

        for (int i = 0; i < weekDays.length; i++) {
            MaterialButton button = new MaterialButton(requireContext());
            button.setText(weekDays[i]);
            button.setCheckable(true);
            final int dayOfWeek = i + 1; // 1=周日, 2=周一, ..., 7=周六

            button.setOnClickListener(v -> {
                boolean isChecked = !button.isChecked();
                button.setChecked(isChecked);

                if (isChecked) {
                    selectedDays.add(dayOfWeek);
                } else {
                    selectedDays.remove(Integer.valueOf(dayOfWeek));
                }
            });

            weekDayContainer.addView(button);
            dayButtons.add(button);
        }

        // 默认选择所有天数
        for (MaterialButton button : dayButtons) {
            button.setChecked(true);
        }
        for (int i = 1; i <= 7; i++) {
            selectedDays.add(i);
        }

        // 日期选择
        Calendar calendar = Calendar.getInstance();
        btnStartDate.setText(getFormattedDate(calendar.getTime()));

        btnStartDate.setOnClickListener(v -> showDatePickerDialog(calendar, btnStartDate));
        btnEndDate.setOnClickListener(v -> showDatePickerDialog(calendar, btnEndDate));

        cbNoEndDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnEndDate.setEnabled(!isChecked);
            if (isChecked) {
                btnEndDate.setText("选择结束时间");
            }
        });

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String habitName = etHabitName.getText().toString().trim();
            if (habitName.isEmpty()) {
                etHabitName.setError("请输入任务名称");
                return;
            }

            int dailyCount = 1;
            try {
                dailyCount = Integer.parseInt(etDailyCount.getText().toString());
                if (dailyCount <= 0) dailyCount = 1;
            } catch (NumberFormatException e) {
                dailyCount = 1;
            }

            Habit habit = new Habit();
            habit.setName(habitName);
            habit.setFrequency(selectedDays);
            habit.setStartDate(calendar.getTime());
            habit.setDailyTargetCount(dailyCount);

            if (!cbNoEndDate.isChecked() && btnEndDate.getText().toString().contains("-")) {
                // 解析结束日期
                // 这里需要实现日期解析逻辑
            }

            habitViewModel.insert(habit);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDatePickerDialog(Calendar calendar, Button targetButton) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    targetButton.setText(getFormattedDate(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private String getFormattedDate(java.util.Date date) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(date);
    }
}