package com.example.timeflow.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
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

        // 初始化新布局的视图
        TextInputEditText etHabitTitle = dialogView.findViewById(R.id.etHabitTitle);
        LinearLayout iconContainer = dialogView.findViewById(R.id.iconContainer);
        MaterialButtonToggleGroup repeatToggleGroup = dialogView.findViewById(R.id.repeat_toggle_group);
        MaterialButton btnWeeklyFixed = dialogView.findViewById(R.id.btnWeeklyFixed);
        MaterialButton btnWeeklyRandom = dialogView.findViewById(R.id.btnWeeklyRandom);
        MaterialButton btnMonthlyFixed = dialogView.findViewById(R.id.btnMonthlyFixed);
        MaterialButton btnMonthlyRandom = dialogView.findViewById(R.id.btnMonthlyRandom);

        FrameLayout repeatContentContainer = dialogView.findViewById(R.id.repeatContentContainer);
        LinearLayout layoutWeeklyFixed = dialogView.findViewById(R.id.layoutWeeklyFixed);
        LinearLayout layoutWeeklyRandom = dialogView.findViewById(R.id.layoutWeeklyRandom);
        LinearLayout layoutMonthlyFixed = dialogView.findViewById(R.id.layoutMonthlyFixed);
        LinearLayout layoutMonthlyRandom = dialogView.findViewById(R.id.layoutMonthlyRandom);

        LinearLayout weekDayContainer = dialogView.findViewById(R.id.weekDayContainer);
        LinearLayout monthDayContainer = dialogView.findViewById(R.id.monthDayContainer);
        NumberPicker npWeeklyCount = dialogView.findViewById(R.id.npWeeklyCount);
        NumberPicker npMonthlyCount = dialogView.findViewById(R.id.npMonthlyCount);

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

        // 设置重复周期按钮组监听
        repeatToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            // 隐藏所有布局
            layoutWeeklyFixed.setVisibility(View.GONE);
            layoutWeeklyRandom.setVisibility(View.GONE);
            layoutMonthlyFixed.setVisibility(View.GONE);
            layoutMonthlyRandom.setVisibility(View.GONE);

            // 显示选中的布局
            if (checkedId == R.id.btnWeeklyFixed) {
                layoutWeeklyFixed.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.btnWeeklyRandom) {
                layoutWeeklyRandom.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.btnMonthlyFixed) {
                layoutMonthlyFixed.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.btnMonthlyRandom) {
                layoutMonthlyRandom.setVisibility(View.VISIBLE);
            }
        });

        // 设置星期选择按钮
        setupWeekDayButtons(weekDayContainer);

        // 设置月份日期选择
        setupMonthDayButtons(monthDayContainer);

        // 设置NumberPicker
        setupNumberPicker(npWeeklyCount, 1, 7, 3);
        setupNumberPicker(npMonthlyCount, 1, 30, 10);
        setupNumberPicker(npDailyCount, 1, 10, 1);

        // 日期选择逻辑
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
            String habitName = etHabitTitle.getText().toString().trim();
            if (habitName.isEmpty()) {
                etHabitTitle.setError("请输入任务名称");
                return;
            }

            int dailyCount = npDailyCount.getValue();
            String encouragement = etEncouragement.getText().toString().trim();

            Habit habit = new Habit();
            habit.setName(habitName);
            habit.setDailyTargetCount(dailyCount);
            habit.setStartDate(calendar.getTime());

            // 根据选择的重复周期类型设置频率 - 修改这部分
            List<Integer> frequency = new ArrayList<>();
            if (btnWeeklyFixed.isChecked()) {
                frequency = getSelectedWeekDays(weekDayContainer);
            } else if (btnWeeklyRandom.isChecked()) {
                for (int i = 1; i <= 7; i++) {
                    frequency.add(i);
                }
            } else if (btnMonthlyFixed.isChecked()) {
                frequency = getSelectedMonthDays(monthDayContainer);
            } else if (btnMonthlyRandom.isChecked()) {
                for (int i = 1; i <= 31; i++) {
                    frequency.add(i);
                }
            }

            habit.setFrequency(frequency);

            if (!cbNoEndDate.isChecked() && btnEndDate.getText().toString().contains("-")) {
                // 解析结束日期逻辑
            }

            habitViewModel.insert(habit);
            dialog.dismiss();
        });

        dialog.show();
    }



    // 设置NumberPicker的方法
    private void setupNumberPicker(NumberPicker numberPicker, int min, int max, int defaultValue) {
        numberPicker.setMinValue(min);
        numberPicker.setMaxValue(max);
        numberPicker.setValue(defaultValue);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setFormatter(value -> value + "次");
    }



    private void setupWeekDayButtons(LinearLayout container) {
        container.removeAllViews();
        // 确保容器水平排列并设置权重总和为 7
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setWeightSum(7f);

        String[] weekDays = {"日", "一", "二", "三", "四", "五", "六"};
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        // 将 2dp 的间距转换为像素
        int marginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        // 设置一个固定的高度，例如 40dp，确保它是圆形的
        int buttonHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, metrics);

        for (int i = 0; i < weekDays.length; i++) {
            MaterialButton button = new MaterialButton(requireContext());

            // 使用权重分配宽度：width 设为 0，weight 设为 1
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    buttonHeight,
                    1.0f
            );
            params.setMargins(marginPx, marginPx, marginPx, marginPx);
            button.setLayoutParams(params);

            // 核心修正：移除 MaterialButton 默认的内边距和最小宽高限制
            button.setPadding(0, 0, 0, 0);
            button.setInsetTop(0);
            button.setInsetBottom(0);
            button.setMinWidth(0);
            button.setMinHeight(0);

            button.setText(weekDays[i]);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            button.setCheckable(true);
            button.setClickable(true);

            // 设置背景和文字颜色
            button.setBackgroundTintList(null);
            button.setBackgroundResource(R.drawable.btn_week_day_selector);
            button.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.week_day_text_color));

            // 设置圆角为高度的一半
            button.setCornerRadius(buttonHeight / 2);

            button.setChecked(true);
            container.addView(button);
        }
    }

    private void setupMonthDayButtons(LinearLayout container) {
        container.removeAllViews();
        container.setOrientation(LinearLayout.VERTICAL);

        int daysInMonth = 31;
        int buttonsPerRow = 7;
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        // 【核心修正 1】精确扣除 XML 布局中的 padding
        // 你的 ScrollView padding="16dp"，左右共 32dp。
        // 我们预留 40dp 保证万无一失。
        int totalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, metrics);
        int availableWidth = metrics.widthPixels - totalPadding;

        // 计算每个球能分配的最大宽度（像素）
        int cellWidth = availableWidth / buttonsPerRow;

        // 设置 Margin 为 1dp
        int marginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, metrics);
        // 按钮直径 = 格子宽 - 左右 Margin
        int buttonSize = cellWidth - (marginPx * 2);

        LinearLayout rowLayout = null;

        for (int i = 1; i <= daysInMonth; i++) {
            if ((i - 1) % buttonsPerRow == 0) {
                rowLayout = new LinearLayout(requireContext());
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setGravity(android.view.Gravity.START); // 最后一行左对齐

                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                container.addView(rowLayout, rowParams);
            }

            if (rowLayout != null) {
                MaterialButton button = new MaterialButton(requireContext());

                // 【核心修正 2】强制 宽 = 高 = 计算出来的像素值
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(buttonSize, buttonSize);
                params.setMargins(marginPx, marginPx, marginPx, marginPx);
                button.setLayoutParams(params);

                // 【核心修正 3】彻底重置所有可能导致按钮撑大的属性
                // 不要设置 setInsetLeft/Right，因为 API 不支持
                button.setPadding(0, 0, 0, 0);
                button.setInsetTop(0);
                button.setInsetBottom(0);

                // 必须同时设置这四个方法为 0，否则 MaterialButton 会维持默认的宽度
                button.setMinWidth(0);
                button.setMinHeight(0);
                button.setMinimumWidth(0);
                button.setMinimumHeight(0);

                // 样式
                button.setCornerRadius(buttonSize / 2);
                button.setText(String.valueOf(i));
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12); // 文字设小一点点
                button.setIncludeFontPadding(false);

                button.setCheckable(true);
                button.setBackgroundTintList(null);
                button.setBackgroundResource(R.drawable.btn_week_day_selector);
                button.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.week_day_text_color));

                button.setChecked(true);
                rowLayout.addView(button);
            }
        }
    }

    private List<Integer> getSelectedWeekDays(LinearLayout container) {
        List<Integer> selectedDays = new ArrayList<>();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton button = (MaterialButton) child;
                if (button.isChecked()) {
                    selectedDays.add(i + 1); // 1=周日, 2=周一, ..., 7=周六
                }
            }
        }
        return selectedDays;
    }

    private List<Integer> getSelectedMonthDays(LinearLayout container) {
        List<Integer> selectedDays = new ArrayList<>();
        int dayCount = 1;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;
                for (int j = 0; j < row.getChildCount(); j++) {
                    View buttonView = row.getChildAt(j);
                    if (buttonView instanceof MaterialButton) {
                        MaterialButton button = (MaterialButton) buttonView;
                        if (button.isChecked()) {
                            selectedDays.add(dayCount);
                        }
                        dayCount++;
                        if (dayCount > 31) break;
                    }
                }
            }
            if (dayCount > 31) break;
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

    private String getFormattedDate(java.util.Date date) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(date);
    }
}