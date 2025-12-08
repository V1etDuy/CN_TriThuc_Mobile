package com.midterm.cntthuc_mobile.Chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.cntthuc_mobile.R;

import java.util.List;

public class ChatSessionAdapter extends RecyclerView.Adapter<ChatSessionAdapter.SessionViewHolder> {
    private final List<ChatSession> sessions;
    private final Context context;
    private final OnSessionClickListener listener;
    private final OnOptionClickListener optionListener;
    public interface OnSessionClickListener {
        void onSessionClick(ChatSession session);
    }
    public interface OnOptionClickListener {
        void onRenameClick(ChatSession session);
        void onDeleteClick(ChatSession session);
    }

    public ChatSessionAdapter(Context context, List<ChatSession> sessions,
                              OnSessionClickListener listener, OnOptionClickListener optionListener) {
        this.context = context;
        this.sessions = sessions;
        this.listener = listener;
        this.optionListener = optionListener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        ChatSession session = sessions.get(position);
        holder.textView.setText(session.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onSessionClick(session));

        holder.menuButton.setOnClickListener(v -> {
            // Gọi hàm hiển thị PopupMenu để chọn Rename/Delete
            showPopupMenu(v, session);
        });

    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageButton menuButton; // Thêm ImageButton
        SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_session_title);
            menuButton = itemView.findViewById(R.id.iv_menu_options);
        }
    }
    private void showPopupMenu(View view, ChatSession session) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenu().add(Menu.NONE, 1, 1, "Rename");
        popup.getMenu().add(Menu.NONE, 2, 2, "Delete");

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == 1) {
                optionListener.onRenameClick(session); // Gọi interface đổi tên
                return true;
            } else if (itemId == 2) {
                optionListener.onDeleteClick(session); // Gọi interface xóa
                return true;
            }
            return false;
        });
        popup.show();
    }
}