package com.example.timeflow.calendar;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.timeflow.R;
import com.example.timeflow.adapter.CalendarAdapter;
import com.example.timeflow.entity.CalendarEvent;

import java.util.Calendar;
import java.util.List;

public class CustomCalendarView extends LinearLayout {

    private ImageButton btnPrevious, btnNext;
    private TextView tvCurrentDate;
    private GridView gridView;
    private Calendar currentDate;
    private CalendarAdapter adapter;
    private List<CalendarEvent> eventList;
    private OnDateSelectedListener dateListener;

    public interface OnDateSelectedListener {
        void onDateSelected(int year, int month, int day);
    }

    public CustomCalendarView(Context context) {
        super(context);
        init(context);
    }

    public CustomCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_calendar, this, true);

        btnPrevious = view.findViewById(R.id.btnPrevious);
        btnNext = view.findViewById(R.id.btnNext);
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        gridView = view.findViewById(R.id.gridView);

        currentDate = Calendar.getInstance();
        updateCalendar();

        btnPrevious.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        btnNext.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }

    private void updateCalendar() {
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH) + 1; // 月份从0开始
        tvCurrentDate.setText(String.format("%d年%d月", year, month));

        if (adapter == null) {
            adapter = new CalendarAdapter(getContext(), currentDate, eventList);
            adapter.setOnDateSelectedListener(dateListener);
            gridView.setAdapter(adapter);
        } else {
            adapter.setCurrentDate(currentDate);
            adapter.setEventList(eventList);
            adapter.notifyDataSetChanged();
        }
    }

    public void setEventList(List<CalendarEvent> eventList) {
        this.eventList = eventList;
        if (adapter != null) {
            adapter.setEventList(eventList);
            adapter.notifyDataSetChanged();
        }
    }

    public void setSelectedDate(Calendar calendar) {
        currentDate = calendar;
        updateCalendar();
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.dateListener = listener;
        if (adapter != null) {
            adapter.setOnDateSelectedListener(listener);
        }
    }
}