package com.midterm.cntthuc_mobile.api_service;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.50.12:3000/";
    // ⚠️ Nếu dùng thiết bị thật → đổi sang IP máy tính, ví dụ "http://192.168.1.10:3000"

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}