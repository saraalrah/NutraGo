package com.example.nutrago.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.nutrago.R;
import com.example.nutrago.database.DatabaseHelper;
import com.example.nutrago.models.Product;
import com.example.nutrago.models.Category;

import java.util.List;

public class EditProductDialog extends DialogFragment {

    public interface OnProductEditedListener {
        void onProductEdited(Product product);
    }

    private OnProductEditedListener listener;
    private EditText etName, etDescription, etPrice;
    private Spinner categorySpinner;
    private Product product;
    private DatabaseHelper dbHelper;
    private List<Category> categories;

    public EditProductDialog(Product product) {
        this.product = product;
    }

    public void setOnProductEditedListener(OnProductEditedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = createDialogView();

        // تهيئة قاعدة البيانات
        dbHelper = new DatabaseHelper(getContext());

        // تحميل الفئات
        loadCategories();

        builder.setView(view)
                .setTitle("Edit Product")
                .setPositiveButton("Update", (dialog, id) -> {
                    String name = etName.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();

                    if (name.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double price = Double.parseDouble(priceStr);

                        // الحصول على الفئة المختارة
                        Category selectedCategory = (Category) categorySpinner.getSelectedItem();
                        int categoryId = selectedCategory != null ? selectedCategory.getId() : 1;

                        // تحديث المنتج في قاعدة البيانات
                        boolean updated = dbHelper.updateProduct(
                                product.getId(),
                                name,
                                description,
                                price,
                                dbHelper.getImagePath(product.getId()), // الاحتفاظ بنفس الصورة
                                categoryId // تحديث الفئة
                        );

                        if (updated) {
                            // إنشاء منتج محدث
                            Product updatedProduct = new Product(product.getId(), name, description,
                                    price, product.getImageResId(), categoryId);

                            Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();

                            if (listener != null) {
                                listener.onProductEdited(updatedProduct);
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to update product", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        return builder.create();
    }

    private View createDialogView() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        // حقل الاسم
        etName = new EditText(getContext());
        etName.setHint("Product Name *");
        etName.setText(product.getName());
        etName.setPadding(30, 30, 30, 30);
        layout.addView(etName);

        addSpace(layout);

        // حقل الوصف
        etDescription = new EditText(getContext());
        etDescription.setHint("Product Description");
        etDescription.setText(product.getDescription());
        etDescription.setPadding(30, 30, 30, 30);
        etDescription.setMinLines(2);
        layout.addView(etDescription);

        addSpace(layout);

        // حقل السعر
        etPrice = new EditText(getContext());
        etPrice.setHint("Price (SAR) *");
        etPrice.setText(String.valueOf(product.getPrice()));
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPrice.setPadding(30, 30, 30, 30);
        layout.addView(etPrice);

        addSpace(layout);

        // تسمية للفئات
        android.widget.TextView categoryLabel = new android.widget.TextView(getContext());
        categoryLabel.setText("📂 Category:");
        categoryLabel.setTextSize(16);
        categoryLabel.setTextColor(0xFF220F84);
        categoryLabel.setPadding(30, 10, 30, 10);
        layout.addView(categoryLabel);

        // Spinner للفئات
        categorySpinner = new Spinner(getContext());
        categorySpinner.setPadding(30, 20, 30, 20);
        android.widget.LinearLayout.LayoutParams spinnerParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        categorySpinner.setLayoutParams(spinnerParams);
        layout.addView(categorySpinner);

        addSpace(layout);

        return layout;
    }

    private void loadCategories() {
        try {
            categories = dbHelper.getAllCategories();

            if (categories != null && !categories.isEmpty()) {
                // إنشاء adapter للفئات
                ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(
                        getContext(),
                        android.R.layout.simple_spinner_item,
                        categories
                ) {
                    @Override
                    public View getView(int position, View convertView, android.view.ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        android.widget.TextView textView = (android.widget.TextView) view;
                        textView.setText(categories.get(position).getName());
                        textView.setTextSize(16);
                        textView.setPadding(20, 20, 20, 20);
                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        android.widget.TextView textView = (android.widget.TextView) view;
                        textView.setText("📂 " + categories.get(position).getName());
                        textView.setTextSize(15);
                        textView.setPadding(30, 25, 30, 25);

                        // تمييز الفئة الحالية
                        if (categories.get(position).getId() == product.getCategoryId()) {
                            textView.setBackgroundColor(0xFFE3F2FD);
                            textView.setTextColor(0xFF1976D2);
                        } else {
                            textView.setBackgroundColor(0xFFFFFFFF);
                            textView.setTextColor(0xFF000000);
                        }
                        return view;
                    }
                };

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);

                // تحديد الفئة الحالية للمنتج
                setCurrentCategory();
            } else {
                Toast.makeText(getContext(), "No categories available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading categories", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCurrentCategory() {
        try {
            if (categories != null && product.getCategoryId() > 0) {
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getId() == product.getCategoryId()) {
                        categorySpinner.setSelection(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // إذا فشل في العثور على الفئة، اختر الأولى كافتراضية
            if (categorySpinner.getAdapter() != null && categorySpinner.getAdapter().getCount() > 0) {
                categorySpinner.setSelection(0);
            }
        }
    }

    private void addSpace(android.widget.LinearLayout layout) {
        View space = new View(getContext());
        space.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 30));
        layout.addView(space);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // تنظيف المراجع
        etName = null;
        etDescription = null;
        etPrice = null;
        categorySpinner = null;
        dbHelper = null;
        categories = null;
    }
}