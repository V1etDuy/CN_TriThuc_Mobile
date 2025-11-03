package com.midterm.cntthuc_mobile.api_service;
import com.midterm.cntthuc_mobile.auth.SignUpRequest;
import com.midterm.cntthuc_mobile.auth.SignUpResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/user/sign-up")
    Call<SignUpResponse> signUp(@Body SignUpRequest request);
}
