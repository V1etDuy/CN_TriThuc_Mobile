package com.midterm.cntthuc_mobile.api_service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.midterm.cntthuc_mobile.Login_Activity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        TokenManager tokenManager = TokenManager.getInstance(context);
        String token = tokenManager.getToken();

        // Nếu không có token thì cho phép request tiếp tục
        if (token == null) {
            return chain.proceed(chain.request());
        }

        // Nếu token hết hạn → logout
        if (tokenManager.isTokenExpired()) {
            tokenManager.clearToken();
            showToast("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");

            Intent intent = new Intent(context, Login_Activity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);

            // Dừng request, ném lỗi để Retrofit biết
            throw new IOException("Token expired");
        }
        Log.d("AuthInterceptor", "Token = " + token);
        // Gắn token vào header
        Request newRequest = chain.request().newBuilder()
                .addHeader("authorization", token)
                .build();

        return chain.proceed(newRequest); // ✅ Trả về okhttp3.Response
    }

    private void showToast(String message) {
        android.os.Handler handler = new android.os.Handler(context.getMainLooper());
        handler.post(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }
}