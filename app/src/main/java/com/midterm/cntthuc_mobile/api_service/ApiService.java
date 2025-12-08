package com.midterm.cntthuc_mobile.api_service;
import com.midterm.cntthuc_mobile.Chat.RenameRequest;
import com.midterm.cntthuc_mobile.Chat.getMessageResponse;
import com.midterm.cntthuc_mobile.auth.SignInRequest;
import com.midterm.cntthuc_mobile.auth.SignUpRequest;
import com.midterm.cntthuc_mobile.auth.SignUpResponse;
import com.midterm.cntthuc_mobile.auth.SimpleResponse;
import com.midterm.cntthuc_mobile.profile.ChangePasswordRequest;
import com.midterm.cntthuc_mobile.profile.ChangeUsernameRequest;
import com.midterm.cntthuc_mobile.profile.ChangeUsernameResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
    @Authorized
    @GET("chat-sessions/get-by-user")
    Call<ResponseBody> getChatSessions(
            @Query("page") int page,
            @Query("limit") int limit
    );
    @Authorized
    @GET("messages/session/{id}/get-messages")
    Call<getMessageResponse> getMessages(
            @Path("id") String sessionId,
            @Query("page") int page,
            @Query("limit") int limit
    );
    @Authorized
    @DELETE("chat-sessions/{sessionId}")
    Call<ResponseBody> deleteChatSession(@Path("sessionId") String sessionId);

    @Authorized
    @PATCH("chat-sessions/{sessionId}/name")
    Call<ResponseBody> renameChatSession(@Path("sessionId") String sessionId, @Body RenameRequest body);
}
