package com.example.timeflow.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.R;
import com.example.timeflow.adapter.CountdownAdapter;
import com.example.timeflow.entity.CountdownEvent;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CountdownFragment extends Fragment {

    private RecyclerView recyclerView;
    private CountdownAdapter adapter;
    private List<CountdownEvent> eventList;
    private Button btnAddEvent;
    private MaterialButtonToggleGroup categoryToggle;

    public CountdownFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_countdown, container, false);

        initViews(view);
        initData();
        setupRecyclerView();
        setClickListeners();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        btnAddEvent = view.findViewById(R.id.btnAddEvent);
    }

    private void initData() {
        eventList = new ArrayList<>();
        // 示例数据
        eventList.add(new CountdownEvent("生日", "生活", "2025-12-25"));
        eventList.add(new CountdownEvent("项目截止", "工作", "2025-11-30"));
    }

    private void setupRecyclerView() {
        adapter = new CountdownAdapter(eventList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setClickListeners() {
        btnAddEvent.setOnClickListener(v -> showAddEventDialog());
    }

    private void showAddEventDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_countdown, null);

        TextInputEditText etEventName = dialogView.findViewById(R.id.etEventName);
        TextInputEditText etEventDate = dialogView.findViewById(R.id.etEventDate);
        categoryToggle = dialogView.findViewById(R.id.categoryToggle);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(v -> {
            String name = etEventName.getText().toString();
            String date = etEventDate.getText().toString();
            String category = getSelectedCategory();

            if (!name.isEmpty() && !date.isEmpty()) {
                CountdownEvent newEvent = new CountdownEvent(name, category, date);
                eventList.add(newEvent);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private String getSelectedCategory() {
        int checkedId = categoryToggle.getCheckedButtonId();
        if (checkedId == R.id.btnLife) return "生活";
        if (checkedId == R.id.btnWork) return "工作";
        if (checkedId == R.id.btnStudy) return "考证";
        return "其他";
    }
}