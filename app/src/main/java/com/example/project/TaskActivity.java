package com.example.project;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class TaskActivity extends  AppCompatActivity{

    ImageView btnDate, btnTag, btnImage;
    TextView txtDate, btnBack, btnOption, btnSave;
    EditText titleInput, contentInput;
    FlexboxLayout tagContainer, attachmentContainer;

    SQLiteDatabase db;
    String taskId;

    private String reminderTime = "";
    private int reminderDaysBefore = 0;
    private boolean reminderRepeatEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task);

        txtDate = findViewById(R.id.txtDate);
        btnBack = findViewById(R.id.textView);
        btnOption = findViewById(R.id.textView2);
        btnSave = findViewById(R.id.saveButton);

        titleInput = findViewById(R.id.titleInput);
        contentInput = findViewById(R.id.contentInput);

        btnDate = findViewById(R.id.icon4);
        btnTag = findViewById(R.id.cardIcon);
        btnImage = findViewById(R.id.imageIcon);

        tagContainer = findViewById(R.id.tagContainer);
        attachmentContainer = findViewById(R.id.attachmentContainer);

        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        taskId = getIntent().getStringExtra("taskId");

        if (title != null || content != null) {
            // Mở từ NoteActivity → dùng dữ liệu từ intent
            if (title != null) titleInput.setText(title);
            if (content != null) contentInput.setText(content);
        } else {
            Log.d("TaskActivity", "Mở từ danh sách, taskId: " + taskId);
            if (taskId != null) {
                loadTask(taskId);
            } else {
                Log.e("TaskActivity", "Không có dữ liệu để hiển thị task!");
            }
        }

        btnDate.setOnClickListener(view -> {
            SetReminderDialog dialog = new SetReminderDialog(taskId);
            dialog.setOnDateSelectedListener(new SetReminderDialog.OnReminderSettingsListener() {
                @Override
                public void onReminderSet(String date, String time, int daysBefore, boolean isRepeat) {
                    if (date.isEmpty()) {
                        txtDate.setText("");
                        reminderTime = "";
                        reminderDaysBefore = 0;
                        reminderRepeatEnabled = false;
                        return;
                    }

                    // Store the reminder settings
                    reminderTime = time;
                    reminderDaysBefore = daysBefore;
                    reminderRepeatEnabled = isRepeat;

                    // Existing date formatting code
                    LocalDate selectedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    LocalDate today = LocalDate.now();

                    int day = selectedDate.getDayOfMonth();
                    int month = selectedDate.getMonthValue();
                    txtDate.setText("Ngày " + day + ", tháng " + month);

                    if (selectedDate.isBefore(today)) {
                        txtDate.setTextColor(getResources().getColor(R.color.red));
                    } else {
                        txtDate.setTextColor(getResources().getColor(R.color.statistics_blue));
                    }
                }
            });
            dialog.show(getSupportFragmentManager(), "SetReminderDialogFragment");
        });

        btnBack.setOnClickListener(view -> {
            finish();
        });

        // Trong btnOption.setOnClickListener
        btnOption.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TaskActivity.this);
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_in_task, null);
            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.show();

            // Lấy các phần tử trong BottomSheet
            TextView pinTask = sheetView.findViewById(R.id.pinTxt);
            TextView convertTask = sheetView.findViewById(R.id.convertTxt);
            TextView deleteTask = sheetView.findViewById(R.id.deleteTxt);

            // Xử lý sự kiện
            pinTask.setOnClickListener(v -> {
                Toast.makeText(TaskActivity.this, "Nhiệm vụ đã được ghim!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });

            convertTask.setOnClickListener(v -> {
                Toast.makeText(TaskActivity.this, "Nhiệm vụ đã được chuyển thành ghi chú!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });

            deleteTask.setOnClickListener(v -> {
                db = DatabaseHelper.getInstance(TaskActivity.this).openDatabase();
                db.beginTransaction();
                try {
                    // Xóa các dữ liệu liên quan đến task
                    db.delete("tbl_task_reminder", "task_id = ?", new String[]{taskId});
                    db.delete("tbl_task_photo", "task_id = ?", new String[]{taskId});
                    db.delete("tbl_task_tag", "task_id = ?", new String[]{taskId});
                    db.delete("tbl_task", "id = ?", new String[]{taskId});

                    db.setTransactionSuccessful();
                    Log.d("TaskActivity", "Task deleted successfully");

                    ReminderService.cancelReminder(TaskActivity.this, taskId);
                } catch (Exception e) {
                    Log.e("TaskActivity", "Error deleting task", e);
                } finally {
                    db.endTransaction();
                    DatabaseHelper.getInstance(TaskActivity.this).closeDatabase();
                }

                bottomSheetDialog.dismiss();
                finish();
            });
        });


        btnTag.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TaskActivity.this);
            View sheetView = getLayoutInflater().inflate(R.layout.add_card_bottom_sheet_in_note, null);
            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.show();

            EditText tagInput = sheetView.findViewById(R.id.cardInputetxt);
            tagInput.requestFocus();

            TextView doneBtn = sheetView.findViewById(R.id.doneTxt);
            tagContainer.setFlexWrap(FlexWrap.WRAP);

            doneBtn.setOnClickListener(v -> {
                String tagText = tagInput.getText().toString();

                if (!tagText.isEmpty()) {
                    TextView tag = new TextView(TaskActivity.this);
                    tag.setText(tagText);
                    tag.setTextSize(18);
                    tag.setTextColor(Color.WHITE);

                    int[] colors = {
                            Color.rgb(52, 152, 219),
                            Color.rgb(46, 204, 113),
                            Color.rgb(231, 76, 60),
                            Color.rgb(155, 89, 182),
                            Color.rgb(241, 196, 15),
                            Color.rgb(26, 188, 156),
                            Color.rgb(230, 126, 34),
                            Color.rgb(22, 160, 133),
                            Color.rgb(142, 68, 173),
                            Color.rgb(192, 57, 43),
                            Color.rgb(44, 62, 80),
                            Color.rgb(127, 140, 141)
                    };
                    int randomColor = colors[new Random().nextInt(colors.length)];

                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setColor(randomColor);
                    drawable.setCornerRadius(50);

                    tag.setBackground(drawable);

                    FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                            FlexboxLayout.LayoutParams.WRAP_CONTENT,
                            FlexboxLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 10, 10);
                    tag.setLayoutParams(params);

                    tag.setPadding(30, 10, 25, 10);
                    tagContainer.addView(tag);

                    bottomSheetDialog.dismiss();
                } else {
                    Toast.makeText(TaskActivity.this, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
                }
            });
        });


        btnImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100);
        });

        btnSave.setOnClickListener(view -> saveTask());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            try {
                // Copy image to app's private storage
                String fileName = "note_img_" + System.currentTimeMillis() + ".jpg";
                Uri localUri = copyImageToAppStorage(selectedImageUri, fileName);

                ImageView attachedPhoto = new ImageView(this);
                attachedPhoto.setImageURI(localUri);
                attachedPhoto.setTag(localUri.toString()); // Store local URI for saving

                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(300, 300);
                params.setMargins(0, 0, 10, 10);
                attachedPhoto.setLayoutParams(params);

                attachmentContainer.addView(attachedPhoto);
            } catch (IOException e) {
                Log.e("NoteActivity", "Error copying image", e);
                Toast.makeText(this, "Không thể thêm ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper method to copy image to app storage
    private Uri copyImageToAppStorage(Uri sourceUri, String fileName) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(sourceUri);
        File destFile = new File(getFilesDir(), fileName);

        FileOutputStream outputStream = new FileOutputStream(destFile);
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        // Return URI to file in app storage
        return Uri.fromFile(destFile);
    }

    private void loadTask(String taskId) {
        db = DatabaseHelper.getInstance(this).openDatabase();

        String query = "SELECT t.id, t.title, t.content, r.date " +
                "FROM tbl_task t " +
                "LEFT JOIN tbl_task_reminder r ON t.id = r.task_id " +
                "WHERE t.id = ? " +
                "ORDER BY t.id DESC";

        Log.d("SQL", "Executing main task query: " + query + " [taskId=" + taskId + "]");

        Cursor cursor = db.rawQuery(query, new String[]{taskId});
        if (cursor != null && cursor.moveToFirst()) {
            titleInput.setText(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            contentInput.setText(cursor.getString(cursor.getColumnIndexOrThrow("content")));

            String date = "";
            int dateColumnIndex = cursor.getColumnIndexOrThrow("date");
            if (!cursor.isNull(dateColumnIndex)) {
                date = cursor.getString(dateColumnIndex);
            }
            txtDate.setText(date);

            if (!date.isEmpty()) {
                Pattern pattern = Pattern.compile("(\\d+)");
                Matcher matcher = pattern.matcher(date);

                int day = 0, month = 0;
                if (matcher.find()) {
                    day = Integer.parseInt(matcher.group(1));
                }
                if (matcher.find()) {
                    month = Integer.parseInt(matcher.group(1));
                }

                LocalDate taskDate = LocalDate.of(LocalDate.now().getYear(), month, day);
                if (taskDate.isBefore(LocalDate.now())) {
                    txtDate.setTextColor(getResources().getColor(R.color.red));
                } else {
                    txtDate.setTextColor(getResources().getColor(R.color.statistics_blue));
                }
            }

            // Load tags
            Cursor tagCursor = db.rawQuery("SELECT * FROM tbl_task_tag WHERE task_id = ?", new String[]{taskId});
            if (tagCursor != null) {
                while (tagCursor.moveToNext()) {
                    TextView tag = new TextView(this);
                    tag.setText(tagCursor.getString(tagCursor.getColumnIndexOrThrow("tag_text")));
                    tag.setTextSize(18);
                    tag.setTextColor(Color.WHITE);

                    int color = Color.parseColor(tagCursor.getString(tagCursor.getColumnIndexOrThrow("tag_color")));
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setColor(color);
                    drawable.setCornerRadius(50);
                    tag.setBackground(drawable);

                    FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                            FlexboxLayout.LayoutParams.WRAP_CONTENT,
                            FlexboxLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 10, 10);
                    tag.setLayoutParams(params);
                    tag.setPadding(30, 10, 25, 10);

                    tagContainer.addView(tag);
                }
                tagCursor.close();
            }

            // Load photos
            Cursor photoCursor = db.rawQuery("SELECT * FROM tbl_task_photo WHERE task_id = ?", new String[]{taskId});
            if (photoCursor != null && photoCursor.getCount() > 0) {
                while (photoCursor.moveToNext()) {
                    ImageView attachedPhoto = new ImageView(this);
                    try {
                        Uri photoUri = Uri.parse(photoCursor.getString(photoCursor.getColumnIndexOrThrow("photo_uri")));

                        if (photoUri.getScheme().equals("file")) {
                            attachedPhoto.setImageURI(photoUri);
                            attachedPhoto.setTag(photoUri.toString());
                        } else {
                            attachedPhoto.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri));
                        }
                    } catch (Exception e) {
                        Log.e("TaskActivity", "Error loading image: " + e.getMessage());
                    }

                    FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(300, 300);
                    params.setMargins(0, 0, 10, 10);
                    attachedPhoto.setLayoutParams(params);

                    attachmentContainer.addView(attachedPhoto);
                }
                photoCursor.close();
            }

            // Load reminder settings
            Cursor reminderCursor = db.rawQuery("SELECT * FROM tbl_task_reminder WHERE task_id = ?", new String[]{taskId});
            if (reminderCursor != null && reminderCursor.moveToFirst()) {
                String timeReminder = reminderCursor.getString(reminderCursor.getColumnIndexOrThrow("time"));
                int daysBefore = reminderCursor.getInt(reminderCursor.getColumnIndexOrThrow("days_before"));
                int isRepeat = reminderCursor.getInt(reminderCursor.getColumnIndexOrThrow("is_repeat"));

                reminderTime = timeReminder != null ? timeReminder : "";
                reminderDaysBefore = daysBefore;
                reminderRepeatEnabled = isRepeat == 1;
            }

            if (reminderCursor != null) reminderCursor.close();
        }

        if (cursor != null) cursor.close();
        DatabaseHelper.getInstance(this).closeDatabase();
    }

    private void saveTask() {
        String title = titleInput.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(TaskActivity.this, "Vui lòng nhập tiêu đề!", Toast.LENGTH_SHORT).show();
            return;
        }

        db = DatabaseHelper.getInstance(this).openDatabase();
        db.beginTransaction();

        try {
            boolean taskExists = false;

            ContentValues taskValues = new ContentValues();
            taskValues.put("title", title);
            taskValues.put("content", contentInput.getText().toString());
            taskValues.put("user_id", LoginSessionManager.getInstance(this).getUserId());
            taskValues.put("priority", 1); // mặc định priority = 1, bạn có thể thay đổi
            taskValues.put("is_completed", 0);
            taskValues.put("category_id", 3);

            if (Integer.parseInt(taskId) != -1) {
                taskExists = true;

                // Cập nhật task
                db.update("tbl_task", taskValues, "id = ?", new String[]{taskId});

                // Xoá dữ liệu liên quan
                db.delete("tbl_task_tag", "task_id = ?", new String[]{taskId});
                db.delete("tbl_task_photo", "task_id = ?", new String[]{taskId});
                db.delete("tbl_task_reminder", "task_id = ?", new String[]{taskId});
            } else {
                // Tạo mới
                taskId = String.valueOf(db.insert("tbl_task", null, taskValues));
            }

            if (Integer.parseInt(taskId) != -1) {
                // Lưu tag
                for (int i = 0; i < tagContainer.getChildCount(); i++) {
                    View child = tagContainer.getChildAt(i);
                    if (child instanceof TextView) {
                        TextView tag = (TextView) child;
                        GradientDrawable background = (GradientDrawable) tag.getBackground();

                        ContentValues tagValues = new ContentValues();
                        tagValues.put("task_id", taskId);
                        tagValues.put("tag_text", tag.getText().toString());
                        tagValues.put("tag_color", String.format("#%06X", background.getColor().getDefaultColor() & 0xFFFFFF));

                        db.insert("tbl_task_tag", null, tagValues);
                    }
                }

                // Lưu ảnh đính kèm
                for (int i = 0; i < attachmentContainer.getChildCount(); i++) {
                    View child = attachmentContainer.getChildAt(i);
                    if (child instanceof ImageView) {
                        ImageView imageView = (ImageView) child;
                        String uriString = (String) imageView.getTag();
                        if (uriString != null) {
                            ContentValues photoValues = new ContentValues();
                            photoValues.put("task_id", taskId);
                            photoValues.put("photo_uri", uriString);
                            db.insert("tbl_task_photo", null, photoValues);
                        }
                    }
                }

                // Lưu cài đặt nhắc nhở
                String reminderDate = txtDate.getText().toString();
                if (!reminderDate.isEmpty()) {
                    ContentValues reminderValues = new ContentValues();
                    reminderValues.put("task_id", taskId);

                    // Parse date từ "Ngày X, tháng Y"
                    String[] parts = reminderDate.split(", ");
                    if (parts.length == 2) {
                        String day = parts[0].replace("Ngày ", "");
                        String month = parts[1].replace("tháng ", "");
                        reminderValues.put("date", String.format("Ngày %d, tháng %d",
                                Integer.parseInt(day), Integer.parseInt(month)));
                    }

                    reminderValues.put("time", reminderTime);
                    reminderValues.put("days_before", reminderDaysBefore);
                    reminderValues.put("is_repeat", reminderRepeatEnabled ? 1 : 0);

                    db.insert("tbl_task_reminder", null, reminderValues);

                    // Nếu bạn có ReminderService riêng cho Task thì đặt ở đây
                    ReminderService.scheduleNoteReminder(
                            this,
                            taskId,
                            title,
                            reminderDate,
                            reminderTime,
                            reminderDaysBefore,
                            reminderRepeatEnabled
                    );

                }

                db.setTransactionSuccessful();
                Log.d("TaskActivity", taskExists ? "Task updated successfully" : "Task created successfully");
            }
        } catch (Exception e) {
            Log.e("TaskActivity", "Error saving task", e);
        } finally {
            db.endTransaction();
            DatabaseHelper.getInstance(this).closeDatabase();
            finish();
        }
    }


}
