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
        setContentView(R.layout.activity_login);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvError = findViewById(R.id.tvError);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // 1️⃣ Kiểm tra đầu vào
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2️⃣ Tạo request body
            SignInRequest request = new SignInRequest(email, password);

            apiService = ApiClient.getClient(this).create(ApiService.class);
            Call<SignUpResponse> call = apiService.signIn(request);

            call.enqueue(new Callback<SignUpResponse>() {
                @Override
                public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        SignUpResponse res = response.body();

                        // 4️⃣ Lưu token bằng TokenManager
                        TokenManager tokenManager = TokenManager.getInstance(Login_Activity.this);
                        tokenManager.saveToken(res.getToken());

                        // 5️⃣ Chuyển trang
                        Toast.makeText(Login_Activity.this, "Đăng nhập thành công!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Login_Activity.this, Chat_Activity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // 6️⃣ Xử lý lỗi từ server
                        try {
                            String errorStr = response.errorBody() != null ? response.errorBody().string() : "";
                            String errorMessage = "Sai email hoặc mật khẩu";

                            if (!errorStr.isEmpty()) {
                                JSONObject json = new JSONObject(errorStr);
                                errorMessage = json.optString("error", errorMessage);
                            }

                            tvError.setText(errorMessage);
                            tvError.setVisibility(View.VISIBLE);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(Login_Activity.this, "Lỗi phản hồi từ server", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<SignUpResponse> call, Throwable t) {
                    // 7️⃣ Lỗi kết nối
                    Toast.makeText(Login_Activity.this, "Kết nối thất bại: " + t.getMessage(), Toast.LENGTH_LONG).show();
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