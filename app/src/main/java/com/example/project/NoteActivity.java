package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
    TextView txtDate, btnBack, btnOption;
    FlexboxLayout tagContainer, attachmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);

        txtDate = findViewById(R.id.txtDate);
        btnBack = findViewById(R.id.textView);
        btnOption = findViewById(R.id.textView2);

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
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            ImageView attachedPhoto = new ImageView(this);
            attachedPhoto.setImageURI(selectedImageUri);

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    300,
                    300);
            params.setMargins(0, 0, 10, 10);
            attachedPhoto.setLayoutParams(params);

            attachmentContainer.addView(attachedPhoto);
        }
    }
}
