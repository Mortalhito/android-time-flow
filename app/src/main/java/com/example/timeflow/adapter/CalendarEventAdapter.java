package com.example.timeflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.room.entity.CalendarEvent;

import java.util.List;

public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.EventViewHolder> {

    private List<CalendarEvent> eventList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(CalendarEvent event);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public CalendarEventAdapter(List<CalendarEvent> eventList) {
        this.eventList = eventList;
    }

    public void updateEvents(List<CalendarEvent> events) {
        this.eventList = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        CalendarEvent event = eventList.get(position);
        holder.bind(event);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView tvEventTitle, tvEventTime, tvEventPriority;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            tvEventPriority = itemView.findViewById(R.id.tvEventPriority);
        }

        public void bind(CalendarEvent event) {
            tvEventTitle.setText(event.getTitle());
            tvEventTime.setText(event.getTime());

            // 根据优先级设置不同的显示
            switch (event.getPriority()) {
                case "high":
                    tvEventPriority.setText("非常紧急");
                    tvEventPriority.setBackgroundColor(itemView.getContext().getColor(R.color.red));
                    break;
                case "medium":
                    tvEventPriority.setText("一般紧急");
                    tvEventPriority.setBackgroundColor(itemView.getContext().getColor(R.color.yellow));
                    break;
                case "low":
                    tvEventPriority.setText("不紧急");
                    tvEventPriority.setBackgroundColor(itemView.getContext().getColor(R.color.green));
                    break;
            }
        }
    }
}