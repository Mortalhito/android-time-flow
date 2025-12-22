package com.example.timeflow.adapter;

import android.app.AlertDialog;
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
import com.example.timeflow.database.DatabaseHelper;
import com.example.timeflow.entity.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<Category> categoryList;
    private DatabaseHelper databaseHelper;
    private Context context;

    public CategoryAdapter(List<Category> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
    }

    // 添加 updateData 方法
    public void updateData(List<Category> newCategoryList) {
        this.categoryList.clear();
        this.categoryList.addAll(newCategoryList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.tvCategoryName.setText(category.getName());
        holder.tvCategoryColor.setBackgroundColor(category.getColor());

        // 默认分类不能删除
        if (category.isDefault()) {
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                // 检查分类是否被使用
                checkCategoryUsageBeforeDelete(category, position);
            });
        }
    }

    /**
     * 检查分类是否被纪念日使用
     */
    private void checkCategoryUsageBeforeDelete(Category category, int position) {
        databaseHelper = new DatabaseHelper(context);

        // 查询是否有纪念日使用了该分类
        boolean isCategoryInUse = databaseHelper.isCategoryUsedByEvents(category.getId());

        if (isCategoryInUse) {
            // 如果分类被使用，弹出提示
            showCategoryInUseDialog(category);
        } else {
            // 如果分类未被使用，弹出确认删除对话框
            showDeleteConfirmationDialog(category, position);
        }
    }

    /**
     * 显示分类被使用的提示对话框
     */
    private void showCategoryInUseDialog(Category category) {
        new AlertDialog.Builder(context)
                .setTitle("无法删除")
                .setMessage("分类 \"" + category.getName() + "\" 正在被纪念日使用，无法删除。\n\n请先修改或删除使用该分类的纪念日。")
                .setPositiveButton("确定", null)
                .show();
    }

    /**
     * 显示确认删除对话框
     */
    private void showDeleteConfirmationDialog(Category category, int position) {
        new AlertDialog.Builder(context)
                .setTitle("确认删除")
                .setMessage("确定要删除分类 \"" + category.getName() + "\" 吗？此操作不可恢复。")
                .setPositiveButton("删除", (dialog, which) -> {
                    // 执行删除操作
                    deleteCategory(category, position);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 执行删除分类操作
     */
    private void deleteCategory(Category category, int position) {
        databaseHelper = new DatabaseHelper(context);
        boolean success = databaseHelper.deleteCategory(category.getId());

        if (success) {
            categoryList.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "分类删除成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "删除失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

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