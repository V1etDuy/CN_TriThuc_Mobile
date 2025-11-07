package com.midterm.cntthuc_mobile.api_service;
import com.midterm.cntthuc_mobile.auth.SignInRequest;
import com.midterm.cntthuc_mobile.auth.SignUpRequest;
import com.midterm.cntthuc_mobile.auth.SignUpResponse;
import com.midterm.cntthuc_mobile.auth.SimpleResponse;
import com.midterm.cntthuc_mobile.profile.ChangePasswordRequest;
import com.midterm.cntthuc_mobile.profile.ChangeUsernameRequest;
import com.midterm.cntthuc_mobile.profile.ChangeUsernameResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/users/sign-up")
    Call<SignUpResponse> signUp(@Body SignUpRequest request);

    @POST("/users/login")
    Call<SignUpResponse> signIn(@Body SignInRequest request);
    @Authorized
    @PATCH("/users/username")
    Call<ChangeUsernameResponse> changeUsername(@Body ChangeUsernameRequest request);
    @Authorized
    @GET("users/get-profile")
    Call<ChangeUsernameResponse> getProfile();
    @Authorized
    @PATCH("users/password")
    Call<SimpleResponse> changePassword(@Body ChangePasswordRequest request);
}
