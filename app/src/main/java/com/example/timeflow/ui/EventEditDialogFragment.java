package com.example.timeflow.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.HashMap;
import java.util.Map;

public class EventEditDialogFragment extends DialogFragment {

    public static final String ARG_EVENT = "event";
    public static final String ARG_SELECTED_DATE = "selected_date";
    public static final String ARG_MODE = "mode"; // "add" or "edit"

    private TextView tvDialogTitle;
    private TextInputEditText etEventTitle, etEventDate, etEventTime;
    private MaterialButtonToggleGroup priorityToggle;
    private MaterialCheckBox cbReminder;
    private View layoutReminderTime;
    private Button btnCancel, btnConfirm;
    private LinearLayout layoutButtons;
    private Spinner spinnerReminderOptions;

    // 添加缺失的变量声明
    private CalendarEvent event;
    private String selectedDate;
    private String mode = "add"; // 默认为添加模式

    // 预定义提醒选项映射
    private Map<String, String> reminderOptionsMap;
    private String[] reminderOptions;
    private String selectedReminderOffset = "0"; // 默认不提醒

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
        initReminderOptions();
        initViews(view);
        setupClickListeners();
        loadData();
        return view;
    }

    private void initReminderOptions() {
        // 初始化提醒选项映射（分钟数）
        reminderOptionsMap = new HashMap<>();
        reminderOptionsMap.put("不提醒", "0");
        reminderOptionsMap.put("准时提醒", "0");
        reminderOptionsMap.put("提前30分钟", "30");
        reminderOptionsMap.put("提前1小时", "60");
        reminderOptionsMap.put("提前2小时", "120");
        reminderOptionsMap.put("提前3小时", "180");
        reminderOptionsMap.put("自定义", "custom");

        reminderOptions = new String[]{
                "不提醒", "准时提醒", "提前30分钟", "提前1小时", "提前2小时", "提前3小时", "自定义"
        };
    }

    private void initViews(View view) {
        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        etEventTitle = view.findViewById(R.id.etEventTitle);
        etEventDate = view.findViewById(R.id.etEventDate);
        etEventTime = view.findViewById(R.id.etEventTime);
        priorityToggle = view.findViewById(R.id.priorityToggle);
        cbReminder = view.findViewById(R.id.cbReminder);
        layoutReminderTime = view.findViewById(R.id.layoutReminderTime);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        layoutButtons = view.findViewById(R.id.layoutButtons);

        // 初始化Spinner
        spinnerReminderOptions = view.findViewById(R.id.spinnerReminderOptions);

        // 设置Spinner适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                reminderOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminderOptions.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // 日期选择
        etEventDate.setOnClickListener(v -> showDatePicker());
        // 时间选择
        etEventTime.setOnClickListener(v -> showTimePicker(etEventTime));

        // 提醒复选框
        cbReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutReminderTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                selectedReminderOffset = "0"; // 不提醒
            }
        });

        // 优先级按钮组监听
        priorityToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            updatePriorityButtonColors();
        });

        // Spinner选择监听
        spinnerReminderOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = reminderOptions[position];
                selectedReminderOffset = reminderOptionsMap.get(selectedOption);

                // 如果选择自定义，显示自定义时间输入框
                if ("custom".equals(selectedReminderOffset)) {
                    showCustomTimePicker();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 取消按钮
        btnCancel.setOnClickListener(v -> dismiss());

        // 确认按钮
        btnConfirm.setOnClickListener(v -> saveEvent());
    }

    private void showCustomTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute1) -> {
            // 计算自定义时间相对于事件时间的偏移量（分钟）
            String eventTimeStr = etEventTime.getText().toString().trim();
            if (!eventTimeStr.isEmpty()) {
                try {
                    String[] eventParts = eventTimeStr.split(":");
                    int eventHour = Integer.parseInt(eventParts[0]);
                    int eventMinute = Integer.parseInt(eventParts[1]);

                    int eventTotalMinutes = eventHour * 60 + eventMinute;
                    int reminderTotalMinutes = hourOfDay * 60 + minute1;

                    int offsetMinutes = eventTotalMinutes - reminderTotalMinutes;
                    if (offsetMinutes < 0) {
                        offsetMinutes = 0; // 不能晚于事件时间
                    }

                    selectedReminderOffset = String.valueOf(offsetMinutes);
                } catch (Exception e) {
                    selectedReminderOffset = "0";
                }
            }
        }, hour, minute, true);
        timePickerDialog.setTitle("选择自定义提醒时间");
        timePickerDialog.show();
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

                    // 设置优先级
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
                    boolean reminderEnabled = event.isReminderEnabled();
                    cbReminder.setChecked(reminderEnabled);
                    layoutReminderTime.setVisibility(reminderEnabled ? View.VISIBLE : View.GONE);

                    // 设置提醒选项
                    if (reminderEnabled) {
                        String reminderOffset = event.getReminderTime(); // 现在存储偏移分钟数
                        setReminderSpinnerSelection(reminderOffset);
                    }
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

                // 默认选择"不提醒"
                spinnerReminderOptions.setSelection(0);
            }
        }
    }

    private void setReminderSpinnerSelection(String offsetMinutes) {
        if (offsetMinutes == null || "0".equals(offsetMinutes)) {
            spinnerReminderOptions.setSelection(0); // 不提醒
            return;
        }

        // 查找匹配的预定义选项
        for (int i = 0; i < reminderOptions.length; i++) {
            String option = reminderOptions[i];
            String optionValue = reminderOptionsMap.get(option);
            if (offsetMinutes.equals(optionValue)) {
                spinnerReminderOptions.setSelection(i);
                return;
            }
        }

        // 如果没有匹配的预定义选项，选择自定义
        spinnerReminderOptions.setSelection(reminderOptions.length - 1); // 最后一个选项是"自定义"
        selectedReminderOffset = offsetMinutes;
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

        // 获取优先级
        String priority = "medium";
        int checkedButtonId = priorityToggle.getCheckedButtonId();
        if (checkedButtonId == R.id.btnPriorityHigh) {
            priority = "high";
        } else if (checkedButtonId == R.id.btnPriorityLow) {
            priority = "low";
        }

        boolean reminderEnabled = cbReminder.isChecked();
        String reminderTime = selectedReminderOffset; // 存储偏移分钟数

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

    private void updatePriorityButtonColors() {
        int checkedButtonId = priorityToggle.getCheckedButtonId();
        resetPriorityButtonColors();

        if (checkedButtonId == R.id.btnPriorityHigh) {
            setPriorityButtonColor(R.id.btnPriorityHigh, R.color.red);
        } else if (checkedButtonId == R.id.btnPriorityMedium) {
            setPriorityButtonColor(R.id.btnPriorityMedium, R.color.yellow);
        } else if (checkedButtonId == R.id.btnPriorityLow) {
            setPriorityButtonColor(R.id.btnPriorityLow, R.color.green);
        }
    }

    private void resetPriorityButtonColors() {
        setPriorityButtonColor(R.id.btnPriorityHigh, R.color.gray_light);
        setPriorityButtonColor(R.id.btnPriorityMedium, R.color.gray_light);
        setPriorityButtonColor(R.id.btnPriorityLow, R.color.gray_light);
    }

    private void setPriorityButtonColor(int buttonId, int colorResId) {
        MaterialButton button = priorityToggle.findViewById(buttonId);
        if (button != null) {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId));
            if (colorResId == R.color.red || colorResId == R.color.green) {
                button.setTextColor(Color.WHITE);
            } else {
                button.setTextColor(Color.BLACK);
            }
        }
    }
}