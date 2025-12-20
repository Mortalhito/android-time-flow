package com.example.timeflow.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.adapter.EventAdapter;
import com.example.timeflow.entity.CalendarEvent;
import com.example.timeflow.view.CustomCalendarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment implements EventEditDialogFragment.OnEventSaveListener {

    private CustomCalendarView calendarView;
    private RecyclerView recyclerViewEvents;
    private EventAdapter eventAdapter;
    private List<CalendarEvent> eventList;
    private ImageView imageView;
    private String currentSelectedDate;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        initData();
        setupCalendar();
        setupEventList();
        setupAddEvent();
        return view;
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        imageView = view.findViewById(R.id.add);
        recyclerViewEvents = view.findViewById(R.id.recyclerViewEvents);
    }

    private void initData() {
        eventList = new ArrayList<>();
    }

    private void setupAddEvent(){
        imageView.setOnClickListener(view -> {
            // 使用当前选中的日期，如果没有则使用今天
            String date = currentSelectedDate != null ? currentSelectedDate :
                    String.format("%d-%02d-%02d",
                            Calendar.getInstance().get(Calendar.YEAR),
                            Calendar.getInstance().get(Calendar.MONTH) + 1,
                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

            EventEditDialogFragment dialog = EventEditDialogFragment.newInstance(date);
            dialog.setOnEventSaveListener(HomeFragment.this);
            dialog.show(getParentFragmentManager(), "EventEditDialogFragment");
        });
    }

    private void setupCalendar() {
        calendarView.setEventList(eventList);
        calendarView.setOnDateSelectedListener((year, month, day) -> {
            currentSelectedDate = String.format("%d-%02d-%02d", year, month + 1, day);
            showEventsForDate(currentSelectedDate);

            // 设置选中日期
            calendarView.setSelectedDate(year, month, day);
        });

        // 设置当前日期为选中状态
        Calendar calendar = Calendar.getInstance();
        calendarView.setSelectedDate(calendar);
        calendarView.setSelectedDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // 初始化当前选中日期
        currentSelectedDate = String.format("%d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void setupEventList() {
        eventAdapter = new EventAdapter(eventList);
        eventAdapter.setOnItemClickListener(new EventAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CalendarEvent event) {
                // 点击事务项，弹出编辑对话框
                EventEditDialogFragment dialog = EventEditDialogFragment.newInstance(event);
                dialog.setOnEventSaveListener(HomeFragment.this);
                dialog.show(getParentFragmentManager(), "EventEditDialogFragment");
            }
        });
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewEvents.setAdapter(eventAdapter);

        // 显示当前日期的事件
        showEventsForDate(currentSelectedDate);
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

    // 实现对话框的回调接口
    @Override
    public void onEventSave(CalendarEvent event, String mode) {
        if ("add".equals(mode)) {
            // 添加新事件
            eventList.add(event);
        } else {
            // 修改事件 - 找到原有事件并替换
            for (int i = 0; i < eventList.size(); i++) {
                if (eventList.get(i).getId().equals(event.getId())) {
                    eventList.set(i, event);
                    break;
                }
            }
        }

        // 更新日历视图
        calendarView.setEventList(eventList);
        // 刷新当前显示的事件列表
        showEventsForDate(currentSelectedDate);
    }

    @Override
    public void onEventDelete(CalendarEvent event) {
        // 删除事件
        eventList.removeIf(e -> e.getId().equals(event.getId()));

        // 更新日历视图
        calendarView.setEventList(eventList);
        // 刷新当前显示的事件列表
        showEventsForDate(currentSelectedDate);
    }
}