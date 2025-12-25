package com.example.timeflow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<Category> categoryList;
    private Context context;

    public CategoryAdapter(List<Category> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 布局文件: item_category.xml
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);

        // ID 对齐: tvCategoryName
        holder.tvCategoryName.setText(category.getName());

        // 动态设置带圆角的背景色
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(15f); // 圆角大小
        shape.setColor(category.getColor()); // 从数据库读取的颜色

        // ID 对齐: tvCategoryColor
        holder.tvCategoryColor.setBackground(shape);

        // ID 对齐: btnDelete (如果分类不是默认的则显示删除)
        holder.btnDelete.setVisibility(category.isDefault() ? View.GONE : View.VISIBLE);
        holder.btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("删除分类")
                    .setMessage("确定删除 \"" + category.getName() + "\" 吗？")
                    .setPositiveButton("删除", (d, w) -> deleteCategory(category, position))
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void deleteCategory(Category category, int position) {
        AppDatabase db = AppDatabase.getInstance(context);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = db.eventDao().getEventCountByCategoryId(category.getId());
            if (count > 0) {
                ((android.app.Activity)context).runOnUiThread(() ->
                        Toast.makeText(context, "该分类下有事件，无法删除", Toast.LENGTH_SHORT).show());
            } else {
                db.categoryDao().delete(category);
                ((android.app.Activity)context).runOnUiThread(() -> {
                    categoryList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public int getItemCount() { return categoryList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        View tvCategoryColor;
        ImageButton btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryColor = itemView.findViewById(R.id.tvCategoryColor);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}