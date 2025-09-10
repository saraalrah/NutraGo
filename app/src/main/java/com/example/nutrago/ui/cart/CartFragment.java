package com.example.nutrago.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrago.R;
import com.example.nutrago.adapters.CartAdapter;
import com.example.nutrago.database.DatabaseHelper;
import com.example.nutrago.models.Product;

import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView cartRecyclerView;
    private TextView totalPriceTextView;
    private TextView emptyCartTextView;
    private Button checkoutButton;
    private CartAdapter cartAdapter;
    private DatabaseHelper dbHelper;

    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        initViews(root);
        setupRecyclerView();
        setupCheckoutButton();

        return root;
    }

    private void initViews(View root) {
        cartRecyclerView = root.findViewById(R.id.cartRecyclerView);
        totalPriceTextView = root.findViewById(R.id.totalPriceTextView);
        checkoutButton = root.findViewById(R.id.checkoutButton);

        // إضافة TextView للسلة الفارغة (إذا لم يكن موجود)
        emptyCartTextView = root.findViewById(R.id.emptyCartTextView);
        if (emptyCartTextView == null) {
            // إنشاء TextView للسلة الفارغة برمجياً
            emptyCartTextView = new TextView(getContext());
            emptyCartTextView.setText("🛒 السلة فارغة\nأضف بعض المنتجات من صفحة الجاليري!");
            emptyCartTextView.setTextSize(16);
            emptyCartTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            emptyCartTextView.setPadding(32, 64, 32, 64);
            emptyCartTextView.setVisibility(View.GONE);

            // إضافة إلى Layout
            ViewGroup parent = (ViewGroup) cartRecyclerView.getParent();
            parent.addView(emptyCartTextView, 0);
        }

        // تهيئة قاعدة البيانات
        dbHelper = new DatabaseHelper(getContext());
    }

    private void setupRecyclerView() {
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadCartItems();
    }

    private void setupCheckoutButton() {
        checkoutButton.setOnClickListener(v -> {
            try {
                List<Product> cartList = dbHelper.getCartItems();
                if (cartList == null || cartList.isEmpty()) {
                    Toast.makeText(getContext(), "🛒 السلة فارغة!", Toast.LENGTH_SHORT).show();
                } else {
                    double total = calculateTotal(cartList);

                    // تأثير بصري على الزر
                    checkoutButton.setText("✅ تم الطلب!");
                    checkoutButton.setBackgroundColor(0xFF4CAF50);

                    Toast.makeText(getContext(),
                            String.format("🎉 شكراً لك! إجمالي الطلب: %.2f ريال", total),
                            Toast.LENGTH_LONG).show();

                    // تنظيف السلة بعد الشراء
                    dbHelper.clearCart();
                    loadCartItems(); // إعادة تحميل السلة

                    // إعادة النص بعد 3 ثوان
                    checkoutButton.postDelayed(() -> {
                        checkoutButton.setText("Checkout");
                        checkoutButton.setBackgroundColor(0xFF220F84);
                    }, 3000);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "❌ خطأ في معالجة الطلب", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCartItems() {
        try {
            List<Product> cartList = dbHelper.getCartItems();

            if (cartList != null && !cartList.isEmpty()) {
                // عرض قائمة المنتجات
                cartRecyclerView.setVisibility(View.VISIBLE);
                emptyCartTextView.setVisibility(View.GONE);

                cartAdapter = new CartAdapter(getContext(), cartList, dbHelper);
                cartAdapter.setOnCartChangeListener(this::updateTotal);
                cartRecyclerView.setAdapter(cartAdapter);

                updateTotal();
            } else {
                // عرض رسالة السلة الفارغة
                cartRecyclerView.setVisibility(View.GONE);
                emptyCartTextView.setVisibility(View.VISIBLE);
                totalPriceTextView.setText("Total: 0.00 SAR");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "❌ خطأ في تحميل السلة", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotal() {
        try {
            List<Product> cartList = dbHelper.getCartItems();
            double total = calculateTotal(cartList);
            totalPriceTextView.setText(String.format("الإجمالي: %.2f ريال", total));
        } catch (Exception e) {
            e.printStackTrace();
            totalPriceTextView.setText("Total: 0.00 SAR");
        }
    }

    private double calculateTotal(List<Product> cartList) {
        double total = 0;
        if (cartList != null) {
            for (Product product : cartList) {
                total += product.getPrice();
            }
        }
        return total;
    }

    @Override
    public void onResume() {
        super.onResume();
        // إعادة تحميل السلة عند العودة للشاشة
        if (dbHelper != null) {
            loadCartItems();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // تنظيف الموارد
        if (dbHelper != null) {
            // لا نحتاج لإغلاق قاعدة البيانات هنا
        }
    }
}