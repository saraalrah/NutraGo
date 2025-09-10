package com.example.nutrago.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.nutrago.R;
import com.example.nutrago.MainActivity;

public class HomeFragment extends Fragment {

    private boolean isAnimationPlayed = false; // متغير لتتبع حالة الأنيميشن

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button welcomeButton = view.findViewById(R.id.playWelcomeAudioButton);

        if (welcomeButton != null && !isAnimationPlayed) {
            // تطبيق الأنيميشن فقط في المرة الأولى
            welcomeButton.clearAnimation();
            welcomeButton.setAlpha(0f);
            welcomeButton.setTranslationY(50f);

            welcomeButton.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setDuration(1000)
                    .setStartDelay(300)
                    .withEndAction(() -> isAnimationPlayed = true)
                    .start();
        } else if (welcomeButton != null) {
            // التأكد من أن الزر في الوضع الطبيعي
            welcomeButton.setAlpha(1f);
            welcomeButton.setTranslationY(0f);
        }

        // إضافة وظيفة تشغيل الصوت عند الضغط على الزر
        if (welcomeButton != null) {
            welcomeButton.setOnClickListener(v -> {
                // التحقق من أن الـ Activity هو MainActivity
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.playWelcomeSound(); // استدعاء دالة تشغيل الصوت
                }
            });
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // إعادة تعيين المتغير عند تدمير الـ view
        isAnimationPlayed = false;
    }
}