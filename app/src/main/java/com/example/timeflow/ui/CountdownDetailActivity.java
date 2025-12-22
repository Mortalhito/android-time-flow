package com.example.timeflow.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.timeflow.R;
import com.example.timeflow.database.DatabaseHelper;
import com.example.timeflow.entity.CountdownEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CountdownDetailActivity extends AppCompatActivity {

    private TextView tvEventTitle, tvTimePrefix, tvDaysDisplay, tvDisplayUnit, tvTargetDateFull;
    private CountdownEvent currentEvent;
    private DatabaseHelper databaseHelper;
    private View section1, section2, section3; // 新增：三个部分的视图引用
    // 显示模式枚举
    private enum DisplayMode {
        DAYS, YEARS_MONTHS_DAYS, MONTHS_DAYS, WEEKS_DAYS
    }

    private DisplayMode currentDisplayMode = DisplayMode.DAYS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_detail);

        initViews();
        initData();
        setupClickListeners();
        updateDisplay();
        setupSectionBackgrounds(); // 新增：设置各部分背景色
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 设置编辑按钮（右上角）
        ImageButton btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(v -> {
            // 预留编辑功能
            // showEditDialog();
        });

        // 初始化三个部分的视图
        section1 = findViewById(R.id.section1);
        section2 = findViewById(R.id.section2);
        section3 = findViewById(R.id.section3);

        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvTimePrefix = findViewById(R.id.tvTimePrefix);
        tvDaysDisplay = findViewById(R.id.tvDaysDisplay);
        tvDisplayUnit = findViewById(R.id.tvDisplayUnit);
        tvTargetDateFull = findViewById(R.id.tvTargetDateFull);

        // 设置返回按钮点击事件
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // 新增方法：设置各部分背景色
    private void setupSectionBackgrounds() {
        // 第一部分：蓝/橙色背景
        if(currentEvent.isPast()){
            section1.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            section1.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        }

        // 第二部分：白色背景（默认就是白色，可以不用设置）
        section2.setBackgroundColor(Color.WHITE);

        // 第三部分：灰色背景
        section3.setBackgroundColor(getResources().getColor(R.color.section3_gray));
    }

    private void initData() {
        databaseHelper = new DatabaseHelper(this);

        String eventId = getIntent().getStringExtra("event_id");
        String debugEventName = getIntent().getStringExtra("debug_event_name");

        System.out.println("接收到的事件ID: " + eventId);
        System.out.println("调试事件名称: " + debugEventName);

        if (eventId != null) {
            try {
                currentEvent = databaseHelper.getEventById(eventId);
                if (currentEvent != null) {
                    displayEventDetails();
                } else {
                    // 尝试从事件列表中查找
                    List<CountdownEvent> allEvents = databaseHelper.getAllEvents();
                    System.out.println("数据库中总事件数: " + allEvents.size());
                    for (CountdownEvent e : allEvents) {
                        System.out.println("数据库中的事件: ID=" + e.getId() + ", 名称=" + e.getSafeName());
                    }

                    Toast.makeText(this, "事件不存在，ID: " + eventId, Toast.LENGTH_LONG).show();
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "加载事件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "事件ID为空", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayEventDetails() {
        if (currentEvent == null) return;

        tvEventTitle.setText(currentEvent.getSafeName());

        // 设置时间前缀（还有/已经）
        if (currentEvent.isPast()) {
            tvTimePrefix.setText("已经");
            tvDaysDisplay.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvTimePrefix.setText("还有");
            tvDaysDisplay.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        }

        // 设置完整的目标日期
        String targetDate = currentEvent.getTargetDate();
        String weekDay = getWeekDayFromDate(targetDate);
        tvTargetDateFull.setText(String.format("目标日：%s %s", targetDate, weekDay));
    }

    private String getWeekDayFromDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date date = sdf.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
            int weekDayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            return weekDays[weekDayIndex];
        } catch (Exception e) {
            return "";
        }
    }

    private void setupClickListeners() {
        // 编辑按钮点击事件
        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            // 预留编辑功能
            // showEditDialog();
        });

        // 第二部分点击切换显示模式
        findViewById(R.id.section2).setOnClickListener(v -> {
            switchDisplayMode();
        });
    }

    private void switchDisplayMode() {
        // 循环切换显示模式
        switch (currentDisplayMode) {
            case DAYS:
                currentDisplayMode = DisplayMode.YEARS_MONTHS_DAYS;
                break;
            case YEARS_MONTHS_DAYS:
                currentDisplayMode = DisplayMode.MONTHS_DAYS;
                break;
            case MONTHS_DAYS:
                currentDisplayMode = DisplayMode.WEEKS_DAYS;
                break;
            case WEEKS_DAYS:
                currentDisplayMode = DisplayMode.DAYS;
                break;
        }
        updateDisplay();
    }

    private void updateDisplay() {
        if (currentEvent == null) return;

        int totalDays = Math.abs(currentEvent.getDaysLeft());

        switch (currentDisplayMode) {
            case DAYS:
                tvDaysDisplay.setText(String.valueOf(totalDays));
                tvDisplayUnit.setText("天");
                break;

            case YEARS_MONTHS_DAYS:
                int years = totalDays / 365;
                int remainingDays = totalDays % 365;
                int months = remainingDays / 30;
                int days = remainingDays % 30;

                if (years > 0) {
                    tvDaysDisplay.setText(String.format("%d年%d个月%d天", years, months, days));
                } else {
                    tvDaysDisplay.setText(String.format("%d个月%d天", months, days));
                }
                tvDisplayUnit.setText("");
                break;

            case MONTHS_DAYS:
                int totalMonths = totalDays / 30;
                int remainingDaysFromMonths = totalDays % 30;
                tvDaysDisplay.setText(String.format("%d个月%d天", totalMonths, remainingDaysFromMonths));
                tvDisplayUnit.setText("");
                break;

            case WEEKS_DAYS:
                int weeks = totalDays / 7;
                int remainingDaysFromWeeks = totalDays % 7;
                tvDaysDisplay.setText(String.format("%d周%d天", weeks, remainingDaysFromWeeks));
                tvDisplayUnit.setText("");
                break;
        }
    }
}