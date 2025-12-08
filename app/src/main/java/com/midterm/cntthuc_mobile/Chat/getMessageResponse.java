package com.midterm.cntthuc_mobile.Chat;

import java.util.List;

public class getMessageResponse {
    private List<ChatMessageItem> chatMessages;

    public List<ChatMessageItem> getChatMessages() {
        return chatMessages;
    }

    public static class ChatMessageItem {
        private String role;
        private String content;

        public String getRole() { return role; }
        public String getContent() { return content; }
    }
}
