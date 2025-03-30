package com.example.project;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class TestScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();

        int rowAffected = db.delete("tbl_user", "id = ?", new String[]{"1"});
        if (rowAffected > 0) {
            Log.d("TestScreen", "Xoá thành công bản ghi có id = 1");
        } else {
            Log.d("TestScreen", "Không tìm thấy bản ghi có id = 1");
        }

        Cursor cursor = db.rawQuery("SELECT * FROM tbl_user", null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(1);
                Log.i("DatabaseHelper", "Username: " + name);
            } while (cursor.moveToNext());
        }
        cursor.close();

        db.close();
    }
}

