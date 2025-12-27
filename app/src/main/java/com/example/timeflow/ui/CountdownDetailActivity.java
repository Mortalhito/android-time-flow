package com.example.timeflow.ui;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.timeflow.R;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.Category;
import com.example.timeflow.room.entity.CountdownEvent;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

public class CountdownDetailActivity extends AppCompatActivity {

    // 严格对应 activity_countdown_detail.xml 中的 ID
    private TextView tvEventTitle, tvTimePrefix, tvDaysDisplay, tvDisplayUnit, tvTargetDateFull;
    private View section1;
    private CountdownEvent currentEvent;
    private AppDatabase db;
    private ImageButton btnBack, btnEdit; // 新增按钮引用
    private enum DisplayMode { DAYS, YEARS_MONTHS_DAYS, MONTHS_DAYS, WEEKS_DAYS }
    private DisplayMode currentDisplayMode = DisplayMode.DAYS;
    private View categoryColorView;
    private TextView tvCategoryName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_countdown_detail);
        db = AppDatabase.getInstance(this);

        // --- 绑定 ID ---
        initViews();

        // --- 设置点击事件 ---
        setupClickListeners();

        initData();
        System.out.println(1);
        // 显示分类名称和颜色

    }

    private void initData() {
        String id = getIntent().getStringExtra("event_id");
        if (id == null) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            CountdownEvent event = db.eventDao().getEventById(id);
            if (event != null) {
                event.calculateDaysLeft();
                runOnUiThread(() -> {
                    currentEvent = event;
                    updateDisplay();

                    if (currentEvent == null) {
                        Toast.makeText(this, "事件不存在", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // 【新增功能：显示倒数本名称和颜色】
                    if (currentEvent.getCategoryName() != null && !currentEvent.getCategoryName().isEmpty()) {
                        tvCategoryName.setText("所属倒数本：" + currentEvent.getCategoryName());

                        // 设置颜色块（方形带小圆角）
                        GradientDrawable shape = new GradientDrawable();
                        shape.setShape(GradientDrawable.RECTANGLE);
                        shape.setCornerRadius(4f);  // 小圆角
                        shape.setColor(currentEvent.getCategoryColor());
                        categoryColorView.setBackground(shape);
                    } else {
                        tvCategoryName.setText("所属倒数本：未知");
                        categoryColorView.setVisibility(View.GONE);  // 隐藏颜色块
                    }
                });
            }

        });


    }

    private void updateDisplay() {
        if (currentEvent == null) return;
        tvEventTitle.setText(currentEvent.getName());
        tvTimePrefix.setText(currentEvent.isPast() ? "已经" : "还有");
        tvTargetDateFull.setText("目标日：" + currentEvent.getTargetDate());

        int totalDays = Math.abs(currentEvent.getDaysLeft());

        switch (currentDisplayMode) {
            case DAYS:
                tvDaysDisplay.setText(String.valueOf(totalDays));
                tvDisplayUnit.setText("天");
                break;
            case YEARS_MONTHS_DAYS:
                int years = totalDays / 365;
                int remDays = totalDays % 365;
                int months = remDays / 30;
                int days = remDays % 30;
                tvDaysDisplay.setText(years > 0 ?
                        String.format("%d年%d个月%d天", years, months, days) :
                        String.format("%d个月%d天", months, days));
                tvDisplayUnit.setText("");
                break;
            case WEEKS_DAYS:
                tvDaysDisplay.setText(String.format("%d周%d天", totalDays / 7, totalDays % 7));
                tvDisplayUnit.setText("");
                break;
            default:
                tvDaysDisplay.setText(String.valueOf(totalDays));
                tvDisplayUnit.setText("天");
                break;
        }
    }

    private void switchDisplayMode() {
        DisplayMode[] modes = DisplayMode.values();
        currentDisplayMode = modes[(currentDisplayMode.ordinal() + 1) % modes.length];
        updateDisplay();
    }

    private void initViews() {
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvTimePrefix = findViewById(R.id.tvTimePrefix);
        tvDaysDisplay = findViewById(R.id.tvDaysDisplay);
        tvDisplayUnit = findViewById(R.id.tvDisplayUnit);
        tvTargetDateFull = findViewById(R.id.tvTargetDateFull);
        section1 = findViewById(R.id.section1);

        // 绑定新添加的两个按钮
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        categoryColorView = findViewById(R.id.categoryColorView);
        tvCategoryName = findViewById(R.id.tvCategoryName);
    }

    private void setupClickListeners() {
        // 返回按钮：直接关闭当前页面
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 编辑按钮：弹出提示（后续可改为跳转编辑页）
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> showEditDialog()); // 点击弹出编辑
        }

        // 中间数字区域点击切换显示模式
        findViewById(R.id.section2).setOnClickListener(v -> switchDisplayMode());
    }

    private void showEditDialog() {
        if (currentEvent == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_countdown, null);

        // 1. 获取控件
        TextInputEditText etName = view.findViewById(R.id.etEventName);
        TextInputEditText etDate = view.findViewById(R.id.etEventDate);
        LinearLayout categoryContainer = view.findViewById(R.id.categoryContainer);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        // 2. 填充当前数据
        etName.setText(currentEvent.getName());
        etDate.setText(currentEvent.getTargetDate());
        btnConfirm.setText("保存修改");

        // 设置日期选择器
        etDate.setOnClickListener(v -> showDatePicker(etDate));

        // 3. 加载并选择当前分类（使用 RadioGroup 确保单选）
        final int[] selectedCategoryId = {currentEvent.getCategoryId()};
        loadCategoriesForEdit(categoryContainer, selectedCategoryId);

        // 4. 保存按钮逻辑
        btnConfirm.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newDate = etDate.getText().toString().trim();

            if (newName.isEmpty() || newDate.isEmpty() || selectedCategoryId[0] == -1) {  // 添加检查是否选中分类
                Toast.makeText(this, "请填写完整信息并选择一个分类", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新对象
            currentEvent.setName(newName);
            currentEvent.setTargetDate(newDate);
            currentEvent.setCategoryId(selectedCategoryId[0]);

            // 写入数据库
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.eventDao().update(currentEvent);
                runOnUiThread(() -> {
                    dialog.dismiss();
                    initData(); // 重新加载页面数据
                    Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
                });
            });
        });

        dialog.setContentView(view);
        dialog.show();
    }

    // 辅助：加载分类并高亮选中项（使用 RadioGroup 确保单选）
    private void loadCategoriesForEdit(LinearLayout container, int[] selectedId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Category> categories = db.categoryDao().getAllCategories();
            runOnUiThread(() -> {
                container.removeAllViews();

                RadioGroup radioGroup = new RadioGroup(this);
                radioGroup.setOrientation(RadioGroup.VERTICAL);
                container.addView(radioGroup);

                int currentId = selectedId[0];

                for (Category cat : categories) {
                    View itemView = LayoutInflater.from(this).inflate(R.layout.item_category_select_radio, radioGroup, false);

                    RadioButton rb = itemView.findViewById(R.id.radioButton);
                    View colorView = itemView.findViewById(R.id.tvCategoryColor);
                    TextView tvName = itemView.findViewById(R.id.tvCategoryName);

                    tvName.setText(cat.getName());

                    // 设置颜色块
                    GradientDrawable drawable = (GradientDrawable) colorView.getBackground().mutate();
                    drawable.setColor(cat.getColor());
                    colorView.setBackground(drawable);

                    // 默认选中当前分类
                    rb.setChecked(cat.getId() == currentId);

                    // 点击整行：手动实现单选互斥
                    itemView.setOnClickListener(v -> {
                        // 遍历所有子项，取消其他选中
                        for (int i = 0; i < radioGroup.getChildCount(); i++) {
                            View child = radioGroup.getChildAt(i);
                            RadioButton childRb = child.findViewById(R.id.radioButton);
                            if (childRb != null) {
                                childRb.setChecked(false);
                            }
                        }
                        // 设置当前选中
                        rb.setChecked(true);
                        selectedId[0] = cat.getId();
                    });

                    radioGroup.addView(itemView);
                }
            });
        });
    }

    // 辅助：日期选择
    private void showDatePicker(TextInputEditText et) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(this, (view, y, m, d) -> {
            String date = String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d);
            et.setText(date);
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }
}