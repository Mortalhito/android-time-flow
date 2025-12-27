package com.example.timeflow.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.adapter.HabitAdapter;
import com.example.timeflow.room.entity.Habit;
import com.example.timeflow.viewmodel.HabitViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

        btnAddHabit = view.findViewById(R.id.btnAddHabit);
        chartContainer = view.findViewById(R.id.chartContainer);
    }

    private void setupRecyclerView() {
        adapter = new HabitAdapter(habitViewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);

        habitViewModel.getAllHabitsWithStats().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                adapter.submitList(list);
            }
        });
    }



    private void setupSimpleChart() {
        chartContainer.removeAllViews();

        for (Habit habit : habitList) {
            View chartItem = LayoutInflater.from(getContext()).inflate(R.layout.item_habit_chart, null);

            TextView tvHabitName = chartItem.findViewById(R.id.tvHabitName);
            android.widget.ProgressBar progressBar = chartItem.findViewById(R.id.progressBar);
            TextView tvPercentage = chartItem.findViewById(R.id.tvPercentage);

            tvHabitName.setText(habit.getName());

            // 获取完成率（这里需要从HabitRecord中获取真实数据）
            float completionRate = calculateCompletionRate(habit);
            progressBar.setProgress((int) completionRate);
            tvPercentage.setText(String.format("%.0f%%", completionRate));

            int color;
            if (completionRate >= 80) {
                color = ContextCompat.getColor(requireContext(), R.color.green);
            } else if (completionRate >= 50) {
                color = ContextCompat.getColor(requireContext(), R.color.yellow);
            } else {
                color = ContextCompat.getColor(requireContext(), R.color.red);
            }
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(color));

            chartContainer.addView(chartItem);
        }
    }

    private float calculateCompletionRate(Habit habit) {
        // 获取习惯的周目标天数
        int weeklyTargetDays = habit.getWeeklyTargetDays();
        if (weeklyTargetDays == 0) return 0f;

        // 这里需要从ViewModel获取实际完成数据
        // 暂时使用模拟数据，实际应该从数据库获取
        int completedDays = 0;
        int totalWeeks = 1; // 从开始日期到现在的周数

        // 计算完成率：实际完成天数 / 应该完成的天数
        float completionRate = 0f;
        if (totalWeeks > 0) {
            int targetDays = weeklyTargetDays * totalWeeks;
            completionRate = targetDays > 0 ? ((float) completedDays / targetDays) * 100 : 0f;
        }

        return Math.min(completionRate, 100f);
    }

    private void setClickListeners() {
        

        btnAddHabit.setOnClickListener(v -> showAddHabitDialog());
    }

    private void showAddHabitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null);
        builder.setView(dialogView);

        // 初始化视图
        TextInputEditText etHabitTitle = dialogView.findViewById(R.id.etHabitTitle);
        MaterialButtonToggleGroup repeatToggleGroup = dialogView.findViewById(R.id.repeat_toggle_group);
        MaterialButton btnWeeklyFixed = dialogView.findViewById(R.id.btnWeeklyFixed);
        MaterialButton btnWeeklyRandom = dialogView.findViewById(R.id.btnWeeklyRandom);
        FrameLayout repeatContentContainer = dialogView.findViewById(R.id.repeatContentContainer);
        LinearLayout layoutWeeklyFixed = dialogView.findViewById(R.id.layoutWeeklyFixed);
        LinearLayout layoutWeeklyRandom = dialogView.findViewById(R.id.layoutWeeklyRandom);
        LinearLayout weekDayContainer = dialogView.findViewById(R.id.weekDayContainer);
        NumberPicker npWeeklyCount = dialogView.findViewById(R.id.npWeeklyCount);
        Button btnStartDate = dialogView.findViewById(R.id.btnStartDate);
        Button btnEndDate = dialogView.findViewById(R.id.btnEndDate);
        NumberPicker npDailyCount = dialogView.findViewById(R.id.npDailyCount);
        TextInputEditText etEncouragement = dialogView.findViewById(R.id.etEncouragement);
        CheckBox cbNoEndDate = dialogView.findViewById(R.id.cb_no_end_date);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // 设置默认选中周定期
        btnWeeklyFixed.setChecked(true);
        layoutWeeklyFixed.setVisibility(View.VISIBLE);

        // 简化重复周期按钮组监听
        repeatToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            // 隐藏所有布局
            layoutWeeklyFixed.setVisibility(View.GONE);
            layoutWeeklyRandom.setVisibility(View.GONE);

            // 显示选中的布局
            if (checkedId == R.id.btnWeeklyFixed) {
                layoutWeeklyFixed.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.btnWeeklyRandom) {
                layoutWeeklyRandom.setVisibility(View.VISIBLE);
            }
        });

        // 设置NumberPicker
        setupNumberPicker(npWeeklyCount, 1, 7, 3);
        setupNumberPicker(npDailyCount, 1, 10, 1);

        // 日期选择逻辑
        Calendar calendar = Calendar.getInstance();
        String today = getFormattedDate(calendar.getTime());
        btnStartDate.setText(today);

        btnStartDate.setOnClickListener(v -> showDatePickerDialog(calendar, btnStartDate));
        btnEndDate.setOnClickListener(v -> showDatePickerDialog(calendar, btnEndDate));

        // 在showAddHabitDialog方法中，确保所有按钮默认选中
        MaterialButton[] dayButtons = {
                dialogView.findViewById(R.id.btnDay0),
                dialogView.findViewById(R.id.btnDay1),
                dialogView.findViewById(R.id.btnDay2),
                dialogView.findViewById(R.id.btnDay3),
                dialogView.findViewById(R.id.btnDay4),
                dialogView.findViewById(R.id.btnDay5),
                dialogView.findViewById(R.id.btnDay6)
        };

        for (MaterialButton button : dayButtons) {
            button.setChecked(true); // 确保默认选中
        }

        cbNoEndDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnEndDate.setEnabled(!isChecked);
            if (isChecked) {
                btnEndDate.setText("选择结束时间");
            }
        });

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String habitName = etHabitTitle.getText().toString().trim();
            if (habitName.isEmpty()) {
                etHabitTitle.setError("请输入习惯名称");
                return;
            }

            String encouragement = etEncouragement.getText().toString().trim();

            // 创建新的Habit对象
            Habit habit = new Habit();
            habit.setName(habitName);
            habit.setStartTime(calendar.getTime());
            habit.setTip(encouragement);

            // 设置频率类型和具体天数
            if (btnWeeklyFixed.isChecked()) {
                // 周定期
                habit.setRandom(false);
                List<Integer> selectedDays = getSelectedWeekDays(weekDayContainer);
                habit.setDays(selectedDays);
            } else if (btnWeeklyRandom.isChecked()) {
                // 周随机
                habit.setRandom(true);
                int weeklyCount = npWeeklyCount.getValue();
                List<Integer> randomDays = new ArrayList<>();
                randomDays.add(weeklyCount); // 只包含一个元素，表示每周随机完成几天
                habit.setDays(randomDays);
            }

            // 处理结束时间
            if (!cbNoEndDate.isChecked()) {
                habit.setEndTime(calendar.getTime());
            } else {
                habit.setEndTime(null); // 无限期
            }

            habitViewModel.insert(habit);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupNumberPicker(NumberPicker numberPicker, int min, int max, int defaultValue) {
        numberPicker.setMinValue(min);
        numberPicker.setMaxValue(max);
        numberPicker.setValue(defaultValue);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setFormatter(value -> value + (numberPicker.getId() == R.id.npWeeklyCount ? "天" : "次"));
    }


    private List<Integer> getSelectedWeekDays(View dialogView) {
        List<Integer> selectedDays = new ArrayList<>();

        // MaterialButton的ID数组
        int[] buttonIds = {R.id.btnDay0, R.id.btnDay1, R.id.btnDay2, R.id.btnDay3,
                R.id.btnDay4, R.id.btnDay5, R.id.btnDay6};

        for (int i = 0; i < buttonIds.length; i++) {
            MaterialButton button = dialogView.findViewById(buttonIds[i]);
            if (button != null && button.isChecked()) {
                // Calendar中周日=1，周一=2，...，周六=7
                selectedDays.add(i + 1);
            }
        }

        return selectedDays;
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

    private String getFormattedDate(Date date) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(date);
    }


//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        if (adapter != null) {
//            adapter.clear();
//        }
//    }
}