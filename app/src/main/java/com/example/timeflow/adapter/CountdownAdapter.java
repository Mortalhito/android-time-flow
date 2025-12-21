package com.example.timeflow.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.entity.CountdownEvent;
import com.example.timeflow.ui.CountdownDetailActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CountdownAdapter extends RecyclerView.Adapter<CountdownAdapter.ViewHolder> {

    private List<CountdownEvent> eventList;
    private OnItemClickListener listener;
    private Context context;
    public interface OnItemClickListener {
        void onItemClick(CountdownEvent event);
        void onItemLongClick(CountdownEvent event);
    }

    public CountdownAdapter(List<CountdownEvent> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<CountdownEvent> newList) {
        if (newList == null) return;

        // 重新计算每个事件的天数
        for (CountdownEvent event : newList) {
            event.getDaysLeft(); // 这会触发重新计算
        }

        Collections.sort(newList, new Comparator<CountdownEvent>() {
            @Override
            public int compare(CountdownEvent event1, CountdownEvent event2) {
                // 未来事件优先于过去事件
                if (!event1.isPast() && event2.isPast()) {
                    return -1;
                }
                if (event1.isPast() && !event2.isPast()) {
                    return 1;
                }

                // 相同类型的事件比较
                if (!event1.isPast()) {
                    // 都是未来事件：天数少的排在前面
                    return event1.getDaysLeft() - event2.getDaysLeft();
                } else {
                    // 都是过去事件：天数绝对值大的排在后面（离现在更远的排在后面）
                    return Math.abs(event1.getDaysLeft()) - Math.abs(event2.getDaysLeft());
                }
            }
        });

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

        holder.tvEventName.setText(event.getSafeName());
        holder.tvCategory.setText(event.getCategoryName());
        holder.tvDaysLeft.setText(event.getDisplayText());
        holder.tvTargetDate.setText(event.getTargetDate());

        // 设置分类标签颜色
        holder.tvCategory.setBackgroundColor(event.getCategoryColor());
        holder.tvCategory.setTextColor(Color.WHITE);

        // 根据事件是否已过去设置不同的背景颜色
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setCornerRadius(16); // 设置圆角

        if (event.isPast()) {
            // 已过去的事件 - 橙黄色背景
            backgroundDrawable.setColor(0xFFFFF3E0); // 浅橙黄背景
            holder.tvDaysLeft.setTextColor(0xFFFF9800); // 橙黄色文字
        } else {
            // 将来的事件 - 蓝色背景
            backgroundDrawable.setColor(0xFFE3F2FD); // 浅蓝色背景
            holder.tvDaysLeft.setTextColor(0xFF2196F3); // 蓝色文字
        }

        holder.itemView.setBackground(backgroundDrawable);

        // 修复：使用正确的 Context 获取资源
        int padding = holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.item_padding);
        holder.itemView.setPadding(padding, padding, padding, padding);

// 在onBindViewHolder方法中修改点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(event);
            }

            // 添加跳转到详情页的代码
            Intent intent = new Intent(context, CountdownDetailActivity.class);
            intent.putExtra("event_id", event.getId());
            context.startActivity(intent);
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