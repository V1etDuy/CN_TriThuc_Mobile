package com.midterm.cntthuc_mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.midterm.cntthuc_mobile.api_service.ApiClient;
import com.midterm.cntthuc_mobile.api_service.ApiService;
import com.midterm.cntthuc_mobile.auth.SignInRequest;
import com.midterm.cntthuc_mobile.auth.SignUpResponse;
import com.midterm.cntthuc_mobile.auth.SimpleResponse;
import com.midterm.cntthuc_mobile.profile.ChangePasswordRequest;
import com.midterm.cntthuc_mobile.profile.ChangeUsernameRequest;
import com.midterm.cntthuc_mobile.profile.ChangeUsernameResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    Button btnSaveUsername, btnSavePassword;
    EditText edtUsername, edtOldPassword, edtNewPassword;
    private ApiService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        btnSaveUsername = findViewById(R.id.btnSaveUsername);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        edtUsername = findViewById(R.id.edtUsername);
        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);

        loadUsername();
        String oldUsername = edtUsername.getText().toString().trim();
        btnSaveUsername.setOnClickListener(v -> {
            String newUsername = edtUsername.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Please type in username", Toast.LENGTH_SHORT).show();
                return;
            }
            if (oldUsername.equals(newUsername)) {
                Toast.makeText(this, "Please change your username", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo request body
            ChangeUsernameRequest request = new ChangeUsernameRequest(newUsername);
            // Gọi API
            Call<ChangeUsernameResponse> call = apiService.changeUsername(request);
            call.enqueue(new Callback<ChangeUsernameResponse>() {
                @Override
                public void onResponse(Call<ChangeUsernameResponse> call, Response<ChangeUsernameResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ChangeUsernameResponse res = response.body();
                        Toast.makeText(ProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                String errorStr = response.errorBody().string();
                                JSONObject json = new JSONObject(errorStr);
                                String errorMessage = json.optString("error", "Lỗi không xác định");

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(ProfileActivity.this, "Lỗi xử lý phản hồi từ server", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Server không phản hồi", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ChangeUsernameResponse> call, Throwable t) {
                    Toast.makeText(ProfileActivity.this,
                            "Kết nối thất bại: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e("ProfileActivity", "API error", t);
                }
            });
        });

        btnSavePassword.setOnClickListener(v -> {
            String oldPassword = edtOldPassword.getText().toString().trim();;
            String newPassword = edtNewPassword.getText().toString().trim();
            if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please type in password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (oldPassword.equals(newPassword)) {
                Toast.makeText(this, "Old password and new password must not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo request body
            ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);
            // Gọi API
            Call<SimpleResponse> call = apiService.changePassword(request);
            call.enqueue(new Callback<SimpleResponse>() {
                @Override
                public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        SimpleResponse res = response.body();
                        Toast.makeText(ProfileActivity.this, "Change password successfully!", Toast.LENGTH_SHORT).show();
                        edtOldPassword.setText("");
                        edtNewPassword.setText("");
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                String errorStr = response.errorBody().string();
                                JSONObject json = new JSONObject(errorStr);
                                String errorMessage = json.optString("error", "Lỗi không xác định");

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(ProfileActivity.this, "Lỗi xử lý phản hồi từ server", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Server not respond", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<SimpleResponse> call, Throwable t) {
                    Toast.makeText(ProfileActivity.this,
                            "Kết nối thất bại: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e("ProfileActivity", "API error", t);
                }
            });
        });



    }

    private void loadUsername() {
        Call<ChangeUsernameResponse> call = apiService.getProfile();
        call.enqueue(new Callback<ChangeUsernameResponse>() {
            @Override
            public void onResponse(Call<ChangeUsernameResponse> call, Response<ChangeUsernameResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String username = response.body().getUser().getUsername();
                    edtUsername.setText(username);

                    // Log ra username
                    Log.d("API_RESPONSE", "Username: " + username);
                } else {
                    if (response.errorBody() != null) {
                        try {
                            String errorStr = response.errorBody().string();
                            JSONObject json = new JSONObject(errorStr);
                            String errorMessage = json.optString("error", "Lỗi không xác định");

                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ProfileActivity.this, "Lỗi xử lý phản hồi từ server", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Server không phản hồi", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<ChangeUsernameResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this,
                        "Kết nối thất bại: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.e("ProfileActivity", "API error", t);
            }
        });
    }
}