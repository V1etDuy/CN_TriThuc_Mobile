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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvError = findViewById(R.id.tvError);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            // Tạo request body
            SignInRequest request = new SignInRequest(email, password);
            // Gọi API
            Call<SignUpResponse> call = apiService.signIn(request);
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

                        String info = "Đăng nhập thành công!";
                        Intent intent = new Intent(Login_Activity.this,Chat_Activity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(Login_Activity.this, info, Toast.LENGTH_LONG).show();
                    } else {
                        if (!response.isSuccessful() && response.errorBody() != null) {
                            try {
                                String errorStr = response.errorBody().string();
                                JSONObject json = new JSONObject(errorStr);
                                String errorMessage = json.optString("error", "Unknown error");
//                                Toast.makeText(Login_Activity.this,
//                                        "Error: " + errorMessage,
//                                        Toast.LENGTH_SHORT).show();
                                tvError.setVisibility(View.VISIBLE);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(Login_Activity.this,
                                        "Lỗi server không xác định",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<SignUpResponse> call, Throwable t) {
                    Toast.makeText(Login_Activity.this,
                            "Kết nối thất bại: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.d("Register_Activity", t.getMessage());
                }
            });
        });


        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(Login_Activity.this, Register_Activity.class);
            startActivity(intent);
        });
    }
}