package com.midterm.cntthuc_mobile.profile;

public class ChangePasswordRequest {
    String oldPassword;
    String newPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

}
