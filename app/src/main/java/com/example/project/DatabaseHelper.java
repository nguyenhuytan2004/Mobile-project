package com.example.project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper"; // Tag để Log dễ đọc
    private static final String DATABASE_NAME = "mobile_db.db"; // Tên file .db
    private static final int DATABASE_VERSION = 1;
    private final Context context;
    private final String DATABASE_PATH;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.DATABASE_PATH = context.getApplicationInfo().dataDir + "/databases/";

        Log.d(TAG, "📌 Database path: " + DATABASE_PATH);
        copyDatabase();
    }

    private void copyDatabase() {
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
        if (dbFile.exists()) {
            Log.d(TAG, "Database đã tồn tại, xoá để cập nhật lại...");
            dbFile.delete();
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

    public SQLiteDatabase openDatabase() {
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
        if (!dbFile.exists()) {
            Log.e(TAG, "Database không tồn tại: " + dbFile.getAbsolutePath());
            return null;
        }

        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
            Log.d(TAG, "Database đã mở thành công!");
            return db;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi mở database", e);
            return null;
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
}