package com.example.nutrago.ui.contact;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nutrago.R;

public class ContactUsFragment extends Fragment {

    private EditText editTextName, editTextEmail, editTextMessage;
    private Button sendButton;

    public ContactUsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            View root = inflater.inflate(R.layout.fragment_contact_us, container, false);

            initViews(root);
            setupClickListeners();

            return root;
        } catch (Exception e) {
            e.printStackTrace();
            // في حالة فشل تحميل التخطيط، إنشاء view بسيط
            return createFallbackView(inflater, container);
        }
    }

    private void initViews(View root) {
        try {
            editTextName = root.findViewById(R.id.editTextName);
            editTextEmail = root.findViewById(R.id.editTextEmail);
            editTextMessage = root.findViewById(R.id.editTextMessage);
            sendButton = root.findViewById(R.id.sendButton);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupClickListeners() {
        if (sendButton != null) {
            sendButton.setOnClickListener(v -> {
                try {
                    String name = editTextName != null ? editTextName.getText().toString().trim() : "";
                    String email = editTextEmail != null ? editTextEmail.getText().toString().trim() : "";
                    String message = editTextMessage != null ? editTextMessage.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(message)) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Message sent! 💌", Toast.LENGTH_SHORT).show();
                        clearFields();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error sending message", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void clearFields() {
        try {
            if (editTextName != null) editTextName.setText("");
            if (editTextEmail != null) editTextEmail.setText("");
            if (editTextMessage != null) editTextMessage.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View createFallbackView(LayoutInflater inflater, ViewGroup container) {
        // إنشاء view بسيط في حالة فشل تحميل التخطيط الأصلي
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setBackgroundColor(0xFFF5F5F5);

        android.widget.TextView title = new android.widget.TextView(getContext());
        title.setText("Contact Us");
        title.setTextSize(24);
        title.setTextColor(0xFF220F84);
        title.setPadding(0, 0, 0, 30);
        title.setGravity(android.view.Gravity.CENTER);
        layout.addView(title);

        android.widget.TextView message = new android.widget.TextView(getContext());
        message.setText("📧 Email: nutrago@example.com\n📞 Phone: +966 12 345 6789\n📍 Location: Riyadh, Saudi Arabia");
        message.setTextSize(16);
        message.setTextColor(0xFF666666);
        message.setPadding(20, 20, 20, 20);
        message.setGravity(android.view.Gravity.CENTER);
        layout.addView(message);

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // تنظيف المراجع
        editTextName = null;
        editTextEmail = null;
        editTextMessage = null;
        sendButton = null;
    }
}