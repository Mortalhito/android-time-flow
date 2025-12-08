package com.example.timeflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timeflow.R;
import com.example.timeflow.entity.CalendarEvent;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<CalendarEvent> eventList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CalendarEvent event);
        void onItemLongClick(CalendarEvent event);
    }

    public EventAdapter(List<CalendarEvent> eventList) {
        this.eventList = eventList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateEvents(List<CalendarEvent> events) {
        this.eventList = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarEvent event = eventList.get(position);

        holder.tvEventTitle.setText(event.getTitle());
        holder.tvEventTime.setText(event.getTime());
        holder.viewPriority.setBackgroundResource(event.getPriorityColor());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(event);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(event);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View viewPriority;
        TextView tvEventTitle, tvEventTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPriority = itemView.findViewById(R.id.viewPriority);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
        }
    }
}