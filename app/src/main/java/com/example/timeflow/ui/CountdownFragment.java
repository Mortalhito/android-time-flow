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
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
    private Calendar selectedCalendar; // 新增：用于存储选择的日期
    private DatabaseHelper databaseHelper;
    private List<Category> categoryList;
    public CountdownFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_countdown, container, false);

        // 初始化数据库
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
        selectedCalendar = Calendar.getInstance(); // 初始化日历
    }



    private void initData() {
        // 从数据库加载分类
        categoryList = databaseHelper.getAllCategories();

        // 从数据库加载事件
        eventList = databaseHelper.getAllEvents();

        // 如果没有默认数据，添加示例数据
        if (eventList.isEmpty()) {
            addSampleData();
            // 重新加载数据
            eventList = databaseHelper.getAllEvents();
        }

        // 确保事件数据正确计算天数
        for (CountdownEvent event : eventList) {
            event.calculateDaysLeft();
        }

        eventList.sort((event1, event2) -> {
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
        });
    }

    private void addSampleData() {
        // 获取默认分类ID（假设第一个是生活，第二个是工作）
        List<Category> categories = databaseHelper.getAllCategories();
        if (categories.size() >= 2) {
            int lifeCategoryId = categories.get(0).getId();
            int workCategoryId = categories.get(1).getId();

            // 使用完整的构造函数
            CountdownEvent event1 = new CountdownEvent("生日", lifeCategoryId, "2025-12-25");
            CountdownEvent event2 = new CountdownEvent("项目截止", workCategoryId, "2025-11-30");

            // 设置ID
            event1.setId(UUID.randomUUID().toString());
            event2.setId(UUID.randomUUID().toString());

            databaseHelper.addEvent(event1);
            databaseHelper.addEvent(event2);

            eventList.add(event1);
            eventList.add(event2);
        }
    }

    private void setupRecyclerView() {
        // 修改：传递 Context 给适配器
        adapter = new CountdownAdapter(eventList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 设置点击监听器（如果需要）
        adapter.setOnItemClickListener(new CountdownAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CountdownEvent event) {
                // 处理点击事件
            }

            @Override
            public void onItemLongClick(CountdownEvent event) {
                // 处理长按事件（如删除）
                showDeleteConfirmationDialog(event);
            }
        });
    }

    // 新增方法：显示删除确认对话框
    private void showDeleteConfirmationDialog(CountdownEvent event) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("删除事件")
                .setMessage("确定要删除 \"" + event.getSafeName() + "\" 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    databaseHelper.deleteEvent(event.getId());
                    eventList = databaseHelper.getAllEvents();
                    adapter.updateData(eventList);
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

        // 确保分类列表是最新的
        categoryList = databaseHelper.getAllCategories();
        setupCategorySelection(categoryContainer);

        etEventDate.setOnClickListener(v -> showDatePicker(etEventDate));

        btnManageCategories.setOnClickListener(v -> {
            dialog.dismiss();
            showManageCategoriesDialog();
        });

        btnConfirm.setOnClickListener(v -> {
            String name = etEventName.getText().toString();
            String date = etEventDate.getText().toString();
            int selectedCategoryId = getSelectedCategoryId(categoryContainer);

            if (!name.isEmpty() && !date.isEmpty() && selectedCategoryId != -1) {
                // 创建新事件
                CountdownEvent newEvent = new CountdownEvent(name, selectedCategoryId, date);
                newEvent.setId(UUID.randomUUID().toString());

                // 设置分类信息
                Category selectedCategory = getCategoryById(selectedCategoryId);
                if (selectedCategory != null) {
                    newEvent.setCategoryName(selectedCategory.getName());
                    newEvent.setCategoryColor(selectedCategory.getColor());
                }

                // 保存到数据库
                databaseHelper.addEvent(newEvent);

                // 刷新列表
                eventList = databaseHelper.getAllEvents();
                adapter.updateData(eventList);
                dialog.dismiss();

                Toast.makeText(getContext(), "事件添加成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "请填写完整信息并选择分类", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }

    // 新增方法：根据ID获取分类
    private Category getCategoryById(int categoryId) {
        for (Category category : categoryList) {
            if (category.getId() == categoryId) {
                return category;
            }
        }
        return null;
    }

    private void setupCategorySelection(LinearLayout container) {
        container.removeAllViews();

        // 改为垂直布局参数
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16); // 设置底部间距

        for (Category category : categoryList) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(category.getName());
            radioButton.setTag(category.getId());
            radioButton.setTextColor(Color.WHITE);
            radioButton.setButtonTintList(ColorStateList.valueOf(Color.WHITE));

            // 设置背景颜色
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(16);
            drawable.setColor(category.getColor());
            radioButton.setBackground(drawable);

            int padding = getResources().getDimensionPixelSize(R.dimen.category_padding);
            radioButton.setPadding(padding, padding/2, padding, padding/2);

            // 设置宽度为匹配父容器
            radioButton.setLayoutParams(params);
            container.addView(radioButton);
        }

        // 默认选择第一个
        if (container.getChildCount() > 0) {
            ((RadioButton) container.getChildAt(0)).setChecked(true);
        }
    }

    private int getSelectedCategoryId(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) child;
                if (radioButton.isChecked()) {
                    return (int) radioButton.getTag();
                }
            }
        }
        return -1;
    }

    // 新增方法：显示日期选择器
    private void showDatePicker(TextInputEditText etEventDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // 格式化日期为 yyyy-MM-dd
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

    private void showManageCategoriesDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_manage_categories, null);

        RecyclerView categoryRecyclerView = dialogView.findViewById(R.id.categoryRecyclerView);
        Button btnAddCategory = dialogView.findViewById(R.id.btnAddCategory);

        // 确保获取最新的分类列表
        categoryList = databaseHelper.getAllCategories();

        CategoryAdapter categoryAdapter = new CategoryAdapter(categoryList);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryRecyclerView.setAdapter(categoryAdapter);

        btnAddCategory.setOnClickListener(v -> {
            showAddCategoryDialog(dialog);
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void showAddCategoryDialog(BottomSheetDialog parentDialog) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);

        TextInputEditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        RecyclerView colorRecyclerView = dialogView.findViewById(R.id.colorRecyclerView);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        List<Integer> colorList = Arrays.asList(
                0xFF42A5F5, 0xFF66BB6A, 0xFFFFA726, 0xFFEF5350,
                0xFFAB47BC, 0xFF5C6BC0, 0xFF26C6DA, 0xFFD4E157
        );

        ColorAdapter colorAdapter = new ColorAdapter(colorList);
        colorRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        colorRecyclerView.setAdapter(colorAdapter);

        btnConfirm.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString();
            int selectedColor = colorAdapter.getSelectedColor();

            if (!name.isEmpty() && selectedColor != -1) {
                // 保存新分类到数据库
                long newCategoryId = databaseHelper.addCategory(name, selectedColor);

                if (newCategoryId != -1) {
                    // 更新分类列表
                    categoryList = databaseHelper.getAllCategories();

                    // 刷新所有相关的适配器
                    if (parentDialog != null) {
                        parentDialog.dismiss();
                    }
                    dialog.dismiss();

                    Toast.makeText(getContext(), "分类添加成功", Toast.LENGTH_SHORT).show();

                    // 刷新添加事件对话框中的分类列表
                    refreshCategoryData();
                } else {
                    Toast.makeText(getContext(), "分类添加失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "请填写分类名称并选择颜色", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }

    // 刷新分类数据
    private void refreshCategoryData() {
        categoryList = databaseHelper.getAllCategories();
        // 这里可以添加其他需要刷新的逻辑
    }
}