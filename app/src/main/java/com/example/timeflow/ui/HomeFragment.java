package com.example.timeflow.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timeflow.R;
import com.example.timeflow.adapter.EventAdapter;
import com.example.timeflow.calendar.CustomCalendarView;
import com.example.timeflow.entity.CalendarEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private CustomCalendarView calendarView;
    private RecyclerView recyclerViewEvents;
    private EventAdapter eventAdapter;
    private List<CalendarEvent> eventList;
    private TextView tvSelectedDate;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        initData();
        setupCalendar();
        setupEventList();

        return view;
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        recyclerViewEvents = view.findViewById(R.id.recyclerViewEvents);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
    }

    private void initData() {
        eventList = new ArrayList<>();
        // 示例事件
        eventList.add(new CalendarEvent("项目会议", "2025-11-15", "high", "14:00"));
        eventList.add(new CalendarEvent("生日聚会", "2025-11-20", "medium", "18:00"));
    }

    private void setupCalendar() {
        calendarView.setEventList(eventList);
        calendarView.setOnDateSelectedListener((year, month, day) -> {
            String selectedDate = String.format("%d-%02d-%02d", year, month + 1, day);
            tvSelectedDate.setText(selectedDate);
            showEventsForDate(selectedDate);
        });

        // 设置当前日期
        Calendar calendar = Calendar.getInstance();
        calendarView.setSelectedDate(calendar);
    }

    private void setupEventList() {
        eventAdapter = new EventAdapter(eventList);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewEvents.setAdapter(eventAdapter);
    }

    private void showEventsForDate(String date) {
        List<CalendarEvent> dailyEvents = new ArrayList<>();
        for (CalendarEvent event : eventList) {
            if (event.getDate().equals(date)) {
                dailyEvents.add(event);
            }
        }
        eventAdapter.updateEvents(dailyEvents);
    }
}