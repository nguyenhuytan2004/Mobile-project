package com.example.project;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class TaskActivity extends  AppCompatActivity{

    ImageView btnDate, btnTag, btnImage;
    TextView txtDate, btnBack, btnOption, btnSave, btnListName, btnDropDownList;
    EditText titleInput, contentInput;
    FlexboxLayout tagContainer, attachmentContainer;

    CheckBox taskCheckBox;

    SQLiteDatabase db;
    String taskId, listId, categoryId;

    private String reminderTime = "";

    private String reminderDateRaw = "";
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
        btnListName = findViewById(R.id.listNameText);
        btnDropDownList = findViewById(R.id.dropDownList);

        tagContainer = findViewById(R.id.tagContainer);
        attachmentContainer = findViewById(R.id.attachmentContainer);
        taskCheckBox = findViewById(R.id.taskCheckBox);

        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        taskId = getIntent().getStringExtra("taskId");
        categoryId = getIntent().getStringExtra("categoryId");
        listId = getIntent().getStringExtra("listId");

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

        // lấy list name
        if (listId != null) {
            db = DatabaseHelper.getInstance(this).openDatabase();
            Cursor cursor = db.rawQuery("SELECT name FROM tbl_list WHERE id = ?", new String[]{listId});

            if (cursor.moveToFirst()) {
                String listName = cursor.getString(0);
                btnListName.setText(listName);
            } else {
                btnListName.setText("(Không tìm thấy)");
            }

            cursor.close();
            DatabaseHelper.getInstance(this).closeDatabase();
        }

        // ✅ Truy vấn DB để lấy is_completed
        db = DatabaseHelper.getInstance(this).openDatabase();
        Cursor cursor = db.rawQuery("SELECT is_completed FROM tbl_task WHERE id = ?", new String[]{taskId});
        if (cursor.moveToFirst()) {
            boolean isCompleted = cursor.getInt(0) == 1;

            // Đặt trạng thái checkbox
            taskCheckBox.setChecked(isCompleted);
            taskCheckBox.setButtonDrawable(isCompleted
                    ? R.drawable.ic_checkbox_complete
                    : R.drawable.ic_checkbox_notcompleted);

            if (isCompleted) {
                titleInput.setPaintFlags(titleInput.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                titleInput.setTextColor(getResources().getColor(R.color.gray));
                titleInput.setAlpha(0.6f);
                contentInput.setAlpha(0.6f);
            }
        }
        cursor.close();

        // ✅ Xử lý khi người dùng check/uncheck
        taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String updateQuery = "UPDATE tbl_task SET is_completed = ? WHERE id = ?";
            db.execSQL(updateQuery, new Object[]{isChecked ? 1 : 0, taskId});

            // Cập nhật giao diện theo trạng thái mới
            taskCheckBox.setButtonDrawable(isChecked
                    ? R.drawable.ic_checkbox_complete
                    : R.drawable.ic_checkbox_notcompleted);

            if (isChecked) {
                titleInput.setPaintFlags(titleInput.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                titleInput.setTextColor(getResources().getColor(R.color.gray));
                titleInput.setAlpha(0.6f);
                contentInput.setAlpha(0.6f);
            } else {
                titleInput.setPaintFlags(titleInput.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                titleInput.setTextColor(getResources().getColor(R.color.black)); // hoặc màu chính bạn đang dùng
                titleInput.setAlpha(1f);
                contentInput.setAlpha(1f);
            }
        });

        btnDropDownList.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TaskActivity.this);
            View view = getLayoutInflater().inflate(R.layout.popup_move_to, null);
            bottomSheetDialog.setContentView(view);
            bottomSheetDialog.show();

            LinearLayout container = view.findViewById(R.id.listCategoryContainer);
            TextView btnCancel = view.findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(v1 -> bottomSheetDialog.dismiss());

            db = DatabaseHelper.getInstance(this).openDatabase();

            Cursor listCursor = db.rawQuery("SELECT id, name FROM tbl_list ORDER BY id", null);

            if (listCursor.moveToFirst()) {
                do {
                    int listIdFromDB = listCursor.getInt(0);
                    String listName = listCursor.getString(1);

                    // Inflate layout block cho từng list
                    View listBlockView = LayoutInflater.from(this).inflate(R.layout.item_list_block, container, false);
                    TextView listTitle = listBlockView.findViewById(R.id.list_title);
                    LinearLayout categoryListLayout = listBlockView.findViewById(R.id.category_list_layout);

                    listTitle.setText(listName);

                    // Truy vấn category thuộc list đó
                    Cursor categoryCursor = db.rawQuery(
                            "SELECT id, name FROM tbl_category WHERE list_id = ?",
                            new String[]{String.valueOf(listIdFromDB)}
                    );

                    while (categoryCursor.moveToNext()) {
                        int categoryIdFromDB = categoryCursor.getInt(0);
                        String categoryName = categoryCursor.getString(1);

                        // Tạo TextView cho mỗi category
                        TextView categoryView = new TextView(this);
                        categoryView.setText("\uD81A\uDD18 " + categoryName);
                        categoryView.setTextColor(Color.parseColor("#CCCCCC"));
                        categoryView.setTextSize(16);
                        categoryView.setPadding(24, 12, 0, 12);

                        // Gắn sự kiện click
                        categoryView.setOnClickListener(catView -> {
                            listId = String.valueOf(listIdFromDB);
                            categoryId = String.valueOf(categoryIdFromDB);
                            btnListName.setText(listName);

                            Toast.makeText(this, getResources().getString(R.string.note_move) + ": " + listName + " / " + categoryName, Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        });

                        categoryListLayout.addView(categoryView);
                    }
                    categoryCursor.close();

                    container.addView(listBlockView);
                } while (listCursor.moveToNext());

                listCursor.close();
            }

            DatabaseHelper.getInstance(this).closeDatabase();
        });



        btnDate.setOnClickListener(view -> {
            SetReminderDialog dialog = new SetReminderDialog(taskId);
            dialog.setOnDateSelectedListener(new SetReminderDialog.OnReminderSettingsListener() {
                @Override
                public void onReminderSet(String date, String time, int daysBefore, boolean isRepeat) {
                    if (date.isEmpty()) {
                        txtDate.setText("");
                        reminderDateRaw = "";
                        reminderTime = "";
                        reminderDaysBefore = 0;
                        reminderRepeatEnabled = false;
                        return;
                    }

                    // Lưu cài đặt
                    reminderDateRaw = date; // dd/MM/yyyy
                    reminderTime = time;
                    reminderDaysBefore = daysBefore;
                    reminderRepeatEnabled = isRepeat;

                    // Parse để hiển thị
                    LocalDate selectedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    LocalDate today = LocalDate.now();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault());
                    String formattedDate = selectedDate.format(formatter);

                    String prefix = getString(R.string.task_date); // "Ngày"
                    txtDate.setText(prefix.isEmpty() ? formattedDate : prefix + " " + formattedDate);

                    txtDate.setTextColor(getResources().getColor(
                            selectedDate.isBefore(today) ? R.color.red : R.color.statistics_blue));
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
                //.makeText(TaskActivity.this, "Nhiệm vụ đã được ghim!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });

            convertTask.setOnClickListener(v -> {
                Intent intent = new Intent(TaskActivity.this, NoteActivity.class);
                intent.putExtra("title", titleInput.getText().toString());
                intent.putExtra("content", contentInput.getText().toString());
                intent.putExtra("noteId", taskId);
                intent.putExtra("listId", listId);
                intent.putExtra("categoryId", categoryId);
                startActivity(intent);

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
                    //Toast.makeText(TaskActivity.this, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(this, "Không thể thêm ảnh", Toast.LENGTH_SHORT).show();
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

            String dateStr = "";
            int dateColumnIndex = cursor.getColumnIndexOrThrow("date");
            if (!cursor.isNull(dateColumnIndex)) {
                dateStr = cursor.getString(dateColumnIndex);
            }

            if (!dateStr.isEmpty()) {
                try {
                    LocalDate taskDate = LocalDate.parse(dateStr);

                    String monthText = getString(R.string.task_month);
                    String formattedDate = taskDate.getDayOfMonth() + " " + monthText + " " + taskDate.getMonthValue();

                    txtDate.setText(formattedDate);

                    // Đổi màu theo ngày quá hạn
                    if (taskDate.isBefore(LocalDate.now())) {
                        txtDate.setTextColor(getResources().getColor(R.color.red));
                    } else {
                        txtDate.setTextColor(getResources().getColor(R.color.statistics_blue));
                    }

                } catch (DateTimeParseException e) {
                    Log.e("DateParse", "Lỗi khi parse ngày: " + dateStr, e);
                    txtDate.setText("Ngày không hợp lệ");
                    txtDate.setTextColor(getResources().getColor(R.color.red));
                }
            } else {
                txtDate.setText("Ngày & Lặp lại");
                txtDate.setTextColor(getResources().getColor(R.color.statistics_blue));
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
            //Toast.makeText(TaskActivity.this, "Vui lòng nhập tiêu đề!", Toast.LENGTH_SHORT).show();
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
            taskValues.put("is_completed", 0);

            int calculatedPriority = calculatePriority(title, contentInput.getText().toString(), db);
            taskValues.put("priority", calculatedPriority);

            if (categoryId == null || categoryId.isEmpty()) {
                Cursor categoryCursor = db.rawQuery("SELECT * FROM tbl_category WHERE list_id = ?", new String[]{listId});
                if (categoryCursor.moveToFirst()) {
                    taskValues.put("category_id", categoryCursor.getInt(categoryCursor.getColumnIndexOrThrow("id")));
                }
                categoryCursor.close();
            } else {
                try {
                    taskValues.put("category_id", Integer.parseInt(categoryId));
                } catch (NumberFormatException e) {
                    Log.e("TaskSave", "Invalid categoryId: " + categoryId);
                }
            }

            Log.d("TaskSave", "Task Values:");
            Log.d("TaskSave", "Title: " + title);
            Log.d("TaskSave", "Content: " + contentInput.getText().toString());
            Log.d("TaskSave", "Priority: " + calculatedPriority);
            Log.d("TaskSave", "Task ID: " + taskId);

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
                String reminderDateDisplay = txtDate.getText().toString();
                String reminderDate = reminderDateRaw;
                Log.d("TaskActivity", "Reminder date input: " + reminderDate);

                String dayPrefix = getString(R.string.task_date);    // Ví dụ "Ngày" hoặc "" nếu tiếng Anh
                String monthPrefix = getString(R.string.task_month); // Ví dụ "tháng" hoặc "Month"

                if (!reminderDate.isEmpty()) {
                    ContentValues reminderValues = new ContentValues();
                    reminderValues.put("task_id", taskId);

                    try {
                        // Parse lại để lưu dạng chuẩn yyyy-MM-dd
                        LocalDate parsedDate = LocalDate.parse(reminderDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        String formattedDate = parsedDate.format(DateTimeFormatter.ISO_LOCAL_DATE); // yyyy-MM-dd
                        reminderValues.put("date", formattedDate);
                        Log.d("TaskActivity", "Lưu ngày nhắc nhở: " + formattedDate);
                    } catch (Exception e) {
                        Log.e("TaskActivity", "Lỗi khi parse date reminder: " + e.getMessage());
                    }

                    reminderValues.put("time", reminderTime);
                    reminderValues.put("days_before", reminderDaysBefore);
                    reminderValues.put("is_repeat", reminderRepeatEnabled ? 1 : 0);

                    long insertResult = db.insert("tbl_task_reminder", null, reminderValues);
                    if (insertResult != -1) {
                        Log.d("TaskActivity", "Reminder lưu thành công, ID: " + insertResult);
                    } else {
                        Log.e("TaskActivity", "Lưu reminder thất bại.");
                    }

                    ReminderService.scheduleTaskReminder(
                            this,
                            taskId,
                            title,
                            reminderDateDisplay, // vẫn giữ giao diện đẹp
                            reminderTime,
                            reminderDaysBefore,
                            reminderRepeatEnabled
                    );
                }
                else {
                    Log.d("TaskActivity", "Không có ngày nhắc nhở được nhập.");
                }

                db.setTransactionSuccessful();
                Log.d("TaskActivity", taskExists ? "Task updated successfully" : "Task created successfully");


            }
        } catch (Exception e) {
            Log.e("TaskActivity", "Error saving task", e);
        } finally {
            db.endTransaction();
            DatabaseHelper.getInstance(this).closeDatabase();
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
    }

    private int calculatePriority(String title, String content, SQLiteDatabase db) {
        double[] x = new double[9];

        int titleLen = title.length();
        x[0] = titleLen > 100 ? 1.0 : titleLen / 100.0;

        int contentLen = content.length();
        x[1] = contentLen > 500 ? 1.0 : contentLen / 500.0;

        x[2] = Softmax.checkHashtagImportance(content) ? 1 : 0;
        x[3] = Softmax.checkHashtagTravel(content) ? 1 : 0;
        x[4] = Softmax.checkHashtagJob(content) ? 1 : 0;
        x[5] = Softmax.checkHashtagEmail(content) ? 1 : 0;
        x[6] = Softmax.checkKeywordImportance(title + " " + content) ? 1 : 0;
        x[7] = Softmax.checkKeywordEmergency(title + " " + content) ? 1 : 0;
        x[8] = 1.0;

        Softmax softmax = new Softmax(9, 4);

        Cursor cursor = null;
        try {
            String query = "SELECT class_id, feature_index, weight FROM tbl_weights";
            Log.d("PriorityCalc", "Executing query: " + query);
            cursor = db.rawQuery(query, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int classId = cursor.getInt(cursor.getColumnIndexOrThrow("class_id"));
                    int featureIndex = cursor.getInt(cursor.getColumnIndexOrThrow("feature_index"));
                    double weight = cursor.getDouble(cursor.getColumnIndexOrThrow("weight"));
                    softmax.setWeight(classId, featureIndex, weight);
                }
            }
        } catch (Exception e) {
            Log.e("PriorityCalc", "Error loading weights", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        // kết quả đổi ngược lại cho trùng matrix 1,2,3,4 - > 4,3,2,1
        int predicted = softmax.predict(x);
        int reversed = 5 - predicted;

        return reversed;
    }
}
