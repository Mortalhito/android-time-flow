// DatabaseHelper.java
package com.example.timeflow.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.timeflow.entity.Category;
import com.example.timeflow.entity.CountdownEvent;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "countdown.db";
    private static final int DATABASE_VERSION = 2;
    private static final String KEY_EVENT_CATEGORY_ID = "category_id";
    // 事件表
    private static final String TABLE_EVENTS = "events";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_TARGET_DATE = "target_date";
    private static final String COLUMN_CATEGORY_ID = "category_id";

    // 分类表
    private static final String TABLE_CATEGORIES = "categories";
    private static final String COLUMN_CATEGORY_NAME = "category_name";
    private static final String COLUMN_COLOR = "color";
    private static final String COLUMN_IS_DEFAULT = "is_default";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建分类表
        String createCategoryTable = "CREATE TABLE " + TABLE_CATEGORIES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_CATEGORY_NAME + " TEXT," +
                COLUMN_COLOR + " INTEGER," +
                COLUMN_IS_DEFAULT + " INTEGER DEFAULT 0)";
        db.execSQL(createCategoryTable);

        // 创建事件表
        String createEventTable = "CREATE TABLE " + TABLE_EVENTS + "(" +
                COLUMN_ID + " TEXT PRIMARY KEY," +
                COLUMN_NAME + " TEXT," +
                COLUMN_CATEGORY_ID + " INTEGER," +
                COLUMN_TARGET_DATE + " TEXT," +
                "FOREIGN KEY(" + COLUMN_CATEGORY_ID + ") REFERENCES " +
                TABLE_CATEGORIES + "(" + COLUMN_ID + "))";
        db.execSQL(createEventTable);

        // 插入默认分类
        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        String[] defaultCategories = {"生活", "工作", "纪念日"};
        int[] defaultColors = {0xFF42A5F5, 0xFF66BB6A, 0xFFFFA726}; // 蓝、绿、橙

        for (int i = 0; i < defaultCategories.length; i++) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CATEGORY_NAME, defaultCategories[i]);
            values.put(COLUMN_COLOR, defaultColors[i]);
            values.put(COLUMN_IS_DEFAULT, 1);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    // 事件相关方法
    public void addEvent(CountdownEvent event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, event.getId());
        values.put(COLUMN_NAME, event.getName());
        values.put(COLUMN_CATEGORY_ID, event.getCategoryId());
        values.put(COLUMN_TARGET_DATE, event.getTargetDate());
        db.insert(TABLE_EVENTS, null, values);
        db.close();
    }

    public Category getCategoryById(int categoryId) {
        Category category = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});

        if (cursor.moveToFirst()) {
            category = new Category(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR))
            );
        }

        cursor.close();
        return category;
    }

    public CountdownEvent getEventById(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return null;
        }

        CountdownEvent event = null;
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT e.*, c." + COLUMN_CATEGORY_NAME + ", c." + COLUMN_COLOR +
                    " FROM " + TABLE_EVENTS + " e " +
                    "LEFT JOIN " + TABLE_CATEGORIES + " c ON e." + COLUMN_CATEGORY_ID + " = c." + COLUMN_ID +
                    " WHERE e." + COLUMN_ID + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{eventId});

            if (cursor.moveToFirst()) {
                event = new CountdownEvent();
                event.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                event.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                event.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)));
                event.setTargetDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TARGET_DATE)));
                event.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)));
                event.setCategoryColor(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR)));

                // 计算天数
                event.calculateDaysLeft();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return event;
    }

    // 在 DatabaseHelper 类中添加以下方法
    public boolean isCategoryUsedByEvents(long categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT COUNT(*) FROM " + TABLE_EVENTS +
                    " WHERE " + COLUMN_CATEGORY_ID + " = ?"; // 使用正确的列名
            cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});

            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                return count > 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close(); // 确保关闭数据库
        }
        return false;
    }

    public List<CountdownEvent> getAllEvents() {
        List<CountdownEvent> events = new ArrayList<>();
        String query = "SELECT e.*, c." + COLUMN_CATEGORY_NAME + ", c." + COLUMN_COLOR +
                " FROM " + TABLE_EVENTS + " e " +
                "LEFT JOIN " + TABLE_CATEGORIES + " c ON e." + COLUMN_CATEGORY_ID + " = c." + COLUMN_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                CountdownEvent event = new CountdownEvent();
                event.setId(cursor.getString(0));
                event.setName(cursor.getString(1));
                event.setCategoryId(cursor.getInt(2));
                event.setTargetDate(cursor.getString(3));
                event.setCategoryName(cursor.getString(4));
                event.setCategoryColor(cursor.getInt(5));
                events.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return events;
    }

    public void deleteEvent(String eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EVENTS, COLUMN_ID + " = ?", new String[]{eventId});
        db.close();
    }

    // 分类相关方法
    public long addCategory(String name, int color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);
        values.put(COLUMN_COLOR, color);
        long id = db.insert(TABLE_CATEGORIES, null, values);
        db.close();
        return id;
    }

    public boolean deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 首先检查是否有事件使用这个分类
        Cursor cursor = db.query(TABLE_EVENTS, null,
                COLUMN_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(categoryId)}, null, null, null);

        if (cursor.getCount() > 0) {
            // 如果有事件使用该分类，不能删除
            cursor.close();
            db.close();
            return false;
        }
        cursor.close();

        // 删除分类（只能删除非默认分类）
        db.delete(TABLE_CATEGORIES,
                COLUMN_ID + " = ? AND " + COLUMN_IS_DEFAULT + " = 0",
                new String[]{String.valueOf(categoryId)});
        db.close();

        return true;
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_CATEGORIES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(cursor.getInt(0));
                category.setName(cursor.getString(1));
                category.setColor(cursor.getInt(2));
                category.setDefault(cursor.getInt(3) == 1);
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }
}