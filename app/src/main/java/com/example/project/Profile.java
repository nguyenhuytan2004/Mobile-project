package com.example.project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Profile extends Activity {
    private static final int PICK_IMAGE_REQUEST = 2;

    TextView btnBack, btnSave;
    TextView nameText, sexText, birthdayText;
    ImageView avatarImage;
    LinearLayout avatarLayout, nameLayout, sexLayout, birthdayLayout;
    SQLiteDatabase db;
    LoginSessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        avatarImage = findViewById(R.id.avatarImage);
        nameText = findViewById(R.id.nameText);
        sexText = findViewById(R.id.sexText);
        birthdayText = findViewById(R.id.birthdayText);

        avatarLayout = findViewById(R.id.avatarLayout);
        nameLayout = findViewById(R.id.nameLayout);
        sexLayout = findViewById(R.id.sexLayout);
        birthdayLayout = findViewById(R.id.birthdayLayout);

        sessionManager = LoginSessionManager.getInstance(this);
        loadUserData();

        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);

        btnBack.setOnClickListener(v -> {
            finish();
        });
        btnSave.setOnClickListener(v -> saveUserData());

        avatarLayout.setOnClickListener(v -> {
            openGallery();
        });
        nameLayout.setOnClickListener(v -> {
            showNameEditDialog();
        });
        sexLayout.setOnClickListener(v -> {
            showSexSelectionDialog();
        });
        birthdayLayout.setOnClickListener(v -> {
            showDatePickerDialog();
        });
    }

    private void loadUserData() {
        db = DatabaseHelper.getInstance(this).openDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_user_information WHERE user_id = ?",
                new String[]{String.valueOf(sessionManager.getUserId())});

        if (cursor.moveToFirst()) {
            String avatarPath = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));
            if (avatarPath != null && !avatarPath.isEmpty()) {
                try {
                    File imgFile = new File(avatarPath);
                    if (imgFile.exists()) {
                        // Set scale type to ensure the image fits within the circular frame
                        avatarImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        Uri imageUri = Uri.fromFile(imgFile);
                        avatarImage.setImageURI(imageUri);
                    } else {
                        avatarImage.setImageResource(R.drawable.ic_user_avatar);
                    }
                } catch (Exception e) {
                    avatarImage.setImageResource(R.drawable.ic_user_avatar);
                }
            } else {
                avatarImage.setImageResource(R.drawable.ic_user_avatar);
            }

            nameText.setText(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
            sexText.setText(cursor.getString(cursor.getColumnIndexOrThrow("sex")));
            birthdayText.setText(cursor.getString(cursor.getColumnIndexOrThrow("birthday")));
        }
        cursor.close();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);

                // Set the image to the avatar
                avatarImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                avatarImage.setImageBitmap(bitmap);
                avatarImage.setTag(selectedImageUri);

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Profile", "Error loading image");
            }
        }
    }
    private void showNameEditDialog() {
        // Tạo layout bằng code
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 10);
        layout.setBackgroundColor(Color.parseColor("#2C2C2C"));

        // Tạo EditText
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(nameText.getText().toString());
        input.setTextColor(Color.WHITE);
        input.setHint(getResources().getString(R.string.profile_input_hint));
        input.setHintTextColor(Color.GRAY);
        input.setPadding(20, 10, 10, 30);

        layout.addView(input);

        // Tạo dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.profile_full_name))
                .setView(layout)
                .setPositiveButton(getResources().getString(R.string.note_save), (d, which) -> {
                    String newName = input.getText().toString().trim();
                    nameText.setText(newName);
                })
                .setNegativeButton(getResources().getString(R.string.cancel), (d, which) -> d.dismiss())
                .create();

        // Bo góc + nền màu tối
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(createDarkRoundedBackground());
            }

            // Màu nút Positive/Negative
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R. color. statistics_blue));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.LTGRAY);
        });

        dialog.show();
    }

    private void showSexSelectionDialog() {
        final String[] options = {getResources().getString(R.string.profile_male), getResources().getString(R.string.profile_female)};

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.profile_sex))
                .setSingleChoiceItems(options,
                        sexText.getText().toString().equals(getResources().getString(R.string.profile_male)) ? 0 : 1,
                        (d, which) -> {
                            sexText.setText(options[which]);
                            sexText.setTextColor(Color.WHITE);
                            d.dismiss();
                        })
                .setNegativeButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                .create();

        // Dark mode style
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(createDarkRoundedBackground());
            }
            ListView listView = dialog.getListView();
            for (int i = 0; i < listView.getChildCount(); i++) {
                View item = listView.getChildAt(i);
                if (item instanceof TextView) {
                    ((TextView) item).setTextColor(Color.WHITE);
                }
            }
            listView.setBackgroundColor(Color.parseColor("#2C2C2C"));
            listView.setDivider(new ColorDrawable(Color.DKGRAY));
            listView.setDividerHeight(1);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.LTGRAY);
        });

        dialog.show();
    }

    private void showDatePickerDialog() {
        // Get current date
        String currentDate = birthdayText.getText().toString();
        String[] dateParts = currentDate.split(" - ");
        int day = 1, month = 1, year = 2000;

        try {
            if (dateParts.length == 3) {
                day = Integer.parseInt(dateParts[0].trim());
                month = Integer.parseInt(dateParts[1].trim());
                year = Integer.parseInt(dateParts[2].trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Profile", "Error parsing date");
        }

        View dialogView = getLayoutInflater().inflate(R.layout.date_picker_dialog, null);
        dialogView.setBackgroundColor(Color.parseColor("#2C2C2C"));

        final NumberPicker dayPicker = dialogView.findViewById(R.id.day_picker);
        final NumberPicker monthPicker = dialogView.findViewById(R.id.month_picker);
        final NumberPicker yearPicker = dialogView.findViewById(R.id.year_picker);

        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(31);
        dayPicker.setValue(day);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(month);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(year);

        setNumberPickerTextColor(dayPicker, Color.WHITE);
        setNumberPickerTextColor(monthPicker, Color.WHITE);
        setNumberPickerTextColor(yearPicker, Color.WHITE);

        dayPicker.setOnValueChangedListener((picker, oldVal, newVal) ->
                dayPicker.post(() -> setNumberPickerTextColor(dayPicker, Color.WHITE)));

        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            updateDaysInMonth(dayPicker, newVal, yearPicker.getValue());
            monthPicker.post(() -> setNumberPickerTextColor(monthPicker, Color.WHITE));
            dayPicker.post(() -> setNumberPickerTextColor(dayPicker, Color.WHITE));
        });

        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            updateDaysInMonth(dayPicker, monthPicker.getValue(), newVal);
            yearPicker.post(() -> setNumberPickerTextColor(yearPicker, Color.WHITE));
            dayPicker.post(() -> setNumberPickerTextColor(dayPicker, Color.WHITE));
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(getResources().getString(R.string.note_save), (dialogInterface, i) -> {
                    int selectedDay = dayPicker.getValue();
                    int selectedMonth = monthPicker.getValue();
                    int selectedYear = yearPicker.getValue();

                    String formattedDate = String.format(Locale.getDefault(),
                            "%02d - %02d - %04d", selectedDay, selectedMonth, selectedYear);
                    birthdayText.setText(formattedDate);
                })
                .setNegativeButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(createDarkRoundedBackground());
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R. color. statistics_blue));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.LTGRAY);
        });

        dialog.show();
    }
    private void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private Drawable createDarkRoundedBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(32f);
        drawable.setColor(Color.parseColor("#2C2C2C"));
        return drawable;
    }

    private void updateDaysInMonth(NumberPicker dayPicker, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        int currentDay = dayPicker.getValue();
        dayPicker.setMaxValue(daysInMonth);

        if (currentDay > daysInMonth) {
            dayPicker.setValue(daysInMonth);
        }
    }

    private void saveUserData() {
        try {
            db = DatabaseHelper.getInstance(this).openDatabase();

            ContentValues values = new ContentValues();
            values.put("full_name", nameText.getText().toString());
            values.put("sex", sexText.getText().toString());
            values.put("birthday", birthdayText.getText().toString());

            // Save avatar if it was changed
            if (avatarImage.getDrawable() != null && avatarImage.getTag() instanceof Uri) {
                Uri avatarUri = (Uri) avatarImage.getTag();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), avatarUri);
                    String filePath = saveImageToInternalStorage(bitmap);
                    values.put("avatar", filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            int rowsAffected = db.update("tbl_user_information",
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(sessionManager.getUserId())});

            if (rowsAffected > 0) {
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseHelper.getInstance(this).closeDatabase();
        }
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        File storageDir = getExternalFilesDir("UserAvatars");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File imageFile = new File(storageDir, imageFileName);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}