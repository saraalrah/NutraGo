package com.example.nutrago.ui.about;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.nutrago.R;

import java.util.ArrayList;
import java.util.List;

public class AboutUsFragment extends Fragment {

    private ImageView logoImage;
    private LinearLayout contentLayout;
    private List<CardView> teamCards;

    public AboutUsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about_us, container, false);

        initViews(root);
        startAnimations();

        return root;
    }

    private void initViews(View root) {
        logoImage = root.findViewById(R.id.logoImage);

        // جمع كل الكروت لتطبيق التأثيرات عليها
        teamCards = new ArrayList<>();

        // يمكنك إضافة IDs للكروت إذا أردت تحكم أكثر في التأثيرات
        // لكن الكود الحالي سيطبق التأثيرات على كل الكروت تلقائياً
    }

    private void startAnimations() {
        // تأثير دوران بسيط على الشعار
        if (logoImage != null) {
            animateLogo();
        }

        // تأثير ظهور تدريجي للمحتوى
        animateContent();
    }

    private void animateLogo() {
        // تأثير دوران لطيف على الشعار
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(logoImage, "rotation", 0f, 360f);
        rotateAnimator.setDuration(2000);
        rotateAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnimator.start();

        // تأثير تكبير وتصغير
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoImage, "scaleX", 0.8f, 1.1f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoImage, "scaleY", 0.8f, 1.1f, 1.0f);
        scaleX.setDuration(1500);
        scaleY.setDuration(1500);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    private void animateContent() {
        // البحث عن كل الكروت في التخطيط وتطبيق تأثيرات عليها
        View rootView = getView();
        if (rootView != null) {
            animateCardsRecursively(rootView, 0);
        }
    }

    private int animateCardsRecursively(View view, int delay) {
        if (view instanceof CardView) {
            animateCard((CardView) view, delay);
            delay += 150; // تأخير بين كل كارت والذي يليه
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                delay = animateCardsRecursively(group.getChildAt(i), delay);
            }
        }
        return delay;
    }

    private void animateCard(CardView card, int delay) {
        // تعيين الموضع الأولي (خارج الشاشة)
        card.setTranslationX(300f);
        card.setAlpha(0f);
        card.setScaleX(0.8f);
        card.setScaleY(0.8f);

        // تأثير الانزلاق من اليمين مع الظهور التدريجي
        card.animate()
                .translationX(0f)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // إضافة تأثير لمسة لطيفة عند الضغط
        card.setOnClickListener(v -> {
            // تأثير نبضة بسيط
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // إعادة تشغيل التأثيرات عند العودة للصفحة
        if (getView() != null) {
            getView().postDelayed(this::startAnimations, 100);
        }
    }
}