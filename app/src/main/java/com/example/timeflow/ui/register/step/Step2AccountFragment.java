package com.example.timeflow.ui.register.step;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.timeflow.R;
import com.example.timeflow.viewmodel.RegisterViewModel;

public class Step2AccountFragment extends Fragment {

    private RegisterViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_step2, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(RegisterViewModel.class);

        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etConfirm = view.findViewById(R.id.etConfirm);
        Button btnNext = view.findViewById(R.id.btnNext);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // 填充已输入的数据
        if (!viewModel.username.isEmpty()) {
            etUsername.setText(viewModel.username);
        }

        // 返回按钮
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        btnNext.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();
            String confirm = etConfirm.getText().toString();

            if (username.isEmpty()) {
                etUsername.setError("请输入用户名");
                return;
            }
            if (username.length() < 3) {
                etUsername.setError("用户名至少3个字符");
                return;
            }
            if (password.length() < 6) {
                etPassword.setError("密码至少6位");
                return;
            }
            if (!password.equals(confirm)) {
                etConfirm.setError("两次密码不一致");
                return;
            }

            // 保存数据到ViewModel
            viewModel.username = username;
            viewModel.password = password;

            goToStep3();
        });

        return view;
    }

    private void goToStep3() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_out_left, R.anim.slide_out_left)
                .replace(R.id.registerContainer, new Step3ProfileFragment())
                .addToBackStack(null)
                .commit();
    }
}