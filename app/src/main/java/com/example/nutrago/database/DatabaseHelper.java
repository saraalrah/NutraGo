package com.example.nutrago.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.nutrago.models.Product;
import com.example.nutrago.models.Category;
import com.example.nutrago.R;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "nutrago.db";
    private static final int DATABASE_VERSION = 2; // Increased version for migration

    // Products table
    private static final String TABLE_PRODUCTS = "products";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_IMAGE_PATH = "image_path";
    private static final String COLUMN_CATEGORY_ID = "category_id"; // Foreign key

    // Categories table
    private static final String TABLE_CATEGORIES = "categories";
    private static final String COLUMN_CATEGORY_NAME = "category_name";
    private static final String COLUMN_CATEGORY_DESCRIPTION = "category_description";
    private static final String COLUMN_CATEGORY_IMAGE = "category_image";

    // Cart table
    private static final String TABLE_CART = "cart";
    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_QUANTITY = "quantity";

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Create categories table first (referenced by products)
            String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_CATEGORIES + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_CATEGORY_NAME + " TEXT NOT NULL UNIQUE,"
                    + COLUMN_CATEGORY_DESCRIPTION + " TEXT,"
                    + COLUMN_CATEGORY_IMAGE + " TEXT"
                    + ")";
            db.execSQL(CREATE_CATEGORIES_TABLE);

            // Create products table with category reference
            String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT NOT NULL,"
                    + COLUMN_DESCRIPTION + " TEXT,"
                    + COLUMN_PRICE + " REAL NOT NULL,"
                    + COLUMN_IMAGE_PATH + " TEXT,"
                    + COLUMN_CATEGORY_ID + " INTEGER,"
                    + "FOREIGN KEY(" + COLUMN_CATEGORY_ID + ") REFERENCES "
                    + TABLE_CATEGORIES + "(" + COLUMN_ID + ")"
                    + ")";
            db.execSQL(CREATE_PRODUCTS_TABLE);

            // Create cart table
            String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_PRODUCT_ID + " INTEGER,"
                    + COLUMN_QUANTITY + " INTEGER DEFAULT 1,"
                    + "FOREIGN KEY(" + COLUMN_PRODUCT_ID + ") REFERENCES "
                    + TABLE_PRODUCTS + "(" + COLUMN_ID + ")"
                    + ")";
            db.execSQL(CREATE_CART_TABLE);

            // Insert default data
            insertDefaultCategories(db);
            insertDefaultProducts(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Product getProductById(int id) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_PRODUCTS, null,
                    COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID));

                cursor.close();
                int imageResId = getImageResourceId(imagePath);
                return new Product(id, name, description, price, imageResId, categoryId);
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 2) {
                // Create categories table
                String CREATE_CATEGORIES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + "("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_CATEGORY_NAME + " TEXT NOT NULL UNIQUE,"
                        + COLUMN_CATEGORY_DESCRIPTION + " TEXT,"
                        + COLUMN_CATEGORY_IMAGE + " TEXT"
                        + ")";
                db.execSQL(CREATE_CATEGORIES_TABLE);

                // Add category_id column to products table
                String ALTER_PRODUCTS_TABLE = "ALTER TABLE " + TABLE_PRODUCTS
                        + " ADD COLUMN " + COLUMN_CATEGORY_ID + " INTEGER";
                db.execSQL(ALTER_PRODUCTS_TABLE);

                // Insert default categories
                insertDefaultCategories(db);

                // Update existing products with default category (set to first category)
                updateExistingProductsWithCategory(db);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // If migration fails, recreate tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
            onCreate(db);
        }
    }

    // Category operations (Read-only)

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_CATEGORIES, null, null, null, null, null, COLUMN_CATEGORY_NAME);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_DESCRIPTION));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_IMAGE));

                    Category category = new Category(id, name, description, imagePath);
                    categories.add(category);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Get products by category (including "All" option)
    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> products = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor;

            if (categoryId == 0) { // Show all products
                cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null);
            } else { // Show products by specific category
                cursor = db.query(TABLE_PRODUCTS, null,
                        COLUMN_CATEGORY_ID + "=?", new String[]{String.valueOf(categoryId)},
                        null, null, null);
            }

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));

                    int imageResId = getImageResourceId(imagePath);
                    Product product = new Product(id, name, description, price, imageResId);
                    products.add(product);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    // Updated product operations to include category
    public long addProduct(String name, String description, double price, String imagePath, int categoryId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_DESCRIPTION, description);
            values.put(COLUMN_PRICE, price);
            values.put(COLUMN_IMAGE_PATH, imagePath);
            values.put(COLUMN_CATEGORY_ID, categoryId);
            return db.insert(TABLE_PRODUCTS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Overloaded method for backward compatibility
    public long addProduct(String name, String description, double price, String imagePath) {
        return addProduct(name, description, price, imagePath, 1); // Default to first category
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));

                    int imageResId = getImageResourceId(imagePath);
                    Product product = new Product(id, name, description, price, imageResId);
                    products.add(product);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean updateProduct(int id, String name, String description, double price, String imagePath, int categoryId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_DESCRIPTION, description);
            values.put(COLUMN_PRICE, price);
            values.put(COLUMN_IMAGE_PATH, imagePath);
            values.put(COLUMN_CATEGORY_ID, categoryId);

            int result = db.update(TABLE_PRODUCTS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Overloaded method for backward compatibility
    public boolean updateProduct(int id, String name, String description, double price, String imagePath) {
        return updateProduct(id, name, description, price, imagePath, 1); // Default category
    }

    public boolean deleteProduct(int id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            // First remove from cart
            db.delete(TABLE_CART, COLUMN_PRODUCT_ID + "=?", new String[]{String.valueOf(id)});
            // Then delete product
            int result = db.delete(TABLE_PRODUCTS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cart operations (unchanged)
    public boolean addToCart(int productId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            Cursor cursor = db.query(TABLE_CART, null,
                    COLUMN_PRODUCT_ID + "=?", new String[]{String.valueOf(productId)},
                    null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                int currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY));
                ContentValues values = new ContentValues();
                values.put(COLUMN_QUANTITY, currentQuantity + 1);
                cursor.close();
                int result = db.update(TABLE_CART, values, COLUMN_PRODUCT_ID + "=?",
                        new String[]{String.valueOf(productId)});
                return result > 0;
            } else {
                if (cursor != null) cursor.close();
                ContentValues values = new ContentValues();
                values.put(COLUMN_PRODUCT_ID, productId);
                values.put(COLUMN_QUANTITY, 1);
                long result = db.insert(TABLE_CART, null, values);
                return result != -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> getCartItems() {
        List<Product> cartItems = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();

            String query = "SELECT p.*, c." + COLUMN_QUANTITY + " FROM " + TABLE_PRODUCTS + " p " +
                    "INNER JOIN " + TABLE_CART + " c ON p." + COLUMN_ID + " = c." + COLUMN_PRODUCT_ID;

            Cursor cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
                    int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY));

                    int imageResId = getImageResourceId(imagePath);

                    for (int i = 0; i < quantity; i++) {
                        Product product = new Product(id, name, description, price, imageResId);
                        cartItems.add(product);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cartItems;
    }

    public boolean removeFromCart(int productId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            int result = db.delete(TABLE_CART, COLUMN_PRODUCT_ID + "=?", new String[]{String.valueOf(productId)});
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void clearCart() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_CART, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper methods
    private void insertDefaultCategories(SQLiteDatabase db) {
        try {
            ContentValues values = new ContentValues();

            // Fixed categories - cannot be modified by users
            values.put(COLUMN_CATEGORY_NAME, "Pre");
            values.put(COLUMN_CATEGORY_DESCRIPTION, "High-quality protein products for muscle building");
            values.put(COLUMN_CATEGORY_IMAGE, "protein_category");
            db.insert(TABLE_CATEGORIES, null, values);

            values.clear();
            values.put(COLUMN_CATEGORY_NAME, "Post");
            values.put(COLUMN_CATEGORY_DESCRIPTION, "Energy boosting supplements for intense workouts");
            values.put(COLUMN_CATEGORY_IMAGE, "preworkout_category");
            db.insert(TABLE_CATEGORIES, null, values);

            values.clear();
            values.put(COLUMN_CATEGORY_NAME, "Energy");
            values.put(COLUMN_CATEGORY_DESCRIPTION, "Nutritious snacks for active lifestyle");
            values.put(COLUMN_CATEGORY_IMAGE, "snacks_category");
            db.insert(TABLE_CATEGORIES, null, values);

            db.insert(TABLE_CATEGORIES, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertDefaultProducts(SQLiteDatabase db) {
        try {
            ContentValues values = new ContentValues();

            values.put(COLUMN_NAME, "Protein Bar");
            values.put(COLUMN_DESCRIPTION, "Delicious protein bar ideal for pre- or post-workout snacking.");
            values.put(COLUMN_PRICE, 15.00);
            values.put(COLUMN_IMAGE_PATH, "protein_bar");
            values.put(COLUMN_CATEGORY_ID, 3); // Healthy Snacks
            db.insert(TABLE_PRODUCTS, null, values);

            values.clear();
            values.put(COLUMN_NAME, "Pre-Workout");
            values.put(COLUMN_DESCRIPTION, "Boost your energy and performance before your workout with this fruity formula.");
            values.put(COLUMN_PRICE, 120.00);
            values.put(COLUMN_IMAGE_PATH, "preworkout");
            values.put(COLUMN_CATEGORY_ID, 2); // Pre-Workout
            db.insert(TABLE_PRODUCTS, null, values);

            values.clear();
            values.put(COLUMN_NAME, "Protein Powder");
            values.put(COLUMN_DESCRIPTION, "Premium protein powder to fuel your recovery and support muscle growth.");
            values.put(COLUMN_PRICE, 150.00);
            values.put(COLUMN_IMAGE_PATH, "protein_powder");
            values.put(COLUMN_CATEGORY_ID, 1); // Protein Supplements
            db.insert(TABLE_PRODUCTS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateExistingProductsWithCategory(SQLiteDatabase db) {
        try {
            // Set default category (first category) for existing products without category
            ContentValues values = new ContentValues();
            values.put(COLUMN_CATEGORY_ID, 1); // Default to first category
            db.update(TABLE_PRODUCTS, values, COLUMN_CATEGORY_ID + " IS NULL", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getImageResourceId(String imagePath) {
        try {
            switch (imagePath) {
                case "protein_bar":
                    return R.drawable.protein_bar;
                case "preworkout":
                    return R.drawable.preworkout;
                case "protein_powder":
                    return R.drawable.protein_powder;
                default:
                    return getCustomImageResourceId(imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.drawable.ic_launcher_background;
        }
    }

    private int getCustomImageResourceId(String imagePath) {
        return R.drawable.ic_launcher_background; // Fallback for custom images
    }

    public String getImagePath(int productId) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_PRODUCTS,
                    new String[]{COLUMN_IMAGE_PATH},
                    COLUMN_ID + "=?",
                    new String[]{String.valueOf(productId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
                cursor.close();
                return imagePath;
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "protein_bar"; // default
    }

    public Category getCategoryById(int categoryId) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_CATEGORIES, null,
                    COLUMN_ID + "=?", new String[]{String.valueOf(categoryId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_DESCRIPTION));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_IMAGE));
                cursor.close();
                return new Category(categoryId, name, description, imagePath);
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}