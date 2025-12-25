package com.example.timeflow.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.timeflow.R;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.CountdownEvent;

public class CountdownDetailActivity extends AppCompatActivity {

    // 严格对应 activity_countdown_detail.xml 中的 ID
    private TextView tvEventTitle, tvTimePrefix, tvDaysDisplay, tvDisplayUnit, tvTargetDateFull;
    private View section1;
    private CountdownEvent currentEvent;
    private AppDatabase db;

    private enum DisplayMode { DAYS, YEARS_MONTHS_DAYS, MONTHS_DAYS, WEEKS_DAYS }
    private DisplayMode currentDisplayMode = DisplayMode.DAYS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_detail);
        db = AppDatabase.getInstance(this);

        // ID 绑定
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvTimePrefix = findViewById(R.id.tvTimePrefix);
        tvDaysDisplay = findViewById(R.id.tvDaysDisplay);
        tvDisplayUnit = findViewById(R.id.tvDisplayUnit);
        tvTargetDateFull = findViewById(R.id.tvTargetDateFull);
        section1 = findViewById(R.id.section1);

        // ID 绑定: btnBack (Toolbar默认或ImageButton), btnDelete
        findViewById(R.id.btnEdit).setOnClickListener(v -> deleteEvent());

        if (section1 != null) {
            section1.setOnClickListener(v -> switchDisplayMode());
        }

        loadData();
    }

    private void loadData() {
        String id = getIntent().getStringExtra("event_id");
        if (id == null) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            CountdownEvent event = db.eventDao().getEventById(id);
            if (event != null) {
                event.calculateDaysLeft();
                runOnUiThread(() -> {
                    currentEvent = event;
                    updateDisplay();
                });
            }
        });
    }

    private void updateDisplay() {
        if (currentEvent == null) return;
        tvEventTitle.setText(currentEvent.getName());
        tvTimePrefix.setText(currentEvent.isPast() ? "已经" : "还有");
        tvTargetDateFull.setText("目标日：" + currentEvent.getTargetDate());

        int totalDays = Math.abs(currentEvent.getDaysLeft());

        switch (currentDisplayMode) {
            case DAYS:
                tvDaysDisplay.setText(String.valueOf(totalDays));
                tvDisplayUnit.setText("天");
                break;
            case YEARS_MONTHS_DAYS:
                int years = totalDays / 365;
                int remDays = totalDays % 365;
                int months = remDays / 30;
                int days = remDays % 30;
                tvDaysDisplay.setText(years > 0 ?
                        String.format("%d年%d个月%d天", years, months, days) :
                        String.format("%d个月%d天", months, days));
                tvDisplayUnit.setText("");
                break;
            case WEEKS_DAYS:
                tvDaysDisplay.setText(String.format("%d周%d天", totalDays / 7, totalDays % 7));
                tvDisplayUnit.setText("");
                break;
            default:
                tvDaysDisplay.setText(String.valueOf(totalDays));
                tvDisplayUnit.setText("天");
                break;
        }
    }

    private void switchDisplayMode() {
        DisplayMode[] modes = DisplayMode.values();
        currentDisplayMode = modes[(currentDisplayMode.ordinal() + 1) % modes.length];
        updateDisplay();
    }

    private void deleteEvent() {
        if (currentEvent == null) return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.eventDao().deleteById(currentEvent.getId());
            runOnUiThread(() -> {
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}