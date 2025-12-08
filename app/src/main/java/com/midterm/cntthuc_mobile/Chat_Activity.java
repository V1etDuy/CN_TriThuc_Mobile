package com.midterm.cntthuc_mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.midterm.cntthuc_mobile.Chat.ChatAdapter;
import com.midterm.cntthuc_mobile.Chat.ChatMessage;
import com.midterm.cntthuc_mobile.Chat.ChatSession;
import com.midterm.cntthuc_mobile.Chat.ChatSessionAdapter;
import com.midterm.cntthuc_mobile.Chat.RenameRequest;
import com.midterm.cntthuc_mobile.Chat.getMessageResponse;
import com.midterm.cntthuc_mobile.api_service.ApiClient;
import com.midterm.cntthuc_mobile.api_service.ApiService;
import com.midterm.cntthuc_mobile.api_service.TokenManager;
import com.midterm.cntthuc_mobile.auth.SignUpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chat_Activity extends AppCompatActivity implements ChatSessionAdapter.OnSessionClickListener,
        ChatSessionAdapter.OnOptionClickListener {
    private ImageView ivLeftIcon;
    private DrawerLayout drawerLayout;
    private ImageView ivRightIcon;
    private Socket socket;
    private String sessionId = null;
    private ImageButton btnSend;
    private EditText etMessage;
    private RecyclerView recyclerChat, recyclerChatList;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private ChatSessionAdapter sessionAdapter;
    private List<ChatSession> sessionList = new ArrayList<>();
    private String token;
    private ApiService apiService;
    private Button btnNewChat;
    private TextView tvStatusReport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        drawerLayout = findViewById(R.id.drawerLayout);
        ivLeftIcon = findViewById(R.id.ivLeftIcon);
        ivRightIcon = findViewById(R.id.ivRightIcon);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        recyclerChat = findViewById(R.id.recyclerView);
        btnNewChat = findViewById(R.id.btnNewChat);
        tvStatusReport = findViewById(R.id.tvStatusReport);

        chatAdapter = new ChatAdapter(this, messages);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);
        recyclerChat.setItemAnimator(new DefaultItemAnimator());

        recyclerChatList = findViewById(R.id.recyclerChatList);
        recyclerChatList.setLayoutManager(new LinearLayoutManager(this));
//        sessionAdapter = new ChatSessionAdapter(this, sessionList, session -> {
//            sessionId = session.getId();
//            drawerLayout.closeDrawer(GravityCompat.START);
//            setInputEnabled(true);
//            getNewSession();
//        });
        sessionAdapter = new ChatSessionAdapter(this, sessionList, session -> {
            sessionId = session.getId();
            drawerLayout.closeDrawer(GravityCompat.START);
            setInputEnabled(true);
            getNewSession();
        }, this);
        recyclerChatList.setAdapter(sessionAdapter);

        token = TokenManager.getInstance(this).getToken();
        apiService = ApiClient.getClient(this).create(ApiService.class);

        ivLeftIcon.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
            loadChatSession();
        });
        ivRightIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(Chat_Activity.this, v);
            popup.getMenuInflater().inflate(R.menu.menu_user_options, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_profile) {
                    Intent intent = new Intent(Chat_Activity.this, ProfileActivity.class);
                    startActivity(intent);  // 👉 Mở trang Profile
                    return true;
                } else if (item.getItemId() == R.id.action_logout) {
                    TokenManager.getInstance(getApplicationContext()).clearToken();

                    Toast.makeText(Chat_Activity.this, "Logged out.", Toast.LENGTH_SHORT).show();

                    // 3. Chuyển hướng đến Login_Activity và đóng các Activity trước đó
                    Intent intent = new Intent(Chat_Activity.this, Login_Activity.class);

                    // Cờ này đảm bảo Login_Activity là Activity duy nhất trên stack và
                    // tất cả các Activity trước đó (như Chat_Activity) đều bị đóng.
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);


                    // Kết thúc Activity hiện tại (Chat_Activity)
                    finish();
                    return true;
                }
                return false;
            });
            popup.show();
        });
        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (msg.isEmpty()) return;
            setInputEnabled(false);
            try {
                JSONObject message = new JSONObject();
                message.put("chatSessionId", sessionId == null ? JSONObject.NULL : sessionId);
                message.put("question", msg);

                addMessage(msg, true);

                socket.emit("ask-question", message);
//                Log.d("Socket", "📨 Sent message: " + message);

                etMessage.setText(""); // Xoá ô nhập sau khi gửi
            } catch (JSONException e) {
                e.printStackTrace();
                setInputEnabled(true);
            }
        });
        btnNewChat.setOnClickListener(v -> {
            messages.clear(); // clear tin cũ
            chatAdapter.notifyDataSetChanged();
            sessionId = null;
            drawerLayout.closeDrawer(GravityCompat.START);

            setInputEnabled(true);

        });
        connectSocket();
    }

    private void getNewSession() {
        messages.clear(); // clear tin cũ
        chatAdapter.notifyDataSetChanged();


        Call<getMessageResponse> call = apiService.getMessages(sessionId, 1, 20);
        call.enqueue(new Callback<getMessageResponse>() {
            @Override
            public void onResponse(Call<getMessageResponse> call, Response<getMessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<getMessageResponse.ChatMessageItem> chatList = response.body().getChatMessages();
                    for (getMessageResponse.ChatMessageItem item : chatList) {
                        messages.add(new ChatMessage(item.getContent(), item.getRole().equals("user")));
                    }
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    recyclerChat.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<getMessageResponse> call, Throwable t) {
                Toast.makeText(Chat_Activity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChatSession() {
        Call<ResponseBody> call = apiService.getChatSessions(1, 20);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray arr = json.getJSONArray("chatSessions");

                        sessionList.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            ChatSession session = new ChatSession();
                            Field idField = ChatSession.class.getDeclaredField("_id");
                            Field titleField = ChatSession.class.getDeclaredField("title");
                            Field createdField = ChatSession.class.getDeclaredField("createdAt");
                            idField.setAccessible(true);
                            titleField.setAccessible(true);
                            createdField.setAccessible(true);
                            idField.set(session, obj.optString("_id"));
                            titleField.set(session, obj.optString("title"));
                            createdField.set(session, obj.optString("createdAt"));
                            sessionList.add(session);
                        }

                        sessionAdapter.notifyDataSetChanged();
//                        Log.d("API", "✅ Loaded " + sessionList.size() + " sessions");

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(Chat_Activity.this, "Parse error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Chat_Activity.this, "Failed to load sessions", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(Chat_Activity.this, "❌ API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void connectSocket() {
        try {
            if (token == null) {
                Toast.makeText(this, "Missing JWT token!", Toast.LENGTH_SHORT).show();
                return;
            }

            IO.Options options = new IO.Options();
            options.transports = new String[]{"websocket"};
            options.forceNew = true;
            options.reconnection = true;
            options.auth = Collections.singletonMap("token", token);

//            socket = IO.socket("http://192.168.50.13:3000/", options);
            socket = IO.socket("http://kazekageiii.xyz:3001/", options);
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() ->
                    Toast.makeText(this, "✅ Connected to server", Toast.LENGTH_SHORT).show()
            ));

            socket.on(Socket.EVENT_DISCONNECT, args -> runOnUiThread(() -> {
                Toast.makeText(this, "❌ Disconnected", Toast.LENGTH_SHORT).show();
                setInputEnabled(true);
            }));

            socket.on(Socket.EVENT_CONNECT_ERROR, args -> runOnUiThread(() ->
                    Toast.makeText(this, "⚠️ Connect error: " + args[0], Toast.LENGTH_SHORT).show()
            ));

            // ✅ Server xác nhận đang xử lý
            socket.on("server-ack", args -> {
                JSONObject data = (JSONObject) args[0];
            });

            // ✅ Khi server gửi câu trả lời
            socket.on("receive-answer", args -> runOnUiThread(() -> {
                try {
                    JSONObject data = (JSONObject) args[0];
                    String answer = data.optString("answer", "(no answer)");
                    String newSessionId = data.optString("chatSessionId", null);

//                    Toast.makeText(this, "💬 Bot: " + answer, Toast.LENGTH_LONG).show();
                    Log.d("Socket", "💾 Saved sessionId: " + newSessionId);

                    addMessage(answer, false);

                    // Lưu sessionId cho những lần chat sau
                    if (newSessionId != null && !newSessionId.equals("null")) {
                        sessionId = newSessionId;
                        Log.d("Socket", "💾 Saved sessionId: " + sessionId);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // --- THÊM DÒNG NÀY: Mở khóa lại nút sau khi nhận tin xong ---
                    setInputEnabled(true);
                    if (tvStatusReport != null) {
                        tvStatusReport.setVisibility(View.GONE);
                    }
                }
            }));

            socket.on("server-report", args -> runOnUiThread(() -> {
                try {
                    JSONObject data = (JSONObject) args[0];
                    String report = data.optString("report", "Processing...");

                    // Hiển thị TextView và cập nhật nội dung
                    if (tvStatusReport != null) {
                        tvStatusReport.setText("AI Status: " + report);
                        tvStatusReport.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            // ✅ Nhận lỗi nghiệp vụ từ server
            socket.on("error-message", args -> runOnUiThread(() -> {
                JSONObject data = (JSONObject) args[0];
                String message = data.optString("message", "Unknown error");
                Toast.makeText(this, "⚠️ Server error: " + message, Toast.LENGTH_SHORT).show();
                setInputEnabled(true);
            }));

            socket.connect();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Socket init failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }
    private void addMessage(String text, boolean isUser) {
        messages.add(new ChatMessage(text, isUser));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        recyclerChat.scrollToPosition(messages.size() - 1);
    }
    // Hàm helper để bật/tắt nhập liệu
    private void setInputEnabled(boolean enabled) {
        etMessage.setEnabled(enabled);
        btnSend.setEnabled(enabled);

        // (Tuỳ chọn) Đổi màu nút gửi một chút để người dùng biết nó đang bị khóa
        btnSend.setAlpha(enabled ? 1.0f : 0.5f);
    }
    @Override
    public void onSessionClick(ChatSession session) {
        // Xử lý khi click vào item (ví dụ: đóng drawer và load chi tiết chat)
        Toast.makeText(this, "Mở phiên: " + session.getTitle(), Toast.LENGTH_SHORT).show();
        drawerLayout.closeDrawer(GravityCompat.START);

        messages.clear(); // clear tin cũ
        chatAdapter.notifyDataSetChanged();


        Call<getMessageResponse> call = apiService.getMessages(sessionId, 1, 20);
        call.enqueue(new Callback<getMessageResponse>() {
            @Override
            public void onResponse(Call<getMessageResponse> call, Response<getMessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<getMessageResponse.ChatMessageItem> chatList = response.body().getChatMessages();
                    for (getMessageResponse.ChatMessageItem item : chatList) {
                        messages.add(new ChatMessage(item.getContent(), item.getRole().equals("user")));
                    }
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    recyclerChat.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<getMessageResponse> call, Throwable t) {
                Toast.makeText(Chat_Activity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Api","Error: " + t.getMessage() );
            }
        });

    }
    @Override
    public void onRenameClick(ChatSession session) {
        showRenameDialog(session);
    }

    @Override
    public void onDeleteClick(ChatSession session) {
        showDeleteConfirmationDialog(session);
    }
    // --- Các hàm Dialog và API ---

    private void showDeleteConfirmationDialog(ChatSession session) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm delete")
                .setMessage("Are you sure you want to delete this chat session ?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteChatSession(session.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showRenameDialog(ChatSession session) {
        final EditText input = new EditText(this);
        // Đặt tên hiện tại vào EditText
        input.setText(session.getTitle());

        new AlertDialog.Builder(this)
                .setTitle("Rename chat session")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newTitle = input.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        renameChatSession(session.getId(), newTitle);
                    } else {
                        Toast.makeText(this, "Name must not be empty!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Phương thức gọi API xóa phiên trò chuyện.
     * Giả định API của bạn là DELETE /chatSessions/{sessionId}
     */
    private void deleteChatSession(String sessionId) {
        apiService.deleteChatSession(sessionId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Chat_Activity.this, "✅ Session deleted!", Toast.LENGTH_SHORT).show();
                    loadChatSession(); // Tải lại danh sách sau khi xóa
                } else {
                    Toast.makeText(Chat_Activity.this, "❌ Error delete: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(Chat_Activity.this, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Phương thức gọi API đổi tên phiên trò chuyện.
     * Giả định API của bạn là PUT /chatSessions/{sessionId}/rename với Body { "newTitle": "..." }
     */
    private void renameChatSession(String sessionId, String newTitle) {
        // Cần có lớp RenameRequest để gửi Body (hoặc dùng Map<String, String>)
        RenameRequest body = new RenameRequest(newTitle);

        apiService.renameChatSession(sessionId, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Chat_Activity.this, "✅ Rename success!", Toast.LENGTH_SHORT).show();
                    loadChatSession(); // Tải lại danh sách sau khi đổi tên
                } else {
                    Toast.makeText(Chat_Activity.this, "❌ Rename error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(Chat_Activity.this, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}