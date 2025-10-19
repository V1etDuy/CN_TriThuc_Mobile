package com.midterm.cntthuc_mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class Chat_Activity extends AppCompatActivity {
    ImageView ivLeftIcon;
    DrawerLayout drawerLayout;
    ImageView ivRightIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        drawerLayout = findViewById(R.id.drawerLayout);
        ivLeftIcon = findViewById(R.id.ivLeftIcon);
        ivRightIcon = findViewById(R.id.ivRightIcon);

        ivLeftIcon.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        ivRightIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(Chat_Activity.this, v);
            popup.getMenuInflater().inflate(R.menu.menu_user_options, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_change_password) {
                    Toast.makeText(Chat_Activity.this, "Đổi mật khẩu", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.action_logout) {
                    Toast.makeText(Chat_Activity.this, "Đăng xuất", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            popup.show();
        });


    }
}