package com.example.nutrago.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.DialogFragment;

import com.example.nutrago.R;
import com.example.nutrago.database.DatabaseHelper;
import com.example.nutrago.models.Category;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class AddProductDialog extends DialogFragment {

    public interface OnProductAddedListener {
        void onProductAdded();
    }

    private OnProductAddedListener listener;
    private EditText etName, etDescription, etPrice;
    private Spinner categorySpinner;
    private ImageView imagePreview;
    private Button selectImageButton;
    private String selectedImagePath = "protein_bar"; // default
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private DatabaseHelper dbHelper;
    private List<Category> categories;

    public void setOnProductAddedListener(OnProductAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // تهيئة قاعدة البيانات
        dbHelper = new DatabaseHelper(getContext());

        // إعداد مشغل اختيار الصورة
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                // عرض الصورة في المعاينة
                                InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                imagePreview.setImageBitmap(bitmap);
                                imagePreview.setVisibility(View.VISIBLE);

                                // حفظ الصورة محلياً
                                selectedImagePath = saveImageToInternalStorage(bitmap);
                                selectImageButton.setText("Change Image");

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        View view = createDialogView();

        builder.setView(view)
                .setTitle("Add New Product")
                .setPositiveButton("Add", (dialog, id) -> {
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

                        long result = dbHelper.addProduct(name, description, price, selectedImagePath, categoryId);

                        if (result != -1) {
                            Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onProductAdded();
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to add product", Toast.LENGTH_SHORT).show();
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
        etName.setPadding(30, 30, 30, 30);
        layout.addView(etName);

        addSpace(layout);

        // حقل الوصف
        etDescription = new EditText(getContext());
        etDescription.setHint("Product Description");
        etDescription.setPadding(30, 30, 30, 30);
        etDescription.setMinLines(2);
        layout.addView(etDescription);

        addSpace(layout);

        // حقل السعر
        etPrice = new EditText(getContext());
        etPrice.setHint("Price (SAR) *");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPrice.setPadding(30, 30, 30, 30);
        layout.addView(etPrice);

        addSpace(layout);

        // تسمية للفئات
        android.widget.TextView categoryLabel = new android.widget.TextView(getContext());
        categoryLabel.setText("📂 Select Category:");
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

        // تحميل الفئات
        loadCategories();

        addSpace(layout);

        // زر اختيار الصورة
        selectImageButton = new Button(getContext());
        selectImageButton.setText("Select Product Image");
        selectImageButton.setBackgroundColor(0xFF009688);
        selectImageButton.setTextColor(0xFFFFFFFF);
        selectImageButton.setPadding(30, 20, 30, 20);
        selectImageButton.setOnClickListener(v -> openImagePicker());
        layout.addView(selectImageButton);

        addSpace(layout);

        // معاينة الصورة
        imagePreview = new ImageView(getContext());
        imagePreview.setLayoutParams(new android.widget.LinearLayout.LayoutParams(300, 300));
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imagePreview.setVisibility(View.GONE);
        imagePreview.setBackgroundColor(0xFFEEEEEE);
        layout.addView(imagePreview);

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
                        textView.setBackgroundColor(0xFFFFFFFF);
                        textView.setTextColor(0xFF000000);
                        return view;
                    }
                };

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);

                // اختيار الفئة الأولى كافتراضية
                if (categories.size() > 0) {
                    categorySpinner.setSelection(0);
                }
            } else {
                Toast.makeText(getContext(), "No categories available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading categories", Toast.LENGTH_SHORT).show();
        }
    }

    private void addSpace(android.widget.LinearLayout layout) {
        View space = new View(getContext());
        space.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 30));
        layout.addView(space);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            // إنشاء اسم ملف فريد
            String fileName = "product_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getContext().getFilesDir(), fileName);

            // حفظ الصورة
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();

            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return "protein_bar"; // fallback to default
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // تنظيف المراجع
        etName = null;
        etDescription = null;
        etPrice = null;
        categorySpinner = null;
        imagePreview = null;
        selectImageButton = null;
        dbHelper = null;
        categories = null;
    }
}