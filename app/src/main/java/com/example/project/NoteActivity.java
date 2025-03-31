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

public class NoteActivity extends AppCompatActivity {
    ImageView btnDate, btnTag, btnImage;
    TextView txtDate, btnBack, btnOption, btnSave;
    EditText titleInput, contentInput;
    FlexboxLayout tagContainer, attachmentContainer;

    SQLiteDatabase db;
    String noteId;

    private String reminderTime = "";
    private int reminderDaysBefore = 0;
    private boolean reminderRepeatEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);

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

        noteId = getIntent().getStringExtra("noteId");
        Log.d("NoteActivity", "Note ID: " + noteId);
        if (noteId != null && !noteId.equals("-1")) {
            loadNote();
        }

        btnDate.setOnClickListener(view -> {
            SetReminderDialog dialog = new SetReminderDialog(noteId);
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

        btnOption.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(NoteActivity.this);
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_in_note, null);
            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.show();

            // Lấy các phần tử trong BottomSheet
            TextView pinNote = sheetView.findViewById(R.id.pinTxt);
            TextView convertNote = sheetView.findViewById(R.id.convertTxt);
            TextView deleteNote = sheetView.findViewById(R.id.deleteTxt);

            // Xử lý sự kiện
            pinNote.setOnClickListener(v -> {
                Toast.makeText(NoteActivity.this, "Ghi chú đã được ghim!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });

            convertNote.setOnClickListener(v -> {
                Toast.makeText(NoteActivity.this, "Ghi chú đã được chuyển đổi thành nhiệm vụ!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });

            deleteNote.setOnClickListener(v -> {
                Toast.makeText(NoteActivity.this, "Ghi chú đã bị xoá!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        });

        btnTag.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(NoteActivity.this);
            View sheetView = getLayoutInflater().inflate(R.layout.add_card_bottom_sheet_in_note, null);
            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.show();

            EditText cardInput = sheetView.findViewById(R.id.cardInputetxt);
            cardInput.requestFocus();

            TextView doneBtn = sheetView.findViewById(R.id.doneTxt);
            tagContainer.setFlexWrap(FlexWrap.WRAP);

            doneBtn.setOnClickListener(v -> {
                String cardContent = cardInput.getText().toString();

                if (!cardContent.isEmpty()) {
                    TextView tag = new TextView(NoteActivity.this);
                    tag.setText(cardContent);
                    tag.setTextSize(18);
                    tag.setTextColor(Color.WHITE);

                    int[] colors = {
                            Color.rgb(52, 152, 219),   // Blue
                            Color.rgb(46, 204, 113),   // Green
                            Color.rgb(231, 76, 60),    // Red
                            Color.rgb(155, 89, 182),   // Purple
                            Color.rgb(241, 196, 15),   // Yellow Dark
                            Color.rgb(26, 188, 156),   // Teal
                            Color.rgb(230, 126, 34),   // Orange
                            Color.rgb(22, 160, 133),   // Dark Teal
                            Color.rgb(142, 68, 173),   // Dark Purple
                            Color.rgb(192, 57, 43),    // Dark Red
                            Color.rgb(44, 62, 80),     // Dark Blue
                            Color.rgb(127, 140, 141)};  // Gray Blue
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
                    Toast.makeText(NoteActivity.this, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100);
        });

        btnSave.setOnClickListener(view -> saveNote());
    }

    // When selecting an image (in onActivityResult)
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

    private void loadNote() {
        Bundle bundle = getIntent().getExtras();
        String noteId = bundle.getString("noteId");

        db = DatabaseHelper.getInstance(this).openDatabase();
        String query = "SELECT n.id, n.title, n.content, r.date " +
                        "FROM tbl_note n " +
                        "LEFT JOIN tbl_note_reminder r ON n.id = r.note_id " +
                        "WHERE n.id = ? " +
                        "ORDER BY n.id DESC";
        Cursor cursor = db.rawQuery(query, new String[]{noteId});

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

                LocalDate noteDate = LocalDate.of(LocalDate.now().getYear(), month, day);

                if (noteDate.isBefore(LocalDate.now())) {
                    txtDate.setTextColor(getResources().getColor(R.color.red));
                }
                else {
                    txtDate.setTextColor(getResources().getColor(R.color.statistics_blue));
                }
            }

            // Load tags
            Cursor tagCursor = db.rawQuery("SELECT * FROM tbl_note_tag WHERE note_id = ?", new String[]{noteId});
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
            Cursor photoCursor = db.rawQuery("SELECT * FROM tbl_note_photo WHERE note_id = ?", new String[]{noteId});
            if (photoCursor != null && photoCursor.getCount() > 0) {
                while (photoCursor.moveToNext()) {
                    ImageView attachedPhoto = new ImageView(this);
                    try {
                        Uri photoUri = Uri.parse(photoCursor.getString(photoCursor.getColumnIndexOrThrow("photo_uri")));

                        // Check if it's a file URI (our app storage)
                        if (photoUri.getScheme().equals("file")) {
                            attachedPhoto.setImageURI(photoUri);
                            attachedPhoto.setTag(photoUri.toString()); // Store local URI for saving
                        } else {
                            // For older content:// URIs, try to load with permission
                            attachedPhoto.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri));
                        }
                    } catch (Exception e) {
                        Log.e("NoteActivity", "Error loading image: " + e.getMessage());
                    }

                    FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                            300,
                            300);
                    params.setMargins(0, 0, 10, 10);
                    attachedPhoto.setLayoutParams(params);

                    attachmentContainer.addView(attachedPhoto);
                }
                photoCursor.close();
            }

            // Load reminder settings
            Cursor reminderCursor = db.rawQuery("SELECT * FROM tbl_note_reminder WHERE note_id = ?", new String[]{noteId});
            if (reminderCursor != null && reminderCursor.moveToFirst()) {
                String timeReminder = reminderCursor.getString(reminderCursor.getColumnIndexOrThrow("time"));
                int daysBefore = reminderCursor.getInt(reminderCursor.getColumnIndexOrThrow("days_before"));
                int isRepeat = reminderCursor.getInt(reminderCursor.getColumnIndexOrThrow("is_repeat"));

                reminderTime = timeReminder != null ? timeReminder : "";
                reminderDaysBefore = daysBefore;
                reminderRepeatEnabled = isRepeat == 1;
            }
        }
        DatabaseHelper.getInstance(this).closeDatabase();
    }

    private void saveNote() {
        String title = titleInput.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(NoteActivity.this, "Vui lòng nhập tiêu đề!", Toast.LENGTH_SHORT).show();
            return;
        }

        db = DatabaseHelper.getInstance(this).openDatabase();
        db.beginTransaction();

        try {
            boolean noteExists = false;

            ContentValues noteValues = new ContentValues();
            noteValues.put("title", title);
            noteValues.put("content", contentInput.getText().toString());
            noteValues.put("user_id", LoginSessionManager.getInstance(this).getUserId());

            if (Integer.parseInt(noteId) != -1) {
                noteExists = true;

                // Update existing note
                db.update("tbl_note", noteValues, "id = ?",
                        new String[]{noteId});

                // Delete existing related data to avoid duplicates
                db.delete("tbl_note_tag", "note_id = ?",
                        new String[]{String.valueOf(noteId)});
                db.delete("tbl_note_photo", "note_id = ?",
                        new String[]{String.valueOf(noteId)});
                db.delete("tbl_note_reminder", "note_id = ?",
                        new String[]{String.valueOf(noteId)});
            } else {
                // Insert new note
                noteId = String.valueOf(db.insert("tbl_note", null, noteValues));
            }

            if (Integer.parseInt(noteId) != -1) {
                // Save tags
                for (int i = 0; i < tagContainer.getChildCount(); i++) {
                    View child = tagContainer.getChildAt(i);
                    if (child instanceof TextView) {
                        TextView tag = (TextView) child;
                        GradientDrawable background = (GradientDrawable) tag.getBackground();

                        ContentValues tagValues = new ContentValues();
                        tagValues.put("note_id", noteId);
                        tagValues.put("tag_text", tag.getText().toString());
                        tagValues.put("tag_color", String.format("#%06X", background.getColor().getDefaultColor() & 0xFFFFFF));

                        db.insert("tbl_note_tag", null, tagValues);
                    }
                }

                // Save photos
                for (int i = 0; i < attachmentContainer.getChildCount(); i++) {
                    View child = attachmentContainer.getChildAt(i);
                    if (child instanceof ImageView) {
                        ImageView imageView = (ImageView) child;
                        String uriString = (String) imageView.getTag();
                        if (uriString != null) {
                            ContentValues photoValues = new ContentValues();
                            photoValues.put("note_id", noteId);
                            photoValues.put("photo_uri", uriString);
                            db.insert("tbl_note_photo", null, photoValues);
                        }
                    }
                }

                // Save reminder setting
                String reminderDate = txtDate.getText().toString();
                if (!reminderDate.isEmpty()) {
                    ContentValues reminderValues = new ContentValues();
                    reminderValues.put("note_id", noteId);

                    // Parse date from "Ngày X, tháng Y" format
                    String[] parts = reminderDate.split(", ");
                    if (parts.length == 2) {
                        String day = parts[0].replace("Ngày ", "");
                        String month = parts[1].replace("tháng ", "");
                        reminderValues.put("date", String.format("Ngày %d, tháng %d",
                                Integer.parseInt(day), Integer.parseInt(month)));
                    }

                    // Save time, days before, and repeat settings
                    reminderValues.put("time", reminderTime);
                    reminderValues.put("days_before", reminderDaysBefore);
                    reminderValues.put("is_repeat", reminderRepeatEnabled ? 1 : 0);

                    db.insert("tbl_note_reminder", null, reminderValues);
                }

                db.setTransactionSuccessful();
                Log.d("NoteActivity", noteExists ? "Note updated successfully" : "Note created successfully");
            }
        } catch (Exception e) {
            Log.e("NoteActivity", "Error saving note", e);
        } finally {
            db.endTransaction();
            DatabaseHelper.getInstance(this).closeDatabase();
            finish();
        }
    }
}
