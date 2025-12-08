package com.example.timeflow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.timeflow.R;
import com.example.timeflow.calendar.CustomCalendarView;
import com.example.timeflow.entity.CalendarEvent;

import java.util.Calendar;
import java.util.List;

public class CalendarAdapter extends BaseAdapter {

    private Context context;
    private Calendar currentDate;
    private List<CalendarEvent> eventList;
    private CustomCalendarView.OnDateSelectedListener listener;

    public CalendarAdapter(Context context, Calendar currentDate, List<CalendarEvent> eventList) {
        this.context = context;
        this.currentDate = (Calendar) currentDate.clone();
        this.eventList = eventList;
    }

    public void setCurrentDate(Calendar currentDate) {
        this.currentDate = (Calendar) currentDate.clone();
    }

    public void setEventList(List<CalendarEvent> eventList) {
        this.eventList = eventList;
    }

    public void setOnDateSelectedListener(CustomCalendarView.OnDateSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return 42; // 6行7列
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        }

        TextView tvDay = convertView.findViewById(R.id.tvDay);
        View dot1 = convertView.findViewById(R.id.dot1);
        View dot2 = convertView.findViewById(R.id.dot2);
        View dot3 = convertView.findViewById(R.id.dot3);

        // 计算当前单元格对应的日期
        Calendar calendar = (Calendar) currentDate.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        int day = position - firstDayOfWeek + 2;
        if (position < firstDayOfWeek - 1 || day > daysInMonth) {
            tvDay.setText("");
            dot1.setVisibility(View.INVISIBLE);
            dot2.setVisibility(View.INVISIBLE);
            dot3.setVisibility(View.INVISIBLE);
        } else {
            tvDay.setText(String.valueOf(day));
            // 检查这一天是否有事件
            String dateStr = String.format("%d-%02d-%02d",
                    currentDate.get(Calendar.YEAR),
                    currentDate.get(Calendar.MONTH) + 1,
                    day);
            updateDotsForDate(dateStr, dot1, dot2, dot3);

            final int finalDay = day;
            convertView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDateSelected(
                            currentDate.get(Calendar.YEAR),
                            currentDate.get(Calendar.MONTH),
                            finalDay
                    );
                }
            });
        }

        return convertView;
    }

    private void updateDotsForDate(String date, View dot1, View dot2, View dot3) {
        int highCount = 0, mediumCount = 0, lowCount = 0;
        if (eventList != null) {
            for (CalendarEvent event : eventList) {
                if (date.equals(event.getDate())) {
                    switch (event.getPriority()) {
                        case "high": highCount++; break;
                        case "medium": mediumCount++; break;
                        case "low": lowCount++; break;
                    }
                }
            }
        }

        dot1.setVisibility(highCount > 0 ? View.VISIBLE : View.INVISIBLE);
        dot2.setVisibility(mediumCount > 0 ? View.VISIBLE : View.INVISIBLE);
        dot3.setVisibility(lowCount > 0 ? View.VISIBLE : View.INVISIBLE);
    }
}