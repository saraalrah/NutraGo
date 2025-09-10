package com.example.nutrago.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrago.R;
import com.example.nutrago.database.DatabaseHelper;
import com.example.nutrago.models.Product;
import com.example.nutrago.dialogs.EditProductDialog;
import com.example.nutrago.ui.details.ProductDetailsFragment;

import java.io.File;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private DatabaseHelper dbHelper;

    // Interface للإبلاغ عن حذف المنتج
    public interface OnProductDeletedListener {
        void onProductDeleted();
    }

    // Interface للإبلاغ عن تعديل المنتج
    public interface OnProductEditedListener {
        void onProductEdited();
    }

    private OnProductDeletedListener deleteListener;
    private OnProductEditedListener editListener;

    public void setOnProductDeletedListener(OnProductDeletedListener listener) {
        this.deleteListener = listener;
    }

    public void setOnProductEditedListener(OnProductEditedListener listener) {
        this.editListener = listener;
    }

    // Constructor يقبل قاعدة البيانات
    public ProductAdapter(Context context, List<Product> productList, DatabaseHelper dbHelper) {
        this.context = context;
        this.productList = productList;
        this.dbHelper = dbHelper;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView name, description, price;
        Button addButton;
        ImageButton deleteButton;
        ImageButton editButton; // زر التعديل الجديد

        public ProductViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            name = itemView.findViewById(R.id.productName);
            description = itemView.findViewById(R.id.productDescription);
            price = itemView.findViewById(R.id.productPrice);
            addButton = itemView.findViewById(R.id.addToCartButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton); // ربط زر التعديل
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = productList.get(position);

        // تحميل الصورة (افتراضية أو مخصصة)
        loadProductImage(holder.productImage, p);

        holder.name.setText(p.getName());
        holder.description.setText(p.getDescription());
        holder.price.setText(String.format("%.2f SAR", p.getPrice()));

        // إضافة وظيفة الانتقال لصفحة التفاصيل عند الضغط على الكارت
        holder.itemView.setOnClickListener(v -> {
            try {
                if (context instanceof androidx.fragment.app.FragmentActivity) {
                    androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) context;

                    // إنشاء fragment جديد لتفاصيل المنتج
                    ProductDetailsFragment detailsFragment =
                            ProductDetailsFragment.newInstance(p.getId());

                    // الانتقال لصفحة التفاصيل
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment_activity_main, detailsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Error opening product details", Toast.LENGTH_SHORT).show();
            }
        });

        // وظيفة زر إضافة للسلة المحسنة
        holder.addButton.setOnClickListener(v -> {
            try {
                if (dbHelper != null) {
                    boolean success = dbHelper.addToCart(p.getId());
                    if (success) {
                        // تأثير بصري على الزر
                        holder.addButton.setText("✓ Added!");
                        holder.addButton.setBackgroundColor(0xFF4CAF50); // أخضر

                        // إعادة النص بعد ثانيتين
                        holder.addButton.postDelayed(() -> {
                            holder.addButton.setText("Add to Cart");
                            holder.addButton.setBackgroundColor(0xFF009688); // اللون الأصلي
                        }, 2000);

                        Toast.makeText(context, "✅ " + p.getName() + " تم إضافته للسلة", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "❌ فشل في إضافة المنتج للسلة", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "⚠️ قاعدة البيانات غير متاحة", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "❌ خطأ في إضافة المنتج", Toast.LENGTH_SHORT).show();
            }
        });

        // وظيفة زر التعديل الجديدة
        holder.editButton.setOnClickListener(v -> {
            showEditProductDialog(p, position);
        });

        // وظيفة زر الحذف
        holder.deleteButton.setOnClickListener(v -> {
            showDeleteConfirmationDialog(p, position);
        });
    }

    private void showEditProductDialog(Product product, int position) {
        EditProductDialog dialog = new EditProductDialog(product);
        dialog.setOnProductEditedListener(updatedProduct -> {
            // تحديث المنتج في القائمة المحلية
            productList.set(position, updatedProduct);
            notifyItemChanged(position);

            Toast.makeText(context, "تم تحديث " + updatedProduct.getName() + " بنجاح", Toast.LENGTH_SHORT).show();

            // إبلاغ Fragment بالتعديل
            if (editListener != null) {
                editListener.onProductEdited();
            }
        });

        // عرض الحوار في السياق المناسب
        if (context instanceof androidx.fragment.app.FragmentActivity) {
            dialog.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "EditProductDialog");
        }
    }

    private void showDeleteConfirmationDialog(Product product, int position) {
        new AlertDialog.Builder(context)
                .setTitle("حذف المنتج")
                .setMessage("هل أنت متأكد من حذف \"" + product.getName() + "\"؟")
                .setPositiveButton("حذف", (dialog, which) -> {
                    deleteProduct(product, position);
                })
                .setNegativeButton("إلغاء", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    private void deleteProduct(Product product, int position) {
        if (dbHelper != null) {
            boolean deleted = dbHelper.deleteProduct(product.getId());

            if (deleted) {
                productList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, productList.size());

                Toast.makeText(context, "تم حذف " + product.getName() + " بنجاح", Toast.LENGTH_SHORT).show();

                if (deleteListener != null) {
                    deleteListener.onProductDeleted();
                }
            } else {
                Toast.makeText(context, "فشل في حذف المنتج", Toast.LENGTH_SHORT).show();
            }
        } else {
            productList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, productList.size());
            Toast.makeText(context, "تم حذف " + product.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProductImage(ImageView imageView, Product product) {
        try {
            if (dbHelper != null) {
                String imagePath = dbHelper.getImagePath(product.getId());

                if (isDefaultImage(imagePath)) {
                    imageView.setImageResource(product.getImageResId());
                } else {
                    loadCustomImage(imageView, imagePath);
                }
            } else {
                imageView.setImageResource(product.getImageResId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(product.getImageResId());
        }
    }

    private boolean isDefaultImage(String imagePath) {
        return imagePath != null && (
                imagePath.equals("protein_bar") ||
                        imagePath.equals("preworkout") ||
                        imagePath.equals("protein_powder")
        );
    }

    private void loadCustomImage(ImageView imageView, String imagePath) {
        try {
            File file = new File(context.getFilesDir(), imagePath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.ic_launcher_background);
                }
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_background);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}