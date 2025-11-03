package com.midterm.cntthuc_mobile.auth;

public class SignUpResponse {
    private User user;
    private String token;
    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    // class lồng cho user object
    public static class User {
        private String username;
        private String email;
        private boolean isDeleted;
        private String createdAt;
        private String updatedAt;
        private int __v;

        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public boolean isDeleted() { return isDeleted; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public int getV() { return __v; }
    }
}
