package com.example.timeflow.ui;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
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

        adapter.setOnItemClickListener(new CountdownAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CountdownEvent event) {
                // 已有点击逻辑（跳转详情），保持不变
            }

            @Override
            public void onItemLongClick(CountdownEvent event) {
                // 新增：弹出确认删除对话框
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("删除倒数日")
                        .setMessage("确定删除 \"" + event.getName() + "\" 吗？")
                        .setPositiveButton("删除", (d, w) -> {
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                db.eventDao().deleteById(event.getId());
                                requireActivity().runOnUiThread(() -> {
                                    eventList.remove(event);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                });
                            });
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
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
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_countdown, null);

        TextInputEditText etName = view.findViewById(R.id.etEventName);
        TextInputEditText etDate = view.findViewById(R.id.etEventDate);
        LinearLayout categoryContainer = view.findViewById(R.id.categoryContainer);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        View btnManageCategories = view.findViewById(R.id.btnManageCategories);

        etDate.setOnClickListener(v -> showDatePicker(etDate));

        // 用于记录用户选中的分类 ID，初始为 -1（未选中）
        final int[] selectedCategoryId = {-1};

        // 创建 RadioGroup 作为容器
        categoryContainer.removeAllViews();
        RadioGroup radioGroup = new RadioGroup(getContext());
        radioGroup.setOrientation(RadioGroup.VERTICAL);
        categoryContainer.addView(radioGroup);

        // 加载所有分类
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Category> categories = db.categoryDao().getAllCategories();
            requireActivity().runOnUiThread(() -> {
                radioGroup.removeAllViews();

                if (categories.isEmpty()) {
                    TextView tvEmpty = new TextView(getContext());
                    tvEmpty.setText("暂无分类，请先添加");
                    tvEmpty.setPadding(0, 40, 0, 40);
                    tvEmpty.setGravity(android.view.Gravity.CENTER);
                    tvEmpty.setTextColor(Color.GRAY);
                    radioGroup.addView(tvEmpty);
                    btnConfirm.setEnabled(false); // 没有分类就禁用确定按钮
                    return;
                }

                for (Category category : categories) {
                    View itemView = getLayoutInflater().inflate(R.layout.item_category_select_radio, radioGroup, false);

                    RadioButton rb = itemView.findViewById(R.id.radioButton);
                    View colorView = itemView.findViewById(R.id.tvCategoryColor);
                    TextView tvName = itemView.findViewById(R.id.tvCategoryName);

                    tvName.setText(category.getName());

                    // 设置颜色块（方形带小圆角）
                    GradientDrawable drawable = (GradientDrawable) colorView.getBackground().mutate();
                    drawable.setColor(category.getColor());
                    colorView.setBackground(drawable);

                    // 点击整行实现手动单选
                    itemView.setOnClickListener(v -> {
                        // 先取消所有其他选中
                        for (int i = 0; i < radioGroup.getChildCount(); i++) {
                            View child = radioGroup.getChildAt(i);
                            RadioButton childRb = child.findViewById(R.id.radioButton);
                            if (childRb != null) {
                                childRb.setChecked(false);
                            }
                        }
                        // 选中当前
                        rb.setChecked(true);
                        selectedCategoryId[0] = category.getId();
                    });

                    radioGroup.addView(itemView);
                }
            });
        });

        // 管理分类按钮
        btnManageCategories.setOnClickListener(v -> {
            dialog.dismiss();
            showManageCategoriesDialog();
        });

        // 确定按钮：保存新事件
        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "请输入事件名称", Toast.LENGTH_SHORT).show();
                return;
            }
            if (date.isEmpty()) {
                Toast.makeText(getContext(), "请选择日期", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedCategoryId[0] == -1) {
                Toast.makeText(getContext(), "请选择一个分类", Toast.LENGTH_SHORT).show();
                return;
            }

            CountdownEvent newEvent = new CountdownEvent();
            newEvent.setName(name);
            newEvent.setTargetDate(date);
            newEvent.setCategoryId(selectedCategoryId[0]);

            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.eventDao().insert(newEvent);
                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    initData(); // 刷新列表
                    Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();
                });
            });
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

    @Override
    public void onResume() {
        super.onResume();
        initData(); // 每次返回 Fragment 时刷新事件和分类
    }

    private void showAddCategoryDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);

        TextInputEditText etName = view.findViewById(R.id.etCategoryName);
        View colorPreview = view.findViewById(R.id.colorPickerPreview);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        final int[] selectedColor = {ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light)};

        // 更新预览圆圈
        updateColorCircle(colorPreview, selectedColor[0]);

        // 点击圆圈打开颜色选择器
        colorPreview.setOnClickListener(v -> new AmbilWarnaDialog(getContext(), selectedColor[0], new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {}

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                selectedColor[0] = color;
                updateColorCircle(colorPreview, color);
            }
        }).show());

        // 【新增标志】：是否是通过“确认”按钮成功添加
        final boolean[] isSuccessAdd = {false};

        // 确认按钮逻辑
        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "请输入名称", Toast.LENGTH_SHORT).show();
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                Category existing = db.categoryDao().getByName(name);
                if (existing != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "已存在同名倒数本，添加失败", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    Category category = new Category(name, selectedColor[0]);
                    db.categoryDao().insert(category);

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();
                        initData();
                        isSuccessAdd[0] = true;  // 【标记：这是成功添加】
                        dialog.dismiss();       // 触发 onDismissListener，但会被下面的判断跳过

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            showAddEventDialog();  // 成功后直接回到添加倒数日（新对话框）
                        }, 300);
                    });
                }
            });
        });

        // 【关键：onDismissListener 判断是否成功添加】
        dialog.setOnDismissListener(d -> {
            if (isSuccessAdd[0]) {
                // 如果是成功添加导致的 dismiss，什么都不做（已经手动打开了添加倒数日）
                return;
            }

            // 否则是用户手动取消（点空白、返回键），回到管理界面
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                showManageCategoriesDialog();
            }, 300);
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