// CountdownFragment.java - 完善分类管理功能
package com.example.timeflow.ui;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.adapter.CategoryAdapter;
import com.example.timeflow.adapter.ColorAdapter;
import com.example.timeflow.adapter.CountdownAdapter;
import com.example.timeflow.database.DatabaseHelper;
import com.example.timeflow.entity.Category;
import com.example.timeflow.entity.CountdownEvent;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CountdownFragment extends Fragment {

    private RecyclerView recyclerView;
    private CountdownAdapter adapter;
    private List<CountdownEvent> eventList;
    private Button btnAddEvent;
    private MaterialButtonToggleGroup categoryToggle;
    private Calendar selectedCalendar;
    private DatabaseHelper databaseHelper;
    private List<Category> categoryList;

    // 添加回调接口用于分类数据刷新
    private OnCategoryUpdateListener categoryUpdateListener;

    public interface OnCategoryUpdateListener {
        void onCategoryUpdated();
    }

    public void setOnCategoryUpdateListener(OnCategoryUpdateListener listener) {
        this.categoryUpdateListener = listener;
    }

    public CountdownFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_countdown, container, false);

        databaseHelper = new DatabaseHelper(getContext());
        initViews(view);
        initData();
        setupRecyclerView();
        setClickListeners();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        btnAddEvent = view.findViewById(R.id.btnAddEvent);
        selectedCalendar = Calendar.getInstance();
    }

    private void initData() {
        categoryList = databaseHelper.getAllCategories();
        eventList = databaseHelper.getAllEvents();

        for (CountdownEvent event : eventList) {
            event.calculateDaysLeft();
        }

        sortEventList();
    }

    private void sortEventList() {
        eventList.sort((event1, event2) -> {
            if (!event1.isPast() && event2.isPast()) {
                return -1;
            }
            if (event1.isPast() && !event2.isPast()) {
                return 1;
            }
            if (!event1.isPast()) {
                return event1.getDaysLeft() - event2.getDaysLeft();
            } else {
                return Math.abs(event1.getDaysLeft()) - Math.abs(event2.getDaysLeft());
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new CountdownAdapter(eventList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new CountdownAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CountdownEvent event) {
                // 点击事件处理
            }

            @Override
            public void onItemLongClick(CountdownEvent event) {
                showDeleteConfirmationDialog(event);
            }
        });
    }

    private void showDeleteConfirmationDialog(CountdownEvent event) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("删除事件")
                .setMessage("确定要删除 \"" + event.getSafeName() + "\" 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    databaseHelper.deleteEvent(event.getId());
                    refreshEventData();
                    Toast.makeText(getContext(), "事件已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void setClickListeners() {
        btnAddEvent.setOnClickListener(v -> showAddEventDialog());
    }

    private void showAddEventDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_countdown, null);

        TextInputEditText etEventName = dialogView.findViewById(R.id.etEventName);
        TextInputEditText etEventDate = dialogView.findViewById(R.id.etEventDate);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);
        Button btnManageCategories = dialogView.findViewById(R.id.btnManageCategories);
        LinearLayout categoryContainer = dialogView.findViewById(R.id.categoryContainer);

        // 刷新分类数据
        refreshCategoryData();
        setupCategorySelection(categoryContainer);

        etEventDate.setOnClickListener(v -> showDatePicker(etEventDate));

        btnManageCategories.setOnClickListener(v -> {
            dialog.dismiss();
            showManageCategoriesDialog();
        });

        btnConfirm.setOnClickListener(v -> {
            String name = etEventName.getText().toString().trim();
            String date = etEventDate.getText().toString().trim();
            int selectedCategoryId = getSelectedCategoryId(categoryContainer);

            if (validateInput(name, date, selectedCategoryId)) {
                addNewEvent(name, selectedCategoryId, date);
                dialog.dismiss();
            }
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private boolean validateInput(String name, String date, int categoryId) {
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "请输入事件名称", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (date.isEmpty()) {
            Toast.makeText(getContext(), "请选择日期", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (categoryId == -1) {
            Toast.makeText(getContext(), "请选择分类", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addNewEvent(String name, int categoryId, String date) {
        String eventId = UUID.randomUUID().toString();
        CountdownEvent newEvent = new CountdownEvent(name, categoryId, date);
        newEvent.setId(eventId);

        Category selectedCategory = getCategoryById(categoryId);
        if (selectedCategory != null) {
            newEvent.setCategoryName(selectedCategory.getName());
            newEvent.setCategoryColor(selectedCategory.getColor());
        }

        databaseHelper.addEvent(newEvent);
        refreshEventData();
        Toast.makeText(getContext(), "事件添加成功", Toast.LENGTH_SHORT).show();
    }

    private void refreshEventData() {
        eventList = databaseHelper.getAllEvents();
        for (CountdownEvent event : eventList) {
            event.calculateDaysLeft();
        }
        sortEventList();
        adapter.updateData(eventList);
    }

    // 完善分类管理对话框
    private void showManageCategoriesDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_manage_categories, null);

        RecyclerView categoryRecyclerView = dialogView.findViewById(R.id.categoryRecyclerView);
        Button btnAddCategory = dialogView.findViewById(R.id.btnAddCategory);

        // 确保获取最新的分类列表
        refreshCategoryData();

        // 创建适配器并设置数据刷新回调
        CategoryAdapter categoryAdapter = new CategoryAdapter(categoryList, getContext());
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryRecyclerView.setAdapter(categoryAdapter);

        btnAddCategory.setOnClickListener(v -> {
            showAddCategoryDialog(dialog, categoryAdapter);
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }

    // 完善添加分类对话框
    // 在 CountdownFragment 中修改 showAddCategoryDialog 方法
    private void showAddCategoryDialog(BottomSheetDialog parentDialog, CategoryAdapter categoryAdapter) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);

        TextInputEditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        RecyclerView colorRecyclerView = dialogView.findViewById(R.id.colorRecyclerView);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        List<Integer> colorList = Arrays.asList(
                0xFF42A5F5, 0xFF66BB6A, 0xFFFFA726, 0xFFEF5350,
                0xFFAB47BC, 0xFF5C6BC0, 0xFF26C6DA, 0xFFD4E157,
                0xFF78909C, 0xFF8D6E63, 0xFFEC407A, 0xFF7E57C2
        );

        ColorAdapter colorAdapter = new ColorAdapter(colorList);
        colorRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
        colorRecyclerView.setAdapter(colorAdapter);

        btnConfirm.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            int selectedColor = colorAdapter.getSelectedColor();

            if (validateCategoryInput(name, selectedColor)) {
                addNewCategory(name, selectedColor, parentDialog, dialog, categoryAdapter);
            }
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void addNewCategory(String name, int color, BottomSheetDialog parentDialog,
                                BottomSheetDialog currentDialog, CategoryAdapter categoryAdapter) {
        long newCategoryId = databaseHelper.addCategory(name, color);

        if (newCategoryId != -1) {
            // 刷新分类数据
            refreshCategoryData();

            // 修复：确保 categoryAdapter 不为 null，然后调用 updateData
            if (categoryAdapter != null) {
                categoryAdapter.updateData(categoryList);
            }

            // 如果 categoryAdapter 为 null，重新创建适配器
            if (categoryAdapter == null) {
                // 重新获取 RecyclerView 并设置适配器
                RecyclerView categoryRecyclerView = parentDialog.findViewById(R.id.categoryRecyclerView);
                if (categoryRecyclerView != null) {
                    categoryAdapter = new CategoryAdapter(categoryList, getContext());
                    categoryRecyclerView.setAdapter(categoryAdapter);
                }
            }

            currentDialog.dismiss();
            Toast.makeText(getContext(), "分类添加成功", Toast.LENGTH_SHORT).show();

            // 刷新添加事件对话框中的分类列表
            refreshCategorySelectionInAddEventDialog();
        } else {
            Toast.makeText(getContext(), "分类添加失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 新增方法：刷新添加事件对话框中的分类选择
    private void refreshCategorySelectionInAddEventDialog() {
        // 这里可以添加逻辑来刷新添加事件对话框中的分类列表
        // 例如，如果添加事件对话框是打开的，需要更新它的分类选择器
    }

    private boolean validateCategoryInput(String name, int selectedColor) {
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "请输入分类名称", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedColor == -1) {
            Toast.makeText(getContext(), "请选择颜色", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 检查分类名称是否已存在
        for (Category category : categoryList) {
            if (category.getName().equalsIgnoreCase(name)) {
                Toast.makeText(getContext(), "分类名称已存在", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }



    // 完善分类选择设置
    private void setupCategorySelection(LinearLayout container) {
        container.removeAllViews();

        if (categoryList.isEmpty()) {
            // 如果没有分类，显示提示信息
            TextView hintText = new TextView(getContext());
            hintText.setText("暂无分类，请先添加分类");
            hintText.setTextColor(Color.GRAY);
            hintText.setTextSize(14);
            hintText.setPadding(16, 16, 16, 16);
            container.addView(hintText);
            return;
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);

        for (Category category : categoryList) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(category.getName());
            checkBox.setTag(category.getId());
            checkBox.setTextColor(Color.WHITE);
            checkBox.setButtonTintList(ColorStateList.valueOf(Color.WHITE));

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(16);
            drawable.setColor(category.getColor());
            checkBox.setBackground(drawable);

            int padding = getResources().getDimensionPixelSize(R.dimen.category_padding);
            checkBox.setPadding(padding, padding/2, padding, padding/2);
            checkBox.setLayoutParams(params);

            checkBox.setOnClickListener(v -> {
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof CheckBox && child != v) {
                        ((CheckBox) child).setChecked(false);
                    }
                }
            });

            container.addView(checkBox);
        }
    }

    private int getSelectedCategoryId(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                if (checkBox.isChecked()) {
                    return (int) checkBox.getTag();
                }
            }
        }
        return -1;
    }

    private void showDatePicker(TextInputEditText etEventDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String selectedDate = sdf.format(selectedCalendar.getTime());
                    etEventDate.setText(selectedDate);
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private Category getCategoryById(int categoryId) {
        for (Category category : categoryList) {
            if (category.getId() == categoryId) {
                return category;
            }
        }
        return null;
    }

    // 刷新分类数据
    private void refreshCategoryData() {
        categoryList = databaseHelper.getAllCategories();
    }
}