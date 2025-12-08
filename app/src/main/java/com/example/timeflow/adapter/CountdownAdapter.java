package com.example.timeflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timeflow.R;
import com.example.timeflow.entity.CountdownEvent;
import java.util.List;

public class CountdownAdapter extends RecyclerView.Adapter<CountdownAdapter.ViewHolder> {

    private List<CountdownEvent> eventList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CountdownEvent event);
        void onItemLongClick(CountdownEvent event);
    }

    public CountdownAdapter(List<CountdownEvent> eventList) {
        this.eventList = eventList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<CountdownEvent> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_countdown, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CountdownEvent event = eventList.get(position);

        holder.tvEventName.setText(event.getName());
        holder.tvCategory.setText(event.getCategory());
        holder.tvDaysLeft.setText(event.getDaysLeft() + "天");
        holder.tvTargetDate.setText(event.getTargetDate());

        // 根据分类设置不同的颜色
        int categoryColor = getCategoryColor(event.getCategory());
        holder.tvCategory.setBackgroundColor(holder.itemView.getContext().getColor(categoryColor));

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

    private int getCategoryColor(String category) {
        switch (category) {
            case "生活": return R.color.category_life;
            case "工作": return R.color.category_work;
            case "考证": return R.color.category_study;
            default: return R.color.category_other;
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvCategory, tvDaysLeft, tvTargetDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDaysLeft = itemView.findViewById(R.id.tvDaysLeft);
            tvTargetDate = itemView.findViewById(R.id.tvTargetDate);
        }
    }
}