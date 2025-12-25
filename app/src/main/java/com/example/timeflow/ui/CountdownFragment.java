package com.example.timeflow.ui;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.adapter.CategoryAdapter;
import com.example.timeflow.adapter.CountdownAdapter;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.Category;
import com.example.timeflow.room.entity.CountdownEvent;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;

public class CountdownFragment extends Fragment {

    private RecyclerView recyclerView;
    private CountdownAdapter adapter;
    private List<CountdownEvent> eventList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private AppDatabase db;
    private Calendar selectedCalendar = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 布局文件: fragment_countdown.xml
        View view = inflater.inflate(R.layout.fragment_countdown, container, false);
        db = AppDatabase.getInstance(requireContext());

        // ID 对齐: recyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CountdownAdapter(eventList, getContext());
        recyclerView.setAdapter(adapter);

        // ID 对齐: btnAddEvent
        view.findViewById(R.id.btnAddEvent).setOnClickListener(v -> showAddEventDialog());

        initData();
        return view;
    }

    private void initData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Category> categories = db.categoryDao().getAllCategories();
            List<CountdownEvent> events = db.eventDao().getAllEventsWithCategory();
            for (CountdownEvent event : events) { event.calculateDaysLeft(); }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    categoryList.clear();
                    categoryList.addAll(categories);
                    eventList.clear();
                    eventList.addAll(events);
                    Collections.sort(eventList, (e1, e2) -> Integer.compare(e1.getDaysLeft(), e2.getDaysLeft()));
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void showAddEventDialog() {
        // 使用你的布局文件 dialog_add_countdown.xml
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_countdown, null);

        TextInputEditText etName = view.findViewById(R.id.etEventName);
        TextInputEditText etDate = view.findViewById(R.id.etEventDate);
        LinearLayout categoryContainer = view.findViewById(R.id.categoryContainer);

        etDate.setOnClickListener(v -> showDatePicker(etDate));

        // 1. 实现单选：清除容器并添加一个 RadioGroup
        categoryContainer.removeAllViews();
        RadioGroup radioGroup = new RadioGroup(getContext());
        radioGroup.setOrientation(RadioGroup.VERTICAL);
        categoryContainer.addView(radioGroup);

        // 动态生成单选按钮
        for (Category category : categoryList) {
            RadioButton rb = new RadioButton(getContext());
            rb.setText(category.getName());
            rb.setTag(category.getId()); // 将分类ID存在Tag里
            rb.setId(View.generateViewId()); // 必须生成ID，RadioGroup才能控制互斥
            radioGroup.addView(rb);
        }

        // ID 对齐：btnConfirm
        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String name = etName.getText().toString();
            String date = etDate.getText().toString();

            // 获取选中的 RadioButton
            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (!name.isEmpty() && !date.isEmpty() && checkedId != -1) {
                RadioButton selectedRb = radioGroup.findViewById(checkedId);
                int catId = (int) selectedRb.getTag();

                AppDatabase.databaseWriteExecutor.execute(() -> {
                    // 插入数据库
                    db.eventDao().insert(new CountdownEvent(name, catId, date));
                    // 刷新主页列表
                    initData();
                });
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "请填写完整并选择一个分类", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnManageCategories).setOnClickListener(v -> {
            dialog.dismiss();
            showManageCategoriesDialog();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showManageCategoriesDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_manage_categories, null);

        RecyclerView rv = view.findViewById(R.id.categoryRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // 假设你已经初始化了 categoryList
        CategoryAdapter categoryAdapter = new CategoryAdapter(categoryList, getContext());
        rv.setAdapter(categoryAdapter);

        // 绑定“添加新倒数本”按钮的点击事件
        view.findViewById(R.id.btnAddCategory).setOnClickListener(v -> {
            // 先关闭当前管理弹窗，再打开添加弹窗
            dialog.dismiss();
            showAddCategoryDialog();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private int selectedColor = 0xFF42A5F5; // 成员变量记录颜色

    private void showAddCategoryDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);

        TextInputEditText etName = view.findViewById(R.id.etCategoryName);
        View colorPickerPreview = view.findViewById(R.id.colorPickerPreview); // 对应新ID

        // 1. 初始化预览块样式（做成圆形）
        updateColorCircle(colorPickerPreview, selectedColor);

        // 2. 点击预览块弹出“光谱选择器”
        colorPickerPreview.setOnClickListener(v -> {
            // 参数：Context, 初始颜色, 监听器
            AmbilWarnaDialog colorPickerDialog = new AmbilWarnaDialog(getContext(), selectedColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // 取消不处理
                }

                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    // 重点：用户选完光谱颜色后更新变量和界面
                    selectedColor = color;
                    updateColorCircle(colorPickerPreview, selectedColor);
                }
            });
            colorPickerDialog.show();
        });

        // 3. 确定按钮
        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    db.categoryDao().insert(new Category(0, name, selectedColor));
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            initData(); // 刷新分类列表
                            dialog.dismiss();
                        });
                    }
                });
            } else {
                Toast.makeText(getContext(), "请输入名称", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }

    /**
     * 将 View 变成美观的彩色圆圈
     */
    private void updateColorCircle(View view, int color) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.OVAL); // 圆形
        shape.setColor(color);
        shape.setStroke(4, Color.LTGRAY); // 增加边框感
        view.setBackground(shape);
    }

    private void showDatePicker(TextInputEditText et) {
        new DatePickerDialog(getContext(), (v, y, m, d) -> {
            selectedCalendar.set(y, m, d);
            et.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.getTime()));
        }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}