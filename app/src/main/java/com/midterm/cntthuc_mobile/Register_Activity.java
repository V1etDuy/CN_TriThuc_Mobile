package com.midterm.cntthuc_mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.midterm.cntthuc_mobile.api_service.ApiClient;
import com.midterm.cntthuc_mobile.api_service.ApiService;
import com.midterm.cntthuc_mobile.auth.SignUpRequest;
import com.midterm.cntthuc_mobile.auth.SignUpResponse;
import retrofit2.Callback;
import retrofit2.Call;
import retrofit2.Response;

public class Register_Activity extends AppCompatActivity {
    Button signUpBtn;
    EditText etUsername, etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signUpBtn = findViewById(R.id.btnSignup);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        signUpBtn.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo request body
            SignUpRequest request = new SignUpRequest(username, email, password);

            // Gọi API
            Call<SignUpResponse> call = apiService.signUp(request);
            call.enqueue(new Callback<SignUpResponse>() {
                @Override
                public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        SignUpResponse res = response.body();
                        // 👉 Lưu token vào SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                        prefs.edit()
                                .putString("token", res.getToken())
                                .apply();

                        String info = "Đăng ký thành công!\n"
                                + "User: " + res.getUser().getUsername() + "\n"
                                + "Email: " + res.getUser().getEmail() + "\n"
                                + "Token: " + res.getToken();

                        Toast.makeText(Register_Activity.this, info, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(Register_Activity.this,
                                "Lỗi server: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SignUpResponse> call, Throwable t) {
                    Toast.makeText(Register_Activity.this,
                            "Kết nối thất bại: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        });

    }
}