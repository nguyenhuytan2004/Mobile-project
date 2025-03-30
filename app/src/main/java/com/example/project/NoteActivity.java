package com.example.project;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class NoteActivity extends AppCompatActivity {
    ImageView btnDate, btnCard, btnImage;
    TextView txtDate, btnBack, btnOption, btnSave;
    EditText titleInput, contentInput;
    FlexboxLayout tagContainer, attachmentContainer;

    SQLiteDatabase db;

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
        btnCard = findViewById(R.id.cardIcon);
        btnImage = findViewById(R.id.imageIcon);

        tagContainer = findViewById(R.id.tagContainer);
        attachmentContainer = findViewById(R.id.attachmentContainer);

        btnDate.setOnClickListener(view -> {
            SetReminderDialogFragment dialog = new SetReminderDialogFragment();
            dialog.setOnDateSelectedListener(date -> {
                if (date.isEmpty()) {
                    txtDate.setText("");
                    return;
                }

                // Chuyển đổi ngày được chọn thành LocalDate (API 26+)
                LocalDate selectedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                LocalDate today = LocalDate.now();

                int day = selectedDate.getDayOfMonth();
                int month = selectedDate.getMonthValue();
                txtDate.setText("Ngày " + day + ", tháng " + month);

                // Đổi màu text
                if (selectedDate.isBefore(today)) {
                    txtDate.setTextColor(getResources().getColor(R.color.red)); // Ngày trong quá khứ
                } else {
                    txtDate.setTextColor(getResources().getColor(R.color.statistics_blue)); // Ngày hôm nay hoặc tương lai
                }
            });
            dialog.show(getSupportFragmentManager(), "CalendarDialog");
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

        btnCard.setOnClickListener(view -> {
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            ImageView attachedPhoto = new ImageView(this);
            attachedPhoto.setImageURI(selectedImageUri);
            attachedPhoto.setTag(selectedImageUri.toString()); // Store URI for saving

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    300,
                    300);
            params.setMargins(0, 0, 10, 10);
            attachedPhoto.setLayoutParams(params);

            attachmentContainer.addView(attachedPhoto);
        }
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
            // Save main note
            ContentValues noteValues = new ContentValues();
            noteValues.put("title", title);
            noteValues.put("content", contentInput.getText().toString());
            noteValues.put("user_id", LoginSessionManager.getInstance(this).getUserId());

            long noteId = db.insert("tbl_user_note", null, noteValues);

            if (noteId != -1) {
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

                // Save reminder if date is set
                String reminderDate = txtDate.getText().toString();
                if (!reminderDate.isEmpty()) {
                    ContentValues reminderValues = new ContentValues();
                    reminderValues.put("note_id", noteId);
                    // Parse date from "Ngày X, tháng Y" format
                    String[] parts = reminderDate.split(", ");
                    if (parts.length == 2) {
                        String day = parts[0].replace("Ngày ", "");
                        String month = parts[1].replace("tháng ", "");
                        int year = LocalDate.now().getYear();
                        reminderValues.put("date", String.format("%02d/%02d/%d",
                                Integer.parseInt(day), Integer.parseInt(month), year));
                    }
                    reminderValues.put("time", "");  // Set actual time if needed
                    reminderValues.put("reminder_text", "");  // Set actual reminder text if needed
                    reminderValues.put("is_repeat", 0);  // Set repeat status if needed
                    db.insert("tbl_note_reminder", null, reminderValues);
                }

                db.setTransactionSuccessful();
                Log.d("NoteActivity", "Note saved successfully");
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
