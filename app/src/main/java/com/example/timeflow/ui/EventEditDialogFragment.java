package com.example.timeflow.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.timeflow.R;
import com.example.timeflow.room.entity.CalendarEvent;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class EventEditDialogFragment extends DialogFragment {

    public static final String ARG_EVENT = "event";
    public static final String ARG_SELECTED_DATE = "selected_date";
    public static final String ARG_MODE = "mode"; // "add" or "edit"

    private TextView tvDialogTitle;
    private TextInputEditText etEventTitle, etEventDate, etEventTime, etReminderTime;
    private MaterialButtonToggleGroup priorityToggle;
    private MaterialCheckBox cbReminder;
    private View layoutReminderTime;
    private Button btnCancel, btnConfirm;
    private LinearLayout layoutButtons;

    // 添加缺失的变量声明
    private CalendarEvent event;
    private String selectedDate;
    private String mode = "add"; // 默认为添加模式

    private OnEventSaveListener listener;

    public interface OnEventSaveListener {
        void onEventSave(CalendarEvent event, String mode);
        void onEventDelete(CalendarEvent event);
    }

    public void setOnEventSaveListener(OnEventSaveListener listener) {
        this.listener = listener;
    }

    public static EventEditDialogFragment newInstance(String selectedDate) {
        EventEditDialogFragment fragment = new EventEditDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_DATE, selectedDate);
        args.putString(ARG_MODE, "add");
        fragment.setArguments(args);
        return fragment;
    }

    public static EventEditDialogFragment newInstance(CalendarEvent event) {
        EventEditDialogFragment fragment = new EventEditDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        args.putString(ARG_MODE, "edit");
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_edit, container, false);
        initViews(view);
        setupClickListeners();
        loadData();
        return view;
    }

    private void initViews(View view) {
        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        etEventTitle = view.findViewById(R.id.etEventTitle);
        etEventDate = view.findViewById(R.id.etEventDate);
        etEventTime = view.findViewById(R.id.etEventTime);
        etReminderTime = view.findViewById(R.id.etReminderTime);
        priorityToggle = view.findViewById(R.id.priorityToggle);
        cbReminder = view.findViewById(R.id.cbReminder);
        layoutReminderTime = view.findViewById(R.id.layoutReminderTime);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        layoutButtons = view.findViewById(R.id.layoutButtons);
    }

    private void setupClickListeners() {
        // 日期选择
        etEventDate.setOnClickListener(v -> showDatePicker());
        // 时间选择
        etEventTime.setOnClickListener(v -> showTimePicker(etEventTime));
        etReminderTime.setOnClickListener(v -> showTimePicker(etReminderTime));

        // 提醒复选框
        cbReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutReminderTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // 优先级按钮组监听 - 简化版本
        priorityToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            // MaterialButtonToggleGroup 默认就是单选模式，不需要额外处理
        });

        // 取消按钮
        btnCancel.setOnClickListener(v -> dismiss());

        // 确认按钮
        btnConfirm.setOnClickListener(v -> saveEvent());

        priorityToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            updatePriorityButtonColors();
        });
    }

    private void loadData() {
        Bundle args = getArguments();
        if (args != null) {
            mode = args.getString(ARG_MODE, "add");

            if (mode.equals("edit")) {
                tvDialogTitle.setText("编辑事务");
                btnConfirm.setText("确认修改");


                event = (CalendarEvent) args.getSerializable(ARG_EVENT);
                if (event != null) {
                    etEventTitle.setText(event.getTitle());
                    etEventDate.setText(event.getDate());
                    etEventTime.setText(event.getTime());

                    // 设置优先级 - 确保正确选中
                    switch (event.getPriority()) {
                        case "high":
                            priorityToggle.check(R.id.btnPriorityHigh);
                            break;
                        case "medium":
                            priorityToggle.check(R.id.btnPriorityMedium);
                            break;
                        case "low":
                            priorityToggle.check(R.id.btnPriorityLow);
                            break;
                    }

                    // 设置提醒
                    cbReminder.setChecked(event.isReminderEnabled());
                    etReminderTime.setText(event.getReminderTime());
                    layoutReminderTime.setVisibility(event.isReminderEnabled() ? View.VISIBLE : View.GONE);
                }
            } else {
                tvDialogTitle.setText("添加事务");
                btnConfirm.setText("确认添加");

                selectedDate = args.getString(ARG_SELECTED_DATE);
                etEventDate.setText(selectedDate);

                // 设置默认时间为当前时间
                Calendar calendar = Calendar.getInstance();
                String time = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                etEventTime.setText(time);

                // 默认选中一般紧急
                priorityToggle.check(R.id.btnPriorityMedium);
                updatePriorityButtonColors();
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            String date = String.format("%d-%02d-%02d", year1, month1 + 1, dayOfMonth);
            etEventDate.setText(date);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute1) -> {
            String time = String.format("%02d:%02d", hourOfDay, minute1);
            editText.setText(time);
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void saveEvent() {
        String title = etEventTitle.getText().toString().trim();
        String date = etEventDate.getText().toString().trim();
        String time = etEventTime.getText().toString().trim();

        // 获取优先级 - 添加调试日志
        String priority = "medium";
        int checkedButtonId = priorityToggle.getCheckedButtonId();
        if (checkedButtonId == R.id.btnPriorityHigh) {
            priority = "high";
        } else if (checkedButtonId == R.id.btnPriorityLow) {
            priority = "low";
        }

        boolean reminderEnabled = cbReminder.isChecked();
        String reminderTime = etReminderTime.getText().toString().trim();

        if (title.isEmpty()) {
            etEventTitle.setError("请输入事务标题");
            return;
        }
        if (date.isEmpty()) {
            etEventDate.setError("请选择日期");
            return;
        }
        if (time.isEmpty()) {
            etEventTime.setError("请选择时间");
            return;
        }
        if (reminderEnabled && reminderTime.isEmpty()) {
            etReminderTime.setError("请设置提醒时间");
            return;
        }

        CalendarEvent newEvent;
        if (mode.equals("edit")) {
            // 修改模式，保留原有的id
            event.setTitle(title);
            event.setDate(date);
            event.setTime(time);
            event.setPriority(priority);
            event.setReminderEnabled(reminderEnabled);
            event.setReminderTime(reminderTime);
            newEvent = event;
        } else {
            // 添加模式，生成新id
            newEvent = new CalendarEvent(title, date, priority, time);
            newEvent.setReminderEnabled(reminderEnabled);
            newEvent.setReminderTime(reminderTime);
        }

        if (listener != null) {
            listener.onEventSave(newEvent, mode);
        }
        dismiss();
    }

    private void deleteEvent() {
        if (event != null) {
            // 创建确认对话框
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("确认删除")
                    .setMessage("确定要删除这个事务吗？此操作不可撤销。")
                    .setPositiveButton("删除", (dialog, which) -> {
                        // 用户确认删除，执行删除操作
                        if (listener != null) {
                            listener.onEventDelete(event);
                        }
                        dismiss();
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        // 用户取消，不做任何操作
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        }
    }

    /**
     * 更新优先级按钮的颜色显示
     */
    private void updatePriorityButtonColors() {
        int checkedButtonId = priorityToggle.getCheckedButtonId();

        // 重置所有按钮颜色
        resetPriorityButtonColors();

        // 设置选中按钮的颜色
        if (checkedButtonId == R.id.btnPriorityHigh) {
            setPriorityButtonColor(R.id.btnPriorityHigh, R.color.red);
        } else if (checkedButtonId == R.id.btnPriorityMedium) {
            setPriorityButtonColor(R.id.btnPriorityMedium, R.color.yellow);
        } else if (checkedButtonId == R.id.btnPriorityLow) {
            setPriorityButtonColor(R.id.btnPriorityLow, R.color.green);
        }
    }

    /**
     * 重置所有优先级按钮的颜色
     */
    private void resetPriorityButtonColors() {
        setPriorityButtonColor(R.id.btnPriorityHigh, R.color.gray_light);
        setPriorityButtonColor(R.id.btnPriorityMedium, R.color.gray_light);
        setPriorityButtonColor(R.id.btnPriorityLow, R.color.gray_light);
    }

    /**
     * 设置优先级按钮的颜色
     */
    private void setPriorityButtonColor(int buttonId, int colorResId) {
        MaterialButton button = priorityToggle.findViewById(buttonId);
        if (button != null) {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId));
            // 根据背景色调整文字颜色以确保可读性
            if (colorResId == R.color.red || colorResId == R.color.green) {
                button.setTextColor(Color.WHITE);
            } else {
                button.setTextColor(Color.BLACK);
            }
        }
    }
}