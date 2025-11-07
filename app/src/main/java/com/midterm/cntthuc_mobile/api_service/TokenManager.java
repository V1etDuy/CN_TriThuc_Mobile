package com.midterm.cntthuc_mobile.api_service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class TokenManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "token";
    private static TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    public boolean isTokenExpired() {
        String token = getToken();
        if (token == null || token.trim().isEmpty()) {
            return true;
        }

        try {
            // Tách phần payload của JWT (nằm giữa 2 dấu ".")
            String[] parts = token.split("\\.");
            if (parts.length < 2) return true;

            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject jsonObject = new JSONObject(payload);

            long exp = jsonObject.getLong("exp"); // thời điểm hết hạn (UNIX timestamp, giây)
            long now = System.currentTimeMillis() / 1000; // đổi sang giây

            return now >= exp;
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Nếu lỗi khi decode => coi như token hết hạn
        }
    }
}