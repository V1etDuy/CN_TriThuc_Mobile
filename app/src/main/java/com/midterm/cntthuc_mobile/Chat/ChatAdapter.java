package com.midterm.cntthuc_mobile.Chat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.cntthuc_mobile.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private final List<ChatMessage> messages;
    private final Context context;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        LinearLayout container;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.tvMessage);
            container = itemView.findViewById(R.id.container);
        }

        void bind(ChatMessage msg) {
            txtMessage.setText(msg.getMessage());

            // LayoutParams để căn trái/phải
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) txtMessage.getLayoutParams();

            if (msg.isUser()) {
                params.gravity = Gravity.END;
                txtMessage.setBackgroundResource(R.drawable.bg_user);
            } else {
                params.gravity = Gravity.START;
                txtMessage.setBackgroundResource(R.drawable.bg_bot);
            }

            txtMessage.setLayoutParams(params);
        }
    }
}