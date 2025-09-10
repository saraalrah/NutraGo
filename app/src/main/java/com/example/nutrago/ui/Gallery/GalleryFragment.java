package com.example.nutrago.ui.Gallery;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nutrago.R;
import com.example.nutrago.adapters.ProductAdapter;
import com.example.nutrago.database.DatabaseHelper;
import com.example.nutrago.models.Product;
import com.example.nutrago.models.Category;
import com.example.nutrago.dialogs.AddProductDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class GalleryFragment extends Fragment
        implements AddProductDialog.OnProductAddedListener,
        ProductAdapter.OnProductDeletedListener,
        ProductAdapter.OnProductEditedListener {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private FloatingActionButton fabAddProduct;
    private LinearLayout categoriesLayout;
    private DatabaseHelper dbHelper;
    private int selectedCategoryId = 0; // 0 means "All"

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product_gallery, container, false);

        try {
            recyclerView = root.findViewById(R.id.productRecyclerView);
            fabAddProduct = root.findViewById(R.id.fabAddProduct);
            categoriesLayout = root.findViewById(R.id.categoriesLayout);

            // تهيئة قاعدة البيانات
            dbHelper = new DatabaseHelper(getContext());

            // تحميل الفئات
            setupCategories();

            // تغيير إلى LinearLayoutManager للصفوف
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            // تحميل البيانات من قاعدة البيانات
            loadProductsFromDatabase(selectedCategoryId);

            // تفعيل زر إضافة منتج جديد
            fabAddProduct.setOnClickListener(v -> {
                AddProductDialog dialog = new AddProductDialog();
                dialog.setOnProductAddedListener(this);
                dialog.show(getParentFragmentManager(), "AddProductDialog");
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading gallery", Toast.LENGTH_SHORT).show();
        }

        return root;
    }

    private void setupCategories() {
        try {
            if (categoriesLayout == null) return;

            // مسح الفئات الموجودة
            categoriesLayout.removeAllViews();

            // إضافة خيار "All"
            TextView allCategoryView = createCategoryButton("All", 0, true);
            categoriesLayout.addView(allCategoryView);

            // جلب الفئات من قاعدة البيانات
            List<Category> categories = dbHelper.getAllCategories();
            if (categories != null) {
                for (Category category : categories) {
                    TextView categoryView = createCategoryButton(category.getName(), category.getId(), false);
                    categoriesLayout.addView(categoryView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading categories", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView createCategoryButton(String categoryName, int categoryId, boolean isSelected) {
        TextView categoryButton = new TextView(getContext());

        // تصميم الزر
        categoryButton.setText(categoryName);
        categoryButton.setPadding(32, 16, 32, 16);
        categoryButton.setTextSize(14);
        categoryButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // إعداد المارجن
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);
        categoryButton.setLayoutParams(params);

        // تطبيق التصميم حسب الحالة
        updateCategoryButtonStyle(categoryButton, isSelected);

        // إضافة وظيفة الضغط
        categoryButton.setOnClickListener(v -> {
            selectedCategoryId = categoryId;
            updateSelectedCategory();
            loadProductsFromDatabase(categoryId);
        });

        return categoryButton;
    }

    private void updateCategoryButtonStyle(TextView categoryButton, boolean isSelected) {
        if (isSelected) {
            // نمط الفئة المختارة
            categoryButton.setBackgroundColor(0xFF220F84);
            categoryButton.setTextColor(0xFFFFFFFF);
            categoryButton.setElevation(4f);
        } else {
            // نمط الفئة العادية
            categoryButton.setBackgroundColor(0xFFE0E0E0);
            categoryButton.setTextColor(0xFF666666);
            categoryButton.setElevation(2f);
        }

        // إضافة الزوايا المدورة
        categoryButton.setBackground(createRoundedBackground(isSelected));
    }

    private android.graphics.drawable.GradientDrawable createRoundedBackground(boolean isSelected) {
        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();
        drawable.setCornerRadius(20f);

        if (isSelected) {
            drawable.setColor(0xFF220F84);
        } else {
            drawable.setColor(0xFFE0E0E0);
        }

        return drawable;
    }

    private void updateSelectedCategory() {
        try {
            if (categoriesLayout == null) return;

            // تحديث جميع أزرار الفئات
            for (int i = 0; i < categoriesLayout.getChildCount(); i++) {
                TextView categoryButton = (TextView) categoriesLayout.getChildAt(i);

                boolean isSelected = false;
                if (i == 0 && selectedCategoryId == 0) { // "All" button
                    isSelected = true;
                } else if (i > 0) {
                    // للفئات العادية، نحتاج للتحقق من ID الفئة
                    List<Category> categories = dbHelper.getAllCategories();
                    if (categories != null && (i - 1) < categories.size()) {
                        Category category = categories.get(i - 1);
                        isSelected = (category.getId() == selectedCategoryId);
                    }
                }

                updateCategoryButtonStyle(categoryButton, isSelected);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProductsFromDatabase(int categoryId) {
        try {
            List<Product> productList;

            if (categoryId == 0) {
                // عرض جميع المنتجات
                productList = dbHelper.getAllProducts();
            } else {
                // عرض منتجات فئة محددة
                productList = dbHelper.getProductsByCategory(categoryId);
            }

            if (productList != null) {
                // إنشاء الـ adapter مع قاعدة البيانات
                productAdapter = new ProductAdapter(getActivity(), productList, dbHelper);

                // ربط وظيفة الحذف والتعديل
                productAdapter.setOnProductDeletedListener(this);
                productAdapter.setOnProductEditedListener(this);

                recyclerView.setAdapter(productAdapter);

                // إظهار رسالة إحصائية
                String message = categoryId == 0 ?
                        "Showing " + productList.size() + " products" :
                        "Showing " + productList.size() + " products in selected category";

                if (productList.isEmpty()) {
                    message = "No products found in this category";
                }

                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No products found", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading products from database", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProductAdded() {
        // إعادة تحميل القائمة بعد إضافة منتج جديد
        loadProductsFromDatabase(selectedCategoryId);
        Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProductDeleted() {
        // إعادة تحميل القائمة بعد حذف منتج
        loadProductsFromDatabase(selectedCategoryId);
        Toast.makeText(getContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProductEdited() {
        // إعادة تحميل القائمة بعد تعديل منتج
        loadProductsFromDatabase(selectedCategoryId);
        Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // إعادة تحميل البيانات عند العودة للشاشة
        if (dbHelper != null) {
            setupCategories(); // إعادة تحميل الفئات
            loadProductsFromDatabase(selectedCategoryId); // إعادة تحميل المنتجات
        }
    }
}