package com.midterm.cntthuc_mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.midterm.cntthuc_mobile.api_service.ApiClient;
import com.midterm.cntthuc_mobile.api_service.ApiService;
import com.midterm.cntthuc_mobile.api_service.TokenManager;
import com.midterm.cntthuc_mobile.auth.SignInRequest;
import com.midterm.cntthuc_mobile.auth.SignUpRequest;
import com.midterm.cntthuc_mobile.auth.SignUpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login_Activity extends AppCompatActivity {
    Button btnLogin;
    TextView tvSignup, tvError;
    EditText etEmail, etPassword;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1️⃣ ƯU TIÊN CAO NHẤT: Kiểm tra Token trước khi làm bất cứ việc gì về giao diện
        TokenManager tokenManager = TokenManager.getInstance(this);
        if (!tokenManager.isTokenExpired()) {
            // Nếu token còn hạn -> Vào thẳng Chat
            Intent intent = new Intent(this, Chat_Activity.class);
            startActivity(intent);
            finish();
            return; // 🛑 Dừng code tại đây, không chạy xuống dưới nữa
        }

        // 2️⃣ Nếu không có token hoặc hết hạn -> Mới bắt đầu nạp giao diện
        setContentView(R.layout.activity_login);

        // 3️⃣ Khởi tạo các thành phần (Chỉ làm khi cần hiển thị màn hình Login)
        apiService = ApiClient.getClient(this).create(ApiService.class);

        // Ánh xạ View (Phải đặt SAU setContentView)
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvError = findViewById(R.id.tvError);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        // 4️⃣ Xử lý sự kiện Click Login
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all information", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                // Hiển thị thông báo lỗi
                tvError.setText("Password must have at least 6 characters.");
                tvError.setVisibility(View.VISIBLE);
                return; // Dừng xử lý tiếp
            }
            // Ẩn thông báo lỗi cũ nếu có
            tvError.setVisibility(View.GONE);

            SignInRequest request = new SignInRequest(email, password);
            Call<SignUpResponse> call = apiService.signIn(request);

            call.enqueue(new Callback<SignUpResponse>() {
                @Override
                public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        SignUpResponse res = response.body();

                        // Lưu token
                        tokenManager.saveToken(res.getToken());

                        Toast.makeText(Login_Activity.this, "Log in successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login_Activity.this, Chat_Activity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            String errorStr = response.errorBody() != null ? response.errorBody().string() : "";
                            String errorMessage = "";
                            if (!errorStr.isEmpty()) {
                                JSONObject json = new JSONObject(errorStr);
                                errorMessage = json.optString("error", errorMessage); // Lấy field "error" hoặc "message" tùy server trả về
                            }
                            tvError.setText(errorMessage); // Set text lỗi cụ thể
                            tvError.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(Login_Activity.this, "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<SignUpResponse> call, Throwable t) {
                    Toast.makeText(Login_Activity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Login_Activity", "onFailure: " + t.getMessage());
                }
            });
        });

        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(Login_Activity.this, Register_Activity.class);
            startActivity(intent);
        });
    }
}