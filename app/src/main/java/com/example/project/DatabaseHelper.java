package com.example.project;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "mobile_db.db";
    private static final int DATABASE_VERSION = 3;
    private final Context context;
    private final String DATABASE_PATH;
    private static DatabaseHelper instance;
    private SQLiteDatabase database;

    // Singleton để đảm bảo chỉ có một instance duy nhất
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.DATABASE_PATH = context.getApplicationInfo().dataDir + "/databases/";

        Log.d(TAG, "Database path: " + DATABASE_PATH);
        copyDatabaseFromAssets();
    }

    private void copyDatabaseFromAssets() {
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
        if (dbFile.exists()) {
            Log.d(TAG, "Database đã tồn tại trên thiết bị, không cần sao chép");
            return;
        }

        try {
            File dbDir = new File(DATABASE_PATH);
            if (!dbDir.exists()) {
                if (!dbDir.mkdirs()) {
                    Log.e(TAG, "Không thể tạo thư mục databases/");
                    return;
                }
            }

            Log.d(TAG, "Sao chép database từ assets...");
            InputStream inputStream = context.getAssets().open(DATABASE_NAME);
            OutputStream outputStream = new FileOutputStream(dbFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
            Log.d(TAG, "Sao chép database thành công!");

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi sao chép database", e);
        }
    }

    public synchronized SQLiteDatabase openDatabase() {
        if (database == null || !database.isOpen()) {
            File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
            if (!dbFile.exists()) {
                Log.e(TAG, "Database không tồn tại trên thiết bị");
                return null;
            }

            try {
                database = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
                Log.d(TAG, "Database đã mở thành công!");
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi mở database", e);
            }
        }
        return database;
    }

    public boolean isPremiumUser(Context context, int userId) {
        SQLiteDatabase db = null;
        boolean isPremium = false;

        try {
            db = DatabaseHelper.getInstance(context).openDatabase();

            // Query the correct column (is_premium) for the specified user
            Cursor cursor = db.rawQuery(
                    "SELECT is_premium FROM tbl_user WHERE id = ?", 
                    new String[] { String.valueOf(userId) });

            if (cursor.moveToFirst()) {
                isPremium = cursor.getInt(0) == 1;
            }

            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking premium status", e);
        } finally {
            if (db != null) {
                DatabaseHelper.getInstance(context).closeDatabase();
            }
        }

        return isPremium;
    }

    // Add this static method
    public static boolean isPremiumUser(Context context) {
        // Get current user ID (assuming you store it somewhere like SharedPreferences)
        LoginSessionManager loginSessionManager = LoginSessionManager.getInstance(context);
        int currentUserId = loginSessionManager.getUserId();

        // Call the instance method with both parameters
        return getInstance(context).isPremiumUser(context, currentUserId);
    }

    public void markTaskAsCompleted(int taskId, boolean isCompleted) {
        SQLiteDatabase db = null;
        try {
            db = openDatabase();
            ContentValues values = new ContentValues();
            values.put("is_completed", isCompleted ? 1 : 0);

            // Store completion datetime when marking as completed
            if (isCompleted) {
                // Get current datetime in ISO 8601 format
                String completionDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                        .format(new Date());
                values.put("completion_datetime", completionDateTime);
            } else {
                // If marking as not completed, clear the completion datetime
                values.putNull("completion_datetime");
            }

            db.update("tbl_task", values, "id = ?", new String[] {String.valueOf(taskId)});
        } catch (Exception e) {
            Log.e(TAG, "Error marking task as completed", e);
        } finally {
            if (db != null) {
                closeDatabase();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Không cần tạo bảng vì database đã có sẵn trong `assets/`
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xử lý nâng cấp database nếu cần
    }

    // Đóng database khi không còn sử dụng
    public synchronized void closeDatabase() {
        if (database != null && database.isOpen()) {
            database.close();
            database = null;
            Log.d(TAG, "Database đã được đóng.");
        }
    }
}
