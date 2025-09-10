package com.example.nutrago.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrago.R;
import com.example.nutrago.database.DatabaseHelper;
import com.example.nutrago.models.Product;

import java.io.File;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<Product> cartList;
    private DatabaseHelper dbHelper;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    private OnCartChangeListener cartChangeListener;

    public void setOnCartChangeListener(OnCartChangeListener listener) {
        this.cartChangeListener = listener;
    }

    // Constructor مع قاعدة البيانات
    public CartAdapter(Context context, List<Product> cartList, DatabaseHelper dbHelper) {
        this.context = context;
        this.cartList = cartList;
        this.dbHelper = dbHelper;
    }

    // Constructor بدون قاعدة البيانات (احتياطي)
    public CartAdapter(Context context, List<Product> cartList) {
        this.context = context;
        this.cartList = cartList;
        this.dbHelper = null;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView name, price;
        ImageButton removeButton; // زر الحذف من السلة

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.cartItemImage);
            name = itemView.findViewById(R.id.cartItemName);
            price = itemView.findViewById(R.id.cartItemPrice);
            removeButton = itemView.findViewById(R.id.removeFromCartButton);
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartList.get(position);

        // تحميل الصورة
        loadProductImage(holder.productImage, product);

        holder.name.setText(product.getName());
        holder.price.setText(String.format("%.2f ريال", product.getPrice()));

        // وظيفة زر الحذف من السلة
        if (holder.removeButton != null) {
            holder.removeButton.setOnClickListener(v -> {
                removeFromCart(product, position);
            });
        }

        // إشعار المستمع بالتغيير
        if (cartChangeListener != null) {
            cartChangeListener.onCartChanged();
        }
    }

    private void removeFromCart(Product product, int position) {
        try {
            if (dbHelper != null) {
                boolean removed = dbHelper.removeFromCart(product.getId());
                if (removed) {
                    cartList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cartList.size());

                    Toast.makeText(context, "❌ تم حذف " + product.getName() + " من السلة", Toast.LENGTH_SHORT).show();

                    if (cartChangeListener != null) {
                        cartChangeListener.onCartChanged();
                    }
                } else {
                    Toast.makeText(context, "فشل في حذف المنتج", Toast.LENGTH_SHORT).show();
                }
            } else {
                // النظام الاحتياطي
                cartList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartList.size());
                Toast.makeText(context, "تم حذف " + product.getName(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "خطأ في حذف المنتج", Toast.LENGTH_SHORT).show();
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
        return cartList.size();
    }
}