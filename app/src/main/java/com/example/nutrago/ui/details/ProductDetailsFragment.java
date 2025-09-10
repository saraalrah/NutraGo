package com.example.nutrago.ui.details;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nutrago.R;
import com.example.nutrago.database.DatabaseHelper;
import com.example.nutrago.models.Product;
import com.example.nutrago.models.Category;
import com.example.nutrago.ui.Gallery.GalleryFragment;

import java.io.File;

public class ProductDetailsFragment extends Fragment {

    private static final String ARG_PRODUCT_ID = "product_id";

    private ImageButton backButton;
    private ImageView productImage;
    private TextView productName, productDescription, productPrice, productCategory;
    private Button addToCartButton;

    private DatabaseHelper dbHelper;
    private int productId;
    private Product currentProduct;

    public static ProductDetailsFragment newInstance(int productId) {
        ProductDetailsFragment fragment = new ProductDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getInt(ARG_PRODUCT_ID);
        }
        dbHelper = new DatabaseHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product_details, container, false);

        initViews(root);
        loadProductDetails();
        setupClickListeners();

        return root;
    }

    private void initViews(View root) {
        backButton = root.findViewById(R.id.backButton);
        productImage = root.findViewById(R.id.productDetailsImage);
        productName = root.findViewById(R.id.productDetailsName);
        productDescription = root.findViewById(R.id.productDetailsDescription);
        productPrice = root.findViewById(R.id.productDetailsPrice);
        productCategory = root.findViewById(R.id.productDetailsCategory);
        addToCartButton = root.findViewById(R.id.addToCartDetailsButton);
    }

    private void loadProductDetails() {
        try {
            currentProduct = getProductById(productId);

            if (currentProduct != null) {
                // عرض اسم المنتج
                productName.setText(currentProduct.getName());

                // عرض الوصف
                if (currentProduct.getDescription() != null && !currentProduct.getDescription().isEmpty()) {
                    productDescription.setText(currentProduct.getDescription());
                } else {
                    productDescription.setText("No description available");
                }

                // عرض السعر
                productPrice.setText(String.format("%.2f SAR", currentProduct.getPrice()));

                // عرض الفئة
                loadProductCategory();

                // تحميل الصورة
                loadProductImage();

            } else {
                Toast.makeText(getContext(), "Product not found", Toast.LENGTH_SHORT).show();
                navigateBack();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading product details", Toast.LENGTH_SHORT).show();
        }
    }

    private Product getProductById(int id) {
        try {
            return dbHelper.getProductById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadProductCategory() {
        try {
            if (currentProduct.getCategoryId() > 0) {
                Category category = dbHelper.getCategoryById(currentProduct.getCategoryId());
                if (category != null) {
                    productCategory.setText("📂 " + category.getName());
                } else {
                    productCategory.setText("📂 Uncategorized");
                }
            } else {
                productCategory.setText("📂 Uncategorized");
            }
        } catch (Exception e) {
            e.printStackTrace();
            productCategory.setText("📂 Category unavailable");
        }
    }

    private void loadProductImage() {
        try {
            String imagePath = dbHelper.getImagePath(productId);

            if (isDefaultImage(imagePath)) {
                // تحميل الصورة الافتراضية
                productImage.setImageResource(currentProduct.getImageResId());
            } else {
                // تحميل الصورة المخصصة
                loadCustomImage(imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            productImage.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private boolean isDefaultImage(String imagePath) {
        return imagePath != null && (
                imagePath.equals("protein_bar") ||
                        imagePath.equals("preworkout") ||
                        imagePath.equals("protein_powder")
        );
    }

    private void loadCustomImage(String imagePath) {
        try {
            File file = new File(getContext().getFilesDir(), imagePath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    productImage.setImageBitmap(bitmap);
                } else {
                    productImage.setImageResource(R.drawable.ic_launcher_background);
                }
            } else {
                productImage.setImageResource(R.drawable.ic_launcher_background);
            }
        } catch (Exception e) {
            e.printStackTrace();
            productImage.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private void setupClickListeners() {
        // زر الرجوع
        backButton.setOnClickListener(v -> navigateBack());

        // زر إضافة للسلة
        addToCartButton.setOnClickListener(v -> addProductToCart());
    }

    private void addProductToCart() {
        try {
            if (currentProduct != null && dbHelper != null) {
                boolean success = dbHelper.addToCart(currentProduct.getId());

                if (success) {
                    // تأثير بصري على الزر
                    addToCartButton.setText("✅ Added to Cart!");
                    addToCartButton.setBackgroundColor(0xFF4CAF50); // أخضر

                    // إعادة النص بعد ثانيتين
                    addToCartButton.postDelayed(() -> {
                        addToCartButton.setText("🛒 Add to Cart");
                        addToCartButton.setBackgroundColor(0xFF220F84); // اللون الأصلي
                    }, 2000);

                    Toast.makeText(getContext(), "✅ " + currentProduct.getName() + " added to cart", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "❌ Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "❌ Error adding to cart", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateBack() {
        try {
            // العودة إلى صفحة Gallery
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main, new GalleryFragment())
                        .addToBackStack(null)
                        .commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // في حالة فشل الانتقال، محاولة إغلاق الـ fragment
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // تنظيف المراجع
        backButton = null;
        productImage = null;
        productName = null;
        productDescription = null;
        productPrice = null;
        productCategory = null;
        addToCartButton = null;
        currentProduct = null;
    }
}