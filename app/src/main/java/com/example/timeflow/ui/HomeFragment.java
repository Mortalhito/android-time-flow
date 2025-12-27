package com.example.timeflow.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.adapter.CalendarEventAdapter;
import com.example.timeflow.room.entity.CalendarEvent;
import com.example.timeflow.room.repository.CalendarEventRepository;
import com.example.timeflow.view.CustomCalendarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment implements EventEditDialogFragment.OnEventSaveListener {

    private CustomCalendarView calendarView;
    private RecyclerView recyclerViewEvents;
    private CalendarEventAdapter calendarEventAdapter;
    private List<CalendarEvent> eventList;
    private ImageView imageView;
    private String currentSelectedDate;
    private CalendarEventRepository calendarEventRepository;

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
        loadAllEvents();
        return view;
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        imageView = view.findViewById(R.id.add);
        recyclerViewEvents = view.findViewById(R.id.recyclerViewEvents);
    }

    private void initData() {
        eventList = new ArrayList<>();
        calendarEventRepository = new CalendarEventRepository(requireContext());
    }

    private void loadAllEvents() {
        calendarEventRepository.getAllEvents(new CalendarEventRepository.EventListListener() {
            @Override
            public void onEventsLoaded(List<CalendarEvent> events) {
                eventList.clear();
                eventList.addAll(events);
                calendarView.setEventList(eventList);
                showEventsForDate(currentSelectedDate);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "加载事件失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAddEvent(){
        imageView.setOnClickListener(view -> {
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
            calendarView.setSelectedDate(year, month, day);
        });

        Calendar calendar = Calendar.getInstance();
        calendarView.setSelectedDate(calendar);
        calendarView.setSelectedDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        currentSelectedDate = String.format("%d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void setupEventList() {
        calendarEventAdapter = new CalendarEventAdapter(eventList);

        // 点击事件 - 编辑
        calendarEventAdapter.setOnItemClickListener(event -> {
            EventEditDialogFragment dialog = EventEditDialogFragment.newInstance(event);
            dialog.setOnEventSaveListener(HomeFragment.this);
            dialog.show(getParentFragmentManager(), "EventEditDialogFragment");
        });

        // 长按事件 - 删除
        calendarEventAdapter.setOnItemLongClickListener(event -> {
            showDeleteConfirmationDialog(event);
        });

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewEvents.setAdapter(calendarEventAdapter);
    }

    private void showDeleteConfirmationDialog(CalendarEvent event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除事务")
                .setMessage("确定要删除 \"" + event.getTitle() + "\" 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    onEventDelete(event);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEventsForDate(String date) {
        calendarEventRepository.getEventsByDate(date, new CalendarEventRepository.EventListListener(){
            @Override
            public void onEventsLoaded(List<CalendarEvent> events) {
                calendarEventAdapter.updateEvents(events);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "加载当日事件失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEventSave(CalendarEvent event, String mode) {
        CalendarEventRepository.OperationListener listener = new CalendarEventRepository.OperationListener() {
            @Override
            public void onSuccess() {
                loadAllEvents(); // 重新加载所有事件以更新日历显示
                Log.d("ReminderDebug", "onEventSave被调用，模式: " + mode);
                Log.d("ReminderDebug", "事件标题: " + event.getTitle());
                Log.d("ReminderDebug", "提醒设置: " + event.isReminderEnabled() + ", " + event.getReminderTime());

                Toast.makeText(requireContext(),
                        "add".equals(mode) ? "添加成功" : "修改成功",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(),
                        "add".equals(mode) ? "添加失败" : "修改失败",
                        Toast.LENGTH_SHORT).show();
            }
        };

        if ("add".equals(mode)) {
            calendarEventRepository.insertEvent(event, listener);
        } else {
            calendarEventRepository.updateEvent(event, listener);
        }
    }

    @Override
    public void onEventDelete(CalendarEvent event) {
        calendarEventRepository.deleteEvent(event, new CalendarEventRepository.OperationListener() {
            @Override
            public void onSuccess() {
                loadAllEvents();
                Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "删除失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}